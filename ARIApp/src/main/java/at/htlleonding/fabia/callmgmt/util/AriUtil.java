package at.htlleonding.fabia.callmgmt.util;

import at.htlleonding.fabia.AriContext;
import ch.loway.oss.ari4java.generated.models.Bridge;
import ch.loway.oss.ari4java.tools.RestException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@ApplicationScoped
public final class AriUtil {
    @Inject
    AriContext ariContext;

    private final Logger logger = LoggerFactory.getLogger(AriUtil.class);

    private void startRecording(String bridgeId, String recordingName) throws RestException {
        ariContext.getAri()
                .bridges()
                .record(bridgeId, recordingName, "wav")
                .setMaxDurationSeconds(10)
                .setMaxSilenceSeconds(5)
                .setBeep(true)
                .execute();
    }

    /**
     * Asks asterisk to start a new recording.
     *
     * @param bridgeId      The bridge id of the caller to record
     * @param recordingName The name of the recording
     */
    public Uni<Void> startRecordingAsync(String bridgeId, String recordingName) {
        return Uni.createFrom().deferred(() -> {
                    try {
                        startRecording(bridgeId, recordingName);
                        return Uni.createFrom().voidItem();
                    } catch (RestException e) {
                        logger.error("Failed to start recording {} for bridge {}", recordingName, bridgeId, e);
                        return Uni.createFrom().failure(e);
                    }
                })
                .runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    private void playSound(String bridgeId, String sound) throws RestException {
        ariContext.getAri()
                .bridges()
                .play(bridgeId, "sound:" + sound)
                .execute();
    }

    /**
     * Asks asterisk to play a specific sound.
     *
     * @param bridgeId The bridge id of the caller to play the sound for
     * @param sound    The name of the sound
     */
    public Uni<Void> playSoundAsync(String bridgeId, String sound) {
        return Uni.createFrom().deferred(() -> {
                    try {
                        playSound(bridgeId, sound);
                        return Uni.createFrom().voidItem();
                    } catch (RestException e) {
                        logger.error("Failed to start playback {} for bridge {}", sound, bridgeId, e);
                        return Uni.createFrom().failure(e);
                    }
                })
                .runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    /**
     * Tries to hang up a call in asterisk.
     *
     * @param channelId The id of the callers channel
     */
    public void hangup(String channelId) {
        try {
            ariContext.getAri()
                    .channels()
                    .hangup(channelId)
                    .execute();
        } catch (RestException e) {
            logger.error("Failed to hangup call for channel {}", channelId, e);
        }
    }

    public Bridge createBridge() {
        try {
            return ariContext.getAri()
                    .bridges()
                    .create()
                    .setType("mixing")
                    .setName("bridge-" + UUID.randomUUID())
                    .execute();
        } catch (RestException e) {
            logger.error("Failed to create bridge", e);
        }

        return null;
    }

    public void destroyBridge(String bridgeId) {
        try {
            ariContext.getAri()
                    .bridges()
                    .destroy(bridgeId)
                    .execute();
        } catch (RestException e) {
            logger.error("Failed to destroy bridge {}", bridgeId, e);
        }
    }

    public void addChannelToBridge(String bridgeId, String channelId) {
        try {
            ariContext.getAri()
                    .bridges()
                    .addChannel(bridgeId, channelId)
                    .execute();
        } catch (RestException e) {
            logger.error("Failed to add channel {} to bridge {}", channelId, bridgeId, e);
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

    private void startMoh(String bridgeId) throws RestException {
        ariContext.getAri()
                .bridges()
                .startMoh(bridgeId)
                .execute();
    }

    public Uni<Void> startMohAsync(String bridgeId) {
        return Uni.createFrom().deferred(() -> {
                    try {
                        startMoh(bridgeId);
                        return Uni.createFrom().voidItem();
                    } catch (RestException e) {
                        logger.error("Failed to start moh for bridge {}", bridgeId, e);
                        return Uni.createFrom().failure(e);
                    }
                })
                .runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    private void endMoh(String bridgeId) throws RestException {
        ariContext.getAri()
                .bridges()
                .stopMoh(bridgeId)
                .execute();
    }

    public Uni<Void> endMohAsync(String bridgeId) {
        return Uni.createFrom().deferred(() -> {
                    try {
                        endMoh(bridgeId);
                        return Uni.createFrom().voidItem();
                    } catch (RestException e) {
                        logger.error("Failed to stop moh for bridge {}", bridgeId, e);
                        return Uni.createFrom().failure(e);
                    }
                })
                .runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }
}

