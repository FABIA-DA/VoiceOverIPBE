package at.htlleonding.fabia;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CoquiRequest {
    @JsonProperty("text")
    private String text;
    @JsonProperty("fileName")
    private String fileName;

    public CoquiRequest(String text, String fileName){
        setText(text);
        setFileName(fileName);
    }

    public String getText(){
        return text;
    }

    public void setText(String text){
        this.text = text;
    }

    public String getFileName(){
        return fileName;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }
}
