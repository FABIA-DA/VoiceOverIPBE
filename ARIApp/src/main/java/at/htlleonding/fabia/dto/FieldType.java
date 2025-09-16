package at.htlleonding.fabia.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldType {
    private Long id;
    private String name;
    private String description;
    private String regex;

    public FieldType() {
    }

    public FieldType(String name, String description, String regex) {
        this.name = name;
        this.description = description;
        this.regex = regex;
    }
}
