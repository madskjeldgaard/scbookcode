

// =====================================================================
// - SuperCollider Basics -
// =====================================================================

// Tutorial 06 - AM, RM and FM synthesis

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

// 	1) LFO (Low Frequency Oscillators)
// 	2) Amplitude modulation (AM) synthesis
// 	3) Ring modulation (RM) synthesis
// 	4) Frequency modulation (FM) synthesis




Server.default = s = Server.internal;
s.boot;
FreqScope.new(300, 0);



// 1) =========  LFO  ==========


// Tremolo - variations in amplitude
(
// LFO (Low Frequency Oscillator) synthesis

// mouseX is the frequency of the tremolo
{
var modulator, carrier;
modulator = SinOsc.kr(MouseX.kr(2,20));
carrier = SinOsc.ar(333, 0, modulator);
carrier!2 // the output
}.play
)


// Vibrato - variation in pitch

(
// LFO (Low Frequency Oscillator) synthesis

// mouseX is the power of the vibrato
// mouseY is the frequency of the vibrato
{
var modulator, carrier;
modulator = SinOsc.ar(MouseY.kr(20, 5), 0, MouseX.kr(5, 20)); 
carrier = SinOsc.ar(440 + modulator, 0, 1);
carrier!2 // the output
}.play
)


// let's make a Theremin:

(
{
var freq;
	freq = MouseY.kr(4000, 200, 'exponential', 0.8);
	SinOsc.ar(freq+(freq*SinOsc.ar(7,0,0.02)), 0, MouseX.kr(0, 0.9)
	)!2
}.play
)


	
// there are special Low Frequency Oscillators (LFOs)

// LFSaw
{ SinOsc.ar(LFSaw.kr(4, 0, 200, 400), 0, 0.7) }.play

// LFTri
{ SinOsc.ar(LFTri.kr(4, 0, 200, 400), 0, 0.7) }.play
{ Saw.ar(LFTri.kr(4, 0, 200, 400), 0.7) }.play

// LFPar
{ SinOsc.ar(LFPar.kr(0.2, 0, 400,800),0, 0.7) }.play

// LFCub
{ SinOsc.ar(LFCub.kr(0.2, 0, 400,800),0, 0.7) }.play

// LFPulse
{ SinOsc.ar(LFPulse.kr(3, 1, 0.3, 200, 200),0, 0.7) }.play
{ SinOsc.ar(LFPulse.kr(3, 1, 0.3, 2000, 200),0, 0.7) }.play


// LFOs can also perform at audio rate
{ LFPulse.ar(LFPulse.kr(3, 1, 0.3, 200, 200),0, 0.7) }.play
{ LFSaw.ar(LFSaw.kr(4, 0, 200, 400), 0, 0.7) }.play
{ LFTri.ar(LFTri.kr(4, 0, 200, 400), 0, 0.7) }.play
{ LFTri.ar(LFSaw.kr(4, 0, 200, 800), 0, 0.7) }.play




// 2) =========  Amplitude modulation  ==========


// AM and FM synthesis are different from LFO due to the low and high sidebands
// which appear when the modulation's frequency enters the audio rate ( > 20 Hz)

// In AM synthesis the modulator is unipolar (from 0 to 1) - so we add 1 to the wave
// and divide by 2 (i.e. multiply by 0.5)

Server.default = s = Server.internal;
s.boot;
FreqScope.new;

// AM Synthesis (new frequencies appear below and above the main freq)
// the sidebands are the sum and the difference of the carrier and the modulator frequency.
// (a 300 Hz carrier and 160 Hz modulator would generate 140 Hz and 460 Hz sidebands)

(
{
var modulator, carrier;
modulator = SinOsc.ar(MouseX.kr(2, 20000, 1), 0, mul:0.5, add:1);
carrier = SinOsc.ar(MouseY.kr(300,2000), 0, modulator);
carrier!2 
}.play
// interesting example of foldover happening in AM
)



(
// if there are harmonics in the wave being modulated, each of the harmonics will have
// sidebands as well. - Check the sawwave.
{
var modulator, carrier;
modulator = SinOsc.ar(MouseX.kr(2, 2000, 1), mul:0.5, add:1);
carrier = Saw.ar(533, modulator);
carrier!2 // the output
}.play

)

// here using .abs to calculate absolute values in the modulator:
// (this results in many sidebands
// try also using .cubed and other unitary operators on the signal.
(
{
var modulator, carrier;
modulator = SinOsc.ar(MouseX.kr(2, 20000, 1)).abs;
carrier = SinOsc.ar(MouseY.kr(200,2000), 0, modulator);
carrier!2 // the output
}.play
)




// 3) =========  Ring modulation  ==========


// Ring Modulation uses a bipolar modulation values (-1 to 1) whereas
// AM uses unipolar modulation values (0 to 1)

(
{
var modulator, carrier;
modulator = SinOsc.ar(MouseX.kr(2, 200, 1));
carrier = SinOsc.ar(333, 0, modulator);
carrier!2 // the output
}.play

)



// 4) =========  FM synthesis  ==========

// FM synthesis


{SinOsc.ar(400 + SinOsc.ar(MouseX.kr(2,2000,1), 0, MouseY.kr(1,1000)), 0, 0.5)!2}.play

// the same as above - with explanations:
(
{
SinOsc.ar(400 // the carrier and the carrier frequency
	+ SinOsc.ar(MouseX.kr(2,2000,1), // the modulator and the modulator frequency
		0, 						// the phase of the modulator
		MouseY.kr(1,1000) 			// the modulation depth (index)
		), 
0,		// the carrier phase 
0.5)		// the carrier amplitude
}.play

)

// as you can see, FM synthesis is a good example of chaotic, non-linear function


// for phase modulation, check out the PMOsc
// phase modulation and frequency modulation is very very similar
 
{ PMOsc.ar(MouseX.kr(300,900), 600, 3, 0, 0.1) }.play; // modulate carfreq

{ PMOsc.ar(300, MouseX.kr(300,900), 3, 0, 0.1) }.play; // modulate modfreq

{ PMOsc.ar(300, 550, MouseX.kr(0,20), 0, 0.1) }.play; // modulate index


// how does the PMOsc work? Let's check the source file (command-J or control-J)

PMOsc  {
	
	*ar { arg carfreq,modfreq,pmindex=0.0,modphase=0.0,mul=1.0,add=0.0; 
		^SinOsc.ar(carfreq, SinOsc.ar(modfreq, modphase, pmindex),mul,add)
	}
	
	*kr { arg carfreq,modfreq,pmindex=0.0,modphase=0.0,mul=1.0,add=0.0; 
		^SinOsc.kr(carfreq, SinOsc.kr(modfreq, modphase, pmindex),mul,add)
	}

}

/////////////////////////


// note the efficiency of FM compared to Additive synthesis:

// FM
{PMOsc.ar(1000, 1367, 12, mul: EnvGen.kr(Env.perc(0, 0.5), Impulse.kr(1)))}.play 
 
// compared to the Additive synthesis:
(
{ 
Mix.ar( 
 SinOsc.ar((1000 + (1367 * (-20..20))).abs,  // we're generating 41 oscillators (see *)
  mul: 0.1*EnvGen.kr(Env.perc(0, 0.5), Impulse.kr(1))) 
)}.play 
) 
 
// * run this line : 
(1000 + (1000 * (-20..20))).abs
// and see the frequency array that is mixed down with Mix.ar
// (I think this is an example from David Cope)


// Phase Modulation
// note how the modulator frequency is a ratio of the carrier frequency
// here that ratio is defined by MouseX 

(
{ var freq, ratio;
freq = LFNoise0.kr(4, 20, 60).round(1).midicps; 
ratio = MouseX.kr(1,4); 
SinOsc.ar(freq, 					// the carrier and the carrier frequency
		SinOsc.ar(freq * ratio, 	// the modulator and the modulator frequency
		0, 						// the phase of the modulator
		MouseY.kr(0.1,10) 			// the modulation depth (index)
		), 
0.5)		// the carrier amplitude
}.play

)

// same patch without the comments and modulator and carrier put into variables
(
{ var freq, ratio, modulator, carrier;

freq = LFNoise0.kr(4, 20, 60).round(1).midicps; 
ratio = MouseX.kr(1,4); 

modulator = SinOsc.ar(freq * ratio, 0, MouseY.kr(0.1,10));
carrier = SinOsc.ar(freq, modulator, 0.5);

carrier	
}.play

)


// Frequency Modulation
// same patch as above but here the frequency is modulated, not the phase
// (minor details)

// same patch without the comments and modulator and carrier put into variables
(
{ var freq, ratio, modulator, carrier;
freq = LFNoise0.kr(4, 20, 60).round(1).midicps; 
ratio = MouseX.kr(1,4); 

modulator = SinOsc.ar(freq * ratio, 0, MouseY.kr(0.1,10));
carrier = SinOsc.ar(freq + (modulator * freq), 0, 0.5);

carrier	
}.play
)


// let's fork it and create a perc env!
(
{
	
	40.do({
		{ 	var freq, ratio, modulator, carrier;
			// create an array of 12 midinotes, choose one, change it to cps and post it:
			freq = Array.fill(12, {arg i; 60 + i}).choose.midicps.postln; 
			ratio = MouseX.kr(0.5,2); 
			
			modulator = SinOsc.ar(freq * ratio, 0, MouseY.kr(0.1,10));
			carrier = SinOsc.ar(freq + (modulator * freq), 0, 0.5);
			
			carrier	* EnvGen.ar(Env.perc, doneAction:2)
		}.play;
		1.wait;
	});
}.fork
)




// two extra patches to play with:

// Frequency Modulation
(
var carrier, carFreq, carAmp, 	
modulator, modFreq, modAmp; 

carFreq = 2000; 
carAmp = 0.2;		
modFreq = 327; 
modAmp = 0.2; 

{
	modAmp = MouseX.kr(0, 1); 		// choose normalized range for modulation
	modFreq = MouseY.kr(1000, 10, 'exponential');
	modulator = SinOsc.ar( modFreq, 0, modAmp);			
	carrier = SinOsc.ar( carFreq + (modulator * carFreq), 0, carAmp);
	
	[ carrier, carrier, modulator ] // on OSX, you can .scope it and see 3 separate channels

}.play
)

// Phase Modulation
(
var carrier, carFreq, carAmp, 		// variables for a carrier
modulator, modFreq, modAmp; 		// and a modulator oscillator.

carFreq = 200; 			// initial parameters for both.
carAmp = 0.2;				// (needed if you decide to turn mouse control off.)
modFreq = 327; 
modAmp = 0.2; 

{ 
	modAmp = MouseX.kr(0, 7); 
	modFreq = MouseY.kr(1000, 10, 'exponential');
	modulator = SinOsc.ar(		// modulator is a Sine oscillator
		modFreq, 					
		0, 
		modAmp);			

	carrier = SinOsc.ar(
		carFreq, 
		modulator, 			// modulate the phase input of the SinOsc.
		carAmp);
	[ carrier, carrier, modulator * 0.2 ] 
}.play
)



// And finally we make a synthDef with FM synthesis, something that we
// can play from a say MIDI keyboard or tune with knobs and sliders:


( 
SynthDef(\fmsynth, {arg outbus = 0, freq=440, carPartial=1, modPartial=1, index=3, mul=0.2, ts=1;
	var mod, car, env;
	// modulator frequency
	mod = SinOsc.ar(freq * modPartial, 0, freq * index );
	// carrier frequency
	car = SinOsc.ar((freq * carPartial) + mod, 0, mul );
	// envelope
	env = EnvGen.ar( Env.perc(0.01, 1), doneAction: 2, timeScale: ts);
	Out.ar( outbus, car * env)
}).load(s);
)


Synth(\fmsynth, [ \outbus, 0, \freq, 600.0, \carPartial, 1.5, \ts, 1]);
Synth(\fmsynth, [ \outbus, 0, \freq, 600.0, \carPartial, 2.5, \ts, 2]);
Synth(\fmsynth, [ \outbus, 0, \freq, 600.0, \carPartial, 3.5, \ts, 2]);
Synth(\fmsynth, [ \outbus, 0, \freq, 600.0, \carPartial, 4.0, \ts, 2]);
Synth(\fmsynth, [ \outbus, 0, \freq, 300.0, \carPartial, 1.5, \ts, 2]);
Synth(\fmsynth, [ \outbus, 0, \freq, 600.0, \carPartial, 0.5, \ts, 2]);

Synth(\fmsynth, [ \outbus, 0, \freq, 600.0, \carPartial, 1.5, \modPartial, 1, \ts, 2]);
Synth(\fmsynth, [ \outbus, 0, \freq, 300.0, \carPartial, 1.5, \modPartial, 1, \ts, 2]);
Synth(\fmsynth, [ \outbus, 0, \freq, 400.0, \carPartial, 1.5, \modPartial, 1, \ts, 2]);
Synth(\fmsynth, [ \outbus, 0, \freq, 800.0, \carPartial, 1.5, \modPartial, 1, \ts, 2]);

Synth(\fmsynth, [ \outbus, 0, \freq, 600.0, \carPartial, 1.5, \modPartial, 1, \ts, 2]);
Synth(\fmsynth, [ \outbus, 0, \freq, 600.0, \carPartial, 1.5, \modPartial, 1.1, \ts, 2]);
Synth(\fmsynth, [ \outbus, 0, \freq, 600.0, \carPartial, 1.5, \modPartial, 1.15, \ts, 2]);
Synth(\fmsynth, [ \outbus, 0, \freq, 600.0, \carPartial, 1.5, \modPartial, 1.2, \ts, 2]);

