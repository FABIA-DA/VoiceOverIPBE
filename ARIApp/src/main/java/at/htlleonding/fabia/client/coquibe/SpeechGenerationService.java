package at.htlleonding.fabia.client.coquibe;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "speech-generation-client")
public interface SpeechGenerationService {
    @POST
    @Path("/convert")
    @Consumes(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
    public Uni<Void> generateSpeech(CoquiRequest request);
}
