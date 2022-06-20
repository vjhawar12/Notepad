package com;

// import libs
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Port;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.result.WordResult;
import java.util.ArrayList;

// speech to text class
public class SpeechToText {
    public static ArrayList<String> wordsSaid = new ArrayList<>(); // create list of comprehensible words spoken
    private static LiveSpeechRecognizer liveSpeechRecognizer; // obj for live speech recognition
    private static final Logger logger = Logger.getLogger(SpeechToText.class.getName()); // basically for debugging
    private static String speechRecogResult; // get result
    private static boolean ignore = false; // acts as a lock to prevent race conditions
    private static boolean speechThreadRunning = false; // lock to prevent race conditions
    private static boolean resourcesThreadRunning = false; // lock to prevent race coditinos
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static String get() { // accessor method to get words spoken
        StringBuilder str = new StringBuilder(); // better than string for appending stuff
        for (String s : wordsSaid) { // convert list to string
            str.append(s);
        }
        System.out.println("You said: " + str); // print out for debugging
        return str.toString(); // return what user saud
    }

    // method to stop recognition
    public static void kill() {
        liveSpeechRecognizer.stopRecognition();
    }

    // start microphone
    private static void startResourcesThread() {
        if (resourcesThreadRunning) { // prevent multiple threads from doing same thing
            logger.log(Level.INFO, "Thread already running");
            return;
        }
        Runnable runnable = () -> { // use lambda for run()
            try {
                resourcesThreadRunning = true; // set flag
                while (true) {
                    if (!AudioSystem.isLineSupported(Port.Info.MICROPHONE)) { // check if mic avialable
                        logger.log(Level.INFO, "Microphone not available");
                    }
                    Thread.sleep(100); // delay a bit
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, null, e); // print wrror
            }
        };
        executorService.submit(runnable); // add obj to execService
    }

    // add spoken words to array
    private static void makeDecision(String speech, List<WordResult> speechWords) {
        wordsSaid.add(speech);
    }

    // start speech recgnition
    private static void startSpeechRecog() {
        if (speechThreadRunning) {
            logger.log(Level.INFO, "Thread already running"); // can't have multiple threads running
            return; // return to prevent data race
        }
        Runnable runnable = () -> { // create anonymous class
            speechThreadRunning = true; // set lock
            ignore = false; // set log
            liveSpeechRecognizer.startRecognition(true); // call helper method

            logger.log(Level.INFO, "Start speaking"); // prompt user to speak
            try {
                while (speechThreadRunning) { // as long as thread alive
                    SpeechResult speechResult = liveSpeechRecognizer.getResult(); // get spoken
                    if (!ignore){ // check flag
                        if (speechResult == null) { // print out speech invalid
                            logger.log(Level.INFO, "Speech incomprehensible");
                        } else { //
                            speechRecogResult = speechResult.getHypothesis(); // get what engine thinks you said
                            System.out.println(speechRecogResult); // for debugging
                            wordsSaid.add(speechResult.getHypothesis()); // add that to list
                            makeDecision(speechRecogResult, speechResult.getWords()); // call helper method
                        }
                    } else {
                        // logger.log(Level.INFO, "Ignoring results");
                    }
                }
            } catch (Exception e) {
                 logger.log(Level.WARNING, null, e); // print out exception
                speechThreadRunning = false; // reset lock
            }
             logger.log(Level.INFO, "exited"); // inform user
        };
        executorService.submit(runnable); // call method
    }

    // init this class
    public static void init() {
        logger.log(Level.INFO, "Loading speech recognizer");
        // load stuff from jar files and external libraries
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
        // instantiate liveSpeechRecognizer obj and set configuration
        try {
            liveSpeechRecognizer = new LiveSpeechRecognizer(configuration);
        } catch (IOException ioException) {
            // logger.log(Level.SEVERE, null, ioException);
        }
        // call helper methods
        startResourcesThread();
        startSpeechRecog();
    }

    // private constructor so multiple speech to text objects aren't made
    private SpeechToText() {}
}
