package at.htlleonding.fabia.client.formbe;

import at.htlleonding.fabia.client.formbe.dtos.OptionResponse;
import at.htlleonding.fabia.client.formbe.dtos.requests.OptionResponseCreationRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;

import java.io.IOException;

public final class OptionResponseClient extends FormBaseClient {
    private static OptionResponseClient client = null;

    private static HttpUrl getUrl() {
        return getBuilder()
                .addPathSegment("api")
                .addPathSegment("option-responses")
                .build();
    }

    public static OptionResponseClient getClient() {
        if (client == null) {
            client = new OptionResponseClient();
        }
        return client;
    }

    public OptionResponse createOptionResponse(long optionResponseId, String tele) throws IOException {
        OptionResponseCreationRequest requestBody = new OptionResponseCreationRequest(optionResponseId, tele);
        String jsonBody = OBJECT_MAPPER.writeValueAsString(requestBody);

        Request request = new Request.Builder()
                .url(getUrl())
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        return getResponse(request, new TypeReference<>() {
        });
    }
}
