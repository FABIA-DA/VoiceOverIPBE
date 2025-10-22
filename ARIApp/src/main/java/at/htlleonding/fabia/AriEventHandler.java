package at.htlleonding.fabia;

import at.htlleonding.fabia.callmanagement.*;
import ch.loway.oss.ari4java.generated.AriWSHelper;
import ch.loway.oss.ari4java.generated.models.ChannelHangupRequest;
import ch.loway.oss.ari4java.generated.models.PlaybackFinished;
import ch.loway.oss.ari4java.generated.models.RecordingFinished;
import ch.loway.oss.ari4java.generated.models.StasisStart;
import ch.loway.oss.ari4java.tools.AriConnectionEvent;

public class AriEventHandler extends AriWSHelper {
    @Override
    public void onConnectionEvent(AriConnectionEvent event) {
        super.onConnectionEvent(event);
    }

    @Override
    protected void onStasisStart(StasisStart message) {
        System.out.println("New call entered Stasis: " + message.getChannel().getName());
        CallSession session = new CallSession(
                message.getChannel().getId(),
                message.getChannel().getName()
        );
        session.setState(CallState.Greeting);
        CallManager.getInstance().addSession(session);
        session.nextAudioOrStep();
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
    protected void onChannelHangupRequest(ChannelHangupRequest message) {
        String channelId = message.getChannel().getId();
        CallManager.getInstance().removeSession(channelId);
    }
}
