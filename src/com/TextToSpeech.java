package com;

// import libs for text to speech
import java.util.Locale; // for location to set lang
import javax.speech.Central; // for tts
import javax.speech.synthesis.Synthesizer; // for tts
import javax.speech.synthesis.SynthesizerModeDesc; // for tts

public class TextToSpeech {
    // static text to speech method
    public static void tts(String text) { // text is the text to be spoken
        try {
            // set voice
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us" + ".cmu_us_kal.KevinVoiceDirectory");
            // set engine
            Central.registerEngineCentral("com.sun.speech.freetts" + ".jsapi.FreeTTSEngineCentral");
            // create a synthesizer obj to read aloud
            Synthesizer synthesizer = Central.createSynthesizer(new SynthesizerModeDesc(Locale.US));
            // alocate memory
            synthesizer.allocate();
            // begin speaking
            synthesizer.resume();
            synthesizer.speakPlainText(text, null);
            // wait when finished until next message
            synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        } catch (Exception e) { // incase of error print message
            e.printStackTrace();
        }
    }
}
