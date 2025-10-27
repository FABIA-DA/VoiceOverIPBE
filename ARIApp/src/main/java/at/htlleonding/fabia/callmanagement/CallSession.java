package at.htlleonding.fabia.callmanagement;

import at.htlleonding.fabia.client.formbe.dtos.Form;
import at.htlleonding.fabia.client.formbe.dtos.Group;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;

import static at.htlleonding.fabia.callmanagement.StateSequence.*;

public final class CallSession {
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
    @Setter
    private Form selectedForm;
    @Getter
    @Setter
    private Integer currentFieldGroupIdx;
    @Getter
    @Setter
    private Integer currentSingleChoiceFieldIdx;
    @Getter
    @Setter
    private Integer currentFieldIdx;
    @Getter
    @Setter
    private BaseState currentBaseState;
    private final Queue<AudioItem> audioQueue = new LinkedList<>();

    public CallSession(String channelId, String channelName) {
        this.channelId = channelId;
        this.channelName = channelName;
        setState(getFirstCallState());
    }

    public void setState(CallState state) {
        this.state = state;
        this.currentBaseState = getFirstBaseState();
    }

    public void goToInput(){
        currentBaseState = getInputState();
    }

    public void enqueueAudio(AudioItem item) {
        if (item == null) {
            return;
        }
        audioQueue.add(item);
    }

    private void advanceBaseState() {
        currentBaseState = StateSequence.advanceBaseState(currentBaseState);
    }

    public void advanceCallState() {
        System.out.println("Advance Call State from " + state);
        setState(StateSequence.advanceCallState(state));
    }

    public void nextAudioOrStep() {
        AudioItem item = audioQueue.poll();
        if (item != null) {
            System.out.println("Next Audio Item: " + item.getName());
            item.start();
        } else {
            if (!StateSequence.baseStateIsDone(currentBaseState)) {
                System.out.println("Advanced base state from " + currentBaseState);
                advanceBaseState();
            }
            CallProcessor.process(this);
        }
    }
}
