package at.htlleonding.fabia.client.coquibe;

import at.htlleonding.fabia.client.BaseClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public final class SpeechGenerationClient extends BaseClient {
    private static SpeechGenerationClient client = null;

    private static HttpRequest buildPostRequest(String url, String jsonBody) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
    }

    @Override
    protected String getBaseUrl() {
        return "http://coqui-be:8000";
    }

    @Override
    protected String getController() {
        return "convert";
    }

    public static SpeechGenerationClient getClient() {
        if(client == null){
            client = new SpeechGenerationClient();
        }
        return client;
    }

    public CompletableFuture<Void> generateSpeech(String text, String fileName) {
        CoquiRequest requestBody = new CoquiRequest(text, fileName);

        try {
            String jsonBody = OBJECT_MAPPER.writeValueAsString(requestBody);

            HttpRequest request = buildPostRequest(buildUrl(null, null), jsonBody);

            return getCompletableFuture(request, new TypeReference<Void>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
