package at.htlleonding.fabia.client.formbe.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class Field {
    private Long id;
    private Long fieldTypeId;
    private String name;
    private String description;
    @JsonProperty("isOptional")
    private boolean isOptional;
    private FieldType type;

    public Field() {}

    public Field(Long fieldTypeId, String name, String description, boolean isOptional, FieldType type) {
        this.fieldTypeId = fieldTypeId;
        this.name = name;
        this.description = description;
        this.isOptional = isOptional;
        this.type = type;
    }
}
