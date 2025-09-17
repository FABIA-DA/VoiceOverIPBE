package at.htlleonding.fabia.client.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class Form {
    private Long id;
    private String name;
    private Long groupId;
    private List<Long> fieldGroupIds;

    public Form() {}

    public Form(String name, Long groupId, List<Long> fieldGroupIds) {
        this.name = name;
        this.groupId = groupId;
        this.fieldGroupIds = fieldGroupIds;
    }
}
