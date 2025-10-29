package at.htlleonding.fabia.client.formbe;

import at.htlleonding.fabia.client.formbe.dtos.Group;
import at.htlleonding.fabia.client.formbe.dtos.GroupListDto;
import at.htlleonding.fabia.client.formbe.dtos.responses.GroupListResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.HttpUrl;
import okhttp3.Request;

import java.io.IOException;
import java.util.List;

public final class GroupClient extends FormBaseClient {
    private static GroupClient client = null;

    private static HttpUrl.Builder getGroupBuilder() {
        return getBuilder()
                .addPathSegment("api")
                .addPathSegment("groups");
    }

    public static GroupClient getClient() {
        if (client == null) {
            client = new GroupClient();
        }
        return client;
    }

    public List<GroupListDto> getAllGroups() throws IOException {
        try {
            Request request = new Request.Builder()
                    .url(getGroupBuilder().build())
                    .get()
                    .build();

            List<GroupListDto> list = getResponse(request, new TypeReference<GroupListResponse>() {
            }).getGroups();
            logger.info("Got all groups");
            return list;
        } catch (IOException e) {
            logger.error("Tried to get all groups with message {}", e.getMessage());
            throw e;
        }
    }

    public Group getGroupById(long id) throws IOException {
        try {
            Request request = new Request.Builder()
                    .url(getGroupBuilder()
                            .addPathSegment(Long.toString(id))
                            .build())
                    .get()
                    .build();

            Group group = getResponse(request, new TypeReference<Group>() {
            });
            logger.info("Got group with id {}", id);
            return group;
        } catch (IOException e) {
            logger.error("Tried to get group with id {}, but {}", id, e.getMessage());
            throw e;
        }
    }
}
