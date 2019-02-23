

// =====================================================================
// - SuperCollider Basics -
// =====================================================================

// Tutorial 10 - Physical Modelling

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

// 	1) Karplus-Strong synthesis
// 	2) A glass synthesis using biquad filter (SOS)
// 	3) Waveguide Flute
// 	4) Some useful filters
// 	5) The MetroGnome
// 	6) TBall examples
// 	7) The STK synthesis kit




/*
In physical modeling, the technique involves modelling the process that creates the sound and not just synthesising the sound itself. As opposed to traditional synthesis types (AM, FM, granular, etc) we are not imitating the sound of an instrument, but rather simulating the instrument itself and the physical laws that are involved in the creation of a sound.

Waveguide synthesis models the physics of the acoustic instrument or sound generating object. It simulates the traveling of waves through a string or a tube. The physical structures of an instrument can be thought of as waveguides or a transmission lines.

The basic idea (as with all acoustic instruments) is to generate an excitation in the forme of noise or impulse or some other form (imitating the plucking or drumming) and then the resonance of the instrument itself using delays, filters and reverbs.

For a good text on physical modelling, check Julius O. Smith's "Physical Audio Signal Processing":
http://ccrma.stanford.edu/~jos/pasp/pasp.html

*/



// 1) =========  Karplus-Strong synthesis ==========


/*
Karplus-Strong synthesis (named after it's authors) is a predecessor of 
physical modeling and is good for synthesising strings and percussion sounds.
*/

// we use a noise ugen to generate a burst
(
{  
 var burstEnv, att = 0, dec = 0.001; //Variable declarations 
 burstEnv = EnvGen.kr(Env.perc(att, dec), gate: Impulse.kr(1)); //envelope 
 PinkNoise.ar(burstEnv); //Noise, amp controlled by burstEnv 
}.play  
)

// but then we use Comb delay to create the delay line that creates the tone


// let's create a synthdef using Karplus-Strong
SynthDef(\ks_guitar, { arg note, pan, rand, delayTime, noiseType=1;
	var x, y, env;
	env = Env.new(#[1, 1, 0],#[2, 0.001]);
	// A simple exciter x, with some randomness.
	x = Decay.ar(Impulse.ar(0, 0, rand), 0.1+rand, WhiteNoise.ar); 
 	x = CombL.ar(x, 0.05, note.reciprocal, delayTime, EnvGen.ar(env, doneAction:2)); 
	x = Pan2.ar(x, pan);
	Out.ar(0, LeakDC.ar(x));
}).store;

// and play the synthdef
(
{
	20.do({
		Synth(\ks_guitar, [\note, 220+(400.rand), 
					\pan, 1.0.rand2, 
					\rand, 0.1+0.1.rand, 
					\delayTime, 2+1.0.rand]);
					
	   (1.0.rand + 0.5).wait;
	});
}.fork
)

// here using patterns
a = Pdef(\kspattern, 
		Pbind(\instrument, \ks_guitar, // using our sine synthdef
					\note, Pseq.new([60, 61, 63, 66], inf).midicps, // freq arg
					\dur, Pseq.new([0.25, 0.5, 0.25, 1], inf),  // dur arg
					\rand, Prand.new([0.2, 0.15, 0.15, 0.11], inf),  // dur arg
					\pan, 2.0.rand-1,
					\delayTime, 2+1.0.rand;  // envdur arg

		)
).play;

// compare using whitenoise and pinknoise as an exciter:

// whitenoise

(
{  
 var burstEnv, att = 0, dec = 0.001; 
 var burst, delayTime, delayDecay = 0.5; 
 var midiPitch = 69; // A 440 
 delayTime = midiPitch.midicps.reciprocal; 
 burstEnv = EnvGen.kr(Env.perc(att, dec), gate: Impulse.kr(1/delayDecay));  
 burst = WhiteNoise.ar(burstEnv);  
 CombL.ar(burst, delayTime, delayTime, delayDecay, add: burst);  
}.play  
) 

// pinknoise
(
{  
 var burstEnv, att = 0, dec = 0.001; 
 var burst, delayTime, delayDecay = 0.5; 
 var midiPitch = 69; // A 440 
 delayTime = midiPitch.midicps.reciprocal; 
 burstEnv = EnvGen.kr(Env.perc(att, dec), gate: Impulse.kr(1/delayDecay));  
 burst = PinkNoise.ar(burstEnv);  
 CombL.ar(burst, delayTime, delayTime, delayDecay, add: burst);  
}.play  
) 



// Another version of the K-S algorithm

// Note that delayTime is controlling the pitch here. The delay time is reciprocal to the pitch. 
// 1/100th of a sec is 100Hz, 1/400th of a sec is 400Hz.


(
SynthDef(\KSpluck, { arg midiPitch = 69, delayDecay = 1.0;
	var burstEnv, att = 0, dec = 0.001;
	var signalOut, delayTime;

	delayTime = [midiPitch, midiPitch + 12].midicps.reciprocal;
	burstEnv = EnvGen.kr(Env.perc(att, dec)); // here using an envelope in the exciter
	signalOut = PinkNoise.ar(burstEnv); 
	signalOut = CombL.ar(signalOut, delayTime, delayTime, delayDecay, add: signalOut); 
	DetectSilence.ar(signalOut, doneAction:2);
	Out.ar(0, signalOut)
	}
).store;
)

(
//Then run this playback task
r = Task({
	{Synth(\KSpluck, 
		[
		\midiPitch, rrand(30, 90), //Choose a pitch
		\delayDecay, rrand(0.1, 3.0) //Choose duration
		]);
		//Choose a wait time before next event
		[0.125, 0.125, 0.25].choose.wait;
	}.loop;
}).play
)


// The KS algorithm has been implemented as a UGen in SuperCollider, so check
// out the Pluck UGen (and this should be more CPU friendly):

(
	{Pluck.ar(WhiteNoise.ar(0.1), Impulse.kr(2), 240.reciprocal, 240.reciprocal, 10, 
		coef:MouseX.kr(-0.999, 0.999))
	}.play(s)
)




// 2) =========  Glass synthesis using a biquad filter ==========


// the formula for the biquad is:

// out(i) = (a0 * in(i)) + (a1 * in(i-1)) + (a2 * in(i-2)) + (b1 * out(i-1)) + (b2 * out(i-2))



(
{
t = Impulse.ar(1);

SOS.ar(t, 0.0, 0.055026, 0.0, MouseY.kr(1.45, 1.998896, 1),  -1 * MouseX.kr(0.999, 0.9998, 1)) ! 2

}.play
)




// 3) =========  Waveguide flute  ==========




// Waveguide flute based upon Hans Mikelson's Csound slide flute (ultimately derived from Perry Cook's)
// STK slide flute physical model.  SuperCollider port by John E. Bower, who kindly allowed for the
// flute's inclusion in this tutorial. 

// Please note that this instrument remains buggy. It's best used in lower registers ( alto or bass
// flute range ).  Intonation and tuning gets off as you go higher, especially above 72.midicps.
// This is a work in progress.  Please feel free to contribute any corrections via the sc-users
// list or by emailing me directly.  Thanks!


(

SynthDef("waveguideFlute", { arg scl = 0.2, pch = 72, ipress = 0.9, ibreath = 0.09, ifeedbk1 = 0.4,
							ifeedbk2 = 0.4, dur = 1, gate = 1, amp = 2;
	
	var kenv1, kenv2, kenvibr, kvibr, sr, cr, block;
	var poly, signalOut, ifqc;
	var aflow1, asum1, asum2, afqc, atemp1, ax, apoly, asum3, avalue, atemp2, aflute1;
	var fdbckArray;
	
	sr = SampleRate.ir;
	cr = ControlRate.ir;
	block = cr.reciprocal;
	
	ifqc = pch.midicps;
	
	// noise envelope
	kenv1 = EnvGen.kr(Env.new( 
		[ 0.0, 1.1 * ipress, ipress, ipress, 0.0 ], [ 0.06, 0.2, dur - 0.46, 0.2 ], 'linear' )
	);
	// overall envelope
	kenv2 = EnvGen.kr(Env.new(
		[ 0.0, amp, amp, 0.0 ], [ 0.1, dur - 0.02, 0.1 ], 'linear' ), doneAction: 2 
	);
	// vibrato envelope
	kenvibr = EnvGen.kr(Env.new( [ 0.0, 0.0, 1, 1, 0.0 ], [ 0.5, 0.5, dur - 1.5, 0.5 ], 'linear') );
	
	// create air flow and vibrato
	aflow1 = LFClipNoise.ar( sr, kenv1 );
	kvibr = SinOsc.ar( 5, 0, 0.1 * kenvibr );
	
	asum1 = ( ibreath * aflow1 ) + kenv1 + kvibr;
	afqc = ifqc.reciprocal - ( asum1/20000 ) - ( 9/sr ) + ( ifqc/12000000 ) - block;
	
	fdbckArray = LocalIn.ar( 1 );
	
	aflute1 = fdbckArray;
	asum2 = asum1 + ( aflute1 * ifeedbk1 );
	
	//ax = DelayL.ar( asum2, ifqc.reciprocal * 0.5, afqc * 0.5 );
	ax = DelayC.ar( asum2, ifqc.reciprocal - block * 0.5, afqc * 0.5 - ( asum1/ifqc/cr ) + 0.001 );
	
	apoly = ax - ( ax.cubed );
	asum3 = apoly + ( aflute1 * ifeedbk2 );
	avalue = LPF.ar( asum3, 2000 );

	aflute1 = DelayC.ar( avalue, ifqc.reciprocal - block, afqc );
	
	fdbckArray = [ aflute1 ];
	
	LocalOut.ar( fdbckArray );
	
	signalOut = avalue;

	OffsetOut.ar( 0, [ signalOut * kenv2, signalOut * kenv2 ] );
	
}).load(s);

)

// test the flute
s.sendMsg("/s_new", "waveguideFlute", -1, 0, 0, "amp", 0.5, "dur", 5, "ipress", 0.90, "ibreath", 0.00536, "ifeedbk1", 0.4, "ifeedbk2", 0.4, "pch", 60 );

// test the flute player's skills:
(

Routine({
	var pitches, durations;
	
	pitches = Pseq( [ 47, 49, 53, 58, 55, 53, 52, 60, 54, 43, 52, 59, 65, 58, 59, 61, 67, 64, 58, 53, 66, 73 ], inf ).asStream;
	durations = Pseq([ Pseq( [ 0.15 ], 17 ), Pseq( [ 2.25, 1.5, 2.25, 3.0, 4.5 ], 1 ) ], inf).asStream;

	17.do({
		var rhythm;
		rhythm = durations.next;		
		s.sendMsg("/s_new", "waveguideFlute", -1, 0, 0, "amp", 0.6, "dur", rhythm, "ipress", 0.93,
			"ibreath", 0.00536, "ifeedbk1", 0.4, "ifeedbk2", 0.4, "pch", pitches.next );
		rhythm.wait;	
	});
	
	5.do({
		var rhythm;
		rhythm = durations.next;		
		s.sendMsg("/s_new", "waveguideFlute", -1, 0, 0, "amp", 0.6, "dur", rhythm + 0.25, "ipress",
			0.93, "ibreath", 0.00536, "ifeedbk1", 0.4, "ifeedbk2", 0.4, "pch", pitches.next 
		);		
		rhythm.wait;
	});
	
}).play;
)





// 4) =========  Some useful filters  ==========


// some useful filters for physical modeling

// remember that the main concepts here are an exciter (such as Impulse or Dust)
// and a resonator (such as Resonators (Resonz, Klank), Delays, Reverbs, etc.)

// you can see the exciter as the plucking of the string or strucking the drum
// or blowing the mouthpiece of a flute

// the resonator is the body of the instrument together with the soundgenerating
// element such as the string or the membrane of the drum.

{ Klank.ar(`[[800, 1071, 1153, 1723], nil, [1, 1, 1, 1]], Impulse.ar(2, 0, 0.2)) }.play;

{ Resonz.ar(Impulse.ar(10)*1.5, MouseY.kr(100,1000), MouseX.kr(0.001,1)) }.play

{ Ringz.ar(Dust.ar(3, 0.3), 2000, 2) }.play

{ Decay.ar(Impulse.ar(XLine.kr(1,50,20), 0.25), 0.2, FSinOsc.ar(600), 0)  }.play;

{ Formant.ar(200, XLine.kr(400, 4000, 8), 200, 0.125) }.play(s)




// 5) =========  The MetroGnome  ==========


// How about trying to synthesise a wooden old-fashioned metronome?

(
SynthDef(\metro, {arg tempo=1, filterfreq=1000, rq=1.0;
var env, signal;
	var rho, theta, b1, b2;
	theta = MouseX.kr(0.02, pi);
	rho = MouseY.kr(0.7, 0.9999999);
	b1 = 2.0 * rho * cos(theta);
	b2 = rho.squared.neg;
	signal = SOS.ar(Impulse.ar(tempo), 1.0, 0.0, 0.0, b1, b2);
	signal = RHPF.ar(signal, filterfreq, rq);
	Out.ar(0, Pan2.ar(signal, 0));
}).load(s)
)

// Move the mouse to find your preferred metronome (low left works best for me)

a = Synth(\metro) // we create our metronome
a.set(\tempo, 0.5.reciprocal) // 120 bpm (0.5.reciprocal = 2 bps)
a.set(\filterfreq, 4000) // try 1000 (for example)
a.set(\rq, 0.1) // try 0.5 (for example)




// Let's reinterpret the Pome symphonique was composed by Gyšrgy Ligeti (in 1962)
// http://www.youtube.com/watch?v=QCp7bL-AWvw

(
SynthDef(\ligetignome, {arg tempo=1, filterfreq=1000, rq=1.0;
var env, signal;
	var rho, theta, b1, b2;
	b1 = 2.0 * 0.97576 * cos(0.161447);
	b2 = 0.97576.squared.neg;
	signal = SOS.ar(Impulse.ar(tempo), 1.0, 0.0, 0.0, b1, b2);
	signal = RHPF.ar(signal, filterfreq, rq);
	Out.ar(0, Pan2.ar(signal, 0));
}).load(s)
)

// and we create 10 different metronomes running in different tempi
// (try with 3 metros or 30 metros)
(
10.do({
	Synth(\ligetignome).set(
		\tempo, (rrand(0.5,1.5)).reciprocal, 
		\filterfreq, rrand(500,4000), 
		\rq, rrand(0.3,0.9) )
});
)




// 6) =========  TBall examples  ==========


// From the TBall helpfile:

	
	// mouse x controls switch of level
	// mouse y controls gravity
	(
	{ 
		var t, sf;
		sf = K2A.ar(MouseX.kr > 0.5) > 0;
		t = TBall.ar(sf, MouseY.kr(0.01, 1.0, 1), 0.01);
		Pan2.ar(Ringz.ar(t * 10, 1200, 0.1), MouseX.kr(-1,1)); 
	}.play;
	)

	
	// mouse x controls step noise modulation rate
	// mouse y controls gravity
	(
	{ 
		var t, sf, g;
		sf = LFNoise0.ar(MouseX.kr(0.5, 100, 1));
		g = MouseY.kr(0.01, 10, 1);
		t = TBall.ar(sf, g, 0.01, 0.002);
		Ringz.ar(t * 4, [600, 645], 0.3); 
	}.play;
	)
	
	
	// this is no mbira: vibrations of a bank of resonators that are 
	// triggered by some bouncing things that bounce one on each resonator
	
	// mouse y controls friction
	// mouse x controls gravity
	(
		{ 
		var sc, g, d, z, lfo, rate;
		g = MouseX.kr(0.01, 100, 1);
		d = MouseY.kr(0.00001, 0.2);
		sc = #[451, 495.5, 595, 676, 734.5]; //azande harp tuning by B. Guinahui
		lfo = LFNoise1.kr(1, 0.005, 1);
		rate = 2.4;
		rate = rate * sc.size.reciprocal;
		z = sc.collect { |u,i|
			var f, in;
			in = Decay.ar(
					Mix(Impulse.ar(rate, [1.0, LFNoise0.kr(rate / 12)].rand, 0.1)), 					0.001
				);
			in = Ringz.ar(in, 
						Array.fill(4, { |i| (i+1) + 0.1.rand2 }) / 2
						* Decay.ar(in,0.02,rand(0.5,1), lfo)						* u, 
						Array.exprand(4, 0.2, 1).sort
						);
			in = Mix(in);
			f = TBall.ar(in * 10, g, d, 0.001);
			
			in + Mix(Ringz.ar(f, u * Array.fill(4, { |i| (i+1) + 0.3.rand2 }) * 2, 0.1))
		};
		Splay.ar(z) * 0.8
		}.play;
	)
	
	
// spring:

	// examples
	// trigger gate is mouse button
	// spring constant is mouse x
	// mouse y controls damping
	(
	{ 
		var inforce, outforce, freq, k, d;
		inforce = K2A.ar(MouseButton.kr(0,1,0)) > 0; 
		k = MouseY.kr(0.1, 20, 1);
		d = MouseX.kr(0.00001, 0.1, 1);
		outforce = Spring.ar(inforce, k, d);
		freq = outforce * 400 + 500; // modulate frequency with the force
		SinOsc.ar(freq, 0, 0.2)
	}.play;
	)
	
	

// 7) =========  The STK synthesis kit  ==========

// Paul Lansky ported the STK physical modeling kit by Perry Cook and Gary Scavone
// for SuperCollider. It can be found on the realizedsound.net website (under sc3-plugins). 
// Here are two examples using a mandolin and a violing bow.

// let's try the mandolin
{StkMandolin.ar(mul:3)}.play  

(SynthDef(\mando, {arg freq, bodysize, pickposition, stringdamping, stringdetune, aftertouch;
	var signal;
	signal = StkMandolin.ar(freq, bodysize, pickposition, stringdamping, stringdetune, aftertouch);
	Out.ar([0,1], signal);
}).load(s)
)


(
Synth(\mando, [	\freq, rrand(300, 600), 
					\bodysize, rrand(22, 64), 
					\pickposition, rrand(22, 88),
					\stringdamping, rrand(44, 80),
					\stringdetune, rrand(1, 10),
					\aftertouch, rrand(44, 80)
					]);
)


// and a violin example:


(
SynthDef(\bow, {arg freq, bowpressure = 64, bowposition = 64, vibfreq=64, vibgain=64, loudness=64;
	var signal;
	signal = StkBowed.ar(freq, bowpressure, bowposition, vibfreq, vibgain, loudness);
	signal = signal * EnvGen.ar(Env.linen, doneAction:2);
	Out.ar([0,1], signal);
}).load(s)
)

(
Task({
	100.do({
		Synth(\bow, [		\freq, rrand(200, 440), 
							\bowpressure, rrand(22, 64), 
							\bowposition, rrand(22, 64),
							\vibfreq, rrand(22, 44),
							\vibgain, rrand(22, 44)
					]);
		1.wait;
	})
}).start;
)


	