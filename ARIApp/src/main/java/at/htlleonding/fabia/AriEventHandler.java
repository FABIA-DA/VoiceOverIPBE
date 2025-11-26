package at.htlleonding.fabia;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.ActiveAudioRegistry;
import ch.loway.oss.ari4java.generated.AriWSHelper;
import ch.loway.oss.ari4java.generated.models.*;
import ch.loway.oss.ari4java.tools.AriConnectionEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AriEventHandler extends AriWSHelper {
    @Inject
    SessionManager sessionManager;

    @Inject
    ActiveAudioRegistry activeAudioRegistry;

    private final Logger logger = LoggerFactory.getLogger(AriEventHandler.class);

    @Override
    protected void onPlaybackStarted(PlaybackStarted message) {
        String prefix = "sound:";
        String media = message.getPlayback().getMedia_uri().substring(prefix.length());

        String channelId = activeAudioRegistry.getPlayback(media).getChannelId();

        if (channelId == null) {
            return;
        }

        CallSession session = sessionManager.getSession(channelId);

        if (session == null) {
            return;
        }

        session.getIsAudioPlaying().set(true);
    }

    @Override
    protected void onRecordingStarted(RecordingStarted message) {
        String channelId = activeAudioRegistry.getRecording(message.getRecording().getName()).getChannelId();

        if(channelId == null) {
            return;
        }

        CallSession session = sessionManager.getSession(channelId);

        if(session == null) {
            return;
        }

        session.getIsAudioPlaying().set(true);
    }

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
       sessionManager.addSession(session);
        session.nextAudioOrStep();
    }

    @Override
    protected void onRecordingFinished(RecordingFinished message) {
        logger.debug("Recording finished: {}", message.getRecording().getName());
        String channelId = activeAudioRegistry.getRecording(message.getRecording().getName()).getChannelId();
        activeAudioRegistry.removeRecording(message.getRecording().getName());

        if(channelId == null) {
            return;
        }

        CallSession session = sessionManager.getSession(channelId);

        if(session == null) {
            return;
        }

        session.getIsAudioPlaying().set(false);
        session.nextAudioOrStep();
    }

    @Override
    protected void onPlaybackFinished(PlaybackFinished message) {
        String prefix = "sound:";
        String media = message.getPlayback().getMedia_uri().substring(prefix.length());

        logger.debug("Playback finished: {}", media);
        String channelId = activeAudioRegistry.getPlayback(media).getChannelId();
        activeAudioRegistry.removePlayback(media);

        if(channelId == null){
            return;
        }

        CallSession session = sessionManager.getSession(channelId);

        if(session == null){
            return;
        }

        session.getIsAudioPlaying().set(false);
        session.nextAudioOrStep();
    }

    @Override
    protected void onChannelHangupRequest(ChannelHangupRequest message) {
        String channelId = message.getChannel().getId();

        if(channelId == null){
            return;
        }

        logger.debug("Channel with id {} hung up", channelId);
        CallSession session = sessionManager.getSession(channelId);

        if(session == null){
            return;
        }

        session.close(true);
    }
}
