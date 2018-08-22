package eu.ubitech.percussiondetector.test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by John Tsantilis on 9/8/2018.
 *
 * @author John Tsantilis <i.tsantilis [at] ubitech [dot] com>
 */
public class VUClient extends JFrame {

    boolean stopaudioCapture = false;
    private ByteArrayOutputStream byteOutputStream;
    private AudioFormat adFormat;
    private TargetDataLine targetDataLine;
    private AudioInputStream InputStream;
    private SourceDataLine sourceLine;
    private Graphics g;

    public static void main(String args[]) {
        new VUClient();
    }

    public VUClient() {
        final JButton capture = new JButton("Capture");
        final JButton stop = new JButton("Stop");
        final JButton play = new JButton("Playback");

        capture.setEnabled(true);
        stop.setEnabled(false);
        play.setEnabled(false);

        capture.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                capture.setEnabled(false);
                stop.setEnabled(true);
                play.setEnabled(false);
                captureAudio();
            }
        });
        getContentPane().add(capture);

        stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                capture.setEnabled(true);
                stop.setEnabled(false);
                play.setEnabled(true);
                stopaudioCapture = true;
                targetDataLine.close();
            }
        });
        getContentPane().add(stop);

        play.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playAudio();
            }
        });
        getContentPane().add(play);

        getContentPane().setLayout(new FlowLayout());
        setTitle("Capture/Playback Demo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 100);
        getContentPane().setBackground(Color.white);
        setVisible(true);

        g = (Graphics) this.getGraphics();
    }

    private void captureAudio() {
        try {
            adFormat = getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, adFormat);
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            targetDataLine.open(adFormat);
            targetDataLine.start();

            Thread captureThread = new Thread(new CaptureThread());
            captureThread.start();
        } catch (Exception e) {
            StackTraceElement stackEle[] = e.getStackTrace();
            for (StackTraceElement val : stackEle) {
                System.out.println(val);
            }
            System.exit(0);
        }
    }

    private void playAudio() {
        try {
            byte audioData[] = byteOutputStream.toByteArray();
            java.io.InputStream byteInputStream = new ByteArrayInputStream(audioData);
            AudioFormat adFormat = getAudioFormat();
            InputStream = new AudioInputStream(byteInputStream, adFormat, audioData.length / adFormat.getFrameSize());
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, adFormat);
            sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceLine.open(adFormat);
            sourceLine.start();
            Thread playThread = new Thread(new PlayThread());
            playThread.start();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    @SuppressWarnings("Duplicates")
    private AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleInbits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleInbits, channels, signed, bigEndian);
    }

    class CaptureThread extends Thread {

        byte tempBuffer[] = new byte[10000];

        public void run() {

            byteOutputStream = new ByteArrayOutputStream();
            stopaudioCapture = false;
            try {
                DatagramSocket clientSocket = new DatagramSocket(8786);
                InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
                while (!stopaudioCapture) {
                    int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                    if (cnt > 0) {
                        DatagramPacket sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length, IPAddress, 9786);
                        clientSocket.send(sendPacket);
                        byteOutputStream.write(tempBuffer, 0, cnt);
                    }
                }
                byteOutputStream.close();
            } catch (Exception e) {
                System.out.println("CaptureThread::run()" + e);
                System.exit(0);
            }
        }
    }

    class PlayThread extends Thread {

        byte tempBuffer[] = new byte[10000];

        public void run() {
            try {
                int cnt;
                while ((cnt = InputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        sourceLine.write(tempBuffer, 0, cnt);
                    }
                }
                //                sourceLine.drain();
                //             sourceLine.close();
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }
        }
    }
}