package at.htlleonding.fabia.client.formbe;

import at.htlleonding.fabia.client.formbe.dtos.Form;
import at.htlleonding.fabia.client.formbe.dtos.FormListDto;
import at.htlleonding.fabia.client.formbe.dtos.responses.FormListResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class FormClient extends FormBaseClient {
    private static FormClient client = null;

    @Override
    protected String getController() {
        return "api/forms";
    }

    public static FormClient getClient() {
        if(client == null) {
            client = new FormClient();
        }
        return client;
    }

    public CompletableFuture<List<FormListDto>> getAllForms() {
        HttpRequest request = buildGetRequest(buildUrl(null, null));

        return getCompletableFuture(request, new TypeReference<FormListResponse>() {})
                .thenApply(FormListResponse::getForms);
    }

    public CompletableFuture<Form> getFormById(long id){
        HttpRequest request = buildGetRequest(buildUrl(Long.toString(id), null));

        return getCompletableFuture(request, new TypeReference<Form>() {});
    }
}
