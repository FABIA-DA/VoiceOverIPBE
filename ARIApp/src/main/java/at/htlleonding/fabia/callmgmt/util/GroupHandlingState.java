package at.htlleonding.fabia.callmgmt.util;

import at.htlleonding.fabia.callmgmt.audiomgmt.RecordingItem;
import at.htlleonding.fabia.client.formbe.dtos.GroupListDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GroupHandlingState {
    private List<GroupListDto> groupList;
    private RecordingItem recording;
    private String transcript;

    public GroupHandlingState() {
    }
}
