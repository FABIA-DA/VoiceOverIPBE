package at.htlleonding.fabia;

import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.ActiveAudioRegistry;
import at.htlleonding.fabia.callmgmt.util.AriUtil;
import ch.loway.oss.ari4java.generated.AriWSHelper;
import ch.loway.oss.ari4java.generated.models.*;
import ch.loway.oss.ari4java.tools.AriConnectionEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles all necessary ari events to start and advance the call
 */
@ApplicationScoped
public class AriEventHandler extends AriWSHelper {
    @Inject
    ActiveAudioRegistry activeAudioRegistry;
    @Inject
    SessionManager sessionManager;
    @Inject
    AriUtil ariUtil;
    @Inject
    CallProcessor callProcessor;

    private final int THREAD_POOL_SIZE = 10;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private final Logger logger = LoggerFactory.getLogger(AriEventHandler.class);
    private final String soundPrefix = "sound:custom/";

    @Override
    protected void onPlaybackStarted(PlaybackStarted message) {
        threadPool.execute(() -> {
            logger.debug("playback started");
            String media = message.getPlayback().getMedia_uri().substring(soundPrefix.length());

            String channelId = activeAudioRegistry.getPlayback(media).getChannelId();

            if (channelId == null) {
                return;
            }

            CallSession session = sessionManager.getSession(channelId);

            if (session == null) {
                return;
            }

            session.getIsAudioPlaying().set(true);
        });

    }

    @Override
    protected void onRecordingStarted(RecordingStarted message) {
        threadPool.execute(() -> {
            logger.debug("recording started");
            String channelId = activeAudioRegistry.getRecording(message.getRecording().getName()).getChannelId();

            if (channelId == null) {
                return;
            }

            CallSession session = sessionManager.getSession(channelId);

            if (session == null) {
                return;
            }

            session.getIsAudioPlaying().set(true);
        });
    }

    @Override
    protected void onStasisStart(StasisStart message) {
        threadPool.execute(() -> {
            Channel channel = message.getChannel();
            logger.debug("New call entered Stasis: {}", channel.getName());

            if (!Objects.equals(channel.getState(), "Up")) {
                ariUtil.answer(channel.getId());
            }

            CallSession session = new CallSession(
                    channel.getId(),
                    channel.getName(),
                    sessionManager,
                    callProcessor,
                    ariUtil, activeAudioRegistry
            );

            session.advanceCallState();
            sessionManager.addSession(session);
            session.nextAudioOrStep();
        });
    }

    @Override
    protected void onRecordingFinished(RecordingFinished message) {
        threadPool.execute(() -> {
            logger.debug("Recording finished: {}", message.getRecording().getName());
            String channelId = activeAudioRegistry.getRecording(message.getRecording().getName()).getChannelId();
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
        });
    }

    @Override
    protected void onPlaybackFinished(PlaybackFinished message) {
        threadPool.execute(() -> {
            String media = message.getPlayback().getMedia_uri().substring(soundPrefix.length());

            logger.debug("Playback finished: {}", media);
            String channelId = activeAudioRegistry.getPlayback(media).getChannelId();
            activeAudioRegistry.removePlayback(media);

            if (channelId == null) {
                logger.warn("Channel id was null in the media");
                return;
            }

            CallSession session = sessionManager.getSession(channelId);

            if (session == null) {
                logger.warn("No session with this channel id in session manager");
                return;
            }

            session.getIsAudioPlaying().set(false);
            session.nextAudioOrStep();
        });
    }

    @Override
    protected void onStasisEnd(StasisEnd message) {
        threadPool.execute(() -> {
            String channelId = message.getChannel() != null ? message.getChannel().getId() : "<null>";
            String name = message.getChannel() != null ? message.getChannel().getName() : "<null>";
            String state = message.getChannel() != null ? message.getChannel().getState() : "<null>";
            logger.info("StasisEnd event - channelId={}, name={}, state={}, fullMessage={}", channelId, name, state, message);
            CallSession session = sessionManager.getSession(channelId);
            if (session != null) {
                logger.info("Calling session.endCall() for channel {}", channelId);
                session.endCall();
            } else {
                logger.info("No session found for channel {} on StasisEnd", channelId);
            }
        });
    }

    @Override
    protected void onChannelDestroyed(ChannelDestroyed message) {
        threadPool.execute(() -> {
            String id = message.getChannel() != null ? message.getChannel().getId() : "<null>";
            logger.info("ChannelDestroyed event - channelId={}, cause={}", id, message);
        });
    }

    /*
    @Override
    protected void onStasisEnd(StasisEnd message) {
        String channelId = message.getChannel().getId();
        logger.info("<<<<===StasisEnd for channel {} ({})===>>>>", channelId, message.getChannel().getName());
        if (channelId == null) {
            return;
        }

        logger.debug("Channel with id {} hung up", channelId);
        CallSession session = sessionManager.getSession(channelId);

        if (session == null) {
            return;
        }

        session.endCall();
    }*/
}
