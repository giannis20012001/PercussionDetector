package eu.ubitech.percussiondetector.percussiondetectorserver;

import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Created by John Tsantilis on 2/8/2018.
 *
 * @author John Tsantilis <i.tsantilis [at] ubitech [dot] com>
 */
public class PercussionDetector implements OnsetHandler {
    public void setDispatcher() {
        if(dispatcher!= null) {
            dispatcher.stop();

        }

        float sampleRate = 44100;
        //float sampleRate = 16000;
        int bufferSize = 512;
        //int bufferSize = 4096;
        //int bufferSize = 10000;
        int overlap = 0;

        //==============================================================================================================
        //==============================================================================================================
        InputStream byteInputStream = new ByteArrayInputStream(AudioStreamServiceGrcpImpl.getReceivedAudioData());
        final AudioFormat audioFormat = getAudioFormat();
        final AudioInputStream audioInputStream = new AudioInputStream(
                byteInputStream,
                audioFormat,
                 AudioStreamServiceGrcpImpl.getReceivedAudioData().length/ audioFormat.getFrameSize());
        JVMAudioInputStream audioStream = new JVMAudioInputStream(audioInputStream);
        // create a new dispatcher
        dispatcher = new AudioDispatcher(audioStream, bufferSize, overlap);
        // add a processor, handle percussion event.
        dispatcher.addAudioProcessor(
                new PercussionOnsetDetector(
                        sampleRate,
                        bufferSize,
                        this,
                        sensitivity,
                        threshold)
        );
        // run the dispatcher (on a new thread).
        //new Thread(dispatcher,"Audio dispatching").start();
        dispatcher.run();

        //==============================================================================================================
        //==============================================================================================================
        //LOGGER.info("Started listening to input stream with params: " + sensitivity + "%, " + threshold + "dB");

    }

    @Override
    public void handleOnset(double time, double salience) {
        LOGGER.info("Percussion at:" + time);

    }

    @SuppressWarnings("Duplicates")
    private AudioFormat getAudioFormat() {
        float sampleRate = 44100F;
        //float sampleRate = 16000F;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;

        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);

    }

    //==================================================================================================================
    //Getter & setters
    //==================================================================================================================
    public Thread getThread() {
        return thread;

    }

    public double getThreshold() {
        return threshold;

    }

    public double getSensitivity() {
        return sensitivity;

    }

    //==================================================================================================================
    //Entity constructor
    //==================================================================================================================
    /**
     * Default constructor
     */
    PercussionDetector() {
        //initialize Sensitivity (in percentage)
        this.sensitivity = 20.0;
        //initialize Threshold (in dB)
        this.threshold = 8.0;

    }

    //==================================================================================================================
    //Entity variables
    //==================================================================================================================
    private Thread thread;
    private double threshold;
    private double sensitivity;
    private AudioDispatcher dispatcher;
    private static final Logger LOGGER = Logger.getLogger(PercussionDetector.class.getName());

}