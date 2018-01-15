package net.happybrackets.assignment_tasks.Session8;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SirenPi implements HBAction {
    final int VOICES = 5;
    final int TEMPO = 96;
    final int KEY = 65; // F
    final int[] SCALE = Pitch.minor; // minor scale

    int lastNote = 4;
    float sensorFilter = 2000;

    @Override
    public void action(HB hb) {
        hb.setStatus("Update received!");
        System.out.println("Update received!");
        hb.reset();
        LSM9DS1 lsm = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        Random rand = new Random();

        //create lists to handle polyphony
        ArrayList<Glide> glide = new ArrayList<Glide>();
        ArrayList<WavePlayer> voice = new ArrayList<WavePlayer>();
        ArrayList<Envelope> envelope = new ArrayList<Envelope>();
        ArrayList<Gain> gain = new ArrayList<Gain>();

        //create delay
        Glide delayTime = new Glide(hb.ac, 100);
        Glide feedbackSens = new Glide(hb.ac, 0.8f);
        TapIn tip = new TapIn(hb.ac,2000f);
        TapOut top = new TapOut(hb.ac, tip, delayTime);
        Gain feedback = new Gain(hb.ac, 1, feedbackSens);

        //Filter
        BiquadFilter filter = new BiquadFilter(hb.ac, 1, BiquadFilter.Type.LP);
        filter.setFrequency(sensorFilter);
        hb.sound(filter);



        //set up clock
        Clock myclock = new Clock(hb.ac, 60000/ TEMPO);
        myclock.setTicksPerBeat(1);
        hb.ac.out.addDependent(myclock);
        myclock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if (myclock.isBeat())
                {
                    System.out.println("Beat!");
                }
                if (myclock.getBeatCount() % 1 == 0) {

                    int newVoice = (lastNote + 1) % VOICES;
                    int note = KEY + SCALE[rand.nextInt(SCALE.length)];
                    System.out.println("Playing voice " + newVoice + " and note " + note);
                    lastNote = newVoice;

                    glide.get(newVoice).setValue(Pitch.mtof(note));
                    gain.get(newVoice).setGain(0.7f / VOICES);
                    System.out.println("Current gain " +gain.get(newVoice).getGain());
                    envelope.get(newVoice).addSegment((float) ((0.7f / VOICES) / 2), 5);
                    envelope.get(newVoice).addSegment((float) (0f), 5);
                    hb.setStatus("Last note: " + lastNote);
                }
            }
        });

        lsm.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                delayTime.setValue((float)(((lsm.getAccelerometerData()[2])* + 1) * 0.5) * 100 + 150);
                feedbackSens.setValue((float) (((lsm.getAccelerometerData()[1]* +1) * 0.45) + 0.25));
                filter.setFrequency((float) (((lsm.getAccelerometerData()[0]* +1) * 3000) + 3500));
                filter.setQ((float) (1-((lsm.getAccelerometerData()[0] + 1) * 0.25f)));
                System.out.println("delay time " + delayTime.getValue() + " feedback " + feedbackSens.getValue() + " frequency " + filter.getFrequency() + "hz");
            }
        });


        //fill lists with objects, create connections
        setup(hb, glide, voice, envelope, gain, tip, top, feedback, filter, lsm);

        hb.ac.out.setGain(0.2f);
    }

    void setup(HB hb, ArrayList glide, ArrayList voice, ArrayList envelope, ArrayList gain, TapIn tip, TapOut top, Gain feedback, BiquadFilter filter, LSM9DS1 lsm) {

        //per voice setup
        for (int i = 0; i < VOICES; i++)
        {
            //set up glide, voice, gain
            glide.add(new Glide(hb.ac, Pitch.mtof(60),0));
            voice.add(new WavePlayer(hb.ac, (Glide) glide.get(i), Buffer.SAW));
            envelope.add(new Envelope(hb.ac));
            gain.add(new Gain(hb.ac, 1, (Envelope)envelope.get(i)));

            //connect I/O per voice
            ((Gain) gain.get(i)).addInput((WavePlayer)voice.get(i));
            filter.addInput((Gain)gain.get(i));
        }

        //set up feedback ONLY ONCE
        feedback.addInput(top);
        tip.addInput(feedback);
        tip.addInput(filter);

        //set up feedback and main out
        hb.sound(feedback);
        hb.sound(filter);

    }
}
