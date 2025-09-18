package at.htlleonding.fabia.client.formbe.dtos.responses;

import at.htlleonding.fabia.client.formbe.dtos.GroupListDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class GroupListResponse {
    private List<GroupListDto> groups;

    public GroupListResponse() {}

    public GroupListResponse(List<GroupListDto> groups) {
        this.groups = groups;
    }
}
