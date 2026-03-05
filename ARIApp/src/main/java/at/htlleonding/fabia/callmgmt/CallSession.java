package at.htlleonding.fabia.callmgmt;

import at.htlleonding.fabia.callmgmt.audiomgmt.AudioItem;
import at.htlleonding.fabia.callmgmt.util.*;
import at.htlleonding.fabia.client.formbe.dtos.*;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static at.htlleonding.fabia.callmgmt.util.StateSequence.*;

/**
 * Represents a session of a caller with everything needed to know their state in the call.
 */
public final class CallSession {
    private final SessionManager sessionManager;
    private final CallProcessor callProcessor;
    private final AriUtil arUtil;

    private static final Logger logger = LoggerFactory.getLogger(CallSession.class);
    /**
     * The id of the bridge where the callers channel from asterisk is in. The id is unique.
     */
    @Getter
    private final String bridgeId;
    /**
     * The name of the channel from asterisk, which contains callers sip username.
     */
    @Getter
    private final String channelName;
    /**
     * Represents the current call state the caller is in.
     */
    @Getter
    private CallState state;
    /**
     * The currently selected group or null.
     */
    @Getter
    @Setter
    private Group selectedGroup;
    /**
     * The selected form to fill out or null.
     */
    @Getter
    private Form selectedForm;
    /**
     * The fields of the form to go through to remove duplicates. Can be empty.
     */
    @Getter
    private final Set<Field> usedFields = new LinkedHashSet<>();
    /**
     * The current index of the field group, which is processed. Can be null.
     */
    @Getter
    private Integer currentFieldGroupIdx;
    /**
     * The current index of the single choice field, which is processed. Can be null.
     */
    @Getter
    private Integer currentSingleChoiceFieldIdx;
    /**
     * The index of the currently processed field. Can be null.
     */
    @Getter
    private Integer currentFieldIdx;
    /**
     * Represents in which step for the current call state the session is.
     */
    @Getter
    @Setter
    private BaseState currentBaseState;
    /**
     * Represents the queue of audio items to process.
     */
    private final ConcurrentLinkedQueue<AudioItem> audioQueue = new ConcurrentLinkedQueue<>();
    /**
     * If the caller has hung up.
     */
    private boolean hasHungUp = false;
    /**
     * Saves data for the group handling.
     */
    @Getter
    private final GroupHandlingState groupHandlingState = new GroupHandlingState();
    /**
     * Saves data for the form handling.
     */
    @Getter
    private final FormHandlingState formHandlingState = new FormHandlingState();
    /**
     * Saves data for the single choice field handling.
     */
    @Getter
    private final SingleChoiceFieldHandlingState singleChoiceFieldHandlingState = new SingleChoiceFieldHandlingState();
    /**
     * Saves data for the field handling.
     */
    @Getter
    private final FieldHandlingState fieldHandlingState = new FieldHandlingState();
    /**
     * True if audio is playing and false if not.
     */
    @Getter
    private final AtomicBoolean isAudioPlaying = new AtomicBoolean(false);

    /**
     * Advances the current base state in the call state.
     */
    private void advanceBaseState() {
        currentBaseState = StateSequence.advanceBaseState(currentBaseState);
        logger.debug("Advance base state to {}", currentBaseState);
    }

    public void skipCurrentHandler(){
        currentBaseState = BaseState.RETRY;
    }

    /**
     * Sets the session to a new call state.
     *
     * @param state The new state to advance from.
     */
    private void setState(CallState state) {
        this.state = state;
        resetBaseState();
    }

    public CallSession(String bridgeId, String channelName, SessionManager sessionManager, CallProcessor callProcessor, AriUtil arUtil) {
        this.bridgeId = bridgeId;
        this.channelName = channelName;
        this.sessionManager = sessionManager;
        this.callProcessor = callProcessor;
        this.arUtil = arUtil;
        setState(getFirstCallState());
    }

    /**
     * True, if the current field group index is not null and a form is selected.
     */
    public boolean fieldGroupValid() {
        return this.currentFieldGroupIdx != null
                && this.selectedForm != null;
    }

    /**
     * True, if the field group is valid and the current single choice field index is not null.
     */
    public boolean singleChoiceFieldValid() {
        return fieldGroupValid()
                && this.currentSingleChoiceFieldIdx != null;
    }

    /**
     * True, if the field group is valid and the current field index is not null.
     */
    public boolean fieldValid() {
        return fieldGroupValid() && this.currentFieldIdx != null;
    }

    /**
     * True, if the field group is valid and in bounds.
     */
    public boolean currentFieldGroupInBounds() {
        return fieldGroupValid()
                && this.currentFieldGroupIdx < this.selectedForm.getFieldGroups().size();
    }

    /**
     * True, if the current field group is valid and there are more.
     */
    public boolean fieldGroupsLeft() {
        return fieldGroupValid()
                && this.currentFieldGroupIdx < this.selectedForm.getFieldGroups().size() - 1;
    }

    /**
     * Checks, whether the current single choice field and field group are in bounds.
     */
    public boolean currentSingleChoiceFieldInBounds() {
        return singleChoiceFieldValid()
                && currentFieldGroupInBounds()
                && this.currentSingleChoiceFieldIdx <
                this.selectedForm.getFieldGroups()
                        .get(this.currentFieldGroupIdx)
                        .getSingleChoiceFields()
                        .size();
    }

    /**
     * Checks, whether the single choice fields are valid and there are some left.
     */
    public boolean singleChoiceFieldsLeft() {
        return singleChoiceFieldValid()
                && this.currentSingleChoiceFieldIdx <
                this.selectedForm.getFieldGroups()
                        .get(this.currentFieldGroupIdx)
                        .getSingleChoiceFields()
                        .size() - 1;
    }

    /**
     * Checks, if the current field is valid and in bounds.
     */
    public boolean currentFieldIdxInBounds() {
        return fieldValid()
                && currentFieldGroupInBounds()
                && this.currentFieldIdx < this.usedFields.size();
    }

    /**
     * Checks, if there are fields left.
     */
    public boolean fieldsLeft() {
        return fieldValid()
                && this.currentFieldIdx < this.usedFields.size() - 1;
    }

    /**
     * True, if the field groups are empty.
     */
    public boolean fieldGroupsEmpty() {
        return this.getSelectedForm().getFieldGroups()
                .isEmpty();
    }

    /**
     * True, if the single choice fields are empty.
     */
    public boolean singleChoiceFieldsEmpty() {
        return this.getSelectedForm().getFieldGroups()
                .get(getCurrentFieldGroupIdx())
                .getSingleChoiceFields()
                .isEmpty();
    }

    /**
     * Checks, whether there are no fields in this form or not.
     */
    public boolean fieldsEmpty() {
        return this.usedFields.isEmpty();
    }

    /**
     * Tries to increase the field group index. If it is null it is set to 0.
     */
    public void tryIncreaseFieldGroupIdx() {
        logger.debug("Trying to increase field group index from {}", currentFieldGroupIdx);
        if (this.getSelectedForm() != null && getCurrentFieldGroupIdx() == null) {
            this.currentFieldGroupIdx = 0;
            resetForFieldGroup();
            return;
        }

        if (currentFieldGroupInBounds()) {
            currentFieldGroupIdx++;
            resetForFieldGroup();
        }
    }

    /**
     * Tries to increase the current single choice field index or sets it to 0 if null.
     */
    public void tryIncreaseSingleChoiceFieldIdx() {
        if (fieldGroupValid() && getCurrentSingleChoiceFieldIdx() == null) {
            this.currentSingleChoiceFieldIdx = 0;
            return;
        }

        if (currentSingleChoiceFieldInBounds()) {
            this.currentSingleChoiceFieldIdx++;
        }
    }

    /**
     * Tries to increase the current field index or sets it to 0 if it is null.
     */
    public void tryIncreaseFieldIdx() {
        if (fieldGroupValid() && getCurrentFieldIdx() == null) {
            this.currentFieldIdx = 0;
            return;
        }

        if (currentFieldIdxInBounds()) {
            this.currentFieldIdx++;
        }
    }

    /**
     * Sets the currently selected form.
     *
     * @param selectedForm The form, which was selected
     */
    public void setSelectedForm(Form selectedForm) {
        this.selectedForm = selectedForm;
        this.currentFieldGroupIdx = null;
        resetIndices();
    }

    /**
     * Resets the single choice index and the current fields.
     */
    private void resetForFieldGroup() {
        this.usedFields.clear();
        this.currentSingleChoiceFieldIdx = null;
        if (currentFieldGroupIdx != null) {
            this.usedFields.addAll(
                    selectedForm.getFieldGroups()
                            .get(getCurrentFieldGroupIdx())
                            .getFields());
        }
    }

    /**
     * @return The current field group by its index.
     */
    public FieldGroup getCurrentFieldGroup() {
        return this.selectedForm.getFieldGroups()
                .get(getCurrentFieldGroupIdx());
    }

    /**
     * @return The current single choice field by its index.
     */
    public SingleChoiceField getCurrentSingleChoiceField() {
        return this.getSelectedForm()
                .getFieldGroups()
                .get(this.getCurrentFieldGroupIdx())
                .getSingleChoiceFields()
                .get(this.getCurrentSingleChoiceFieldIdx());
    }

    /**
     * @return The current field by the index.
     */
    public Field getCurrentField() {
        return this.usedFields.toArray(Field[]::new)[getCurrentFieldIdx()];
    }

    /**
     * Adds new fields to the fields to be processed.
     * Needed because of the conditional addition by single choice fields and its selected options.
     *
     * @param fields The new fields.
     */
    public void addFields(Collection<? extends Field> fields) {
        this.usedFields.addAll(fields);
    }

    /**
     * Resets the base state inside the current call state.
     */
    public void resetBaseState() {
        this.currentBaseState = getFirstBaseState();
    }

    /**
     * Steers the call to handle a field group.
     */
    public void goToFieldGroup() {
        setState(CallState.FIELD_GROUP);
    }

    /**
     * Steers the call to handle a field.
     */
    public void goToField() {
        setState(CallState.FIELD);
    }

    /**
     * Resets the index for all fields.
     */
    public void resetIndices() {
        this.currentSingleChoiceFieldIdx = null;
        this.currentFieldIdx = null;
    }

    /**
     * Adds a new audio item to the queue.
     *
     * @param item The new audio to process.
     */
    public void enqueueAudio(AudioItem item) {
        if (item == null) {
            return;
        }
        audioQueue.add(item);
    }

    /**
     * Ends the call by advancing to the last call state.
     */
    public void goToGoodbye() {
        setState(CallState.GOODBYE);
    }

    /**
     * Ends the call if not already done.
     *
     * @param hasSelfHungUp If the caller already hung up
     */
    public void endCall(boolean hasSelfHungUp) {
        this.hasHungUp = true;
        sessionManager.removeSession(this.bridgeId);
        audioQueue.clear();

        if (!hasSelfHungUp) {
            arUtil.hangup(this.bridgeId);
        }
    }

    /**
     * Advances the call state to the next.
     */
    public void advanceCallState() {
        setState(StateSequence.advanceCallState(state));
        logger.debug("Advance call state to {}", this.state);
    }

    /**
     * Plays the next audio item or handles the next step.
     */
    public void nextAudioOrStep() {
        if (isAudioPlaying.get()) {
            return;
        }
        if (hasHungUp) {
            return;
        }

        AudioItem item = audioQueue.poll();
        if (item != null) {
            logger.debug("Next audio item: {}", item.getName());
            item.startAsync()
                    .subscribe().with(
                            success -> {
                            },
                            failure -> {
                            }
                    );
        } else {
            if (currentBaseState != BaseState.DONE) {
                advanceBaseState();
            }
            callProcessor.process(this);
        }
    }
}
