package at.htlleonding.fabia.client.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class Group {
    private Long id;
    private String name;
    private Long parentId;
    private List<Long> subgroupIds;
    private List<Long> formIds;

    public Group() {}

    public Group(String name, Long parentId, List<Long> subgroupIds, List<Long> formIds) {
        this.name = name;
        this.parentId = parentId;
        this.subgroupIds = subgroupIds;
        this.formIds = formIds;
    }
}
