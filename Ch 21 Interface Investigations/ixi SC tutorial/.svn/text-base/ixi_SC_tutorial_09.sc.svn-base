

// =====================================================================
// - SuperCollider Basics -
// =====================================================================

// Tutorial 09 - Granular Synthesis

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

// 	1) TGrains
//	2) Warp
// 	3) Custom built grainular synthesis
// 	4) The messaging style
// 	5) Munger





// 1) =========  TGrains  ==========

// TGrains is an SC UGen that performs granular synthesis

b = Buffer.alloc(s, 44100 * 2.0, 1); // 2 seconds mono buffer

(
// we load our record and playback synth definitions
SynthDef(\recBuf,{ arg out=0, bufnum=0;
	var in;
	in = AudioIn.ar(1);
	RecordBuf.ar(in, bufnum);
}).load(s);

SynthDef(\playBuf,{ arg out = 0, bufnum;
	var signal;
	signal = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum));
	Out.ar(out, signal ! 2)
}).load(s)
)


// we record into the buffer (for ca 2 secs)
x = Synth(\recBuf, [\out, 0, \bufnum, b.bufnum]);
x.free;

// test the buffer
x = Synth(\playBuf, [\bufnum, b.bufnum])


// let's try TGrains:

(
SynthDef(\tgrains,{ arg out=0, buffer=0;
	var signal, trate, dur;
	trate = MouseY.kr(1,30);
	dur = 2/trate;
	signal = 	TGrains.ar(2, Impulse.ar(trate), buffer, 1, MouseX.kr(0,BufDur.kr(buffer)), dur, 0, 0.8, 2);
	Out.ar(0, signal)
}).load(s);
)

g = Synth(\tgrains, [\buffer, b.bufnum])

// NOTE: The buffer can contain a wavetable created with .sine1, sine2, cheby, etc.
// (it doesn't have to be a "sample")




// 2) =========  The Warp ugens  ==========

// (Check the sourcecode for Warp1 (hit Apple+Y on the name) and look at the various
// Granular UGens available apart from TGrains)

// Warp1 - warp a buffer with a warp factor (stretching/compressing the buffer)

SynthDef(\warp, {arg buffer = 0, pointer =0.3;
	var out;
	// warp factor of 0.25 - stretch sound by 4 times
	// freqScale of 1.5 - pitching the sound up by half octave
	out = Warp1.ar(1, buffer, MouseX.kr(0,2), MouseY.kr(1.5, 0.5), 0.1, -1, 8, 0.15, 1.0);
	Out.ar(0, out);
}).send(s);

g = Synth(\warp, [\buffer, b.bufnum]);




// GrainIn - Grain a live input

b = Buffer.alloc(s, 44100 * 14.0, 1); // 14 second mono buffer

SynthDef(\grainin, {arg buffer = 0;
	var out, pointer, filelength, env, dir;
	out = GrainIn.ar(1, Dust.ar(20), 0.1, AudioIn.ar(1)*0.4);
	Out.ar(0, out);
}).send(s);

g = Synth(\grainin, [\buffer, b.bufnum]);


// or another test:
(
SynthDef(\sagrain, {arg amp=1, grainDur=0.1, grainSpeed=10, panWidth=0.5;
	var pan, granulizer;
	pan = LFNoise0.kr(grainSpeed, panWidth);
	granulizer = GrainIn.ar(2, Impulse.kr(grainSpeed), grainDur, Mix.ar(AudioIn.ar([1,2])), pan);
	Out.ar(0, granulizer * amp);
}).send(s);
)

x = Synth(\sagrain)

x.set(\grainDur, 0.02)
x.set(\amp, 0.02)
x.set(\amp, 1)

x.set(\grainDur, 0.1)
x.set(\grainSpeed, 5)
x.set(\panWidth, 1)



// 3) =========  Custom built granular synthesis  ==========

// the source here is a simple sinewave
// and we use a Gaussian curve for the envelope of the grain (Env.sine)
(
SynthDef(\sineGrain, { arg freq = 800, amp = 0.1, dur = 0.1;
	var signal;
	signal = FSinOsc.ar(freq, 0, EnvGen.kr(Env.sine(dur, amp), doneAction: 2));
	OffsetOut.ar(0, signal!2); 
}).load(s);
)

Synth(\sineGrain, [\freq, 500, \dur, 20.reciprocal]) // 20 ms grain

(
Task({
   1000.do({ 
   		Synth(\sineGrain, [	\freq, rrand(300, 600), // 
							\amp, rrand(0.05,0.2),
							\dur, rrand(0.06, 0.2)
						]);
		0.01.wait;
	});
}).start;
)


// here the source is a buffer

b = Buffer.read(s, "sounds/a11wlk01-44_1.aiff");

// now play it
(
SynthDef(\bufGrain,{ arg out = 0, bufnum, rate=1.0, amp = 0.1, dur = 0.1, startPos=0;
	var signal;
	signal = PlayBuf.ar(1, bufnum, rate, startPos) * EnvGen.kr(Env.sine(dur, amp), doneAction: 2);
	OffsetOut.ar(out, signal ! 2)
}).load(s)
)

Synth(\bufGrain, [\bufnum, b.bufnum])


(
Task({
   1000.do({ 
   		Synth(\bufGrain, [		\bufnum, b.bufnum,
   							\rate, rrand(0.8, 1.2),
							\amp, rrand(0.05,0.2),
							\dur, rrand(0.06, 0.2),
							\startPos, rrand(133,666)
						]);
		0.01.wait;
	});
}).start;
)

// another example:

(
SynthDef(\grain, {|freq, amp, dur, pan|
		 var signal;
		 signal = Pan2.ar(SinOsc.ar(freq, 0, amp) *
		 				EnvGen.ar(
		 						//Env.sine(dur - 0.001),  // two different envelopes
		 						Env.perc(0.001, dur- 0.001),
		 						doneAction: 2),
		 				pan);
		 OffsetOut.ar(0, signal)
		}).load(s)
)			

(
var time = 0, totalTime = 10, thisGrainDur, message, wait;

fork{
	block{| break |		
	  inf.do{
		message = [   \freq, exprand(1500, 11000),
				       \amp, rrand(-18.0, -6.0).dbamp,
				       \dur, thisGrainDur = exprand(0.02, 0.1),
				       \pan, 1.0.rand2
 				     ];
 				
	 	Synth(\grain, message);

	 	wait = thisGrainDur * rrand(0.05, 0.5); // divide by some number (2, 4, 8)
	 	time = time + wait;
	 	if (time > totalTime) { break.value };
	 	wait.wait
	   }
	};
}
)



// 4) =========  Messaging style  ==========


// The Messaging style of sending OSC from the language to the server

// you might have notised that the periodicity of the sound was 
// a bit jittery. This is because we are sending OSC messages from 
// the language to the server in real-time, not taking latency into account.

// This can be better controlled with messaging style rather than the object style

// Here is the object style:
// (an object you can put into variable and can be messaged)
x = Synth(\sineGrain, [\freq, 500, \dur, 0.1])

// And the messaging style:
// (sends an OSC bundle to the server. 0.2 is the scheduled time ahead)
(
s.sendBundle(0.2, 	["/s_new", \sineGrain, x = s.nextNodeID, 0, 1], 
				["/n_set", x, \freq, 400, \dur, 0.1]
		);
)

(
var density, graindur, freq, amp;
density = 0.05; // clouds with 50 ms intervals
graindur = 0.1; // the duration of the grain is 100 ms
freq = 300;
amp = 0.2;
Task({
   1000.do({ 
   		// uncomment the object style and comment the messaging style and listen!
   		/*
   		Synth(\sineGrain, [	\freq, freq,
							\amp, amp,
							\dur, graindur
						]);
		*/
		s.sendBundle(0.2, 
				["/s_new", \sineGrain, x = s.nextNodeID, 0, 1], 
				["/n_set", x, \freq, freq, \amp, amp, \dur, graindur]
		);
		
		density.wait; 
	});
}).start;
)



// There can be different envelopes in the grains. Here we use a Perc envelope:
(
SynthDef(\sineGrainWPercEnv, { arg freq = 800, amp = 0.1, dur = 0.1;
	var signal;
	signal = FSinOsc.ar(freq, 0, EnvGen.kr(Env.perc(0.01, dur), doneAction: 2)*amp);
	OffsetOut.ar(0, signal!2); 
}).load(s);
)

(
var density, graindur, freq, amp;
density = 0.01; // clouds with 10 ms intervals
graindur = 0.2; // the duration of the grain is 200 ms
freq = 300;
amp = 0.2;
Task({
   1000.do({ 
		s.sendBundle(0.2, 
				["/s_new", \sineGrainWPercEnv, x = s.nextNodeID, 0, 1], 
				["/n_set", x, \freq, freq, \amp, amp, \dur, graindur]
		);
		
		density.wait; 
	});
}).start;
)




// 5) =========  Munger  ==========

// Munger is another Granular Synthesis Ugen:


(
SynthDef(\mungerGrain,{ arg out = 0, bufnum, rate=1.0, amp = 0.1, dur = 0.1, startPos=0;
	var signal;
	signal = 
	Munger.ar(bufnum, 	//buffer numer: beware: for some reason i-rate does not work
			BufFrames.kr(bufnum),
			10.0, 	//rate in ms must be > 0
			10.0, 	//rate variation in ms
			1000.0, 	//grain lenght in ms
			20.0, 	//length var
			0.25, 	//speed
			0.9, 	//speed var
			0.1, 	//gain
			0.1, 	//gain var
			1, 		//direction  0: random; 1: fwd; -1: bwd;
			-1.0, 	//position -1 random
			90, 		//max voices
			0.5,
			0 		//smoothPitch ==1: speed var continuos; ==0: diatonic steps
			);
	Out.ar(out, signal)
}).load(s)
)

Synth(\mungerGrain, [\bufnum, b.bufnum])

