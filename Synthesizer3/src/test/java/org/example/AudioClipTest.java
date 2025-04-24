package org.example;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.LineUnavailableException;

import static org.junit.jupiter.api.Assertions.*;

class AudioClipTest {
    public void runAllTests(){
        this.testGetSample();
        this.testGetSample2();
        this.testSetSample();
        this.testSetSample2();
        this.testSetSample3();
        System.out.println("All the tests passed!");
    }

    @Test
    void testGetSample() {
        AudioClip clipTest1 = new AudioClip();
        Assertions.assertEquals(0, clipTest1.getSample(0));
    }

    @Test
    void testGetSample2(){
        AudioClip clipTest2 = new AudioClip();
        clipTest2.setSample(35, -55);
        Assertions.assertEquals(-55, clipTest2.getSample(35));
    }

    @Test
    void testSetSample(){
        AudioClip clipTest = new AudioClip();
        for (short shortValue = Short.MIN_VALUE; shortValue < Short.MAX_VALUE; shortValue++) {
            clipTest.setSample(0, shortValue);
            Assertions.assertEquals(shortValue, clipTest.getSample(0));
        }
    }

    @Test
    void testSetSample2(){
        AudioClip clipTest2 = new AudioClip();
        clipTest2.setSample(4987, 1006);
        Assertions.assertEquals(1006, clipTest2.getSample(4987));
    }

    @Test
    void testSetSample3(){
        AudioClip clipTest3 = new AudioClip();
        for (int i = 0; i < AudioClip.sampleRate; i++){
            clipTest3.setSample(i, Short.MAX_VALUE);
            Assertions.assertEquals(Short.MAX_VALUE, clipTest3.getSample(i));
        }
    }

//    @Test
//    void testGetData(){
//        AudioClip clipTest = new AudioClip();
//        Assertions.assertEquals(0, clipTest.getData());
//    }

    @Test
    void testplayClip() throws LineUnavailableException {
        SineWave wave = new SineWave();
        wave.getClip().playClip();
    }

}