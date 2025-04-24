package org.example;

import javax.sound.sampled.*;
import java.util.Arrays;

public class AudioClip {
    static final float duration = 2.0F;
    public static final int sampleRate = 44100;
    byte[] clipsArray = new byte[sampleRate * (int) duration];

    //bytes to integer -> combining
    public int getSample(int index) {
        int leastSigByte = clipsArray[2 * index] & 0xFF;
        int mostSigByte = clipsArray[(2 * index) + 1];
        return (mostSigByte << 8) | leastSigByte;
    }

    //integer to bytes -> breaking
    public void setSample(int index, int value) {
        short minClamp = Short.MIN_VALUE;
        short maxClamp = Short.MAX_VALUE;
        if (value < minClamp)
            value = minClamp;
        if (value > maxClamp)
            value = maxClamp;

        byte leastSigByte = (byte) value;
        byte mostSigByte = (byte) (value >> 8);
        clipsArray[2 * index] = leastSigByte;
        clipsArray[(2 * index) + 1] = mostSigByte;
    }

    public byte[] getData() {
        return Arrays.copyOf(clipsArray, clipsArray.length);
    }

    public void playClip() throws LineUnavailableException {
        Clip c = AudioSystem.getClip();
        AudioFormat format16 = new AudioFormat(44100, 16, 1, true, false);
        c.open(format16, clipsArray, 0, clipsArray.length);
        c.start();
        c.loop(2);

        while (c.getFramePosition() < sampleRate || c.isActive() || c.isRunning()) {
        }

        c.close();
    }

    public void combineClip(AudioClip clip) {
        for (int j = 0; j < AudioClip.sampleRate; j++) {
            this.setSample(j, this.getSample(j) + clip.getSample(j));
        }
    }
}


