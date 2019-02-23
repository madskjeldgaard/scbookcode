

// =====================================================================
// - SuperCollider Basics -
// =====================================================================

// Tutorial 05 - Subtractive Synthesis Basics

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

// 	1) Noise sources
// 	2) Common filter types
// 	3) Bell synthesis



/* 
The idea of subtractive synthesis is to use filters to filter out frequencies from 
broadband sound sources (rich in frequencies). Whitenoise is the richest source as it 
distributes frequencies evenly across the spectra, but it might not fit for all purposes.
*/


// 1) =========  Noise sources ==========

// note that there is no frequency argument for the noise UGens.
// ... noise doesn't have a frequency!!!
// Check the helpfile to see what the argument stands for.

// whitenoise
{WhiteNoise.ar(1)}.plot(1)
{WhiteNoise.ar(1)}.play
{WhiteNoise.ar(1)}.scope
{WhiteNoise.ar(1)}.freqscope

// pinknoise 
{PinkNoise.ar(1)}.plot(1)
{PinkNoise.ar(1)}.play
{PinkNoise.ar(1)}.freqscope

// brownnoise
{BrownNoise.ar(1)}.plot(1)
{BrownNoise.ar(1)}.play
{BrownNoise.ar(1)}.freqscope

// take a look at the source file called Noise.sc (or hit Apple+Y on WhiteNoise)
// you will find lots of interesting noise generators. For example these:

{ Crackle.ar(XLine.kr(0.99, 2, 10), 0.4) }.freqscope.scope;

{ LFDNoise0.ar(XLine.kr(1000, 20000, 10), 0.1) }.freqscope.scope;

{ LFClipNoise.ar(XLine.kr(1000, 20000, 10), 0.1) }.freqscope.scope;

// impulse
{ Impulse.ar(80, 0.7) }.play
{ Impulse.ar(4, 0.7) }.play

// dust (random impulses)
{ Dust.ar(80) }.play
{ Dust.ar(4) }.play



// what's this?
{WhiteNoise.ar(1) * EnvGen.ar(Env.perc(0.001,0.5), doneAction:2)}.play

// is this better?
{LPF.ar(WhiteNoise.ar(1), 3300) * EnvGen.ar(Env.perc(0.001,0.5), doneAction:2)}.play

// ???
(
fork{
	100.do({
		{LPF.ar(WhiteNoise.ar(1), MouseX.kr(200,20000, 1)) 
			* EnvGen.ar(Env.perc(0.001,0.5), doneAction:2)}.play;
		1.wait;
	});
}
)



// ???
(
fork{
	100.do({
		{LPF.ar(Saw.ar(440), MouseX.kr(200,20000, 1)) 
			* EnvGen.ar(Env.perc(0.001,0.5), doneAction:2)}.play;
		1.wait;
	});
}
)

// ah, a whawha, ha ha!

{LPF.ar(Saw.ar(440), 440+(10000* SinOsc.kr(1, 0, 0.9, 1))) }.play;

{LPF.ar(WhiteNoise.ar(0.4), 440+(10000* SinOsc.kr(1.3, 0, 0.8, 1))) }.play;



// 2) ========= Common filter types ==========


// So let's go through the main filters:

// for those of you on linux or windows machines, you could use XLine instead of MouseX
// as in : XLine.kr(40,20000, 3)

// low pass filter
{LPF.ar(WhiteNoise.ar(0.4), MouseX.kr(40,20000,1)!2) }.play;

// low pass filter with XLine
{LPF.ar(WhiteNoise.ar(0.4), XLine.kr(40,20000, 3, doneAction:2)!2) }.play;

// high pass filter
{HPF.ar(WhiteNoise.ar(0.4), MouseX.kr(40,20000,1)!2) }.play;

// band pass filter (the Q is controlled by the MouseY)
{BPF.ar(WhiteNoise.ar(0.4), MouseX.kr(40,20000,1), MouseY.kr(0.01,1)!2) }.play;

// Mid EQ filter attenuates or boosts a frequency band
{MidEQ.ar(WhiteNoise.ar(0.024), MouseX.kr(40,20000,1), MouseY.kr(0.01,1), 24)!2 }.play;

// what's happening here?
(
{
var signal = MidEQ.ar(WhiteNoise.ar(0.4), MouseX.kr(40,20000,1), MouseY.kr(0.01,1), 24);
BPF.ar(signal, MouseX.kr(40,20000,1), MouseY.kr(0.01,1)) !2
}.play;
)

// resonant filter
{ Resonz.ar(WhiteNoise.ar(0.5), MouseX.kr(40,20000,1), 0.1)!2 }.play

// a short impulse won't resonate
{ Resonz.ar(Dust.ar(0.5), 2000, 0.1) }.play

// for that we use Ringz
{ Ringz.ar(Dust.ar(3, 0.3), MouseX.kr(200,6000,1), 2) }.play

// X is frequency and Y is ring time
{ Ringz.ar(Impulse.ar(4, 0, 0.3),  MouseX.kr(200,6000,1), MouseY.kr(0.04,6,1)) }.play

// what if we want to "resonate" many frequencies?

// hmm?
{ Ringz.ar(Dust.ar(3, 0.3), 440, 2) + Ringz.ar(Dust.ar(3, 0.3), 880, 2) }.play

// ah that's better (using only one Dust to trigger the ring filters):
(
{ 
var trigger, freq;
trigger = Dust.ar(3, 0.3);
freq = 440;
Ringz.ar(trigger, 440, 2, 0.3) 		+ 
Ringz.ar(trigger, freq*2, 2, 0.3) 	+ 
Ringz.ar(trigger, freq*3, 2, 0.3) !2
}.play
)

// but there is a better way:

// Klank is a bank of resonators like Ringz, but the frequency is fixed. (there is DynKlank)

{ Klank.ar(`[[800, 1071, 1153, 1723], nil, [1, 1, 1, 1]], Impulse.ar(2, 0, 0.1)) }.play;

// whitenoise input
{ Klank.ar(`[[440, 980, 1220, 1560], nil, [2, 2, 2, 2]], WhiteNoise.ar(0.005)) }.play;

// AudioIn input
{ Klank.ar(`[[220, 440, 980, 1220], nil, [1, 1, 1, 1]], AudioIn.ar([1])*0.001) }.play;



// Formlet is an interesting resonance filter
// NOTE: we use Impulse here as it is a very short impulse (containing all freqs)
{ Formlet.ar(Impulse.ar(4, 0.9), MouseX.kr(300,2000), 0.006, 0.1) }.play;

// or LFNoise0
{ Formlet.ar(LFNoise0.ar(4, 0.2), MouseX.kr(300,2000), 0.006, 0.1) }.play;



// 3) ========= Bell synthesis ==========

// let's make a bell sound using subtractive synthesis
(
{
var chime, freqSpecs, burst, harmonics = 10;
var burstEnv, burstLength = 0.001;

freqSpecs = `[
	{rrand(100, 1200)}.dup(harmonics), //freq array
	{rrand(0.3, 1.0)}.dup(harmonics).normalizeSum, //amp array
	{rrand(2.0, 4.0)}.dup(harmonics)]; //decay rate array

burstEnv = Env.perc(0, burstLength); //envelope times
burst = PinkNoise.ar(EnvGen.kr(burstEnv, gate: Impulse.kr(1))*0.3); //Noise burst

Klank.ar(freqSpecs, burst)
}.play
)





