package at.htlleonding.fabia;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioConverter {
    public static void convertWav(String inputPath, String outputPath) throws IOException, UnsupportedAudioFileException {
        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);

        AudioInputStream originalStream = AudioSystem.getAudioInputStream(inputFile);

        AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                8000,                  // sample rate
                16,                    // sample size in bits
                1,                     // channels (mono)
                2,                     // frame size (bytes per frame)
                8000,                  // frame rate
                false                  // little endian
        );

        AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, originalStream);

        AudioSystem.write(convertedStream, AudioFileFormat.Type.WAVE, outputFile);

        originalStream.close();
        convertedStream.close();
    }
}
