package at.htlleonding.fabia.callmgmt.util;

import at.htlleonding.fabia.callmgmt.audiomgmt.RecordingItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldHandlingState {
    private RecordingItem recording;
    private RecordingItem correctnessRecording;
    private boolean inputMatched;
    private boolean retry;
    private String transcript;
    private String correctnessTranscript;

    public FieldHandlingState(){
    }
}
