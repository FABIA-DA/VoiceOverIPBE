package at.htlleonding.fabia.client.formbe.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class OptionResponseCreationRequest {
    private long id;
    private String telephoneNumber;

    public OptionResponseCreationRequest() {
    }

    public OptionResponseCreationRequest(long id, String telephoneNumber) {
        this.id = id;
        this.telephoneNumber = telephoneNumber;
    }
}
