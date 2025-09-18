package at.htlleonding.fabia.client.formbe;

import at.htlleonding.fabia.client.formbe.dtos.OptionResponse;
import at.htlleonding.fabia.client.formbe.dtos.requests.OptionResponseCreationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public final class OptionResponseClient extends FormBaseClient {
    private static OptionResponseClient client = null;

    @Override
    protected String getController() {
        return "api/option-responses";
    }

    public static OptionResponseClient getClient() {
        if (client == null) {
            client = new OptionResponseClient();
        }
        return client;
    }

    public CompletableFuture<OptionResponse> createOptionResponse(long optionResponseId, String tele) {
        OptionResponseCreationRequest requestBody = new OptionResponseCreationRequest(optionResponseId, tele);

        try {
            String jsonBody = OBJECT_MAPPER.writeValueAsString(requestBody);

            HttpRequest request = buildPostRequest(buildUrl(null, null), jsonBody);

            return getCompletableFuture(request, new TypeReference<OptionResponse>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
