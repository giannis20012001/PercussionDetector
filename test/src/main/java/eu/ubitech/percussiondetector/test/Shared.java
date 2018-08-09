package eu.ubitech.percussiondetector.test;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer.Info;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Vector;

/**
 * Created by John Tsantilis on 30/7/2018.
 *
 * @author John Tsantilis <i.tsantilis [at] ubitech [dot] com>
 */
public class Shared {
    @SuppressWarnings("Duplicates")
    public static Vector<Info> getMixerInfo(final boolean supportsPlayback, final boolean supportsRecording) {
        final Vector<Info> infos = new Vector<Info>();
        final Info[] mixers = AudioSystem.getMixerInfo();
        for (final Info mixerinfo : mixers) {
            if (supportsRecording && AudioSystem.getMixer(mixerinfo).getTargetLineInfo().length != 0) {
                // Mixer capable of recording audio if target LineWavelet length != 0
                infos.add(mixerinfo);

            } else if (supportsPlayback && AudioSystem.getMixer(mixerinfo).getSourceLineInfo().length != 0) {
                // Mixer capable of audio play back if source LineWavelet length != 0
                infos.add(mixerinfo);
            }

        }

        return infos;

    }

    public static String toLocalString(Object info) {
        if(!isWindows())
            return info.toString();
        String defaultEncoding = Charset.defaultCharset().toString();
        try {
            return new String(info.toString().getBytes("windows-1252"), defaultEncoding);

        } catch(UnsupportedEncodingException ex) {
            return info.toString();

        }

    }

    public static String getOsName() {
        if(OS == null)
            OS = System.getProperty("os.name");

        return OS;

    }

    public static boolean isWindows() {
        return getOsName().startsWith("Windows");

    }

    //==================================================================================================================
    //Class variables
    //==================================================================================================================
    private static String OS = null;

}