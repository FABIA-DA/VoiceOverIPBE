package at.htlleonding.fabia.client.formbe;

import at.htlleonding.fabia.client.formbe.dtos.Group;
import at.htlleonding.fabia.client.formbe.dtos.responses.GroupListResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "group-client")
public interface GroupService {
    @GET
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<GroupListResponse> getAllGroups();

    @GET
    @Path("{id}")
    public Uni<Group> getGroupById(@PathParam("id") Long id);
}
