package at.htlleonding.fabia.callmanagement;

import at.htlleonding.fabia.client.formbe.dtos.Form;
import at.htlleonding.fabia.client.formbe.dtos.Group;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;

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

    private void advanceBaseState() {
        switch (currentBaseState) {
            case Info -> currentBaseState = BaseState.List;
            case List -> currentBaseState = BaseState.RequestInput;
            case RequestInput -> currentBaseState = BaseState.ProcessInput;
            case ProcessInput -> currentBaseState = BaseState.Confirm;
            case Confirm -> currentBaseState = BaseState.Decide;
            case Decide -> currentBaseState = BaseState.Done;
        }
    }

    public CallSession(String channelId, String channelName) {
        this.channelId = channelId;
        this.channelName = channelName;
    }

    public void setState(CallState state){
        this.state = state;
        this.currentBaseState = BaseState.Info;
    }

    public void enqueueAudio(AudioItem item){
        if(item == null){
            return;
        }
        audioQueue.add(item);
    }

    public void nextAudioOrStep(){
        AudioItem item = audioQueue.poll();
        if (item != null) {
            item.start();
        } else {
            if(currentBaseState != BaseState.Done){
                System.out.println("Advanced base state");
                advanceBaseState();
            }
            System.out.println("Process call state");
            CallProcessor.process(this);
        }
    }
}
