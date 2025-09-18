package at.htlleonding.fabia.client.formbe.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class Group {
    private Long id;
    private String name;
    private Long parentId;
    private String parentName;
    private List<Subgroup> subgroups;
    private List<Form> forms;

    public Group() {}

    public Group(String name, Long parentId, String parentName, List<Subgroup> subgroups, List<Form> forms) {
        this.name = name;
        this.parentId = parentId;
        this.parentName = parentName;
        this.subgroups = subgroups;
        this.forms = forms;
    }
}
