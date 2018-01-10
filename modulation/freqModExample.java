package net.happybrackets.tutorial.session3;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;

public class freqModExample extends Application {
    public static void Main(){
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        AudioContext ac = new AudioContext();
        ac.start();

        Envelope baseFreq = new Envelope(ac,500);
        baseFreq.addSegment(1000,2000);
        float modAmount = 500;
        WavePlayer modulator = new WavePlayer(ac, new Function(baseFreq) {
            @Override
            public float calculate() {
                return x[0] * 1.1f;
            }
        }, Buffer.SINE);

        Function modSignal = new Function(modulator, baseFreq) {
            @Override
            public float calculate() {
                    return x[0] * modAmount + x[1];
            }
        };

        WavePlayer carrier = new WavePlayer(ac, modSignal, Buffer.SINE);
        ac.out.addInput(carrier);
        ac.out.setGain(0.1f);
        WaveformVisualiser.open(ac);
    }
}
