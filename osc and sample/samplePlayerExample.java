package net.happybrackets.tutorial.session3;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;

public class samplePlayerExample extends Application {
    public static void Main(){
        launch( );
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        AudioContext ac = new AudioContext();
        ac.start();

        //set up sample
        Sample s = SampleManager.sample("data/audio/Nylon_Guitar/Clean_C_harm.wav");

        //set up sample group
        //SampleManager.group("guitar","data/audio/Nylon_Guitar");

        //load sample into sample player object
        SamplePlayer sp = new SamplePlayer(ac, s);
        sp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
        sp.setKillOnEnd(false);
        ac.out.addInput(sp);

        //waveform
        WaveformVisualiser.open(ac);
    }
}
