package at.htlleonding.fabia.client.formbe.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class FieldGroup {
    private Long id;
    private String name;
    private List<SingleChoiceField> singleChoiceFields;
    private List<Field> fields;

    public FieldGroup() {}

    public FieldGroup(String name, List<SingleChoiceField> singleChoiceFields, List<Field> fields) {
        this.name = name;
        this.singleChoiceFields = singleChoiceFields;
        this.fields = fields;
    }
}
