package at.htlleonding.fabia.client.whisperbe;

import at.htlleonding.fabia.client.BaseClient;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class TranscriptionClient extends BaseClient {
    private static TranscriptionClient client = null;

    private static HttpRequest buildPostRequest(String url, Path filePath) throws FileNotFoundException {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofFile(filePath))
                .header("Accept", "application/json")
                .header("Content-Type", "application/octet-stream")
                .build();
    }

    @Override
    protected String getBaseUrl() {
        return "http://whisper-be:8000";
    }

    @Override
    protected String getController() {
        return "transcribe";
    }

    public static TranscriptionClient getClient() {
        if (client == null) {
            client = new TranscriptionClient();
        }
        return client;
    }

    public CompletableFuture<String> transcribe(String filePath){
        try{
            HttpRequest request = buildPostRequest(buildUrl(null, null), Path.of(filePath));

            return getCompletableFuture(request, new TypeReference<WhisperResponse>() {})
                    .thenApply(WhisperResponse::getText);
        } catch (FileNotFoundException e) {
            System.out.println("Whisper transcription error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
