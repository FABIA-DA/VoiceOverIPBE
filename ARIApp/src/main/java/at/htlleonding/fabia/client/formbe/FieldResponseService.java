package at.htlleonding.fabia.client.formbe;

import at.htlleonding.fabia.client.formbe.dtos.FieldResponse;
import at.htlleonding.fabia.client.formbe.dtos.requests.FieldResponseCreationRequest;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "field-response-client")
public interface FieldResponseService {
    /**
     * Creates a response for a specific field.
     * @param request The data to refer to the caller and the value
     * @return The new field response
     */
    @POST
    @Path("")
    @Consumes(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
    public Uni<FieldResponse> createFieldResponse(FieldResponseCreationRequest request);
}
