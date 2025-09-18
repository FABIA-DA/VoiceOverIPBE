package at.htlleonding.fabia.client.formbe;

import at.htlleonding.fabia.client.BaseClient;

import java.net.URI;
import java.net.http.HttpRequest;

public abstract class FormBaseClient extends BaseClient {
    @Override
    protected String getBaseUrl() {
        return "http://localhost:8080";
    }

    protected static HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();
    }

    protected static HttpRequest buildPostRequest(String url, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();
    }
}
