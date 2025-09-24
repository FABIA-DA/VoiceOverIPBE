package at.htlleonding.fabia.client.whisperbe;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class WhisperResponse {
    private String text;

    public WhisperResponse() {}

    public WhisperResponse(String text) {
        setText(text);
    }
}
