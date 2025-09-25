package at.htlleonding.fabia.client.whisperbe;

import at.htlleonding.fabia.client.BaseClient;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class TranscriptionClient extends BaseClient {
    private static TranscriptionClient client = null;

    private static HttpRequest buildPostRequest(String url, Path filePath) {
        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        String CRLF = "\r\n";
        String fileName = filePath.getFileName().toString();

        try {
            String mimeType = Files.probeContentType(filePath);

            StringBuilder sb = new StringBuilder();
            sb.append("--").append(boundary).append(CRLF);
            sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileName).append("\"").append(CRLF);
            sb.append("Content-Type: ").append(mimeType != null ? mimeType : "application/octet-stream").append(CRLF);
            sb.append(CRLF);

            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] preamble = sb.toString().getBytes();
            byte[] epilogue = (CRLF + "--" + boundary + "--" + CRLF).getBytes();

            HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofByteArrays(
                    java.util.List.of(preamble, fileBytes, epilogue)
            );

            return HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("Accept", "application/json")
                    .POST(bodyPublisher)
                    .build();
        }
        catch (IOException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage(), e);
        }
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
        HttpRequest request = buildPostRequest(buildUrl(null, null), Path.of(filePath));

        return getCompletableFuture(request, new TypeReference<WhisperResponse>() {})
                .thenApply(WhisperResponse::getText);
    }
}
