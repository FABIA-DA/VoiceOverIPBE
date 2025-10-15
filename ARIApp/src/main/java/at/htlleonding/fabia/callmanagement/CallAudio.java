package at.htlleonding.fabia.callmanagement;

import at.htlleonding.fabia.AriContext;
import ch.loway.oss.ari4java.tools.RestException;

public final class CallAudio {
    public static void playNext(CallSession session) {
        String next = session.getAudioQueue().poll();

        if (next == null) {
            CallProcessor.process(session);
            return;
        }

        playSound(session, next);
    }

    public static void startRecording(CallSession session, String recordingName) {
        try {
            AriContext.getInstance().channels()
                    .record(session.getChannelId(), recordingName, "wav")
                    .setMaxDurationSeconds(10)
                    .setMaxSilenceSeconds(3)
                    .setBeep(true)
                    .execute();
        } catch (RestException e) {
            System.err.println("Failed to start recording " + recordingName + " for channel " + session.getChannelId());
            e.printStackTrace();
            enqueueError(session);
        }
    }

    private static void playSound(CallSession session, String sound) {
        try {
            AriContext.getInstance().channels().play(session.getChannelId(), "sound:" + sound).execute();
        } catch (RestException e) {
            System.out.println("Failed playback for sound " + sound + " for channel " + session.getChannelId());
            e.printStackTrace();
            enqueueError(session);
        }
    }

    public static void enqueueError(CallSession session) {
        session.getAudioQueue().add("error");
        playNext(session);
    }
}
