package at.htlleonding.fabia;

import ch.loway.oss.ari4java.ARI;
import ch.loway.oss.ari4java.AriVersion;
import ch.loway.oss.ari4java.generated.models.ChannelDtmfReceived;
import ch.loway.oss.ari4java.generated.models.Message;
import ch.loway.oss.ari4java.generated.models.RecordingFinished;
import ch.loway.oss.ari4java.generated.models.StasisStart;
import ch.loway.oss.ari4java.tools.ARIException;
import ch.loway.oss.ari4java.tools.AriConnectionEvent;
import ch.loway.oss.ari4java.tools.AriWSCallback;
import ch.loway.oss.ari4java.tools.RestException;

import java.util.List;

public class Main {
    public static void main(String[] args) throws ARIException, InterruptedException {
        String ariUrl = "http://asterisk:8088";
        String ariUser = "ariuser";
        String ariPass = "aripass";
        String stasisApp = "my-ari-app";

        ARI ari = ARI.build(ariUrl, stasisApp, ariUser, ariPass, AriVersion.IM_FEELING_LUCKY);

        ari.events()
                .eventWebsocket(stasisApp)
                .setSubscribeAll(true)
                .execute(new AriWSCallback<Message>() {
            @Override
            public void onSuccess(Message event) {
                System.out.println("Success: " + event.toString());

                if (event instanceof StasisStart start) {
                    String channelId = start.getChannel().getId();

                    try {
                        ari.channels().answer(channelId).execute();
                        ari.channels().play(channelId, "sound:greeting").execute();

                        String recordingName = "caller_recording_" + System.currentTimeMillis();
                        System.out.println("Recording channel: " + channelId + " with name: " + recordingName);
                        ari.channels().record(channelId, recordingName, "wav")
                                .setMaxDurationSeconds(10)
                                .setMaxSilenceSeconds(3)
                                .setBeep(true)
                                .execute();
                    } catch (RestException e) {
                        e.printStackTrace();
                    }
                }

                if (event instanceof RecordingFinished recordingFinished) {
                    String recordingName = recordingFinished.getRecording().getName();
                    String filePath = "/app/recordings/" + recordingName + ".wav";

                    System.out.println("Recording finished: " + recordingName);
                    System.out.println("Sending file: " + filePath);

                    AudioUploader.sendFile(
                                    "http://localhost:8000/transcribe",
                                    filePath)
                            .subscribe(
                                    res -> {
                                        System.out.println("Transcribed text: " + res);
                                    },
                                    err -> System.err.println("Error during file upload: " + err.getMessage())
                            );
                }

                if(event instanceof ChannelDtmfReceived) {
                    String button = ((ChannelDtmfReceived) event).getDigit();
                    System.out.println("Button pressed: " + button);
                }
//                AudioUploader.sendFile(
//                        "http://localhost:8000/transcribe",
//                        Path.of("ARIApp/audio/Ragebait.m4a").toString())
//                                .subscribe(
//                                        res -> System.out.println("Transcribed text:" + res),
//                                        err -> System.err.println("Error during file upload: " + err.getMessage()
//                                ));
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