package at.htlleonding.fabia.client.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class OptionResponse {
    private Long id;
    private Long optionId;
    private String telephoneNumber;

    public OptionResponse() {}

    public OptionResponse(Long optionId, String telephoneNumber) {
        this.optionId = optionId;
        this.telephoneNumber = telephoneNumber;
    }
}
