package net.happybrackets.assignment_tasks.session7;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.net.SocketAddress;
import java.util.Random;

public class NetDRUM implements HBAction{

    int tempo = 120;

    @Override
    public void action(HB hb) {
        hb.reset();
        Random rand = new Random();
        hb.setStatus("Update received " + rand.nextInt(10));
        //load a set of sounds
        

        hb.addControllerListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
                if (msg.getName().startsWith("/kick") || msg.getName().startsWith("/k")){
                    kick(hb);
                }
                if (msg.getName().startsWith("/snare") || msg.getName().startsWith("/s")){
                    snare(hb);
                }
                if (msg.getName().startsWith("/hihat") || msg.getName().startsWith("/h")){
                    hihat(hb);
                }
                if (msg.getName().startsWith("/crash") || msg.getName().startsWith("/c")){
                    crash(hb);
                }
                if (msg.getName().startsWith("/tempo")){
                    tempo = (int) msg.getArg(0);
                }

                if (msg.getName().startsWith("/play")){
                    char[] pattern = ((String) msg.getArg(0)).toCharArray();
                    for (int i = 0; i < pattern.length; i++) {
                        switch(pattern[i]){
                            case 'k':
                                kick(hb);
                                break;
                            case 's':
                                snare(hb);
                                break;
                            case 'h':
                                hihat(hb);
                                break;
                            case 'c':
                                crash(hb);
                                break;
                                default:
                                    break;
                        }
                        try {
                            Thread.sleep(60000 / tempo);
                        }catch (Exception e){

                        }
                    }
                }
            }

        });
    }


    private void crash(HB hb) {
        Noise noise = new Noise(hb.ac);
        Envelope noiseEnv = new Envelope(hb.ac, 0.1f);
        Gain noiseGain = new Gain(hb.ac, 1, noiseEnv);

        noiseEnv.addSegment(0, 800, new KillTrigger(noiseGain));


        noiseGain.addInput(noise);
        hb.ac.out.addInput(noiseGain);
    }

    private void hihat(HB hb) {
        Noise noise = new Noise(hb.ac);
        Envelope noiseEnv = new Envelope(hb.ac, 0.1f);
        Gain noiseGain = new Gain(hb.ac, 1, noiseEnv);

        noiseEnv.addSegment(0, 15, new KillTrigger(noiseGain));


        noiseGain.addInput(noise);
        hb.ac.out.addInput(noiseGain);
    }

    private void kick(HB hb) {
        //set up kick consisting of a sine waveform
        Envelope freqEnvelope = new Envelope(hb.ac, Pitch.mtof(60));
        Envelope ampEnvelope = new Envelope(hb.ac, 0.5f);
        WavePlayer sine = new WavePlayer(hb.ac, freqEnvelope, Buffer.SINE);

        Gain amp = new Gain(hb.ac, 1, ampEnvelope);
        amp.addInput(sine);

        freqEnvelope.addSegment(Pitch.mtof(36), 40);
        ampEnvelope.addSegment(0.2f, 200);
        ampEnvelope.addSegment(0f, 100, new KillTrigger(amp));

        hb.ac.out.addInput(amp);
    }

    private void snare(HB hb) {
        //set up snare consisting of two sine waveforms and a noise oscillator
        WavePlayer pitch1 = new WavePlayer(hb.ac, Pitch.mtof(72), Buffer.SINE);
        WavePlayer pitch2 = new WavePlayer(hb.ac, Pitch.mtof(77) + 15, Buffer.SINE);
        Noise noise = new Noise(hb.ac);

        //set up amplitude controls
        Envelope ampEnv = new Envelope(hb.ac,0.3f);
        Envelope noiseAmp = new Envelope(hb.ac, 0.4f);
        Gain amp = new Gain(hb.ac, 1, ampEnv);
        Gain triangle = new Gain(hb.ac, 1, 0.07f);
        Gain noisegain = new Gain(hb.ac, 1, noiseAmp);

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

        hb.ac.out.addInput(amp);
    }

}
