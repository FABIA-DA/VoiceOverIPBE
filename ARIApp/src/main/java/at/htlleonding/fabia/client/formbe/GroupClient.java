package at.htlleonding.fabia.client.formbe;

import at.htlleonding.fabia.client.formbe.dtos.Group;
import at.htlleonding.fabia.client.formbe.dtos.GroupListDto;
import at.htlleonding.fabia.client.formbe.dtos.responses.GroupListResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class GroupClient extends FormBaseClient {
    private static GroupClient client = null;

    @Override
    protected String getController() {
        return "api/groups";
    }

    public static GroupClient getClient() {
        if(client == null) {
            client = new GroupClient();
        }
        return client;
    }

    public CompletableFuture<List<GroupListDto>> getAllGroups() {
        HttpRequest request = buildGetRequest(buildUrl(null, null));

        return getCompletableFuture(request, new TypeReference<GroupListResponse>() {})
                .thenApply(GroupListResponse::getGroups);
    }

    public CompletableFuture<Group> getGroupById(long id) {
        HttpRequest request = buildGetRequest(buildUrl(Long.toString(id), null));

        return getCompletableFuture(request, new TypeReference<Group>() {});
    }
}
