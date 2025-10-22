package at.htlleonding.fabia;

import at.htlleonding.fabia.callmanagement.CallManager;
import at.htlleonding.fabia.callmanagement.CallSession;
import at.htlleonding.fabia.callmanagement.CallState;
import at.htlleonding.fabia.client.coquibe.SpeechGenerationClient;
import at.htlleonding.fabia.client.whisperbe.TranscriptionClient;
import ch.loway.oss.ari4java.generated.models.*;
import ch.loway.oss.ari4java.tools.ARIException;
import ch.loway.oss.ari4java.tools.AriConnectionEvent;
import ch.loway.oss.ari4java.tools.AriWSCallback;
import ch.loway.oss.ari4java.tools.RestException;

import java.io.IOException;


public class Main {
    public static void main(String[] args) throws ARIException, InterruptedException {
        AriContext.getInstance().events()
                .eventWebsocket(AriContext.stasisApp)
                .setSubscribeAll(true)
                .execute(new AriWSCallback<Message>() {
                    @Override
                    public void onSuccess(Message event) {
                        System.out.println("Success: " + event.toString());


                        if (event instanceof StasisStart start) {
                            Channel channel = start.getChannel();
                            System.out.println("Name: " + start.getChannel().getName());

                            CallManager manager = CallManager.getInstance();
                            manager.addSession(new CallSession(channel.getId(), channel.getName(), CallState.EnterGroup));

                            try {
                                AriContext.getInstance().channels().answer(channel.getId()).execute();
                                AriContext.getInstance().channels().play(channel.getId(), "sound:greeting").execute();

                                String recordingName = channel.getId() + "_" + System.currentTimeMillis() + "_rec";
                                System.out.println("Recording channel: " + channel.getId() + " with name: " + recordingName);

                                CallSession session = manager.getSession(channel.getId());
                                if (session != null) {
                                    session.setCurrentRecordingName(recordingName);
                                }
                                AriContext.getInstance()
                                        .channels()
                                        .record(channel.getId(), recordingName, "wav")
                                        .setMaxDurationSeconds(10)
                                        .setBeep(true)
                                        .execute();
                            } catch (RestException e) {
                                e.printStackTrace();
                            }
                        }

                        if (event instanceof RecordingFinished recordingFinished) {
                            CallManager manager = CallManager.getInstance();

                            String recordingName = recordingFinished.getRecording().getName();
                            String[] name_parts = recordingName.split("_");

                            if (name_parts.length == 0) {
                                throw new RuntimeException("The name of the recording is invalid");
                            }

                            String channelId = name_parts[0];
                            CallSession session = manager.getSession(channelId);

                            String filePath = "/app/recordings/" + recordingName + ".wav";

                            System.out.println("Recording finished: " + recordingName);
                            System.out.println("Sending file: " + filePath);

                            try {
                                transcribeAndPlay(session, recordingName, filePath);
                            } catch (RestException | IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        if (event instanceof ChannelHangupRequest channelHangupRequest) {
                            System.out.println("ChannelHangupRequest: " + channelHangupRequest.toString());
                        }

                        if (event instanceof ChannelDtmfReceived channelDtmfReceived) {
                            String button = channelDtmfReceived.getDigit();
                            System.out.println("Button pressed: " + button);

                            if ("#".equals(button)) {
                                String channelId = null;
                                if (channelDtmfReceived.getChannel() != null) {
                                    channelId = channelDtmfReceived.getChannel().getId();
                                } else {
                                    // fallback if API provides channelId directly
                                    try {
                                        java.lang.reflect.Method m = channelDtmfReceived.getClass().getMethod("getChannelId");
                                        Object cid = m.invoke(channelDtmfReceived);
                                        if (cid instanceof String) channelId = (String) cid;
                                    } catch (Exception ignored) {
                                    }
                                }

                                if (channelId != null) {
                                    try {
                                        CallManager manager = CallManager.getInstance();
                                        CallSession session = manager.getSession(channelId);

                                        if (session != null && session.getCurrentRecordingName() != null) {
                                            // use recordings().stop(recordingName) instead of non-existing recordStop(...)
                                            AriContext.getInstance().recordings().stop(session.getCurrentRecordingName()).execute();
                                            System.out.println("Stopped recording: " + session.getCurrentRecordingName() + " for channel: " + channelId);
                                        } else {
                                            System.out.println("Could not determine recording name to stop for channel: " + channelId);
                                        }
                                    } catch (RestException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    System.out.println("Could not determine channel id to stop recording.");
                                }
                            }
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

    public static void transcribeAndPlay(CallSession session, String recordingName, String filePath) throws RestException, IOException {
        String audioName = recordingName + "_transcribe";
        String mediaPath = "sound:" + audioName;

        AriContext.getInstance().channels().startMoh(session.getChannelId()).execute();

        try {
            String transcription = TranscriptionClient.getClient().transcribe(filePath);
            System.out.println("Transcribed text: " + transcription);

            SpeechGenerationClient.getClient().generateSpeech(transcription, audioName);
            System.out.println("Generated audio");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }

        AriContext.getInstance().channels().stopMoh(session.getChannelId()).execute();
        AriContext.getInstance().channels().play(session.getChannelId(), mediaPath).execute();
    }
}