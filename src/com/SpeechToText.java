package com;

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

public class SpeechToText {
    private static LiveSpeechRecognizer liveSpeechRecognizer;
    private static final Logger logger = Logger.getLogger(SpeechToText.class.getName());
    private static String speechRecogResult;
    private static boolean ignore = false;
    private static boolean speechThreadRunning = false;
    private static boolean resourcesThreadRunning = false;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private static final Object[] objs = new Object[] {liveSpeechRecognizer, logger, executorService, speechRecogResult};

    public static String get() {
        return speechRecogResult;
    }

    public static void kill() {
        for (int i = 0; i < 3; i++) {
            objs[i] = null;
        }
        ignore = false;
        speechThreadRunning = false;
        resourcesThreadRunning = false;
    }

    private static void startResourcesThread() {
        if (resourcesThreadRunning) {
            logger.log(Level.INFO, "Thread already running");
            return;
        }
        Runnable runnable = () -> {
            try {
                resourcesThreadRunning = true;
                while (true) {
                    if (!AudioSystem.isLineSupported(Port.Info.MICROPHONE)) {
                        logger.log(Level.INFO, "Microphone not available");
                    }
                    Thread.sleep(350);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, null, e);
            }
        };
        executorService.submit(runnable);
    }

    private static void makeDecision(String speech, List<WordResult> speechWords) {
        System.out.println(speech);
    }

    private static void startSpeechRecog() {
        if (speechThreadRunning) {
            logger.log(Level.INFO, "Thread already running");
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                speechThreadRunning = true;
                ignore = false;
                liveSpeechRecognizer.startRecognition(true);

                logger.log(Level.INFO, "Start speaking");
                try {
                    while (speechThreadRunning) {
                        SpeechResult speechResult = liveSpeechRecognizer.getResult();
                        if (!ignore){
                            if (speechResult == null) {
                                logger.log(Level.INFO, "Speech incomprehensible");
                            } else {
                                speechRecogResult = speechResult.getHypothesis();
                                System.out.println(speechRecogResult);
                                makeDecision(speechRecogResult, speechResult.getWords());
                            }
                        } else {
                            logger.log(Level.INFO, "Ignoring results");
                        }
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, null, e);
                    speechThreadRunning = false;
                }
                logger.log(Level.INFO, "exited");
            }
        };
        executorService.submit(runnable);
    }

    public static void start() {
        logger.log(Level.INFO, "Loading speech recognizer");
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
        try {
            liveSpeechRecognizer = new LiveSpeechRecognizer(configuration);
        } catch (IOException ioException) {
            logger.log(Level.SEVERE, null, ioException);
        }
        startResourcesThread();
        startSpeechRecog();
    }

    private SpeechToText() {}
}
