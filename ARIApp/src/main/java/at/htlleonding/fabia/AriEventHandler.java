package at.htlleonding.fabia;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.ActiveAudioRegistry;
import ch.loway.oss.ari4java.generated.AriWSHelper;
import ch.loway.oss.ari4java.generated.models.*;
import ch.loway.oss.ari4java.tools.AriConnectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AriEventHandler extends AriWSHelper {
    private final Logger logger = LoggerFactory.getLogger(AriEventHandler.class);

    @Override
    protected void onPlaybackStarted(PlaybackStarted message) {
        String prefix = "sound:";
        String media = message.getPlayback().getMedia_uri().substring(prefix.length());

        String channelId = ActiveAudioRegistry.getInstance().getPlayback(media).getChannelId();

        if (channelId == null) {
            return;
        }

        CallSession session = SessionManager.getInstance().getSession(channelId);

        if (session == null) {
            return;
        }

        session.getIsAudioPlaying().set(true);
    }

    @Override
    protected void onRecordingStarted(RecordingStarted message) {
        String channelId = ActiveAudioRegistry.getInstance().getRecording(message.getRecording().getName()).getChannelId();

        if(channelId == null) {
            return;
        }

        CallSession session = SessionManager.getInstance().getSession(channelId);

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
        SessionManager.getInstance().addSession(session);
        session.nextAudioOrStep();
    }

    @Override
    protected void onRecordingFinished(RecordingFinished message) {
        logger.debug("Recording finished: {}", message.getRecording().getName());
        String channelId = ActiveAudioRegistry.getInstance().getRecording(message.getRecording().getName()).getChannelId();
        ActiveAudioRegistry.getInstance().removeRecording(message.getRecording().getName());

        if(channelId == null) {
            return;
        }

        CallSession session = SessionManager.getInstance().getSession(channelId);

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
        String channelId = ActiveAudioRegistry.getInstance().getPlayback(media).getChannelId();
        ActiveAudioRegistry.getInstance().removePlayback(media);

        if(channelId == null){
            return;
        }

        CallSession session = SessionManager.getInstance().getSession(channelId);

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
        CallSession session = SessionManager.getInstance().getSession(channelId);

        if(session == null){
            return;
        }

        session.close(true);
    }
}
