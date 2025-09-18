package at.htlleonding.fabia.client.formbe.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class FormListDto {
    private long id;
    private long groupId;
    private String name;
    private String groupName;
    private int fieldGroupCount;

    public FormListDto() {}

    public FormListDto(long groupId, String name, String groupName, int fieldGroupCount) {
        this.groupId = groupId;
        this.name = name;
        this.groupName = groupName;
        this.fieldGroupCount = fieldGroupCount;
    }
}
