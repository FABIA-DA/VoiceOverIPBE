package at.htlleonding.fabia.client.formbe.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class Form {
    private Long id;
    private String name;
    private Long groupId;
    private String groupName;
    private List<FieldGroup> fieldGroups;

    public Form() {}

    public Form(String name, Long groupId, List<FieldGroup> fieldGroups) {
        this.name = name;
        this.groupId = groupId;
        this.fieldGroups = fieldGroups;
    }
}
