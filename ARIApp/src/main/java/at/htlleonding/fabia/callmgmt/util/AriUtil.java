package at.htlleonding.fabia.callmgmt.util;

import at.htlleonding.fabia.AriContext;
import ch.loway.oss.ari4java.tools.RestException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public final class AriUtil {
    @Inject
    AriContext ariContext;

    private final Logger logger = LoggerFactory.getLogger(AriUtil.class);

    private void startRecording(String channelId, String recordingName) throws RestException {
        ariContext.getAri()
                .channels()
                .record(channelId, recordingName, "wav")
                .setMaxDurationSeconds(10)
                .setMaxSilenceSeconds(5)
                .setBeep(true)
                .execute();
    }

    /**
     * Asks asterisk to start a new recording.
     *
     * @param channelId     The channel id of the caller to record
     * @param recordingName The name of the recording
     */
    public Uni<Void> startRecordingAsync(String channelId, String recordingName) {
        return Uni.createFrom().deferred(() -> {
                    try {
                        startRecording(channelId, recordingName);
                        return Uni.createFrom().voidItem();
                    } catch (RestException e) {
                        logger.error("Failed to start recording {} for channel {}", recordingName, channelId, e);
                        return Uni.createFrom().failure(e);
                    }
                })
                .runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    private void playSound(String channelId, String sound) throws RestException {
        ariContext.getAri()
                .channels()
                .play(channelId, "sound:custom/" + sound)
                .execute();
    }

    /**
     * Asks asterisk to play  specific sound.
     *
     * @param channelId The channel id of the caller to play the sound for
     * @param sound     The name of the sound
     */
    public Uni<Void> playSoundAsync(String channelId, String name, String sound) {
        return Uni.createFrom().deferred(() -> {
            try {
                playSound(channelId, sound);
                return Uni.createFrom().voidItem();
            } catch (RestException e) {
                String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                if (msg.contains("404") || msg.contains("channel not found")) {
                    logger.warn("Start playback: channel missing, ignoring {} for {}", name, channelId);
                    return Uni.createFrom().voidItem();
                }
                logger.error("Failed to start playback {} for channel {}. ARI error: {}. Request: play(channel={}, media=sound:custom/{})",
                        name, channelId, e.getMessage(), channelId, sound, e);
                return Uni.createFrom().failure(e);
            } catch (Throwable t) {
                logger.error("Unexpected error when starting playback {} for channel {}: {}", name, channelId, t.toString(), t);
                return Uni.createFrom().voidItem();
            }
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    /**
     * Tries to hang up a call in asterisk.
     *
     * @param channelId The id of the callers channel
     */
    public void hangup(String channelId) {
        if (channelId == null) {
            logger.warn("hangup called with null channelId");
            return;
        }
        try {
            if (ariContext == null || ariContext.getAri() == null) {
                logger.warn("ARI client unavailable, cannot hangup {}", channelId);
                return;
            }
            ariContext.getAri().channels().hangup(channelId).execute();
        } catch (RestException e) {
            logger.error("Failed to hangup call for channel {}", channelId, e);
        } catch (Throwable t) {
            logger.error("Unexpected error when hanging up channel {}: {}", channelId, t.toString(), t);
        }
    }

    public void answer(String channelId) {
        try {
            ariContext.getAri()
                    .channels()
                    .answer(channelId)
                    .execute();
        } catch (RestException e) {
            logger.error("Failed to answer call for channel {}", channelId, e);
        }
    }

    private void startMoh(String channelId) throws RestException {
        ariContext.getAri()
                .channels()
                .startMoh(channelId)
                .execute();
    }

    public Uni<Void> startMohAsync(String channelId) {
        return Uni.createFrom().deferred(() -> {
            try {
                startMoh(channelId);
                return Uni.createFrom().voidItem();
            } catch (RestException e) {
                String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                if (msg.contains("404") || msg.contains("channel not found")) {
                    logger.warn("Start MOH: channel missing, ignoring for {}", channelId);
                    return Uni.createFrom().voidItem();
                }
                logger.error("Failed to start moh for channel {}", channelId, e);
                return Uni.createFrom().failure(e);
            } catch (Throwable t) {
                logger.error("Unexpected error when starting moh for channel {}: {}", channelId, t.toString(), t);
                return Uni.createFrom().voidItem();
            }
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    private void endMoh(String channelId) throws RestException {
        ariContext.getAri()
                .channels()
                .stopMoh(channelId)
                .execute();
    }

    public Uni<Void> endMohAsync(String channelId) {
        return Uni.createFrom().deferred(() -> {
            try {
                endMoh(channelId);
                return Uni.createFrom().voidItem();
            } catch (RestException e) {
                String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                if (msg.contains("404") || msg.contains("channel not found")) {
                    logger.warn("End MOH: channel missing, ignoring for {}", channelId);
                    return Uni.createFrom().voidItem();
                }
                logger.error("Failed to stop moh for channel {}", channelId, e);
                return Uni.createFrom().failure(e);
            } catch (Throwable t) {
                logger.error("Unexpected error when stopping moh for channel {}: {}", channelId, t.toString(), t);
                return Uni.createFrom().voidItem();
            }
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    public boolean channelExists(String channelId) {
        try {
            return ariContext != null && ariContext.getAri() != null
                    && ariContext.getAri().channels().get(channelId).execute() != null;
        } catch (RestException e) {
            return false;
        } catch (Throwable t) {
            logger.debug("channelExists unexpected: {}", t.toString());
            return false;
        }
    }
}

