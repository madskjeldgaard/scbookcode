

// =====================================================================
// - SuperCollider Basics -
// =====================================================================

// Tutorial 07 - Envelopes

// =====================================================================
// - ixi audio tutorial - www.ixi-audio.net
// =====================================================================


/*		
		---------------------------------------------------------------
 		Copyright (c) 2005-2008, ixi audio.
 		This work is licensed under a Creative Commons 
		Attribution-NonCommercial-ShareAlike 2.0 England & Wales License.
 		http://creativecommons.org/licenses/by-nc-sa/2.0/uk/
		---------------------------------------------------------------
*/



// ========== Contents of this tutorial ==========

// 	1) Envelope generator
// 	2) Envelope types
// 	3) Triggers and gates
//	4) MIDI keyboard example




// 1) =========  Envelope generator  ==========

// To create an envelope, we need an envelope generator
// The envelope generator can calculate various types of envelopes
// it contains the envelope and performs the doneActions (what 
// happens when the envelope ends)


EnvGen.ar(envelope, gate, levelScale, levelBias, timeScale, doneAction)

doneActions:
	
	 0   do nothing when the envelope has ended.
	 1   pause the synth running, it is still resident.
	 2   remove the synth and deallocate it.
	 3   remove and deallocate both this synth and the preceeding node.
	 4   remove and deallocate both this synth and the following node.
	 5   remove and deallocate this synth 
	 	and if the preceeding node is a group then do g_freeAll on it, else n_free it.
	 6   remove and deallocate this synth 
	 	and if the following node is a group then do g_freeAll on it, else n_free it.
	 7   remove and deallocate this synth and all preceeding nodes in this group
	 8   remove and deallocate this synth and all following nodes in this group
	
	 9   remove and deallocate this synth and pause the preceeding node
	 10  remove and deallocate this synth and pause the following node
	 11  remove and deallocate this synth 
	 	and if the preceeding node is a group then do g_deepFree on it, else n_free it.
	 12  remove and deallocate this synth 
	 	and if the following node is a group then do g_deepFree on it, else n_free it.
	 13	remove and deallocate this synth and all other nodes in this group (before and after)



// 2) =========  Envelope typees  ==========

// try running the following lines and look at the output:

		Env.linen(1, 2, 3, 0.6).test.plot;
	 	Env.triangle(1, 1).test.plot;
	 	Env.sine(1,1).test.plot;
		Env.perc(0.05, 1, 1, -4).test.plot;
		Env.adsr(0.02, 0.2, 0.25, 1, 1, -4).test.plot;
		Env.asr(0.02, 0.5, 1, 1).test.plot;
		Env.cutoff(1, 1).test(2).plot;
		// using .new you can define your own envelope
		Env.new([0, 1, 0.3, 0.8, 0], [2, 3, 1, 4],'sine').test.plot;
		Env.new([0,1, 0.3, 0.8, 0], [2, 3, 1, 4],'linear').test.plot;



// 3) =========  Triggers and gates  ==========



// gate holds the EnvGen open. Here using Dust (random impulses) to trigger a new envelope
{EnvGen.ar(Env.adsr(0.001, 0.8, 0, 0.01, 1), Dust.ar(1)) *  SinOsc.ar}.play

// Here using Impulse (periodic impulses)
{EnvGen.ar(Env.adsr(0.0001, 0.8, 0, 0.01, 1), Impulse.ar(2)) *  SinOsc.ar}.play

// With a doneAction: 2 we kill the synth after the first envelope
{EnvGen.ar(Env.adsr(0.0001, 0.8, 0, 0.01, 1), Impulse.ar(2), doneAction:2) *  SinOsc.ar}.play


// Envelopes can be used everywhere in the code, not just for amplitude
(
{
	40.do({
		{ 	var freq, ratio, modulator, carrier;
			// create an array of 12 midinotes, choose one, change it to cps and post it:
			freq = Array.fill(12, {arg i; 60 + i}).choose.midicps.postln; 
			ratio =  2; // EnvGen.ar(Env.perc(0.05, 1)); // try this
			
			modulator = SinOsc.ar(freq * ratio, 0, EnvGen.ar(Env.sine(0.5, 1))*15);
			carrier = SinOsc.ar(freq + (modulator * freq), 0, 0.5);
			
			carrier	*  EnvGen.ar(Env.perc(0.01, 1), doneAction:2)
		}.play;
		1.wait;
	});
}.fork
)


// Triggers

// in the example above we saw how Dust and Impulse could be used to trigger
// an envelope.

// the trigger can be set from everywhere (code, GUI, system, etc)
// (but use "t_" in front of trigger arguments.

(
a = { arg t_gate = 1;
	var freq;
	freq = EnvGen.kr(Env.new([200, 200, 800], [0, 1.6]), t_gate);
        SinOsc.ar(freq,
                0, 0.2
        ) ! 2 
}.play;
)

a.set(\t_gate, 1)  // do this repeatedly

a.free

(
a = { arg t_gate = 1;
	var env;
	env = EnvGen.kr(Env.adsr, t_gate);
     
     SinOsc.ar(888, 0, 1 * env) ! 2 
}.play;
)

a.set(\t_gate, 1)  // do this repeatedly


a.free





// if you want to keep the same synth on the server and trigger it
// from another process than the synthesis control parameter process
// you can use gates and triggers for the envelope.

// use doneAction:0 to keep the synth on the server after the envelope is finished

// WITH A FLEXIBLE TIME ENVELOPE (USING GATE)
SynthDef(\trigtest, {arg freq, amp, dur=1, gate;
	var signal, env;
	env = EnvGen.ar(Env.adsr(0.01, dur, amp, 0.7), gate, doneAction:0); 
	signal = SinOsc.ar(freq) * env;
	Out.ar(0, signal);
}).load(s)


a = Synth(\trigtest, [\freq, 333, \amp, 1, \gate, 1])
a.set(\gate, 0)


a.set(\gate, 1)
a.set(\gate, 0)

a.set(\freq, 788)

a.set(\gate, 1)
a.set(\gate, 0)



// WITH A FIXED TIME ENVELOPE (USING TRIGGER)
// here we use a t_trig to retrigger the synth
SynthDef(\trigtest2, {arg freq, amp, dur=1, t_trig;
	var signal, env;
	env = EnvGen.ar(Env.perc(0.01, dur, amp), t_trig, doneAction:0); 
	signal = SinOsc.ar(freq) * env;
	Out.ar(0, signal);
}).load(s)


a = Synth(\trigtest2, [\freq, 333, \amp, 1, \t_trig, 1])

a.set(\freq, 788)
a.set(\t_trig, 1);
a.set(\amp, 0.28)
a.set(\t_trig, 1);

a.set(\freq, 588)
a.set(\t_trig, 1);
a.set(\amp, 0.8)
a.set(\t_trig, 1);




// 4) =========  MIDI keyboard example  ==========


/* 
In order to use MIDI, we connect our peripherals and make sure they are working.
Then we ...

a) initialise the MIDIClient:
MIDIClient.init;

b) start the MIDIIn responcer
MIDIIn.connect;

c) then we define what midi methods we are waiting for - such as :
noteOn 
noteOff
polytouch
control
program
touch
bend
sysex

NOTE: all the synthdefs in these examples are quite boring and uninteresting.
It's up to you to make them sound good!

*/

// Now we set up our MIDI functions:

MIDIClient.init(2,2); // check how many sources you have (in the post window)
// in my case: Sources: [ IAC Driver : IAC Bus 1, FireWire 410 : FireWire 410 ]
MIDIIn.connect(1, MIDIClient.sources.at(1));  // select the source you want (I want the FireWire 410 as source)
MIDIIn.noteOn = {arg src, chan, num, vel; [src, chan, num, vel].postln;};

//MIDIIn.connect(0, MIDIClient.sources.at(0).uid);

// this is the synthdef we are going to use
(
SynthDef(\midisynth1, { arg freq=440, filter=400, gate=0.0, vibrato=0.0;
	var x;
	x = Saw.ar(freq * SinOsc.ar(vibrato/20, 0, 1, 1), 1);
	x = MidEQ.ar(x, freq+filter, 0.3, 12);
	x = EnvGen.kr(Env.adsr, gate, Latch.kr(gate, gate)) * x;
	Out.ar(0, x!2);
}).load(s);
)

x = Synth(\midisynth1);
// let's try our synth

x.set(\freq, 444);
x.set(\gate, 1 );
x.set(\vibrato, 20 );
x.set(\gate, 0 );
x.free;



(
//set the MIDI into action:
x = Synth(\midisynth1); // we are controlling the same synth, therefore no doneAction in the envelope

MIDIIn.noteOn = {arg src, chan, num, vel;
				x.set(\freq, num.midicps);
				x.set(\gate, vel / 127 );
				//x.set(\formfreq, vel / 127 * 1000);
			};
MIDIIn.noteOff = { arg src,chan,num,vel;
				x.set(\gate, 0.0);
			};
MIDIIn.bend = { arg src,chan,val;
				(val * 0.048828125).postln;
				//val.postln;
				x.set(\filter, val*0.148828125 );
			};
MIDIIn.control = {arg src, chan, num, vel;
			//vel.postln;
			x.set(\vibrato, vel );
}
)



// the program above is monophonic.
// for polyphony, we store each note in an array and control the array
(
SynthDef(\midisynth2, { arg freq=440, filter=400, gate=0.0, vibrato=0.0;
	var x;
	x = Saw.ar(freq * SinOsc.ar(vibrato/20, 0, 1, 1), 1);
	x = MidEQ.ar(x, freq+filter, 0.3, 12);
	x = EnvGen.kr(Env.adsr, gate, Latch.kr(gate, gate), doneAction:2) * x;
	Out.ar(0, x!2);
}).load(s);
)


(
var poly;
poly = Array.fill(128, 0); // array with all possible notes

MIDIIn.noteOn = {arg src, chan, num, vel;
	if( poly[num].notNil ){
		poly.put(num, Synth(\midisynth2, [\freq, num.midicps, \gate, vel/127]))
	}{
		Ê// sustain pedal safety. if previous synth exists, free it.Ê
		poly[num].set(\gate, 0); 
		poly.put(num, Synth(\midisynth2, [\freq, num.midicps, \gate, vel/127]))
	}
};

MIDIIn.noteOff = { arg src,chan,num,vel;
	poly[num].set(\gate, 0);
};

MIDIIn.bend = { arg src,chan,val;
	poly.do({arg synth; if( synth!=0 , { synth.set(\filter, val*0.148828125 ) }); });
};

MIDIIn.control = {arg src, chan, num, vel;
	poly.do({arg synth; if( synth!=0 , { synth.set(\vibrato, vel ) }); });
};


)




////////// another way is to use the NoteOnResponder (check the class file)

(
SynthDef(\midisynth3, {|freq=400, gate=1, vol=1|Ê
	var signal;
	signal = Saw.ar(freq, vol) * EnvGen.kr(Env.asr(0.1, 1, 1), gate:gate, doneAction:2);
	Out.ar(0,Êsignal!2)
}).load(s);
)

(
var n, d, poly;
poly = Array.fill(128, 0);
n = NoteOnResponder({|src,chan,num,veloc|Ê
	if( poly[num].notNil ){
		poly.put(num, Synth(\midisynth3, [\freq, num.midicps, \vol, veloc/127]))
	}{
		Ê// sustain pedal safety. if previous synth exists, free it.Ê
		poly[num].set(\gate, 0); 
		poly.put(num, Synth(\midisynth3, [\freq, num.midicps, \vol, veloc/127]))
	}
});
d = NoteOffResponder({|src,chan,num,veloc| poly[num].set(\gate, 0) });
CmdPeriod.doOnce({n.remove; d.remove}); // on command + period, free the note responders (on and off)
)

