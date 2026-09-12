package at.htlleonding.fabia;

import at.htlleonding.fabia.call.context.runtime.CallContext;
import at.htlleonding.fabia.callmgmt.*;
import at.htlleonding.fabia.callmgmt.audiomgmt.ActiveAudioRegistry;
import at.htlleonding.fabia.callmgmt.util.AriUtil;
import ch.loway.oss.ari4java.generated.AriWSHelper;
import ch.loway.oss.ari4java.generated.models.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static at.htlleonding.fabia.call.context.runtime.CallContext.instance;

/**
 * Handles all necessary ARI events to start and advance the call.
 */
@ApplicationScoped
public class AriEventHandler extends AriWSHelper {

    @Inject
    ActiveAudioRegistry activeAudioRegistry;

    @Inject
    AriUtil ariUtil;

    @Inject
    CallSession callSession;

    private static final Logger logger =
            LoggerFactory.getLogger(AriEventHandler.class);

    private static final int THREAD_POOL_SIZE = 10;

    private final ExecutorService threadPool =
            Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    private static final String SOUND_PREFIX = "sound:custom/";

    @Override
    protected void onStasisStart(StasisStart message) {
        threadPool.execute(() -> {
            Channel channel = message.getChannel();

            if (channel == null) {
                logger.warn("Received StasisStart without a channel");
                return;
            }

            String channelId = channel.getId();
            String channelName = channel.getName();

            logger.debug(
                    "New call entered Stasis: channelId={}, channelName={}",
                    channelId,
                    channelName
            );

            CallContext context = instance();

            context.createCall(channelId);

            context.run(channelId, () -> {
                if (!Objects.equals(channel.getState(), "Up")) {
                    ariUtil.answer(channelId);
                }

                callSession.initialize(
                        channelId,
                        channelName
                );

                callSession.advanceCallState();

                callSession.initialize(channelId, channelName);
                callSession.advanceCallState();

                callSession.nextAudioOrStep();
            });
        });
    }

    @Override
    protected void onPlaybackStarted(PlaybackStarted message) {
        threadPool.execute(() -> {
            logger.debug("Playback started");

            String mediaUri = message.getPlayback().getMedia_uri();

            if (mediaUri == null || !mediaUri.startsWith(SOUND_PREFIX)) {
                logger.warn("Unexpected playback media URI: {}", mediaUri);
                return;
            }

            String media = mediaUri.substring(SOUND_PREFIX.length());

            var playback = activeAudioRegistry.getPlayback(media);

            if (playback == null) {
                logger.warn("No playback registered for {}", media);
                return;
            }

            String channelId = playback.getChannelId();

            if (channelId == null) {
                logger.warn("Playback {} has no channel ID", media);
                return;
            }

            instance().run(channelId, () -> {
                CallSession session = callSession;
                if (session == null) {
                    logger.warn("No session found for channel {}", channelId);
                    return;
                }
                session.getIsAudioPlaying().set(true);
            });
        });
    }

    @Override
    protected void onPlaybackFinished(PlaybackFinished message) {
        threadPool.execute(() -> {
            String mediaUri =
                    message.getPlayback().getMedia_uri();

            if (mediaUri == null || !mediaUri.startsWith(SOUND_PREFIX)) {
                logger.warn(
                        "Unexpected playback media URI: {}",
                        mediaUri
                );
                return;
            }

            String media =
                    mediaUri.substring(SOUND_PREFIX.length());

            logger.debug("Playback finished: {}", media);

            var playback =
                    activeAudioRegistry.getPlayback(media);

            if (playback == null) {
                logger.warn(
                        "No playback registered for {}",
                        media
                );
                return;
            }

            String channelId = playback.getChannelId();

            activeAudioRegistry.removePlayback(media);

            if (channelId == null) {
                logger.warn(
                        "Channel ID was null for playback {}",
                        media
                );
                return;
            }

            instance().run(channelId, () -> {
                CallSession session = callSession;
                if (session == null) {
                    logger.warn("No session found for channel {}", channelId);
                    return;
                }
                session.getIsAudioPlaying().set(false);
                session.nextAudioOrStep();
            });
        });
    }

    @Override
    protected void onRecordingStarted(RecordingStarted message) {
        threadPool.execute(() -> {
            logger.debug("Recording started");

            var recording =
                    activeAudioRegistry.getRecording(
                            message.getRecording().getName()
                    );

            if (recording == null) {
                logger.warn(
                        "No recording registered for {}",
                        message.getRecording().getName()
                );
                return;
            }

            String channelId = recording.getChannelId();

            if (channelId == null) {
                logger.warn(
                        "Recording {} has no channel ID",
                        message.getRecording().getName()
                );
                return;
            }

            instance().run(channelId, () -> {
                CallSession session = callSession;
                if (session == null) {
                    logger.warn("No session found for channel {}", channelId);
                    return;
                }
                session.getIsAudioPlaying().set(true);
            });
        });
    }

    @Override
    protected void onRecordingFinished(RecordingFinished message) {
        threadPool.execute(() -> {
            String recordingName =
                    message.getRecording().getName();

            logger.debug(
                    "Recording finished: {}",
                    recordingName
            );

            var recording =
                    activeAudioRegistry.getRecording(recordingName);

            if (recording == null) {
                logger.warn(
                        "No recording registered for {}",
                        recordingName
                );
                return;
            }

            String channelId = recording.getChannelId();

            activeAudioRegistry.removeRecording(recordingName);

            if (channelId == null) {
                logger.warn(
                        "Recording {} has no channel ID",
                        recordingName
                );
                return;
            }

            instance().run(channelId, () -> {
                CallSession session = callSession;
                if (session == null) {
                    logger.warn("No session found for channel {}", channelId);
                    return;
                }
                session.getIsAudioPlaying().set(false);
                session.nextAudioOrStep();
            });
        });
    }

    @Override
    protected void onStasisEnd(StasisEnd message) {
        threadPool.execute(() -> {
            Channel channel = message.getChannel();

            if (channel == null) {
                logger.warn("Received StasisEnd without a channel");
                return;
            }

            String channelId = channel.getId();

            logger.info(
                    "StasisEnd event - channelId={}, name={}, state={}",
                    channelId,
                    channel.getName(),
                    channel.getState()
            );

            CallContext context = instance();

            if (context.getCall(channelId) != null) {
                context.run(channelId, () -> {
                    CallSession session = callSession;
                    if (session != null) {
                        logger.info("Calling session.endCall() for channel {}", channelId);
                        session.endCall();
                    } else {
                        logger.info("No session found for channel {}", channelId);
                    }
                });

                context.destroyCall(channelId);
            } else {
                logger.info("No CallState found for channel {}", channelId);
            }
        });
    }

    @Override
    protected void onChannelDestroyed(ChannelDestroyed message) {
        threadPool.execute(() -> {
            Channel channel = message.getChannel();

            String channelId =
                    channel != null
                            ? channel.getId()
                            : "<null>";

            logger.info(
                    "ChannelDestroyed event - channelId={}",
                    channelId
            );
        });
    }
}