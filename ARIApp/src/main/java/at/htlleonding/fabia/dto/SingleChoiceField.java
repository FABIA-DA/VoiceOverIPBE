package at.htlleonding.fabia.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SingleChoiceField {
    private Long id;
    private String name;
    private List<Option> options;

    public SingleChoiceField() {}

    public SingleChoiceField(String name, List<Option> options) {
        this.name = name;
        this.options = options;
    }
}
