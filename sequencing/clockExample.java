package net.happybrackets.tutorial.session3;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;

public class clockExample extends Application {
    public static void Main(){
        launch();
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        AudioContext ac = new AudioContext();
        ac.start();
        final int BPM = 120;

        //set up clock with 120 bpm
        Clock clock = new Clock(ac, (60000 / BPM) );
        System.out.println(clock.getTempo());

        clock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if (clock.isBeat()){
                    System.out.print("\r\n" + clock.getBeatCount() + " BEAT");
                } else {
                    System.out.print(" tick ");
                }
            }
        });

        ac.out.addDependent(clock);

    }
}
