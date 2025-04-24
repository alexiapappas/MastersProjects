package org.example;

public interface AudioComponent {
    AudioClip getClip();

    boolean hasInput();

    void connectInput(AudioClip component, int index);
}

