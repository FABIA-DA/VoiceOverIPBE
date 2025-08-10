package at.htlleonding.fabia;

import ch.loway.oss.ari4java.ARI;
import ch.loway.oss.ari4java.AriVersion;
import ch.loway.oss.ari4java.generated.models.ChannelDtmfReceived;
import ch.loway.oss.ari4java.generated.models.Message;
import ch.loway.oss.ari4java.tools.ARIException;
import ch.loway.oss.ari4java.tools.AriConnectionEvent;
import ch.loway.oss.ari4java.tools.AriWSCallback;
import ch.loway.oss.ari4java.tools.RestException;

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

                if(event.getType().equals("ChannelDtmfReceived")){
                    String digit = ((ChannelDtmfReceived) event).getDigit();
                    System.out.println("Digit pressed: " + digit);
                }

                String channelId = event.getAsterisk_id();
                try {
                    ari.channels()
                            .play(channelId, "sound:greeting")
                            .execute();
                } catch(RestException e) {
                    System.err.println("Error playing audio: " + e.getMessage());
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