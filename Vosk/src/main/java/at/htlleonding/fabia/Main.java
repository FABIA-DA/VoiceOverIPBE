package at.htlleonding.fabia;

public class Main {
    public static void main(String[] args) {
        try {
            // download the model from their website and put the extracted folder in the models directory
            Decoder.decode("audio/greeting.wav", "models/vosk-model-de-0.21");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}