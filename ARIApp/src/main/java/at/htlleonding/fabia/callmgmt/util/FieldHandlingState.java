package at.htlleonding.fabia.callmgmt.util;

import at.htlleonding.fabia.callmgmt.audiomgmt.RecordingItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldHandlingState {
    private RecordingItem recording;
    private boolean inputMatched;
    private boolean retry;

    public FieldHandlingState(){
    }
}
