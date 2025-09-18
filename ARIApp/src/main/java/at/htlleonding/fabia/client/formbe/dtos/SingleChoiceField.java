package at.htlleonding.fabia.client.formbe.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class SingleChoiceField {
    private Long id;
    private String name;
    private List<Option> options;

    public SingleChoiceField() {}

    public SingleChoiceField(String name, List<Option> options) {
        this.name = name;
        this.options = options;
    }
}
