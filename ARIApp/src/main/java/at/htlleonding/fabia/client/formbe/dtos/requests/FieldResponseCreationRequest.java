package at.htlleonding.fabia.client.formbe.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class FieldResponseCreationRequest {
    private long fieldId;
    private String telephoneNumber;
    private String value;

    public FieldResponseCreationRequest() {}

    public FieldResponseCreationRequest(long fieldId, String telephoneNumber, String value){
        this.fieldId = fieldId;
        this.telephoneNumber = telephoneNumber;
        this.value = value;
    }
}
