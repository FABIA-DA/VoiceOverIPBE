package at.htlleonding.fabia.client.dtos.responses;

import at.htlleonding.fabia.client.dtos.FieldType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class FieldTypeListResponse {
    private List<FieldType> types;

    public FieldTypeListResponse() {}

    public FieldTypeListResponse(List<FieldType> types) {
        this.types = types;
    }
}
