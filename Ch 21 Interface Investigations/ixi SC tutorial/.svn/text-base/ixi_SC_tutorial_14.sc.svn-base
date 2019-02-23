

// =====================================================================
// - SuperCollider Basics -
// =====================================================================

// Tutorial 14 - Musical Patterns on SCServer

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

// 	1) Stepper and Select
// 	2) PulseCount and PulseDivider
// 	3) Demand UGens




// 1) ========= Stepper and Select ==========



// the stepper is a pulse counter that outputs a signal

// a scale of frequencies from 500 to 1600 in steps of 100 (as it's multiplied by 100)
{SinOsc.ar( Stepper.kr(Impulse.kr(10), 0, 4, 16, 1) * 100, 0, 0.2)}.play;

// we poll the Stepper to see the output
// and here the steps are -3 so there are more interesting step sequences
{SinOsc.ar(Stepper.kr(Impulse.kr(6), 0, 5, 15, -3).poll(6, "stepper") * 80, 0, 0.2)}.play;


// and here we use Lag (generating a line from the current value to the next in specified time) for the frequency
{SinOsc.ar(Lag.kr(Stepper.kr(Impulse.kr(6), 0, 5, 25, -4) * 90, 0.2), 0, 0.2)}.play;

// perhaps more understandable like this:
(
{
	SinOsc.ar( 		// the sine
		Lag.kr( 		// our lag
			Stepper.kr(Impulse.kr(6), 0, 5, 25, -4) * 90, // the stepper
			0.2),	// the time of the lag
		0,  			// phase of the sine
		0.2) 		// amplitude of the sine
}.play;
)


////////////////////////////////// select

(
{
	var scale, cycle;
	//scale = Array.fill(12,{ arg i; 60 + i }).midicps; // we fill an array with a scale
	scale = [60, 61, 63, 64, 65, 67, 68, 69, 70].midicps; // we fill an array with a scale
	cycle = scale.size / 2;

	SinOsc.ar(
			Select.kr( 
				LFSaw.kr(0.4, 1, cycle, cycle),
				scale
			)
	);
}.play;
)


////////////////////////////////// select and stepper together

// here we use the Stepper to do what LFSaw did above, it is just stepping through the pitchArray
// and not generating the pitches like in the Stepper examples above.

(
var pitchArray; //Declare a variable to hold the array
	//load the array with midi pitches
pitchArray = [60, 62, 64, 65, 67, 69, 71, 72].midicps; 
{
	SinOsc.ar(
		Select.kr(
			Stepper.kr(Impulse.kr(8), max: pitchArray.size-1), // try with Dust
			pitchArray),
		mul: 0.5)
}.play
)





// 2) ========= PulseCount and PulseDivider ==========



// we could also use PulseCount to get at the items of the array
(
{
	var scale, cycle;
	//scale = Array.fill(12,{ arg i; 60 + i }).midicps; // we fill an array with a scale
	scale = [60, 61, 63, 64, 65, 67, 68, 70].midicps; // we fill an array with a scale
	cycle = scale.size / 2;

	SinOsc.ar(
			Select.kr( 
				PulseCount.ar(Impulse.ar(scale.size), Impulse.ar(1)), // we go through the scale in 1 sec
				scale
			)
	);
}.play;
)

// PulseDivider is also an interesting UGen,
// it outputs an impulse when it has received a certain numbers of impulses

// here we use it to create a drummer in one synthdefinition.
// (quite primitive, and just for fun, but look at the CPU : )

(
SynthDef(\drummer, { arg out=0, tempo=4;
	var snare, base, hihat;
	tempo = Impulse.ar(tempo); // for a drunk drummer replace Impulse with Dust !!!

	snare = 	WhiteNoise.ar(Decay2.ar(PulseDivider.ar(tempo, 4, 2), 0.005, 0.5));
	base = 	SinOsc.ar(Line.ar(120,60, 1), 0, Decay2.ar(PulseDivider.ar(tempo, 4, 0), 0.005, 0.5));
	hihat = 	HPF.ar(WhiteNoise.ar(1), 10000) * Decay2.ar(tempo, 0.005, 0.5);
	
	Out.ar(out,(snare + base + hihat) * 0.4!2)
}).load(s);
)

a = Synth(\drummer);
a.set(\tempo, 6);
a.set(\tempo, 18);
a.set(\tempo, 180); // check the CPU! no increase.





// 3) ========= Demand UGens ==========


/*
In Tutorial 2 we saw how we could use Patterns to control the server.
Patterns are language-side streams used to control the server.

The Demand UGens are server side and don't need the SC language
So you could use this from languages like Python, Java, etc.

The Demand UGens follow the logic of the Pattern classes of the SCLang, 
so it will not be too alien to you by now. We will look at Patterns in the
next tutorial.
*/



(
{
	var freq, trig, reset, seq1;
	trig = Impulse.kr(10);
	seq1 = SinOsc.ar(2, mul: 200, add: 700); 
	freq = Demand.kr(trig, 0, seq1);
	SinOsc.ar(freq + [0,0.7]).cubed.cubed * 0.1;
}.play;
)

// same as above, but here we Demand more frequently and the sine is slower
// and we poll the freq
(
{
	var freq, trig, reset, seq1, trigrate;
	trigrate = 20;
	trig = Impulse.kr(trigrate);
	seq1 = SinOsc.ar(1, mul: 200, add: 700).poll(trigrate.reciprocal, "freq"); 
	freq = Demand.kr(trig, 0, seq1);
	SinOsc.ar(freq + [0,0.7]).cubed.cubed * 0.1;
}.play;
)

// Using LFSaw instead of a SinOsc
(
{
	var freq, trig, reset, seq1, seq2;
	trig = Impulse.kr(10);
	seq1 = LFSaw.ar(1, mul: 200, add: 700); 
	freq = Demand.kr(trig, 0, seq1);
	SinOsc.ar(freq + [0,0.7]).cubed.cubed * 0.1;
}.play;
)

// Using LFTri and now we use the mouse to control the mul and add of the Freq osc.
(
{
	var freq, trig, reset, seq1, seq2;
	trig = Impulse.kr(10);
	seq1 = LFTri.ar(1, mul: MouseX.kr(200,1000), add: MouseY.kr(200,1000)).poll(10.reciprocal, "freq"); 
	freq = Demand.kr(trig, 0, seq1);
	SinOsc.ar(freq + [0,0.7]).cubed.cubed * 0.1;
}.play;
)


// There are useful Ugens like Dseq and Drand (compare to Pseq and Prand)
(
{
	var freq, trig, reset, seq1, seq2;
	trig = Impulse.kr(10);
	seq1 = Drand([72, 75, 79, 82]-12, inf).midicps; 
	seq2 = Dseq([72, 75, 79, Drand([82,84,86])], inf).midicps; 
	freq = Demand.kr(trig, 0, [seq1, seq2]);
	SinOsc.ar(freq + [0,0.7]).cubed.cubed * 0.1;
}.play;
)

// Dseries
(
{ 
	var a, freq, trig;
	a = Dseries(0, 1.4, 20); // we build a series of values
	trig = Impulse.kr(MouseX.kr(1, 40, 1));
	freq = Demand.kr(trig, Impulse.kr(0.5), a) * 30 + 340; 
	SinOsc.ar(freq) * 0.1

}.play;
)
	

// and Dgeom
(
{ 
	var a, freq, trig;
	a = Dgeom(1, 1.4, 20); // we build a series of values
	trig = Impulse.kr(MouseX.kr(1, 40, 1));
	freq = Demand.kr(trig, Impulse.kr(0.5), a) * 30 + 340; 
	SinOsc.ar(freq) * 0.1

}.play;
)

// The Dbrown and Dibrown Ugens are good for random walk (drunken walk)
(
{ 
	var a, freq, trig;
	a = Dibrown(0, 20, 2, inf);
	trig = Impulse.kr(MouseX.kr(1, 40, 1));
	freq = Demand.kr(trig, 0, a) * 30 + 340; 
	SinOsc.ar(freq) * 0.1
}.play;
)


// Dwhite is whitenoise - not drunk anymore but jumping around madly
(
{ 
	var a, freq, trig;
	a = Diwhite(0, 15, inf);
	trig = Impulse.kr(MouseX.kr(1, 40, 1));
	freq = Demand.kr(trig, 0, a) * 30 + 340; 
	SinOsc.ar(freq) * 0.1

}.play;
)


// Using TDuty to demand results from demand rate UGens
(
{
	var minDur = 0.1, delta = 0.01;
	var trig = TDuty.ar(Dbrown(minDur, minDur+delta), 0, Dwhite(0, 1));
	Ringz.ar(trig, TRand.ar(2000,4050, trig), 0.1)
}.play
)
	
	
