package at.htlleonding.fabia.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Option {
    private Long id;
    private String name;
    private List<Long> fieldIds;

    public Option() {}

    public Option(String name, List<Long> fieldIds) {
        this.name = name;
        this.fieldIds = fieldIds;
    }
}
