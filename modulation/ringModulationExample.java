package net.happybrackets.tutorial.session3;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Mult;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;

public class ringModulationExample extends Application {
    public static void Main(){
        launch( );
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        AudioContext ac = new AudioContext();
        ac.start();

        //set up sample
        Sample s = SampleManager.sample("path/to/sample");

        //load sample into sample player object
        SamplePlayer carrier = new SamplePlayer(ac, s);
        carrier.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
        carrier.setKillOnEnd(false);
        ac.out.addInput(carrier);

        //set up frequency to be modulated
        WavePlayer modulator = new WavePlayer(ac, 1, Buffer.SINE);
        Gain g = new Gain(ac, 1, 0.1f);
        //g.addInput(modulator);

        //multiply carrier by modulator
        Mult m = new Mult(ac, carrier, modulator);

        //set up output
        ac.out.setGain(0.1f);
        ac.out.addInput(m);

        //waveform
        WaveformVisualiser.open(ac);
    }
}
