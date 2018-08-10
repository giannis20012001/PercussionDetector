package eu.ubitech.percussiondetectorserver;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Created by John Tsantilis on 2/8/2018.
 *
 * @author John Tsantilis <i.tsantilis [at] ubitech [dot] com>
 */

public class PercussionDetector implements OnsetHandler {
    public void setNewMixer() {
        if(dispatcher!= null) {
            dispatcher.stop();

        }

        float sampleRate = 44100;
        int bufferSize = 4096;
        int overlap = 0;

        //==============================================================================================================
        //==============================================================================================================
        InputStream byteInputStream = new ByteArrayInputStream(receivedAudioData);
        final AudioFormat audioFormat = new AudioFormat(sampleRate, 16, 1, true, true);

        LOGGER.info(String.valueOf(receivedAudioData.length));
        LOGGER.info(String.valueOf(audioFormat.getFrameSize()));

        AudioInputStream inputStream = new AudioInputStream(
                byteInputStream,
                audioFormat,
                4096); //receivedAudioData.length / audioFormat.getFrameSize()

        JVMAudioInputStream audioStream = new JVMAudioInputStream(inputStream);
        // create a new dispatcher
        dispatcher = new AudioDispatcher(audioStream, bufferSize, overlap);
        // add a processor, handle percussion event.
        dispatcher.addAudioProcessor(new PercussionOnsetDetector(
                sampleRate,
                bufferSize,
                this,
                sensitivity,
                threshold));
        // run the dispatcher (on a new thread).
        new Thread(dispatcher,"Audio dispatching").start();

        //==============================================================================================================
        //==============================================================================================================
        LOGGER.info("Started listening to input stream with params: " + sensitivity + "%, " + threshold + "dB");

    }

    @Override
    public void handleOnset(double time, double salience) {
        System.out.println("Percussion at:" + time + "\n");

    }

    //==================================================================================================================
    //Entity constructor
    //==================================================================================================================
    /**
     * Default constructor
     *
     */
    PercussionDetector(byte receivedAudioData[]) {
        //initialize input stream buffer (byte array)
        this.receivedAudioData = receivedAudioData;
        //initialize Sensitivity (in percentage)
        this.sensitivity = 20.0;
        //initialize Threshold (in dB)
        this.threshold = 8.0;

    }

    //==================================================================================================================
    //Entity variables
    //==================================================================================================================
    private double threshold;
    private double sensitivity;
    private byte receivedAudioData[];
    private AudioDispatcher dispatcher;
    private static final Logger LOGGER = Logger.getLogger(PercussionDetector.class.getName());

}