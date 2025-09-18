package at.htlleonding.fabia.client.formbe;

import at.htlleonding.fabia.client.formbe.dtos.FieldResponse;
import at.htlleonding.fabia.client.formbe.dtos.requests.FieldResponseCreationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public final class FieldResponseClient extends FormBaseClient {
    private static FieldResponseClient client = null;

    @Override
    protected String getController() {
        return "api/field-responses";
    }

    public static FieldResponseClient getClient() {
        if(client == null) {
            client = new FieldResponseClient();
        }
        return client;
    }

    public CompletableFuture<FieldResponse> createFieldResponse(long fieldId, String tele, String value){
        FieldResponseCreationRequest requestBody = new FieldResponseCreationRequest(fieldId, tele, value);

        try{
            String jsonBody = OBJECT_MAPPER.writeValueAsString(requestBody);

            HttpRequest request = buildPostRequest(buildUrl(null, null), jsonBody);

            return getCompletableFuture(request, new TypeReference<FieldResponse>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
