package at.htlleonding.fabia.client.formbe;

import at.htlleonding.fabia.client.formbe.dtos.FieldResponse;
import at.htlleonding.fabia.client.formbe.dtos.requests.FieldResponseCreationRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;

public final class FieldResponseClient extends FormBaseClient {
    private static FieldResponseClient client = null;

    private static HttpUrl getUrl() {
        return getBuilder()
                .addPathSegment("api")
                .addPathSegment("field-responses")
                .build();
    }

    public static FieldResponseClient getClient() {
        if (client == null) {
            client = new FieldResponseClient();
        }
        return client;
    }

    public FieldResponse createFieldResponse(long fieldId, String tele, String value) throws IOException {
        try{
            FieldResponseCreationRequest requestBody = new FieldResponseCreationRequest(fieldId, tele, value);
            String jsonBody = OBJECT_MAPPER.writeValueAsString(requestBody);

            Request request = new Request.Builder()
                    .url(getUrl())
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            FieldResponse response = getResponse(request, new TypeReference<>() {
            });
            logger.debug("Created FieldResponse for fieldId {}, tele {}, value: {}", fieldId, tele, value);
            return response;
        }
        catch (IOException e){
            logger.error("Tried to create field response with field id {}, but {}", fieldId, e.getMessage());
            throw e;
        }
    }
}
