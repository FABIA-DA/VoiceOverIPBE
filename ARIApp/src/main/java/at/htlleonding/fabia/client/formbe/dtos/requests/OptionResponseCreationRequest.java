package at.htlleonding.fabia.client.formbe.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class OptionResponseCreationRequest {
    private long optionId;
    private String telephoneNumber;

    public OptionResponseCreationRequest() {
    }

    public OptionResponseCreationRequest(long optionId, String telephoneNumber) {
        setOptionId(optionId);
        setTelephoneNumber(telephoneNumber);
    }
}
