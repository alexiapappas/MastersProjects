package org.example;
import javax.sound.sampled.LineUnavailableException;

public class Main {
    public static void main(String[] args) throws LineUnavailableException {
        SineWave wave = new SineWave(440);
        VolumeAdjuster volAd = new VolumeAdjuster(0.2);
        volAd.connectInput(wave.getClip(), 0);

        SineWave wave2 = new SineWave(380);

        LinearRamp ramp = new LinearRamp(50, 2000);

        VFSineWave vfs = new VFSineWave();

        vfs.connectInput(ramp.getClip(), 0);
        AudioClip wave6 = vfs.getClip();
        wave6.playClip();


        Mixer mixer = new Mixer();
        mixer.connectInput(wave.getClip(), 0);
        mixer.connectInput(wave2.getClip(), 1);
        mixer.getClip().playClip();
    }
}
