package at.htlleonding.fabia.callmgmt.util;

import at.htlleonding.fabia.callmgmt.audiomgmt.RecordingItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SingleChoiceFieldHandlingState {
    private RecordingItem recording;
    private boolean optionMatch;
    private boolean retry;
    private String transcript;

    public SingleChoiceFieldHandlingState(){
    }
}
