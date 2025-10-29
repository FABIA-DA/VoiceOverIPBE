package at.htlleonding.fabia.client.formbe.dtos;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.Instant;


@Getter
@Setter
public final class OptionResponse {
    private Long id;
    private Long optionId;
    private String telephoneNumber;
    private Instant submittedAt;

    public OptionResponse() {}

    public OptionResponse(Long optionId, String telephoneNumber, Instant submittedAt) {
        setOptionId(optionId);
        setTelephoneNumber(telephoneNumber);
        setSubmittedAt(submittedAt);
    }
}
