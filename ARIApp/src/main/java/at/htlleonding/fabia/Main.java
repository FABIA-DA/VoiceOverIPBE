package at.htlleonding.fabia;

import at.htlleonding.fabia.client.coquibe.SpeechGenerationClient;
import at.htlleonding.fabia.client.whisperbe.TranscriptionClient;
import ch.loway.oss.ari4java.ARI;
import ch.loway.oss.ari4java.AriVersion;
import ch.loway.oss.ari4java.generated.models.*;
import ch.loway.oss.ari4java.tools.ARIException;
import ch.loway.oss.ari4java.tools.AriConnectionEvent;
import ch.loway.oss.ari4java.tools.AriWSCallback;
import ch.loway.oss.ari4java.tools.RestException;

import java.net.http.HttpClient;

public class Main {
    private final static HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

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
                            Channel channel = start.getChannel();
                            System.out.println("Name: " + start.getChannel().getName());

                            CallManager manager = CallManager.getInstance();
                            manager.addSession(new CallSession(channel.getId(), channel.getName(), CallState.EnteringGroup));

                            try {
                                ari.channels().answer(channel.getId()).execute();
                                ari.channels().play(channel.getId(), "sound:greeting").execute();

                                String recordingName = channel.getId() + "_" + System.currentTimeMillis() + "_rec";
                                System.out.println("Recording channel: " + channel.getId() + " with name: " + recordingName);
                                ari.channels().record(channel.getId(), recordingName, "wav")
                                        .setMaxDurationSeconds(10)
                                        .setMaxSilenceSeconds(3)
                                        .setBeep(true)
                                        .execute();
                            } catch (RestException e) {
                                e.printStackTrace();
                            }
                        }

                        if (event instanceof RecordingFinished recordingFinished) {
                            CallManager manager = CallManager.getInstance();

                            String recordingName = recordingFinished.getRecording().getName();
                            String[] name_parts =  recordingName.split("_");

                            if(name_parts.length == 0) {
                                throw new RuntimeException("The name of the recording is invalid");
                            }

                            String channelId = name_parts[0];
                            CallSession session = manager.getSession(channelId);

                            String filePath = "/app/recordings/" + recordingName + ".wav";

                            System.out.println("Recording finished: " + recordingName);
                            System.out.println("Sending file: " + filePath);

                            switch(session.getState()) {
                                case EnteringGroup -> {
                                    //Say Groups
                                    transcribeAndPlay(ari, session, recordingName, filePath);
                                }

                                case EnteringForm -> {
                                    //Say Forms
                                    transcribeAndPlay(ari, session, recordingName, filePath);
                                }

                                default -> {
                                    System.out.println("Invalid session state");
                                }
                            }
                        }

                        if (event instanceof ChannelDtmfReceived channelDtmfReceived) {
                            String button = channelDtmfReceived.getDigit();
                            System.out.println("Button pressed: " + button);
                        }
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
        Thread.currentThread().join();
    }

    public static void transcribeAndPlay(ARI ari, CallSession session, String recordingName,  String filePath) {
        String audioName = recordingName + "_transcribe";
        String mediaPath = "sound:" + recordingName + "_transcribe";

        TranscriptionClient.getClient().transcribe(filePath).thenAccept(transcription -> {
            System.out.println("Transcribed text: " + transcription);
            SpeechGenerationClient.getClient().generateSpeech(transcription, audioName).thenAccept(_ -> {
                System.out.println("Generated audio");

                try {
                    ari.channels().play(session.getChannelId(), mediaPath).execute();

                    String newRecordingName = "caller_recording_" + System.currentTimeMillis();
                    System.out.println("Starting new recording: " + newRecordingName);
                    ari.channels().record(session.getChannelId(), newRecordingName, "wav")
                            .setMaxDurationSeconds(10)
                            .setMaxSilenceSeconds(3)
                            .setBeep(true)
                            .execute();

                } catch (RestException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }
}