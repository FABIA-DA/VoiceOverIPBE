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

    public Option() {}

    public Option(String name, List<Field> fields) {
        this.name = name;
        this.fields = fields;
    }
}
