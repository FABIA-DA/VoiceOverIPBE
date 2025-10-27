package at.htlleonding.fabia;

import at.htlleonding.fabia.callmanagement.*;
import ch.loway.oss.ari4java.generated.AriWSHelper;
import ch.loway.oss.ari4java.generated.models.*;
import ch.loway.oss.ari4java.tools.AriConnectionEvent;

public class AriEventHandler extends AriWSHelper {
    @Override
    public void onConnectionEvent(AriConnectionEvent event) {
        super.onConnectionEvent(event);
    }

    @Override
    protected void onStasisStart(StasisStart message) {
        try {
            System.out.println("New call entered Stasis: " + message.getChannel().getName());
            CallSession session = new CallSession(
                    message.getChannel().getId(),
                    message.getChannel().getName()
            );
            session.advanceCallState();
            CallManager.getInstance().addSession(session);
            session.nextAudioOrStep();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onRecordingFinished(RecordingFinished message) {
        System.out.println("Recording finished: " + message.getRecording().getName());
        String channelId = ActiveAudioRegistry.getInstance().getRecording(message.getRecording().getName()).getChannelId();
        CallSession session = CallManager.getInstance().getSession(channelId);

        session.nextAudioOrStep();
    }

    @Override
    protected void onPlaybackFinished(PlaybackFinished message) {
        String prefix = "sound:";
        String media = message.getPlayback().getMedia_uri().substring(prefix.length());
        System.out.println("Playback finished: " + media);
        String channelId = ActiveAudioRegistry.getInstance().getPlayback(media).getChannelId();
        CallSession session = CallManager.getInstance().getSession(channelId);

        session.nextAudioOrStep();
    }

    @Override
    protected void onStasisEnd(StasisEnd message) {
        String channelId = message.getChannel().getId();
        CallManager.getInstance().removeSession(channelId);
    }
}
