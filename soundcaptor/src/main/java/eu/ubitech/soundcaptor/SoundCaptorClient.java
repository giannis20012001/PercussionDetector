package eu.ubitech.soundcaptor;

import eu.ubitech.percussiondetectorserver.Shared;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.util.logging.Logger;

/**
 * Created by John Tsantilis on 9/8/2018.
 *
 * @author John Tsantilis <i.tsantilis [at] ubitech [dot] com>
 */
public class SoundCaptorClient {
    public void captureAudio(Mixer mixer) throws LineUnavailableException {
        float sampleRate = 44100;
        int bufferSize = 4096;

        final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, true);
        final DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
        targetDataLine.open(format, bufferSize); //bufferSize --> numberOfSamples
        targetDataLine.start();
        //create a separate thread to run gRCP transmission process
        Thread soundTransmitter = new Thread(new GrpcTransmitter(targetDataLine));
        soundTransmitter.start();
        LOGGER.info("Started listening to input mic with " + Shared.toLocalString(mixer.getMixerInfo().getName()));

    }

    //==================================================================================================================
    //Main method
    //==================================================================================================================
    public static void main(String[] args) {
        new SoundCaptorClient();

    }

    //==================================================================================================================
    //Entity constructor
    //==================================================================================================================
    /**
     * Default constructor
     *
     */
    private SoundCaptorClient() {
        for(Mixer.Info info : Shared.getMixerInfo(false, true)){
            LOGGER.info(info.toString());
            if (info.getName().contains("default")) {
                try {
                    captureAudio(AudioSystem.getMixer(info));

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
    private Mixer mixer;
    private static final Logger LOGGER = Logger.getLogger(GrpcTransmitter.class.getName());

}