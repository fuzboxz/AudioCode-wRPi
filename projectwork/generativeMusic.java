package net.happybrackets.tutorial.session3;

import com.sun.tools.javac.comp.Env;
import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;

import java.io.File;
import java.util.Random;

import static javafx.application.Platform.exit;
import static net.beadsproject.beads.data.Buffer.SAW;
import static net.beadsproject.beads.data.Buffer.SQUARE;

public class generativeMusic extends Application {
    final int rootNote = 63;
    final int BPM = 167;

    public static void Main() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //set up audiocontext
        AudioContext ac = new AudioContext();
        ac.start();

        //set up recordtofile object, add dependent and route audio into recorder
        RecordToFile recorder = new RecordToFile(ac, 2, new File("generativeMusic.wav"));
        ac.out.addDependent(recorder);
        recorder.addInput(ac.out);

        //set up clock object to sequence audio

        Clock clock = new Clock(ac, 60000 / BPM);
        ac.out.addDependent(clock);

        //clock events
        clock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                clockEvent(clock, ac);
            }
        });


        //set up DelayTrigger to run for 128 beats
        DelayTrigger dt = new DelayTrigger(ac, (128 * (60000 / BPM)), new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                //kill audiocontext and exit
                recorder.kill();
                System.out.println("Stopped recording, stopping process...");
                ac.stop();
                exit();
            }
        });
        ac.out.addDependent(dt);
    }

    private void clockEvent(Clock clock, AudioContext ac){

        if(clock.isBeat()) {
            Random rand = new Random();

            //kick every three beat
            if (clock.getBeatCount() % 3 == 0) {
                kick(ac);
            }

            //bass on every third beat with an offset of 2
            if (clock.getBeatCount() % 3 == 2) {
                bass(ac);
            }

            //off-beat snare
            if (clock.getBeatCount() % 4 == 2) {
                snare(ac);
            }

            //random hihat on every beat after every second 32nd beat
            if (clock.getBeatCount() % 64 > 32) {
                if (rand.nextInt(8) > 2) {
                    hihat(ac);
                }
            }

            //string
            if (clock.getBeatCount() % 32 == 0 && clock.getBeatCount() >= 32 && clock.getBeatCount() < 96) {
                string(ac);
            }

            //pluck
            if (rand.nextInt(32) > 12){
                pluck(ac);
            }

            //crash every 64 beat
            if (clock.getBeatCount() % 64 == 0) {
                crash(ac);
            }

        }
    }

    private void string(AudioContext ac) {
        //set up bass consisting of a filtered 3 osc detuned saw waveform
        Random rand = new Random();

        //set up envelopes and oscillators
        Envelope freqEnvelope = new Envelope(ac, Pitch.mtof(rootNote + Pitch.minor[rand.nextInt(5)] + 12));
        Envelope filterEnv = new Envelope(ac, 1500);
        Envelope ampEnvelope = new Envelope(ac, 0.00f);
        WavePlayer saw1 = new WavePlayer(ac, freqEnvelope, SAW);
        WavePlayer saw2 = new WavePlayer(ac, new Function(freqEnvelope) {
            @Override
            public float calculate() {
                return freqEnvelope.getCurrentValue() + 15;
            }
        }, SAW);
        WavePlayer saw3 = new WavePlayer(ac, new Function(freqEnvelope) {
            @Override
            public float calculate() {
                return freqEnvelope.getCurrentValue() - 15;
            }
        }, SAW);
        WavePlayer saw4 = new WavePlayer(ac, new Function(freqEnvelope) {
            @Override
            public float calculate() {
                return freqEnvelope.getCurrentValue() + 20;
            }
        }, SAW);
        WavePlayer saw5 = new WavePlayer(ac, new Function(freqEnvelope) {
            @Override
            public float calculate() {
                return freqEnvelope.getCurrentValue() - 20;
            }
        }, SAW);

        //route detuned saws to their own gain container
        Gain detunedSaws = new Gain(ac, 1, 0.3f);
        detunedSaws.addInput(saw2);
        detunedSaws.addInput(saw3);
        detunedSaws.addInput(saw4);
        detunedSaws.addInput(saw5);

        //set up filter and route fundamental saw and the detuned saw group
        filterEnv.addSegment(750, 20);
        BiquadFilter filter = new BiquadFilter(ac, 2, BiquadFilter.LP);
        filter.addInput(saw1);
        filter.addInput(detunedSaws);
        filter.setFrequency(filterEnv);
        filter.setQ(2);


        //set up amp
        Gain amp = new Gain(ac, 1, ampEnvelope);
        amp.addInput(filter);

        //specify envelope slope
        ampEnvelope.addSegment(0.2f, ((60000 / BPM) * 8));
        ampEnvelope.addSegment(0.1f, ((60000 / BPM) * 16));
        ampEnvelope.addSegment(0f, ((60000 / BPM) * 8), new KillTrigger(amp));

        ac.out.addInput(amp);
    }

    private void pluck(AudioContext ac) {
        //set up pluck consisting of a filtered square waveform
        Random rand = new Random();

        //set up envelopes and oscillators
        Envelope freqEnvelope = new Envelope(ac, Pitch.mtof(rootNote + Pitch.minor[rand.nextInt(5)] + 12));
        Envelope filterEnv = new Envelope(ac, 2500);
        Envelope ampEnvelope = new Envelope(ac, 0.20f);
        WavePlayer square = new WavePlayer(ac, freqEnvelope, SQUARE);


        //set up filter and route fundamental saw and the detuned saw group
        filterEnv.addSegment(1500, 20);
        BiquadFilter filter = new BiquadFilter(ac, 2, BiquadFilter.LP);
        filter.addInput(square);
        filter.setFrequency(filterEnv);
        filter.setQ(2);


        //set up amp
        Gain amp = new Gain(ac, 1, ampEnvelope);
        amp.addInput(filter);

        //specify envelope slope
        ampEnvelope.addSegment(0.1f, 10 + rand.nextInt(4) * 20);
        ampEnvelope.addSegment(0f, 100, new KillTrigger(amp));

        if (rand.nextInt(8) == 0){
            TapIn tip = new TapIn(ac, 10000);
            tip.addInput(amp);
            TapOut top = new TapOut(ac, tip, (60000 / BPM) /2 );
            Gain feedbackGain = new Gain(ac, 1, 0.5f);
            feedbackGain.addInput(top);
            ac.out.addInput(feedbackGain);
            tip.addInput(feedbackGain);
        }

        ac.out.addInput(amp);

    }

    private void bass(AudioContext ac) {
        //set up bass consisting of a filtered 3 osc detuned saw waveform
        Random rand = new Random();

        //set up envelopes and oscillators
        Envelope freqEnvelope = new Envelope(ac, Pitch.mtof(rootNote + Pitch.minor[rand.nextInt(5)] - 24));
        Envelope filterEnv = new Envelope(ac, 1500);
        Envelope ampEnvelope = new Envelope(ac, 0.20f);
        WavePlayer saw1 = new WavePlayer(ac, freqEnvelope, SAW);
        WavePlayer saw2 = new WavePlayer(ac, new Function(freqEnvelope) {
            @Override
            public float calculate() {
                return freqEnvelope.getCurrentValue() + 15;
            }
        }, SAW);
        WavePlayer saw3 = new WavePlayer(ac, new Function(freqEnvelope) {
            @Override
            public float calculate() {
                return freqEnvelope.getCurrentValue() - 15;
            }
        }, SAW);

        //route detuned saws to their own gain container
        Gain detunedSaws = new Gain(ac, 1, 0.27f);
        detunedSaws.addInput(saw2);
        detunedSaws.addInput(saw3);

        //set up filter and route fundamental saw and the detuned saw group
        filterEnv.addSegment(750, 20);
        BiquadFilter filter = new BiquadFilter(ac, 2, BiquadFilter.LP);
        filter.addInput(saw1);
        filter.addInput(detunedSaws);
        filter.setFrequency(filterEnv);
        filter.setQ(2);


        //set up amp
        Gain amp = new Gain(ac, 1, ampEnvelope);
        amp.addInput(filter);

        //specify envelope slope
        ampEnvelope.addSegment(0.1f, 400 + rand.nextInt(2) * 200);
        ampEnvelope.addSegment(0f, 100, new KillTrigger(amp));

        ac.out.addInput(amp);
    }

    private void crash(AudioContext ac) {
        Noise noise = new Noise(ac);
        Envelope noiseEnv = new Envelope(ac, 0.1f);
        Gain noiseGain = new Gain(ac, 1, noiseEnv);

        noiseEnv.addSegment(0, 800, new KillTrigger(noiseGain));


        noiseGain.addInput(noise);
        ac.out.addInput(noiseGain);
    }

    private void hihat(AudioContext ac) {
        Noise noise = new Noise(ac);
        Envelope noiseEnv = new Envelope(ac, 0.1f);
        Gain noiseGain = new Gain(ac, 1, noiseEnv);

        noiseEnv.addSegment(0, 15, new KillTrigger(noiseGain));


        noiseGain.addInput(noise);
        ac.out.addInput(noiseGain);
    }

    private void kick(AudioContext ac) {
        //set up kick consisting of a sine waveform
        Envelope freqEnvelope = new Envelope(ac, Pitch.mtof(60));
        Envelope ampEnvelope = new Envelope(ac, 0.5f);
        WavePlayer sine = new WavePlayer(ac, freqEnvelope, Buffer.SINE);

        Gain amp = new Gain(ac, 1, ampEnvelope);
        amp.addInput(sine);

        freqEnvelope.addSegment(Pitch.mtof(36), 40);
        ampEnvelope.addSegment(0.2f, 200);
        ampEnvelope.addSegment(0f, 100, new KillTrigger(amp));

        ac.out.addInput(amp);
    }

    private void snare(AudioContext ac) {
        //set up snare consisting of two sine waveforms and a noise oscillator
        WavePlayer pitch1 = new WavePlayer(ac, Pitch.mtof(72), Buffer.SINE);
        WavePlayer pitch2 = new WavePlayer(ac, Pitch.mtof(77) + 15, Buffer.SINE);
        Noise noise = new Noise(ac);

        //set up amplitude controls
        Envelope ampEnv = new Envelope(ac,0.3f);
        Envelope noiseAmp = new Envelope(ac, 0.4f);
        Gain amp = new Gain(ac, 1, ampEnv);
        Gain triangle = new Gain(ac, 1, 0.07f);
        Gain noisegain = new Gain(ac, 1, noiseAmp);

        //define envelope
        noiseAmp.addSegment(0.1f, 100);
        noiseAmp.addSegment(0f, 25);
        ampEnv.addSegment( 0.1f, 100);
        ampEnv.addSegment( 0.0f, 25, new KillTrigger(amp));

        //hook up connections
        noisegain.addInput(noise);
        triangle.addInput(pitch1);
        triangle.addInput(pitch2);
        amp.addInput(noisegain);
        amp.addInput(triangle);

        ac.out.addInput(amp);
    }


}
