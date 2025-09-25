package at.htlleonding.fabia.client.coquibe;

import at.htlleonding.fabia.client.BaseClient;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public final class SpeechGenerationClient extends BaseClient {
    private static SpeechGenerationClient client = null;

    private static final HttpUrl URL = new HttpUrl.Builder()
            .scheme("http")
            .host("coqui-be")
            .port(8000)
            .addPathSegment("convert")
            .build();

    public static SpeechGenerationClient getClient() {
        if(client == null){
            client = new SpeechGenerationClient();
        }
        return client;
    }

    public void generateSpeech(String text, String fileName) throws IOException {
        CoquiRequest requestBody = new CoquiRequest(text, fileName);
        String jsonBody = OBJECT_MAPPER.writeValueAsString(requestBody);
        System.out.println(jsonBody);

        Request request = new Request.Builder()
                .url(URL)
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        getResponse(request, new TypeReference<Void>() {});
    }
}
