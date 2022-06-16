import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Port;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.result.WordResult;

public class SpeechToText {
    private LiveSpeechRecognizer recognizer;
    private String speechRecognitionResult;
    private boolean ignoreSpeechRecognitionResults = false;
    private boolean speechRecognizerThreadRunning = false;
    private boolean resourcesThreadRunning;
    private ExecutorService eventsExecutorService = Executors.newFixedThreadPool(2);

    public SpeechToText() throws IOException {

    }

}