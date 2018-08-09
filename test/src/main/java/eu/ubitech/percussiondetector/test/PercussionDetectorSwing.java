package eu.ubitech.percussiondetector.test;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by John Tsantilis on 30/7/2018.
 *
 * @author John Tsantilis <i.tsantilis [at] ubitech [dot] com>
 */

public class PercussionDetectorSwing extends JFrame implements OnsetHandler {
    @SuppressWarnings("Duplicates")
    private JSlider initialzeThresholdSlider() {
        JSlider thresholdSlider = new JSlider(0,20);
        thresholdSlider.setValue((int)threshold);
        thresholdSlider.setPaintLabels(true);
        thresholdSlider.setPaintTicks(true);
        thresholdSlider.setMajorTickSpacing(5);
        thresholdSlider.setMinorTickSpacing(1);
        thresholdSlider.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    threshold = source.getValue();
                    try {
                        setNewMixer(currentMixer);

                    } catch (LineUnavailableException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();

                    } catch (UnsupportedAudioFileException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();

                    }

                }

            }

        });

        return thresholdSlider;

    }

    @SuppressWarnings("Duplicates")
    private JSlider initializeSensitivitySlider(){
        JSlider sensitivitySlider = new JSlider(0,100);
        sensitivitySlider.setValue((int)sensitivity);
        sensitivitySlider.setPaintLabels(true);
        sensitivitySlider.setPaintTicks(true);
        sensitivitySlider.setMajorTickSpacing(20);
        sensitivitySlider.setMinorTickSpacing(10);
        sensitivitySlider.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    sensitivity = source.getValue();
                    try {
                        setNewMixer(currentMixer);

                    } catch (LineUnavailableException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();

                    } catch (UnsupportedAudioFileException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();

                    }

                }

            }

        });

        return sensitivitySlider;

    }

    private void setNewMixer(Mixer mixer) throws LineUnavailableException, UnsupportedAudioFileException {
        if(dispatcher!= null) {
            dispatcher.stop();

        }

        currentMixer = mixer;

        float sampleRate = 44100;
        int bufferSize = 512;
        int overlap = 0;

        textArea.append("Started listening with " + Shared.toLocalString(mixer.getMixerInfo().getName()) + "\n\tparams: " + sensitivity + "%, " + threshold + "dB\n");

        final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true,
                true);
        final DataLine.Info dataLineInfo = new DataLine.Info(
                TargetDataLine.class, format);
        TargetDataLine line;
        line = (TargetDataLine) mixer.getLine(dataLineInfo);
        final int numberOfSamples = bufferSize;
        line.open(format, numberOfSamples);
        line.start();
        final AudioInputStream stream = new AudioInputStream(line);

        JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
        // create a new dispatcher
        dispatcher = new AudioDispatcher(audioStream, bufferSize,
                overlap);

        // add a processor, handle percussion event.
        dispatcher.addAudioProcessor(new PercussionOnsetDetector(sampleRate,
                bufferSize, this,sensitivity,threshold));

        // run the dispatcher (on a new thread).
        new Thread(dispatcher,"Audio dispatching").start();

    }

    @Override
    public void handleOnset(double time, double salience) {
        textArea.append("Percussion at:" + time + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());

    }

    public static void main(String... strings) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    //ignore failure to set default look en feel;
                }
                JFrame frame = new PercussionDetectorSwing();
                frame.pack();
                frame.setSize(640, 480);
                frame.setVisible(true);

            }

        });

    }

    //==================================================================================================================
    //Class Constructor
    //==================================================================================================================
    public PercussionDetectorSwing() {
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Percussion Detector");

        JPanel inputPanel = new InputPanel();
        inputPanel.addPropertyChangeListener("mixer",
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent arg0) {
                        try {
                            setNewMixer((Mixer) arg0.getNewValue());
                        } catch (LineUnavailableException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (UnsupportedAudioFileException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });


        JSlider sensitivitySlider = initializeSensitivitySlider();
        JSlider thresholdSlider = initialzeThresholdSlider();
        JPanel params = new JPanel(new GridLayout(0,1));
        params.setBorder(new TitledBorder("2. Set the algorithm parameters"));

        JLabel label = new JLabel("Threshold");
        label.setToolTipText("Energy rise within a frequency bin necessary to count toward broadband total (dB).");
        params.add(label);
        params.add(thresholdSlider);
        label = new JLabel("Sensitivity");
        label.setToolTipText("Sensitivity of peak detector applied to broadband detection function (%).");
        params.add(label);
        params.add(sensitivitySlider);

        JPanel paramsAndInputPanel = new JPanel(new GridLayout(1,0));
        paramsAndInputPanel.add(inputPanel);
        paramsAndInputPanel.add(params);


        textArea = new JTextArea();
        textArea.setEditable(false);

        this.add(paramsAndInputPanel,BorderLayout.NORTH);
        this.add(new JScrollPane(textArea),BorderLayout.CENTER);

    }

    //==================================================================================================================
    //Class variables
    //==================================================================================================================
    private double threshold;
    private double sensitivity;
    private Mixer currentMixer;
    private  AudioDispatcher dispatcher;
    private final JTextArea textArea;
    private static final long serialVersionUID = 3501426880288136245L;

}
