package at.htlleonding.fabia.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionResponse {
    private Long id;
    private Long optionId;
    private String telephoneNumber;

    public OptionResponse() {}

    public OptionResponse(Long optionId, String telephoneNumber) {
        this.optionId = optionId;
        this.telephoneNumber = telephoneNumber;
    }
}
