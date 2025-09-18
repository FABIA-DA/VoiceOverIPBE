package at.htlleonding.fabia.client.formbe.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class Subgroup {
    private long id;
    private String name;

    public Subgroup() {}

    public Subgroup(String name) {
        this.name = name;
    }
}
