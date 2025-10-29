package at.htlleonding.fabia.client.formbe.dtos;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.Instant;

@Getter
@Setter
public final class FieldResponse {
    private Long id;
    private Long fieldId;
    private String telephoneNumber;
    private String value;
    private Instant submittedAt;

    public FieldResponse() {}

    public FieldResponse(Long fieldId, String telephoneNumber, String value, Instant submittedAt) {
        setFieldId(fieldId);
        setTelephoneNumber(telephoneNumber);
        setValue(value);
        setSubmittedAt(submittedAt);
    }
}
