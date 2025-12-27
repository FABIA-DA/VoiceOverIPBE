package at.htlleonding.fabia.callmgmt.audiomgmt;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public final class ActiveAudioRegistry {
    private final Map<String, PlaybackItem> activePlaybacks = new ConcurrentHashMap<>();
    private final Map<String, RecordingItem> activeRecordings = new ConcurrentHashMap<>();

    /**
     * Registers a new recording.
     * @param item The recording item
     */
    public void registerRecording(RecordingItem item) {
        activeRecordings.put(item.getName(), item);
    }

    /**
     * Registers a new playback.
     * @param item The playback item
     */
    public void registerPlayback(PlaybackItem item) {
        activePlaybacks.put(item.getName(), item);
    }

    /**
     * Gets the recording by its name.
     * @param name The name of the recording
     * @return The saved recording
     */
    public RecordingItem getRecording(String name) {
        return activeRecordings.get(name);
    }

    /**
     * Gets a playback by its name.
     * @param name The name of the playback
     * @return The saved playback
     */
    public PlaybackItem getPlayback(String name) {
        return activePlaybacks.get(name);
    }

    /**
     * Tries to remove a recording by its name.
     * @param name The name of the recording
     */
    public void removeRecording(String name) {
        activePlaybacks.remove(name);
    }

    /**
     * Removes a playback by its name.
     * @param name The name of the playback
     */
    public void removePlayback(String name) {
        activePlaybacks.remove(name);
    }
}
