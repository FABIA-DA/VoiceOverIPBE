package at.htlleonding.fabia.callmgmt;

import at.htlleonding.fabia.callmgmt.audiomgmt.AudioItem;
import at.htlleonding.fabia.callmgmt.audiomgmt.RecordingItem;
import at.htlleonding.fabia.callmgmt.util.*;
import at.htlleonding.fabia.client.formbe.dtos.*;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static at.htlleonding.fabia.callmgmt.util.StateSequence.*;

public final class CallSession {
    private static final Logger logger = LoggerFactory.getLogger(CallSession.class);
    @Getter
    private final String channelId;
    @Getter
    private final String channelName;
    @Getter
    private CallState state;
    @Getter
    @Setter
    private Group selectedGroup;
    @Getter
    private Form selectedForm;
    @Getter
    private final Set<Field> usedFields = new LinkedHashSet<>();
    @Getter
    private Integer currentFieldGroupIdx;
    @Getter
    private Integer currentSingleChoiceFieldIdx;
    @Getter
    private Integer currentFieldIdx;
    @Getter
    @Setter
    private BaseState currentBaseState;
    private final ConcurrentLinkedQueue<AudioItem> audioQueue = new ConcurrentLinkedQueue<>();
    private boolean hasHungUp = false;
    @Getter
    private final GroupHandlingState groupHandlingState = new GroupHandlingState();
    @Getter
    private final FormHandlingState formHandlingState = new FormHandlingState();
    @Getter
    private final SingleChoiceFieldHandlingState singleChoiceFieldHandlingState = new SingleChoiceFieldHandlingState();
    @Getter
    private final FieldHandlingState fieldHandlingState = new FieldHandlingState();
    @Getter
    private final AtomicBoolean isAudioPlaying = new AtomicBoolean(false);

    private void advanceBaseState() {
        currentBaseState = StateSequence.advanceBaseState(currentBaseState);
        logger.debug("Advance base state to {}", currentBaseState);
    }

    private void setState(CallState state) {
        this.state = state;
        resetBaseState();
    }

    public CallSession(String channelId, String channelName) {
        this.channelId = channelId;
        this.channelName = channelName;
        setState(getFirstCallState());
    }

    public boolean fieldGroupValid() {
        return this.currentFieldGroupIdx != null
                && this.selectedForm != null;
    }

    public boolean singleChoiceFieldValid() {
        return fieldGroupValid()
                && this.currentSingleChoiceFieldIdx != null;
    }

    public boolean fieldValid() {
        return fieldGroupValid() && this.currentFieldIdx != null;
    }

    public boolean currentFieldGroupInBounds() {
        return fieldGroupValid()
                && this.currentFieldGroupIdx < this.selectedForm.getFieldGroups().size();
    }

    public boolean fieldGroupsLeft() {
        return fieldGroupValid()
                && this.currentFieldGroupIdx < this.selectedForm.getFieldGroups().size() - 1;
    }

    public boolean currentSingleChoiceFieldInBounds() {
        return singleChoiceFieldValid()
                && currentFieldGroupInBounds()
                && this.currentSingleChoiceFieldIdx <
                this.selectedForm.getFieldGroups()
                        .get(this.currentFieldGroupIdx)
                        .getSingleChoiceFields()
                        .size();
    }

    public boolean singleChoiceFieldsLeft() {
        return singleChoiceFieldValid()
                && this.currentSingleChoiceFieldIdx <
                this.selectedForm.getFieldGroups()
                        .get(this.currentFieldGroupIdx)
                        .getSingleChoiceFields()
                        .size() - 1;
    }

    public boolean currentFieldIdxInBounds() {
        return fieldValid()
                && currentFieldGroupInBounds()
                && this.currentFieldIdx < this.usedFields.size();
    }

    public boolean fieldsLeft() {
        return fieldValid()
                && this.currentFieldIdx < this.usedFields.size() - 1;
    }

    public boolean fieldGroupsEmpty() {
        return this.getSelectedForm().getFieldGroups()
                .isEmpty();
    }

    public boolean singleChoiceFieldsEmpty() {
        return this.getSelectedForm().getFieldGroups()
                .get(getCurrentFieldGroupIdx())
                .getSingleChoiceFields()
                .isEmpty();
    }

    public boolean fieldsEmpty() {
        return this.usedFields.isEmpty();
    }

    public void tryIncreaseFieldGroupIdx() {
        if (this.getSelectedForm() != null && getCurrentFieldGroupIdx() == null) {
            setCurrentFieldGroupIdx(0);
            return;
        }

        if (currentFieldGroupInBounds()) {
            setCurrentFieldGroupIdx(currentFieldGroupIdx + 1);
        }
    }

    public void tryIncreaseSingleChoiceFieldIdx() {
        if (fieldGroupValid() && getCurrentSingleChoiceFieldIdx() == null) {
            this.currentSingleChoiceFieldIdx = 0;
            return;
        }

        if (currentSingleChoiceFieldInBounds()) {
            this.currentSingleChoiceFieldIdx++;
        }
    }

    public void tryIncreaseFieldIdx() {
        if (fieldGroupValid() && getCurrentFieldIdx() == null) {
            this.currentFieldIdx = 0;
            return;
        }

        if (currentFieldIdxInBounds()) {
            this.currentFieldIdx++;
        }
    }

    public void setSelectedForm(Form selectedForm) {
        this.selectedForm = selectedForm;
        this.currentFieldGroupIdx = null;
        resetIndexes();
    }

    private void setCurrentFieldGroupIdx(Integer currentFieldGroupIdx) {
        this.usedFields.clear();
        this.currentFieldGroupIdx = currentFieldGroupIdx;
        if (currentFieldGroupIdx != null) {
            this.usedFields.addAll(
                    selectedForm.getFieldGroups()
                            .get(getCurrentFieldGroupIdx())
                            .getFields());
        }
    }

    public FieldGroup getCurrentFieldGroup() {
        return this.selectedForm.getFieldGroups()
                .get(getCurrentFieldGroupIdx());
    }

    public SingleChoiceField getCurrentSingleChoiceField() {
        return this.getSelectedForm()
                .getFieldGroups()
                .get(this.getCurrentFieldGroupIdx())
                .getSingleChoiceFields()
                .get(this.getCurrentSingleChoiceFieldIdx());
    }

    public Field getCurrentField() {
        return this.usedFields.toArray(Field[]::new)[getCurrentFieldIdx()];
    }

    public void addFields(Collection<? extends Field> fields) {
        this.usedFields.addAll(fields);
    }

    public void resetBaseState() {
        this.currentBaseState = getFirstBaseState();
    }

    public void goToFieldGroup() {
        setState(CallState.FIELD_GROUP);
    }

    public void resetIndexes() {
        this.currentSingleChoiceFieldIdx = null;
        this.currentFieldIdx = null;
    }

    public void enqueueAudio(AudioItem item) {
        if (item == null) {
            return;
        }
        audioQueue.add(item);
    }

    public void closeCall() {
        setState(CallState.GOODBYE);
    }

    public void close(boolean hasSelfHungUp) {
        this.hasHungUp = true;
        SessionManager.getInstance().removeSession(this.channelId);
        audioQueue.clear();

        if (!hasSelfHungUp) {
            AriUtil.hangup(this.channelId);
        }
    }

    public void advanceCallState() {
        setState(StateSequence.advanceCallState(state));
        logger.debug("Advance call state to {}", this.state);
    }

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
            item.start();
        } else {
            if (currentBaseState != BaseState.DONE) {
                advanceBaseState();
            }
            CallProcessor.process(this);
        }
    }
}
