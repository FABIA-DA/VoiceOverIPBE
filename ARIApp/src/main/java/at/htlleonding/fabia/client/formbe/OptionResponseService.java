package at.htlleonding.fabia.client.formbe;

import at.htlleonding.fabia.client.formbe.dtos.OptionResponse;
import at.htlleonding.fabia.client.formbe.dtos.requests.OptionResponseCreationRequest;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "option-response-client")
public interface OptionResponseService {
    @POST
    @Path("")
    @Consumes(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
    public Uni<OptionResponse> createOptionResponse(OptionResponseCreationRequest request);
}
