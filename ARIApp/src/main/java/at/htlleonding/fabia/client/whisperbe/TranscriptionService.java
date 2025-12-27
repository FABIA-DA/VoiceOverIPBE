package at.htlleonding.fabia.client.whisperbe;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestForm;

@RegisterRestClient(configKey = "transcription-client")
public interface TranscriptionService {
    /**
     * Transcribes the audio of a passed file.
     * @param pathToAudio The path to the audio file
     * @return The transcribed text
     */
    @POST
    @Path("/transcribe")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Uni<WhisperResponse> transcribe(@RestForm("file") java.nio.file.Path pathToAudio);
}
