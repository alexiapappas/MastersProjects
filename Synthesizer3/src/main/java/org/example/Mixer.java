package org.example;

import java.util.ArrayList;

public class Mixer implements AudioComponent {
    private static AudioClip[] input = new AudioClip[10];

    @Override
    public AudioClip getClip() {
        AudioClip result = new AudioClip();
        for (int i = 0; i < input.length; i++) {
            AudioClip temp = input[i];
            if (temp == null)
                continue;
            result.combineClip(temp);
        }
        return result;
    }

        @Override
        public boolean hasInput () {
            return input != null;
        }

        @Override
        public void connectInput (AudioClip component, int index){
            input[index] = component;
        }
}
