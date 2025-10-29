package at.htlleonding.fabia.client.coquibe;

import at.htlleonding.fabia.client.BaseClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public final class SpeechGenerationClient extends BaseClient {
    private static SpeechGenerationClient client = null;

    private static final HttpUrl URL = new HttpUrl.Builder()
            .scheme("http")
            .host("coqui-be")
            .port(8000)
            .addPathSegment("convert")
            .build();

    private final Logger logger = LoggerFactory.getLogger(SpeechGenerationClient.class);

    @Override
    protected void configureHttpClient(OkHttpClient.Builder builder) {
        builder.protocols(List.of(Protocol.HTTP_1_1));
    }

    public static SpeechGenerationClient getClient() {
        if (client == null) {
            client = new SpeechGenerationClient();
        }
        return client;
    }

    public void generateSpeech(String text, String fileName) throws IOException {
        CoquiRequest requestBody = new CoquiRequest(text, fileName);
        try {
            String jsonBody = OBJECT_MAPPER.writeValueAsString(requestBody);

            Request request = new Request.Builder()
                    .url(URL)
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            getResponse(request, new TypeReference<Void>() {});
            logger.info("Generated speech for text \"{}\" and file name {}", text, fileName);
        } catch (IOException e) {
            logger.error("Tried to generate speech for text \"{}\" with file name {}, but {} with message: {}", text, fileName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }
}
