package at.htlleonding.fabia.client.coquibe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoquiRequest {
    private String text;
    private String fileName;

    public CoquiRequest() {

    }

    public CoquiRequest(String text, String fileName) {
        setText(text);
        setFileName(fileName);
    }
}
