package eu.ubitech.percussiondetector.test.udp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class AudioUDPClient {
    private void initiateAudio() {
        try {
            DatagramSocket socket = new DatagramSocket(9786);
            byte[] audioBuffer = new byte[10000];
            while (true) {
                DatagramPacket packet = new DatagramPacket(audioBuffer, audioBuffer.length);
                socket.receive(packet);
                //System.out.println("RECEIVED: " + packet.getAddress().getHostAddress() + " " + packet.getPort());
                try {
                    byte audioData[] = packet.getData();
                    InputStream byteInputStream = new ByteArrayInputStream(audioData);
                    AudioFormat audioFormat = getAudioFormat();
                    audioInputStream = new AudioInputStream(
                            byteInputStream,
                            audioFormat,
                            audioData.length / audioFormat.getFrameSize());
                    DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
                    sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                    sourceDataLine.open(audioFormat);
                    sourceDataLine.start();
                    playAudio();

                } catch (Exception e) {
                    System.out.println(e);
                    System.exit(0);

                }

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    @SuppressWarnings("Duplicates")
    private void playAudio() {
        byte[] buffer = new byte[10000];
        try {
            int count;
            while ((count = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
                if (count > 0) {
                    sourceDataLine.write(buffer, 0, count);

                }

            }

        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);

        }

    }

    @SuppressWarnings("Duplicates")
    private AudioFormat getAudioFormat() {
        float sampleRate = 44100F;
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
        new AudioUDPClient();

    }

    //==================================================================================================================
    //Class Constructor
    //==================================================================================================================
    private AudioUDPClient() {
        System.out.println("Audio UDP Client Started");
        initiateAudio();
        System.out.println("Audio UDP Client Terminated");

    }

    //==================================================================================================================
    //Class variables
    //==================================================================================================================
    private AudioInputStream audioInputStream;
    private SourceDataLine sourceDataLine;

}
