package at.htlleonding.fabia.client.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class FieldResponse {
    private Long id;
    private Long fieldId;
    private String telephoneNumber;
    private String value;

    public FieldResponse() {}

    public FieldResponse(Long fieldId, String telephoneNumber, String value) {
        this.fieldId = fieldId;
        this.telephoneNumber = telephoneNumber;
        this.value = value;
    }
}
