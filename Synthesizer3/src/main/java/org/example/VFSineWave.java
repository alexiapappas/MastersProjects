package org.example;
import static java.lang.Math.PI;
import static java.lang.Math.sin;

public class VFSineWave implements AudioComponent {
    AudioClip input;
    private short maxValue = Short.MAX_VALUE / 2;

    @Override
    public AudioClip getClip() {
        AudioClip clip = new AudioClip();
        double phase = 0;
        for (int i = 0; i < AudioClip.sampleRate; i++){
            phase += 2 * PI * clip.getSample(i) / AudioClip.sampleRate;
            clip.setSample(i, (int)(maxValue * sin(phase)));
        }
        if (hasInput()){
            clip.combineClip(input);
        }
        return clip;
    }

    @Override
    public boolean hasInput() {
        return input != null;
    }

    @Override
    public void connectInput(AudioClip component, int index) {
        this.input = component;
    }
}
