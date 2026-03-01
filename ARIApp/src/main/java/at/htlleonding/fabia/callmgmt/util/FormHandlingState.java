package at.htlleonding.fabia.callmgmt.util;

import at.htlleonding.fabia.callmgmt.audiomgmt.RecordingItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FormHandlingState {
    private RecordingItem recording;
    private String transcript;

    public FormHandlingState(){
    }
}
