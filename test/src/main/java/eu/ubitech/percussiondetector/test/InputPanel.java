package eu.ubitech.percussiondetector.test;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by John Tsantilis on 30/7/2018.
 *
 * @author John Tsantilis <i.tsantilis [at] ubitech [dot] com>
 */

public class InputPanel extends JPanel {
    public InputPanel(){
        super(new BorderLayout());
        this.setBorder(new TitledBorder("1. Choose a microphone input"));
        JPanel buttonPanel = new JPanel(new GridLayout(0,1));
        ButtonGroup group = new ButtonGroup();
        for(Mixer.Info info : Shared.getMixerInfo(false, true)){
            JRadioButton button = new JRadioButton();
            button.setText(Shared.toLocalString(info));
            buttonPanel.add(button);
            group.add(button);
            button.setActionCommand(info.toString());
            button.addActionListener(setInput);
        }

        this.add(new JScrollPane(buttonPanel,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
        this.setMaximumSize(new Dimension(300,150));
        this.setPreferredSize(new Dimension(300,150));

    }

    private ActionListener setInput = new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent arg0) {
            for(Mixer.Info info : Shared.getMixerInfo(false, true)){
                if(arg0.getActionCommand().equals(info.toString())){
                    Mixer newValue = AudioSystem.getMixer(info);
                    InputPanel.this.firePropertyChange("mixer", mixer, newValue);
                    InputPanel.this.mixer = newValue;

                    break;

                }

            }

        }

    };

    //==================================================================================================================
    //Class variables
    //==================================================================================================================
    Mixer mixer = null;
    private static final long serialVersionUID = 1L;

}
