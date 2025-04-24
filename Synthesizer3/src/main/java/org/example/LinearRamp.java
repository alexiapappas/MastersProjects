package org.example;

public class LinearRamp implements AudioComponent{
    AudioClip input;
    double start_ = 50;
    double stop_ = 2000;

    public LinearRamp(double start, double stop){
        this.start_ = start;
        this.stop_ = stop;
    }

    @Override
    public AudioClip getClip() {
      AudioClip clip = new AudioClip();
      for (int i = 0; i < AudioClip.sampleRate; i++){
          clip.setSample(i, (int) ((start_ * (AudioClip.sampleRate - i) + stop_ * i) / AudioClip.sampleRate));
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
        input = component;
    }
}
