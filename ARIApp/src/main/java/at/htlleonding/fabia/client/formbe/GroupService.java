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
    /**
     * Gets all groups.
     * @return The groups in a list response
     */
    @GET
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<GroupListResponse> getAllGroups();

    /**
     * Gets a specific group by its id.
     * @param id The id of the group.
     * @return The full group.
     */
    @GET
    @Path("{id}")
    public Uni<Group> getGroupById(@PathParam("id") Long id);
}
