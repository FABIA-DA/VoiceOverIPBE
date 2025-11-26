package at.htlleonding.fabia.callmgmt.audiomgmt;

import jakarta.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public final class ActiveAudioRegistry {
    private final Map<String, AudioItem> activePlaybacks = new ConcurrentHashMap<>();
    private final Map<String, AudioItem> activeRecordings = new ConcurrentHashMap<>();

    public void registerRecording(AudioItem item) {
        activeRecordings.put(item.getName(), item);
    }

    public void registerPlayback(AudioItem item) {
        activePlaybacks.put(item.getName(), item);
    }

    public AudioItem getRecording(String name) {
        return activeRecordings.get(name);
    }

    public AudioItem getPlayback(String name) {
        return activePlaybacks.get(name);
    }

    public void removeRecording(String name) {
        activePlaybacks.remove(name);
    }

    public void removePlayback(String name) {
        activePlaybacks.remove(name);
    }
}
