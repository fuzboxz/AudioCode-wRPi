package net.happybrackets.tutorial.session3;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.beadsproject.beads.ugens.ZMap;
import net.happybrackets.controller.gui.WaveformVisualiser;

import java.applet.AudioClip;

public class lfoExample extends Application {
    public static void Main(){
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        AudioContext ac = new AudioContext();
        ac.start();



        //lfos
        WavePlayer lfoAmp = new WavePlayer(ac, 1, Buffer.SINE);
        WavePlayer lfoFrq = new WavePlayer(ac, 2.5f, Buffer.SINE);


        // mapper audio object, one channel
        ZMap mappedLFOAmp = new ZMap(ac,1);

        //OPTION 1: map range to values
        mappedLFOAmp.setRanges(-1,1,0,0.1f);
        mappedLFOAmp.addInput(lfoAmp);
        Gain gain = new Gain(ac, 1, mappedLFOAmp);


        //OPTION 2: custom mapper Function
        Function mappedToLfOFreq = new Function(lfoFrq) {
            @Override
            public float calculate() {
                return (float) (((x[0]+1)*0.5)*256);
            }
        };

        //OPTION 3: set up as UGen
        /*
        UGen mappedUgen = new UGen(ac, 1,1) {
            @Override
            public void calculateBuffer() {

            }
        }; */

        //oscillator playing middle-c
        WavePlayer wp = new WavePlayer(ac, mappedToLfOFreq, Buffer.SAW);

        gain.addInput(wp);
        ac.out.addInput(gain);
        WaveformVisualiser.open(ac);
    }

}
