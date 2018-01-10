package net.happybrackets.tutorial.session3;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.RecordToFile;
import net.beadsproject.beads.ugens.WavePlayer;

import java.io.File;

import static javafx.application.Platform.exit;

public class audioRecordExample extends Application {
    public static void Main(){
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        AudioContext ac = new AudioContext();
        ac.start();

        WavePlayer wp = new WavePlayer(ac, 500, Buffer.SINE);
        ac.out.addInput(wp);
        ac.out.setGain(0.1f);

        //create recording object
        RecordToFile record = new RecordToFile(ac, 2, new File("outfile.wav"));
        //update cycle from ac.out
        ac.out.addDependent(record);
        //route ac.out to record
        record.addInput(ac.out);

        //specify DelayTrigger to kill recording after 5 seconds
        DelayTrigger dt = new DelayTrigger(ac, 5000, new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                record.kill();
                System.out.println("recorded has ended");
                ac.stop();
                exit();
            }
        });
        //add DelayTrigger to ac.out
        ac.out.addDependent(dt);
    }
}
