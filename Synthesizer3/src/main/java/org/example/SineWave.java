package org.example;
import static java.lang.Math.sin;
import static java.lang.Math.PI;

public class SineWave implements AudioComponent{
    private double frequency_;
    private int maxValue_;

    SineWave(){
        frequency_ = 440;
        maxValue_ = Short.MAX_VALUE / 2;
    }

    public SineWave(double frequency){
        this.frequency_ = frequency;
        maxValue_ = Short.MAX_VALUE / 2;
    };

    @Override
    public AudioClip getClip(){
        AudioClip clip = new AudioClip();
        for (int i = 0; i < AudioClip.sampleRate; i++){
            double sample = maxValue_ * sin(2 * PI * frequency_ * i / AudioClip.sampleRate);
            clip.setSample(i, (short)sample);
        }
        return clip;
    }

    @Override
    public boolean hasInput(){
        return false;
    }

    @Override
    public void connectInput(AudioClip component, int index){
        return;
    }
}
