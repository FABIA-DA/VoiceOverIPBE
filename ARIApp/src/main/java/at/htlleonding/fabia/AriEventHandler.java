package at.htlleonding.fabia;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.ActiveAudioRegistry;
import at.htlleonding.fabia.callmgmt.util.AriUtil;
import ch.loway.oss.ari4java.generated.AriWSHelper;
import ch.loway.oss.ari4java.generated.models.*;
import ch.loway.oss.ari4java.tools.AriConnectionEvent;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Handles all necessary ari events to start and advance the call
 */
@AllArgsConstructor
public class AriEventHandler extends AriWSHelper {
    private SessionManager sessionManager;
    private ActiveAudioRegistry activeAudioRegistry;
    private CallProcessor callProcessor;
    private AriUtil ariUtil;

    private final Logger logger = LoggerFactory.getLogger(AriEventHandler.class);

    @Override
    public void onConnectionEvent(AriConnectionEvent event) {
        super.onConnectionEvent(event);
    }

    @Override
    protected void onPlaybackStarted(PlaybackStarted message) {
        logger.debug("playback started");
        String prefix = "sound:";
        String media = message.getPlayback().getMedia_uri().substring(prefix.length());

        String channelId = activeAudioRegistry.getPlayback(media).getBridgeId();

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
        logger.debug("recording started");
        String channelId = activeAudioRegistry.getRecording(message.getRecording().getName()).getBridgeId();

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
    protected void onStasisStart(StasisStart message) {
        Channel channel = message.getChannel();
        logger.debug("New call entered Stasis: {}", channel.getName());

        if(!Objects.equals(channel.getState(), "Up")){
            ariUtil.answer(channel.getId());
        }

        Bridge bridge = ariUtil.createBridge();

        if(bridge == null){
            return;
        }

        ariUtil.addChannelToBridge(bridge.getId(), channel.getId());

        CallSession session = new CallSession(
                bridge.getId(),
                channel.getName(),
                sessionManager,
                callProcessor,
                ariUtil
        );

        session.advanceCallState();
        sessionManager.addSession(session);
        session.nextAudioOrStep();
    }

    @Override
    protected void onRecordingFinished(RecordingFinished message) {
        logger.debug("Recording finished: {}", message.getRecording().getName());
        String channelId = activeAudioRegistry.getRecording(message.getRecording().getName()).getBridgeId();
        activeAudioRegistry.removeRecording(message.getRecording().getName());

        if (channelId == null) {
            return;
        }

        CallSession session = sessionManager.getSession(channelId);

        if (session == null) {
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
        String channelId = activeAudioRegistry.getPlayback(media).getBridgeId();
        activeAudioRegistry.removePlayback(media);

        if (channelId == null) {
            return;
        }

        CallSession session = sessionManager.getSession(channelId);

        if (session == null) {
            return;
        }

        session.getIsAudioPlaying().set(false);
        session.nextAudioOrStep();
    }

    @Override
    protected void onStasisEnd(StasisEnd message) {
        String channelId = message.getChannel().getId();

        if (channelId == null) {
            return;
        }

        logger.debug("Channel with id {} hung up", channelId);
        CallSession session = sessionManager.getSession(channelId);

        if (session == null) {
            return;
        }

        session.endCall(true);
        ariUtil.destroyBridge(session.getBridgeId());
        sessionManager.removeSession(session.getBridgeId());
    }
}
