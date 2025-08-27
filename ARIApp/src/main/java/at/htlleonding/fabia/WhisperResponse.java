package at.htlleonding.fabia;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WhisperResponse {
    private String text;

    @JsonCreator
    public WhisperResponse(@JsonProperty("text") String text) {
        setText(text);
    }

    public String getText(){
        return text;
    }

    public void setText(String text){
        this.text = text.trim();
    }
}
