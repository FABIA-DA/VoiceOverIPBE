package at.htlleonding.fabia.client;

import at.htlleonding.fabia.client.dtos.FieldType;
import at.htlleonding.fabia.client.dtos.responses.FieldTypeListResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class FieldTypeClient extends BaseClient {
    @Override
    protected String getController() {
        return "api/field-types";
    }

    public CompletableFuture<List<FieldType>> getAllFieldTypesAsync() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(null, null)))
                .GET()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        return getCompletableFuture(request, new TypeReference<FieldTypeListResponse>() {})
                .thenApply(FieldTypeListResponse::getTypes);
    }

    public CompletableFuture<FieldType> getFieldTypeByIdAsync(long fieldTypeId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(Long.toString(fieldTypeId), null)))
                .GET()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        return getCompletableFuture(request, new TypeReference<FieldType>() {});
    }
}
