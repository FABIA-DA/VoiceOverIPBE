package at.htlleonding.fabia.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class BaseClient {
    private final static String BASE_URL = "http://localhost:8080";
    protected final static HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    protected final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected abstract String getController();

    protected static boolean responseSuccessful(int status) {
        return status == 200;
    }

    protected String buildUrl(String action, Map<String, String> queryParams) {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);
        urlBuilder.append("/").append(getController());

        if (action != null) {
            urlBuilder.append("/").append(action);
        }

        if (queryParams == null
                || queryParams.isEmpty()
                || queryParams.entrySet().stream().anyMatch(s -> s.getKey() == null || s.getValue() == null)) {
            return urlBuilder.toString();
        }

        boolean first = true;
        for (Map.Entry<String, String> param : queryParams.entrySet()) {
            if (first) {
                urlBuilder.append("?");
                first = false;
            } else {
                urlBuilder.append("&");
            }

            urlBuilder.append(param.getKey()).append("=").append(param.getValue());
        }

        return urlBuilder.toString();
    }

    protected static <T> CompletableFuture<T> getCompletableFuture(HttpRequest request, TypeReference<T> type) {
        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (responseSuccessful(response.statusCode())) {
                        try {
                            return OBJECT_MAPPER.readValue(
                                    response.body(),
                                    type
                            );
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        throw new RuntimeException("HTTP error: " + response.statusCode() + " " + response.body());
                    }
                });
    }
}
