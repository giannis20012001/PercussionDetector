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
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

/**
 * Created by John Tsantilis on 31/7/2018.
 *
 * @author John Tsantilis <i.tsantilis [at] ubitech [dot] com>
 */

public class PercussionDetector implements OnsetHandler {
    @SuppressWarnings("Duplicates")
    private void setNewMixer(Mixer mixer) throws LineUnavailableException {
        if(dispatcher!= null) {
            dispatcher.stop();

        }

        float sampleRate = 44100;
        int bufferSize = 512;
        int overlap = 0;
        //final int numberOfSamples = bufferSize;

        //==============================================================================================================
        //==============================================================================================================
        final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, true);
        final DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine line;
        line = (TargetDataLine) mixer.getLine(dataLineInfo);
        line.open(format, bufferSize); //bufferSize --> numberOfSamples
        line.start();
        final AudioInputStream stream = new AudioInputStream(line);
        JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
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
        System.out.println("Started listening with " + Shared.toLocalString(mixer.getMixerInfo().getName())
                + " params: " + sensitivity + "%, " + threshold + "dB");

    }

    @Override
    public void handleOnset(double time, double salience) {
        System.out.println("Percussion at:" + time + "\n");

    }

    public static void main(String[] args) {
        new PercussionDetector();

    }

    //==================================================================================================================
    //Constructors
    //==================================================================================================================
    /**
     * Default constructor
     */
    public PercussionDetector() {
        //initialize Sensitivity (in percentage)
        this.sensitivity = 20.0;
        //initialze Threshold (in dB)
        this.threshold = 8.0;
        for(Mixer.Info info : Shared.getMixerInfo(false, true)){
            System.out.println(info);
            if (info.getName().contains("default")) {
                try {
                    setNewMixer(AudioSystem.getMixer(info));

                } catch (LineUnavailableException e) {
                    e.printStackTrace();

                }

                break;

            }

        }

    }

    //==================================================================================================================
    //Entity variables
    //==================================================================================================================
    private double threshold;
    private double sensitivity;
    private  AudioDispatcher dispatcher;
    private static final long serialVersionUID = 1L;

}
