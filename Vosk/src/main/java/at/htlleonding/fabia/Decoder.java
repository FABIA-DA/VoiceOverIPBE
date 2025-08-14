package at.htlleonding.fabia;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Decoder {
    public static void decode(String filePath, String modelPath) {
        if (!Files.exists(Paths.get(filePath))) {
            System.out.printf("Audio file does not exist: %s%n", filePath);
            return;
        }

        if (!Files.exists(Paths.get(modelPath))) {
            throw new IllegalArgumentException("Model directory does not exist: " + modelPath);
        }

        LibVosk.setLogLevel(LogLevel.DEBUG);

        try (Model model = new Model(modelPath);
             InputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(filePath)));
             Recognizer recognizer = new Recognizer(model, 16000)) {

            int nbytes;
            byte[] b = new byte[4096];
            while ((nbytes = ais.read(b)) >= 0) {
                if (recognizer.acceptWaveForm(b, nbytes)) {
                    //System.out.println(recognizer.getResult());
                } else {
                    System.out.println(recognizer.getPartialResult());
                }
            }

            System.out.println(recognizer.getFinalResult());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
