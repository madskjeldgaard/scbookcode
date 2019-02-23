

// =====================================================================
// - SuperCollider Basics -
// =====================================================================

// Tutorial 04 - Additive Synthesis Basics

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

// 	1) Creating complex waves out of sines
// 	2) Bell Synthesis
// 	3) Stupid harmonics GUI
// 	4) Some Additive SynthDefs with routines playing them
// 	5) Polishook patch
// 	6) Using Control
//	7) Using Klang and DynKlang



/* 
The Fourier Theorem states that any sound can be described as a function
made out of pure sinewaves. If we add enough sinewaves together with different
frequency, phase and amplitude envelopes, we can simulate any sound. 
*/

// making complex sounds out of sinusoidals

Server.default = s = Server.internal.boot;

// create a new analyzer
FreqScope.new(500, 300);


// so what's additive synthesis?
// adding waves together:
{SinOsc.ar(440,0, 0.4) + SinOsc.ar(880, 0, 0.4)!2}.play

// etc:

(
{	
var freq = 200;
SinOsc.ar(freq, 0, 0.2)   + 
SinOsc.ar(freq*2, 0, 0.2) +
SinOsc.ar(freq*3, 0, 0.2) + 
SinOsc.ar(freq*4, 0, 0.2) 
!2}.play
)

// but this is not really practical


// first a little trick:

// a) here is an array with 5 items:
Array.fill(5, {arg i; i.postln;});

// b) this is the same as (using a shortcut):
{arg i; i.postln;}.dup(5)

// c) or simply (using another shortcut):
{arg i; i.postln;}!5

// d) we can then sum the items in the array (add them together):
Array.fill(5, {arg i; i.postln;}).sum;

// e) we could do it this way as well:
sum({arg i; i.postln;}.dup(5));

// f) or this way:
({arg i; i.postln;}.dup(5)).sum;

// g) or this way:
({arg i; i.postln;}!5).sum;

// h) or simply this way:
sum({arg i; i.postln;}!5);






// 1) ========= Creating complex waves out of sines ==========



// NOW, let's get started:

// A saw wave
// where the harmonics (overtones) are decreasing in amplitude
// (harmonics are integer multiples of the fundamental)

(
f = {
        ({arg i;
                var j = i + 1;
                SinOsc.ar(300 * j, 0, 0.6/j) // try pi in the phase argument
        } ! 30).sum // we sum this function 30 times
!2}; // and we make it a stereo signal
)

f.plot;
f.play;

// Inverse saw wave

(
f = {
       ({arg i;
                var j = i + 1;
                SinOsc.ar(300 * j, pi, 0.6/j) // note pi 
        } ! 30).sum;
};
)

f.plot;
f.play;


// Pulse wave

//  pulse wave (If the length of the on time of the pulse is equal to the length of the 
//  off time (also known as a duty cycle of 1:1) then the pulse wave may also be 
//  called a square wave.

(
f = {
        ({arg i;
                var j = i * 2 + 1; // the odd harmonics (1,3,5,7,etc)
                SinOsc.ar(300 * j, 0, 1/j)
        } ! 20).sum;
};
)

f.plot;
f.play;


// triangle wave

(
f = {
        ({arg i;
                var j = i * 2 + 1;
                SinOsc.ar(300 * j, pi/2, 0.7/j.squared) // cosine wave (phase shift)
        } ! 20).sum;
};
)

f.plot;
f.play;


// and how would we create a synth definition out of this.... simple:
SynthDef(\triwave, {arg freq=400;
       Out.ar(0, 
        ({arg i;
                var j = i * 2 + 1;
                SinOsc.ar(freq * j, pi/2, 0.6 / j.squared) // cosine wave (phase shift)
        } ! 20).sum // sum all the waves into one
        !2) // make the signal stereo
}).load(s)

Synth(\triwave, [\freq, 300])
Synth(\triwave, [\freq, 900])

// (But we don't need to as there is a UGen (written in C, thus faster) called LFTri)


////////////////////// funky waves:

// nice one
(
f = {
        ({arg i;
                var j = i + 1;
                SinOsc.ar(MouseX.kr(20,300) * j, 0, 0.1/j + i)
        } ! 60).sum;
}
)
f.plot( maxval:nil, minval:nil);
f.play;


// another (sine - saw)
(
f = {
        ({arg i;
                var j = i + 1;
                SinOsc.ar(MouseX.kr(20,800) * j, pi/2, 1/j.squared)
        } ! 30).sum;
}
)

f.plot;
f.play;


// 
(
f = {
        ({arg i;
                var j = i + 1;
                SinOsc.ar(MouseX.kr(20,800) * j.distort, pi/2, 1/j.squared)
        } ! 60).sum;
};
)

f.plot;
f.play;



// 
(
f = {
        ({arg i;
                var j = i * 2.cubed + 1;
                SinOsc.ar(MouseX.kr(20,800) * j, 0, 1/j)
        } ! 20).sum;
};
)
f.plot;
f.play;


// 
(
f = {
        ({arg i;
                var j = i * 2.squared.distort + 1;
                SinOsc.ar(MouseX.kr(20,800) * j, 0, 1/j)
        } ! 20).sum;
};
)

f.plot(minval:nil, maxval:nil);
f.play;




// Blip works like Saw, apart from all the harmonics have the same amplitude
// but you can control dynamically how many harmonics there are
{ Blip.ar(200, MouseX.kr(1,110),0.8)!2 }.freqscope;

// compare with a saw wave
{ Saw.ar(200 *MouseX.kr(1,100),0.8)!2 }.freqscope;

// see how the saw wave is band limited (using the FreqScope)???
// compare that with a non-band limited UGen like LFSaw
{ LFSaw.ar(200 *MouseX.kr(1,100),0.8)!2 }.freqscope;




// 2) ========= Bell Synthesis ==========

// we try to create a bell like sound:

// using the synthdef from last tutorial, but we add duration to the envelope
(
SynthDef(\stereosineWenv, {arg freq=333, amp=0.4, dur, pan=0.0; // we add a new argument
	var signal, env;
	env = EnvGen.ar(Env.perc(0.01, dur), doneAction:2); // doneAction gets rid of the synth
	signal = SinOsc.ar(freq, 0, amp) * env;
	signal = Pan2.ar(signal, pan);
	Out.ar(0, signal);
}).load(s) // we load the synthdef into the server
)

(
var numberOfSynths;
numberOfSynths = 15;
Array.fill(numberOfSynths, {Synth(\stereosineWenv, 
								[	\freq, 300+(430.rand),
									\phase, 1.0.rand,
									\amp, numberOfSynths.reciprocal, //.reci = (1/nr)
									\dur, 2+(1.0.rand)])
				;});
)



// you could also put the array of partials (inharmonic spectra) into the synthdef:
(
SynthDef(\sabell, {arg freq=333, amp=0.4, dur=2, pan=0.0; // we add a new argument
	var signal, env;
	env = EnvGen.ar(Env.perc(0.01, dur), doneAction:2); // doneAction gets rid of the synth
	signal = Array.fill(15, {SinOsc.ar(freq+(430.rand), 1.0.rand, 15.reciprocal)}) * env;
	signal = Pan2.ar(signal, pan);
	Out.ar(0, signal);
}).load(s)
)

Synth(\sabell) // same sound all the time

Synth(\sabell, [\freq, 444+(400.rand)]) // new frequency, but same sound

// why?

// this is because the array (and the 430.rand) is defined when you compile the
// synth definition. Try to recompile the synthdef and you get a new sound

// This can be good in for many usages, but not the right for others. (later, we will see why)

// you can also do this using the Klang Ugen
// Klang is a bank of sine oscillators

{Klang.ar(`[ [800, 1000, 1200],[0.3, 0.3, 0.3],[pi,pi,pi]], 1, 0)}.play


// again, using the synthdef structure compiles it using one array
(
SynthDef(\saklangbell, {arg freq=400, amp=0.4, dur=2, pan=0.0; // we add a new argument
	var signal, env;
	env = EnvGen.ar(Env.perc(0.01, dur), doneAction:2); // doneAction gets rid of the synth
	signal = Klang.ar(`[freq*[1,2,3,4], [0.25, 0.25, 0.25, 0.25], nil]) * env;
	signal = Pan2.ar(signal, pan);
	Out.ar(0, signal);
}).load(s)
)

Synth(\saklangbell, [\freq, 300])




// 3) ========= Stupid harmonics GUI ==========


// a patch showing harmonics of a fundamental (the slider on the right is the fundamental freq)

SynthDef(\oscsynth, { arg bufnum, rate = 440, ts= 1; 
	a = Osc.ar(bufnum, rate, 0, 0.2) * EnvGen.ar(Env.perc(1), timeScale:ts, doneAction:2);
	Out.ar(0, a ! 2);
}).load(s);

(
var bufsize, ms, slid, cspec, rate;
var harmonics;

rate = 220;
bufsize=4096;  // check the bufsize
harmonics=20;

b=Buffer.alloc(s, bufsize, 1);

x = Synth(\oscsynth, [\bufnum, b.bufnum, \ts, 0.1]);

// GUI :
w = SCWindow("harmonics", Rect(200, 470, 20*harmonics+140,150)).front;
ms = SCMultiSliderView(w, Rect(20, 20, 20*harmonics, 100));
ms.value_(Array.fill(harmonics,0.0));
ms.isFilled_(true);
ms.valueThumbSize_(1.0);
ms.indexThumbSize_(10.0);
ms.strokeColor_(Color.blue);
ms.fillColor_(Color.blue(alpha: 0.2));
ms.gap_(10);
ms.action_({b.sine1(ms.value, false, true, true)}); // setting the harmonics !!!!
slid=SCSlider(w, Rect(20*harmonics+30, 20, 20, 100));
cspec= ControlSpec(70,1000, 'exponential', 10, 440);
slid.action_({	
	rate = cspec.map(slid.value); 	
	rate.postln;
	x.set(\rate, cspec.map(slid.value)); 
	});
slid.value_(0.3); 
slid.action.value;
SCButton(w, Rect(20*harmonics+60, 20, 60, 16))
	.states_([["Plot it!",Color.black,Color.clear]])
	.action_({	OSCIIBufferPlot.plot(b) });
SCButton(w, Rect(20*harmonics+60, 40, 60, 16))
	.states_([["Start it!",Color.black,Color.clear], ["Stop it!",Color.black,Color.clear]])
	.action_({arg sl;
		if(sl.value ==1, {x = Synth(\oscsynth, [\bufnum, b.bufnum, \rate, rate, \ts, 1000]);
			},{x.free;});
	});	
SCButton(w, Rect(20*harmonics+60, 60, 60, 16))
	.states_([["Play it!",Color.black,Color.clear]])
	.action_({
		Synth(\oscsynth, [\bufnum, b.bufnum, \rate, rate, \ts, 0.1]);
	});	
SCButton(w, Rect(20*harmonics+60, 80, 60, 16))
	.states_([["Play rand!",Color.black,Color.clear]])
	.action_({
		Synth(\oscsynth, [\bufnum, b.bufnum, \rate, rrand(20,100)+50, \ts, 0.1]);
	});	
)




// 4) ========= Some Additive SynthDefs with routines playing them ==========



// - Harmonics and amps passed in lists as arguments


// note the # in front of the arrays in the arguments. It means that they are
// literal (fixed size) arrays. And we need to declare them in the argument.

(
SynthDef(\addSynthArray, { arg freq=300, dur=0.5, mul=100, addDiv=8, 
					freqs = #[1, 2, 3, 4, 5, 6, 7,8,9,10, 11,12,13,14,15], amps = #[ 0.30136557845783, 0.15068278922892, 0.10045519281928, 0.075341394614458, 0.060273115691566, 0.050227596409638, 0.043052225493976, 0.037670697307229, 0.033485064273092, 0.030136557845783, 0.027396870768894, 0.025113798204819, 0.023181967573679, 0.021526112746988, 0.020091038563855 ]; 
					
	var lfo, signal, env;
	var n = 3;
	env = EnvGen.ar(Env.perc(0.01, dur), doneAction: 2);
	signal = Mix.arFill(freqs.size, {arg i;
				SinOsc.ar(
					freq * freqs[i], 
					0,
					amps[i]	
				)});
	
	Out.ar(0, signal.dup * env)
	}).load(s)
)

// a saw with 15 harmonics
Synth(\addSynthArray, [\freq, 200])
Synth(\addSynthArray, [\freq, 300])
Synth(\addSynthArray, [\freq, 400])

// compare with a saw with "infinite" harmonics : ) - or up to your Nyquist frequency
{Saw.ar(300)* EnvGen.ar(Env.perc(0.01, 0.5), doneAction: 2)!2}.play


// test the routine here below. uncommend and comment the variables f and a
(
fork {  // fork is basically a Routine

        z = Synth("addSynthArray");  // we create the synth we're gonna use
        1.wait;
        
        100.do {
        		// FREQUENCY of harmonics
         		//f = Array.fill(15, {arg i; i=i+1; i}).postln; // harmonic spectra (saw wave)
         		f = Array.fill(15, {10.0.rand}).postln; // inharmonic spectra (a bell?)
         		
         		// AMPLITUDE of harmonics
         		//a = Array.fill(15, {arg i; i=i+1; 1/i;}).normalizeSum.postln; // saw wave amps
         		a = Array.fill(15, {1.0.rand}).normalizeSum.postln; // random amp on each harmonic

         	   	Synth(\addSynthArray).setn(\freqs, f, \amps, a);
            	1.wait;
        };
      }  
)




// - Harmonics and amps and envelopes passed in lists as arguments

(
SynthDef(\addSynthArray2, { arg freq=433, dur=4.5, mul=100, addDiv=8, 
	// harmonic frequencies
	harmfreqs = #[1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12,13,14,15], 
	// amplitudes
	amps = #[ 0.30136557845783, 0.15068278922892, 0.10045519281928, 0.075341394614458, 0.060273115691566, 0.050227596409638, 0.043052225493976, 0.037670697307229, 0.033485064273092, 0.030136557845783, 0.027396870768894, 0.025113798204819, 0.023181967573679, 0.021526112746988, 
0.020091038563855 ], 
	// amplitude envelopes
	envs = #[[ 0.41959595680237, 1.8657131195068 ], [ 0.29434531927109, 0.34258818626404 ], [ 0.26926666498184, 1.5906579494476 ], [ 0.058589160442352, 1.679356098175 ], [ 0.3920511007309, 4.4811072349548 ], [ 0.18552374839783, 5.7129707336426 ], [ 0.019342243671417, 4.0162460803986 ], [ 0.4850749373436, 4.1913135051727 ], [ 0.043438136577606, 0.8137264251709 ], [ 0.08804988861084, 0.60232543945312 ], [ 0.42290794849396, 2.9702847003937 ], [ 0.081685602664948, 5.7034306526184 ], [ 0.13184547424316, 4.1668574810028 ], [ 0.25576674938202, 3.5108048915863 ], [ 0.32525300979614, 4.3760755062103]]; 
	
	var lfo, signal, env;
	//var n = 3;
	env = EnvGen.ar(Env.perc(0.001, dur), doneAction: 2);
	signal = Mix.arFill(harmfreqs.size, {arg i;
				SinOsc.ar(
					freq * harmfreqs[i], 
					0,
					amps[i] *
						EnvGen.kr( // put an envelope on each of the sine waves
							Env.perc(envs[i][0], envs[i][1]), doneAction: 0)
				)
				});
	//signal * env
	Out.ar(0, signal.dup * env)
	}).load(s)
)

// test it
{Synth(\addSynthArray2, [\freq, 433])}.play(s);

(
fork {  
        //z = Synth("addSynthArray2");  
        
        1.wait;
        100.do {
        		// FREQUENCY of harmonics
         		//f = Array.fill(15, {arg i; i=i+1; i}).postln; // harmonic spectra (saw wave)
         		f = Array.fill(15, {10.0.rand}).postln; // inharmonic spectra (a bell?)
         		
         		// AMPLITUDE of harmonics
         		//a = Array.fill(15, {arg i; i=i+1; 1/i;}).normalizeSum.postln; // saw wave amps
         		//a = Array.fill(15, {1.0.rand}).normalizeSum.postln; // random amp on each harmonic
         		a = Array.fill(15, {arg i; i=i+1; (1.0.rand)/(i*0.3)}).normalizeSum.postln;
         		
         		// ENVELOPES for the amplitudes
         		e = Array.fill(15, {[0.5.rand, 6.0.rand]}).postln;

         	   	Synth(\addSynthArray2).setn(\harmfreqs, f, \amps, a, \envs, e );
            	2.0.wait;
        }
      } 
)




// 5) ========= Polishook patch ==========




(
{	var n = 12;
	
	Mix.arFill(
			n,									// generate n sine waves
			{ 
			SinOsc.ar(							// each with a possible frequency between
				[67.0.rrand(2000), 67.0.rrand(2000)],	// low.rrand(high) ... floating point values
				0, 
				n.reciprocal						// scale the amplitude of each sine wave
												// according to the value of n
			)
			*
			EnvGen.kr(							// put an envelope on each of the sine waves
				Env.sine(2.0.rrand(17)), 
				doneAction: 0 					// deallocate envelopes only when the
												// entire sound is complete (why?)
			)
		}
	)
	*											// put an envelope over the whole patch
	EnvGen.kr(
		Env.perc(11, 6), 
		doneAction: 2, 
		levelScale: 0.75
	) 
												
}.play
)



// 6) ========= Using Control ==========


/*
There is another way to store arrays within a SynthDef.
This is using the Control class. The controls are good for passing
arrays into running Synths. We therefore have to use the Control UGen
inside our SynthDef.
*/


// Here we make an array of 20 frequency values inside a Control variable
// We pass this array to the SinOsc UGen which makes a "multichannel expansion"
// i.e. it creates a sinewave in 20 succeedent audio busses. (If you had a sound
// card with 20 channels, you'd get a sine out of each channel)
// But here we mix the sines into one signal. 
// Finally in the Out UGen we use "! 2" which is a multichannel expansion trick
// that makes this a 2 channel signal. (we could have used signal.dup)


SynthDef("manySines", {arg out=0;
	var sines, control, numsines;
	numsines = 20;
	control = Control.names(\array).kr(Array.rand(numsines, 400.0, 1000.0));
	sines = Mix(SinOsc.ar(control, 0, numsines.reciprocal)) ;
	Out.ar(out, sines ! 2);
}).send(s);

b = Synth("manySines");

// and here we can change the frequencies of the Control

b.setn(\array, Array.rand(20, 200, 1600)); 
b.setn(\array, {rrand(200, 1600)}!20); 
b.setn(\array, {rrand(200, 1600)}.dup(20));
// NOTE: All three lines above do exactly the same, just different syntax


// The following code is an adaption of the code in the Control helpfile.
// Note that we are using DynKlank (dynamic Klank) and we use .kr
// (control rate) rather than .ir (iteration rate). This allows us to change the
// synth in runtime

(
SynthDef("control-dynklank", { arg out=0, freq;
	var klank, n, harm, amp, ring;
	n = 9;
	// harmonics
	harm = Control.names(\harm).kr(Array.series(4,1,1));
	// amplitudes
	amp = Control.names(\amp).kr(Array.fill(4,0.05));
	// ring times
	ring = Control.names(\ring).kr(Array.fill(4,1));
	klank = DynKlank.ar(`[harm,amp,ring], {ClipNoise.ar(0.003)}.dup, freq);
	Out.ar(out, klank);
}).send(s);
)

a = Synth("control-dynklank", [\freq, 300]);
b = Synth("control-dynklank", [\freq, 400]);


a.setn(\harm,  Array.rand(4, 1.0, 4.7))
a.setn(\amp, Array.rand(4, 0.005, 0.1))
a.setn(\ring, Array.rand(4, 0.005, 1.0))

b.setn(\harm,  Array.rand(4, 1.0, 4.7))
b.setn(\amp, Array.rand(4, 0.005, 0.1))
b.setn(\ring, Array.rand(4, 0.005, 1.0))




// 7) ========= Using Klang and Dynklang ==========


/*

It can be laborous to build an array of synths and set the frequencies and amplitudes
of each. For that there is a UGen called Klang. Klang is a bank of sine oscillators.
NOTE: Klang is a bank of oscillators (good for additive synthesis), and Klank is a 
bank of resonators (good for subtractive synthesis).

*/


// ----- Using Klang

// bank of 12 oscillators of frequencies between 600 and 1000
{ Klang.ar(`[ Array.rand(12, 600.0, 1000.0), nil, nil ], 1, 0) * 0.05 }.play;

// here we create synths every 2 seconds
(
{
loop({
	{
		Pan2.ar(Klang.ar(`[ Array.rand(12, 200.0, 2000.0), nil, nil ], 1, 0), 1.0.rand) 
			* EnvGen.kr(Env.sine(4), 1, 0.02, doneAction: 2);
	}.play;
	2.wait;
})
}.fork;
)


// ----- Using the Dynklang

/*
Klang can not recieve updates to its frequencies nor can it be modulated.
For that we use DynKlang (Dynamic Klang).
*/

// frequency modulation
(
{ 
	DynKlang.ar(`[ 
		[800, 1000, 1200] + SinOsc.kr([2, 3, 0.2], 0, [130, 240, 1200]),
		[0.6, 0.4, 0.3],
		[pi,pi,pi]
	]) * 0.1
}.freqscope;
)

// amplitude modulation
(
{ 
	DynKlang.ar(`[ 
		[800, 1600, 2400, 3200],
		[0.1, 0.1, 0.1, 0.1] + SinOsc.kr([0.1, 0.3, 0.8, 0.05], 0, [1, 0.8, 0.8, 0.6]),
		[pi,pi,pi]
	]
) * 0.1
}.freqscope;
)

// the following patch shows how a GUI is used to control the amplitudes of the
// DynKlang oscillator array


(	// create multichannel controls directly with literal arrays:
SynthDef(\dynsynth, {| freqs (#[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]), 
	amps (#[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]), 
	rings (#[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0])|
	Out.ar(0, DynKlang.ar(`[freqs, amps, rings]))
}).load(s)
)

(
var bufsize, ms, slid, cspec, rate;
var harmonics;

harmonics = 20;


x = Synth(\dynsynth).setn(
				\freqs, Array.fill(harmonics, {|i| 110*(i+1)}), 
				\amps, Array.fill(harmonics, {0})
				);

// GUI :
w = SCWindow("harmonics", Rect(200, 470, 20*harmonics+40,140)).front;
ms = SCMultiSliderView(w, Rect(20, 10, 20*harmonics, 110));
ms.value_(Array.fill(harmonics,0.0));
ms.isFilled_(true);
ms.valueThumbSize_(1.0);
ms.indexThumbSize_(10.0);
ms.strokeColor_(Color.blue);
ms.fillColor_(Color.blue(alpha: 0.2));
ms.gap_(10);
ms.action_({ arg ms;
	x.setn(\amps, ms.value*harmonics.reciprocal);
}); // setting the harmonics !!!!
)




