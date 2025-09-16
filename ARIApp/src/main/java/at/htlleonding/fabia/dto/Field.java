package at.htlleonding.fabia.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Field {
    private Long id;
    private Long fieldTypeId;
    private String name;
    private String description;
    private boolean isOptional;

    public Field() {}

    public Field(Long fieldTypeId, String name, String description, boolean isOptional) {
        this.fieldTypeId = fieldTypeId;
        this.name = name;
        this.description = description;
        this.isOptional = isOptional;
    }
}
