package at.htlleonding.fabia.client.formbe;

import at.htlleonding.fabia.client.formbe.dtos.Form;
import at.htlleonding.fabia.client.formbe.dtos.FormListDto;
import at.htlleonding.fabia.client.formbe.dtos.responses.FormListResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.HttpUrl;
import okhttp3.Request;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class FormClient extends FormBaseClient {
    private static FormClient client = null;

    private static HttpUrl.Builder getFormBuilder() {
        return getBuilder()
                .addPathSegment("api")
                .addPathSegment("forms");
    }

    public static FormClient getClient() {
        if (client == null) {
            client = new FormClient();
        }
        return client;
    }

    public List<FormListDto> getAllForms() throws IOException {
        try {
            Request request = new Request.Builder()
                    .url(getFormBuilder().build())
                    .get()
                    .build();

            List<FormListDto> list = getResponse(request, new TypeReference<>() {
            });
            logger.info("Got all forms");
            return list;
        }
        catch (Exception e) {
            logger.error("Tried to get all forms, but {}", e.getMessage());
            throw e;
        }
    }

    public Form getFormById(long id) throws IOException {
        try{
            Request request = new Request.Builder()
                    .url(getFormBuilder()
                            .addPathSegment(Long.toString(id))
                            .build())
                    .get()
                    .build();

            Form form = getResponse(request, new TypeReference<Form>() {
            });
            logger.info("Got form with id {}", id);
            return form;
        }
        catch (IOException e) {
            logger.error("Tried to get form with id {}, but {}", id, e.getMessage());
            throw e;
        }
    }
}
