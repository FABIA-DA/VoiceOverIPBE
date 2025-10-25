package at.htlleonding.fabia.client.formbe.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class Option {
    private Long id;
    private String name;
    private List<Field> fields;
    private Long singleChoiceFieldId;

    public Option() {}

    public Option(String name, List<Field> fields, Long singleChoiceFieldId) {
        this.name = name;
        this.fields = fields;
        this.singleChoiceFieldId = singleChoiceFieldId;
    }
}
