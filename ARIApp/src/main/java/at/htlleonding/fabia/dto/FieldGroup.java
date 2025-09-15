package at.htlleonding.fabia.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FieldGroup {
    private Long id;
    private String name;
    private List<Long> singleChoiceFieldIds;
    private List<Long> fieldIds;

    public FieldGroup() {}

    public FieldGroup(String name, List<Long> singleChoiceFieldIds, List<Long> fieldIds) {
        this.name = name;
        this.singleChoiceFieldIds = singleChoiceFieldIds;
        this.fieldIds = fieldIds;
    }
}
