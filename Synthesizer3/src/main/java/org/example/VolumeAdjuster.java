package org.example;

public class VolumeAdjuster implements AudioComponent {
    private AudioClip input_;
    public double scale_;

    VolumeAdjuster(double scale){
        this.scale_ = scale;
    }

    @Override
    public AudioClip getClip() {
        // if (component = null)
        //        throw error
        AudioClip waveClip = input_;
        AudioClip newClip = new AudioClip();
        for (int i = 0; i < AudioClip.sampleRate; i++){
            newClip.setSample(i, (int) (waveClip.getSample(i) * scale_));
        }
        return newClip;
    }

    @Override
    public boolean hasInput() {
        return false;
    }

    @Override
    public void connectInput(AudioClip component, int index) {
        input_ = component;
    }
}


