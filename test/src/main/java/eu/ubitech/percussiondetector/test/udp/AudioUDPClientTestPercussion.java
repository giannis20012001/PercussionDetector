package eu.ubitech.percussiondetector.test.udp;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by John Tsantilis on 28/8/2018.
 *
 * @author John Tsantilis <i.tsantilis [at] ubitech [dot] com>
 */
@SuppressWarnings("Duplicates")
public class AudioUDPClientTestPercussion implements OnsetHandler {
    private void initiateAudio() {
        try {
            DatagramSocket socket = new DatagramSocket(9786);
            byte[] audioBuffer = new byte[10000];
            while (true) {
                DatagramPacket packet = new DatagramPacket(audioBuffer, audioBuffer.length);
                socket.receive(packet);
                //System.out.println("RECEIVED: " + packet.getAddress().getHostAddress() + " " + packet.getPort());
                try {
                    audioData = packet.getData();
                    if (setDispatcherFlag) {
                        setDispatcher();
                        setDispatcherFlag = false;

                    }

                } catch (Exception e) {
                    System.out.println(e);
                    System.exit(0);

                }

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

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
        InputStream byteInputStream = new ByteArrayInputStream(audioData);
        final AudioFormat audioFormat = getAudioFormat();
        final AudioInputStream audioInputStream = new AudioInputStream(
                byteInputStream,
                audioFormat,
                audioData.length / audioFormat.getFrameSize());
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
        thread = new Thread(dispatcher,"Audio dispatching");
        thread.start();

        //==============================================================================================================
        //==============================================================================================================
        System.out.println("Started listening to input stream with params: " + sensitivity + "%, " + threshold + "dB");

    }

    @Override
    public void handleOnset(double time, double salience) {
        System.out.println("Percussion at:" + time);

    }

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
    //Main method
    //==================================================================================================================
    public static void main(String[] args) {
        new AudioUDPClientTestPercussion();

    }

    //==================================================================================================================
    //Class Constructor
    //==================================================================================================================
    private AudioUDPClientTestPercussion() {
        //initialize Sensitivity (in percentage)
        this.sensitivity = 20.0;
        //initialize Threshold (in dB)
        this.threshold = 8.0;

        System.out.println("Audio UDP Client Started");
        initiateAudio();
        System.out.println("Audio UDP Client Terminated");

    }

    //==================================================================================================================
    //Class variables
    //==================================================================================================================
    private double threshold;
    private double sensitivity;
    //==================================================================================================================
    private Thread thread;
    private byte audioData[];
    private boolean setDispatcherFlag = true;
    private AudioDispatcher dispatcher;
    private SourceDataLine sourceDataLine;
    private AudioInputStream audioInputStream;

}
