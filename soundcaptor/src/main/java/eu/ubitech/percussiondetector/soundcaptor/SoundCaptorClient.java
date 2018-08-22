package eu.ubitech.percussiondetector.soundcaptor;

import eu.ubitech.percussiondetector.percussiondetectorserver.Shared;

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
    private void captureAudio(Mixer mixer) throws LineUnavailableException {
        final AudioFormat audioFormat = getAudioFormat();
        final DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        TargetDataLine targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
        //targetDataLine.open(audioFormat, 10000); //bufferSize --> numberOfSamples
        targetDataLine.open(audioFormat);
        targetDataLine.start();
        //create a separate thread to run gRCP transmission process
        Thread soundTransmitter = new Thread(new GrpcTransmitter(targetDataLine));
        soundTransmitter.start();
        LOGGER.info("Started listening to input mic with " + Shared.toLocalString(mixer.getMixerInfo().getName()));

    }

    @SuppressWarnings("Duplicates")
    private AudioFormat getAudioFormat() {
        //float sampleRate = 44100F;
        float sampleRate = 16000F;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;

        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);

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