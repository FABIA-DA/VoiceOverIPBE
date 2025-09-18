package at.htlleonding.fabia.client.formbe.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class GroupListDto {
    private long id;
    private long parentId;
    private String name;
    private String parentName;
    private int subgroupCount;
    private int formCount;

    public GroupListDto() {
    }

    public GroupListDto(long parentId, String name, String parentName, int subgroupCount, int formCount) {
        this.parentId = parentId;
        this.name = name;
        this.parentName = parentName;
        this.subgroupCount = subgroupCount;
        this.formCount = formCount;
    }
}
