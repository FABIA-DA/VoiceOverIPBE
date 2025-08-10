package at.htlleonding.fabia;

import ch.loway.oss.ari4java.ARI;
import ch.loway.oss.ari4java.AriVersion;
import ch.loway.oss.ari4java.generated.models.Message;
import ch.loway.oss.ari4java.generated.models.StasisStart;
import ch.loway.oss.ari4java.tools.ARIException;
import ch.loway.oss.ari4java.tools.AriConnectionEvent;
import ch.loway.oss.ari4java.tools.AriWSCallback;
import ch.loway.oss.ari4java.tools.RestException;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws ARIException, InterruptedException {
        String ariUrl = "http://localhost:8088";
        String ariUser = "ariuser";
        String ariPass = "aripass";
        String stasisApp = "my-ari-app";

        ARI ari = ARI.build(ariUrl, stasisApp, ariUser, ariPass, AriVersion.IM_FEELING_LUCKY);

        ari.events()
                .eventWebsocket(stasisApp)
                .setSubscribeAll(true)
                .execute(new AriWSCallback<Message>() {
            @Override
            public void onSuccess(Message result) {
                if (result instanceof StasisStart) {
                    StasisStart startEvent = (StasisStart) result;
                    String channelId = startEvent.getChannel().getId();

                    System.out.println("Caller connected on channel: " + channelId);

                    try {
                        AudioConverter.convertWav("ARIApp/unconvertedAudio/ragebait.wav", "ARIApp/audio/ragebait.wav");
                        ari.channels()
                                .play(channelId, "sound:custom/ragebait")
                                .execute();
                        System.out.println("Playing audio to caller...");
                    } catch (RestException e) {
                        System.err.println("Error playing audio: " + e.getMessage());
                    } catch (UnsupportedAudioFileException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                /*
                AudioUploader.sendFile(
                        "http://localhost:8000/transcribe",
                        Path.of("ARIApp/audio/Ragebait.m4a").toString())
                                .subscribe(
                                        res -> System.out.println("Transcribed text:" + res),
                                        err -> System.err.println("Error during file upload: " + err.getMessage()
                                ));*/
            }

            @Override
            public void onFailure(RestException e) {
                System.out.println("Failure: " + e.getMessage());
            }

            @Override
            public void onConnectionEvent(AriConnectionEvent event) {
                System.out.println("Connection Event Received " + event.toString());
            }
        });

        // Keep running
        Thread.currentThread().join();    }
}