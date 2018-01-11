/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.assignment_tasks.session3;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

import java.util.Random;

/**
 * Study the code below.
 *
 * Consider the following:
 * Why doesn't the SamplePlayer need to be destroyed, as was the case with Noise and WavePlayer in previous examples?
 * Why doesn't the sample data get read every time the sound is played?
 *
 * Tasks:
 *
 * 1) Loop the SamplePlayer so that you get an alternating (backwards-forwards) loop over the last 25% of the file.
 *
 * 2) Now that you are looping the SamplePlayer, apply an ADSR envelope, as you did in the code tasks in the previous
 * session, including killing the sound (remember because you are passing the sound through a new UGen object, that
 * object will not be killed automatically).
 *
 * 3) Use the 'getPitch().setValue(x)' method on the SamplePlayer to pitch each note to follow a random pentatonic
 * pattern. Note that the desired frequency is not the same as the playback rate. A playback rate of 1 will play the
 * sound at its original frequency. The sample has a pitch class of "A natural".
 */
public class CodeTask3_1 extends Application implements BeadsChecker.BeadsCheckable {

    int bufPos = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //the AudioContext
        AudioContext ac = new AudioContext();
        ac.start();
        //a StringBuffer used to record anything you want to print out
        StringBuffer buf = new StringBuffer();
        //do your work here, using the function below
        task(ac, buf);
        //poll StringBuffer for new console output
        new Thread(() -> {
            while(true) {
                String newText = buf.substring(bufPos);
                bufPos += newText.length();
                System.out.print(newText);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }).start();
        //finally, this creates a window to visualise the waveform
        WaveformVisualiser.open(ac);
    }

    @Override
    public void task(AudioContext ac, StringBuffer stringBuffer, Object... objects) {
        //********** do your work here ONLY **********
        //clock
        Clock c = new Clock(ac, 500);
        ac.out.addDependent(c);
        c.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if (c.getCount() % 32 == 0) {
                    SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("data/audio/Nylon_Guitar/Clean_A_harm.wav"));
                    sp.setKillOnEnd(false);

                    //set up random pitch
                    Random rand = new Random();

                    UGen pitch = sp.getPitchUGen();
                    pitch.setValue(Pitch.mtof(0 + Pitch.pentatonic[rand.nextInt(5)]));
                    sp.setPitch(pitch);

                    //set alternating loop start
                    sp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);

                    //set up loop start
                    float start = (float) ((sp.getSample().getLength() / 4) * 3);
                    UGen loopStart = sp.getLoopStartUGen();
                    loopStart.setValue(start);
                    sp.setLoopStart(loopStart);

                    //ADSR envelope and gain
                    Envelope adsr = new Envelope(ac, 0f);
                    Gain gain = new Gain(ac, 1, adsr);
                    gain.addInput(sp);

                    adsr.addSegment(0.2f, 20);
                    adsr.addSegment(0.1f, 100);
                    adsr.addSegment(0.1f, 200);
                    adsr.addSegment(0.0f, 20, new KillTrigger(new Bead() {
                        @Override
                        protected void messageReceived(Bead bead) {
                            new KillTrigger(gain);
                            new KillTrigger(loopStart);
                        }
                    }));

                    ac.out.addInput(gain);

                }
            }
        });
        //********** do your work here ONLY **********
    }
}
