package at.htlleonding.fabia;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.ActiveAudioRegistry;
import ch.loway.oss.ari4java.generated.AriWSHelper;
import ch.loway.oss.ari4java.generated.models.*;
import ch.loway.oss.ari4java.tools.AriConnectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AriEventHandler extends AriWSHelper {
    private final Logger logger = LoggerFactory.getLogger(AriEventHandler.class);

    @Override
    public void onConnectionEvent(AriConnectionEvent event) {
        super.onConnectionEvent(event);
    }

    @Override
    protected void onStasisStart(StasisStart message) {
        logger.debug("New call entered Stasis: {}", message.getChannel().getName());
        CallSession session = new CallSession(
                message.getChannel().getId(),
                message.getChannel().getName()
        );
        session.advanceCallState();
        SessionManager.getInstance().addSession(session);
        session.nextAudioOrStep();
    }

    @Override
    protected void onRecordingFinished(RecordingFinished message) {
        logger.debug("Recording finished: {}", message.getRecording().getName());
        String channelId = ActiveAudioRegistry.getInstance().getRecording(message.getRecording().getName()).getChannelId();
        CallSession session = SessionManager.getInstance().getSession(channelId);

        session.nextAudioOrStep();
    }

    @Override
    protected void onPlaybackFinished(PlaybackFinished message) {
        String prefix = "sound:";
        String media = message.getPlayback().getMedia_uri().substring(prefix.length());
        logger.debug("Playback finished: {}", media);
        String channelId = ActiveAudioRegistry.getInstance().getPlayback(media).getChannelId();
        CallSession session = SessionManager.getInstance().getSession(channelId);

        session.nextAudioOrStep();
    }

    @Override
    protected void onChannelHangupRequest(ChannelHangupRequest message) {
        String channelId = message.getChannel().getId();
        logger.debug("Channel with id {} hung up", channelId);
        CallSession session = SessionManager.getInstance().getSession(channelId);
        session.close(true);
    }
}
