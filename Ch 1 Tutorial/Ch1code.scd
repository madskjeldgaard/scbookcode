"Hello world"

Server.default = s = Server.internal.boot;

/////////////

play({SinOsc.ar(LFNoise0.kr(12, mul: 600, add: 1000), 0.3)})

/////////////

play({RLPF.ar(Dust.ar([12, 15]), LFNoise1.ar(1/[3, 4], 1500, 1600), 0.02)})

///////////// Figure 1.1 Example of additive synthesis

play({
	var sines = 100, speed = 6;
	Mix.fill(sines, 
		{arg x; 
			Pan2.ar(
				SinOsc.ar(x+1*100, 
					mul: max(0, 
						LFNoise1.kr(speed) +
						Line.kr(1, -1, 30)
					)
				), rand2(1.0))})/sines})

/////////////

rand(100)

exprand(1.0, 100.0) 

thisThread.randSeed = 666; {rand(10.0)} ! 10;

dup("echo", 20)

round([3.141, 5.9265, 358.98], 0.01)

sort([23, 54, 678, 1, 21, 91, 34, 78])

round(dup({exprand(1, 10)}, 100), 0.1)

sort(round(dup({exprand(1, 10)}, 100), 0.1))

/////////////

                            exprand(1.0, 1000.0)
                   dup({exprand(1.0, 1000.0)}, 100)
           sort(dup({exprand(1.0, 1000.0)}, 100))
round(sort(dup({exprand(1.0, 1000.0)}, 100)), 0.01)

///////////// Figure 1.3 Fortuitous futuristic nested music.

(
play(
	{
		CombN.ar(
			SinOsc.ar(
				midicps(
					LFNoise1.ar(3, 24, 
						LFSaw.ar([5, 5.123], 0, 3, 80)
					)
				), 
				0, 0.4), 
			1, 0.3, 2)
	}
)
)

/////////////

[45, 13, 10, 498, 78].sort // collection of items, do this; sort yourself

"echo".dup(20) // echo, do this; repeat yourself 20 times

50.midicps // the number 50, do this; convert yourself into Hz

444.cpsmidi // 444, do this; convert yourself into a midi number

100.rand // 100, do this; pick a number between 0 and yourself

{100.rand}.dup(50) // random picking function, do this; repeat 50 times

[1.001, 45.827, 187.18].round(0.1) // collection of items; round to 0.1

// Mac users only, sorry
"I've just picked up a fault in the AE35 unit".speak // Hal; plot to kill me

/////////////

1000.0 // a number

1000.0.rand // choose a number between 0 and 1000

1000.0.rand.round(0.01) // choose a number and round it

1000.0.rand.round(0.01).post // choose, round, then post

{1000.0.rand.round(0.01).postln}.dup(100).plot // choose, round, dup, plot

{1000.0.rand.round(0.01).postln}.dup(100).postln.sort.plot // choose etc., sort, plot

//Mac only
1000.0.rand.round(0.01).postln.asString.speak // convert to string and speak

5 + 10 * 4

[0, 11, 10, 1, 9, 8, 2, 3, 7, 4, 6, 5].reverse // retrograde of a 12-tone row

12 - [0, 11, 10, 1, 9, 8, 2, 3, 7, 4, 6, 5].reverse // retrograde inversion

[0, 2, 4, 5, 6, 7, 9, 11].scramble // diatonic scale

[60, 62, 64, 67, 69].mirror // pentatonic

[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11].rotate

[60, 62, 64, 65, 67, 69, 71].midicps.round(0.1) // convert midi to frequency in Hz

[1, 0.75, 0.5, 0.25, 0.125].choose // maybe durations?

0.125 * [1, 2, 3, 4, 5, 6, 7, 8].choose // multiples of a smallest quantize value

[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11].permute(6) 

/////////////

exprand(1, 1000.0)

{exprand(1, 1000.0)}

dup(rand(1000.0), 5)  // picks a number, duplicates it

dup({rand(1000.0)}, 5)  //duplicates the function of picking a number

// essentially, this (which has a similar result) 
[rand(1000.0), rand(1000.0), rand(1000.0), rand(1000.0), rand(1000.0)]

/////////////

{LFNoise0.ar}.play  //play a series of random numbers

{LFNoise0.ar(10000)}.plot // plot those numbers

{LFNoise0.ar(10000)}.scope // play and show on a scope

{100.rand}.dup(10) // pick 10 random numbers

{100.rand} ! 10 // same as above

{100.rand}.dup(10).postln.plot // pick 10 numbers, post, then plot them

{100.rand}.dup(100).sort.plot // pick 100 numbers, sort them, then plot

/////////////

{Blip.ar(25, LFNoise0.kr(5, 12, 14), 0.3)}.play // single channel

{Blip.ar(25, LFNoise0.kr([5, 10], 12, 14), 0.3)}.play // stereo

{Blip.ar(25, LFNoise0.kr([5, 10, 2, 25], 12, 14), 0.3)}.play // quad

{Blip.ar(25, LFNoise0.kr([5, 4, 7, 9, 5, 1, 9, 2], 12, 14), 0.3)}.play // 8

/////////////

{PMOsc.ar(440, 550, 7)}.play // pretty boring, like an FM radio

{PMOsc.ar(440, MouseY.kr(1, 550), MouseX.kr(1, 15))}.play

{PMOsc.ar(100, 500, 10, 0, 0.5)}.play // all arguments listed in order

{PMOsc.ar(carfreq: 100, modfreq: 500, pmindex: 10, mul: 0.5)}.play // keywords

{PMOsc.ar(carfreq: 100, mul: 0.5, pmindex: 10, modfreq: 500)}.play // mixed

{SinOsc.ar(mul: MouseX.kr(0, 1.0))}.scope

///////////// Figure 1.4 VCO, VCF, VCA

(
{
	Blip.ar(
		TRand.kr( // frequency or VCO
			100, 1000, // range
			Impulse.kr(Line.kr(1, 20, 60))), // trigger
		TRand.kr( // number of harmonics or VCF
			1, 10, // range
			Impulse.kr(Line.kr(1, 20, 60))), // trigger
		Linen.kr( // mul, or amplitude, VCA
			Impulse.kr(Line.kr(1, 20, 60)), // trigger
			0, // attack
			0.5, // sustain level
			1/Line.kr(1, 20, 60)) // trigger
		)
}.play
)

/////////////

(
a = 440;
b = 3;
c = "math operations";
[c, a, b, a*b, a + b, a.pow(b), a.mod(b)]
)

// same as
["math operations", 440, 3, 440*3, 440 + 3, 440.pow(3), 440.mod(3)]

/////////////

(
{
r = MouseX.kr(1/3, 10);
SinOsc.ar(mul: Linen.kr(Impulse.kr(r), 0, 1, 1/r))
}.play
)

///////////// Example 1.5 Synthesis example with variables and statements

(
// run this first
p = { // make p equal to this function
r = Line.kr(1, 20, 60); // rate
// r = LFTri.kr(1/10) * 3 + 7;
t = Impulse.kr(r); // trigger
// t = Dust.kr(r);
e = Linen.kr(t, 0, 0.5, 1/r); // envelope uses r and t
f = TRand.kr(1, 10, t); // triggered random also uses t
// f = e + 1 * 4;
Blip.ar(f*100, f, e) // f, and e used in Blip
}.play
)

p.free;  // run this to stop it

///////////// Figure 1.6 Phase modulation with modulator as ratio

(
{ // carrier and modulator not linked
	r = Impulse.kr(10);
	c = TRand.kr(100, 5000, r);
	m = TRand.kr(100, 5000, r);
	PMOsc.ar(c, m, 12)*0.3
}.play
)

(
{
	var rate = 4, carrier, modRatio; // declare variables
	carrier = LFNoise0.kr(rate) * 500 + 700;
	modRatio = MouseX.kr(1, 2.0);
	// modulator expressed as ratio, therefore timbre
	PMOsc.ar(carrier, carrier*modRatio, 12)*0.3
}.play
)

/////////////

{SinOsc.ar}.play // generates a temp__reallyLongNumber name

// names the synth and output bus 0 (left) explicitly
SynthDef("sine", {Out.ar(0, SinOsc.ar)}).play

SynthDef("sine", {Out.ar(1, SinOsc.ar)}).play // right channel

// or

(
SynthDef("one_tone_only", {
	var out, freq = 440;
	out = SinOsc.ar(freq);
	Out.ar(0, out)
}).play
)

// then to play it

Synth("one_tone_only");

/////////////

(
SynthDef("different_tones", {
	arg freq = 440; // declare an argument and give it a default value
	var out; 
	out = SinOsc.ar(freq)*0.3;
	Out.ar(0, out)
}).play
)

/////////////

// Run all four, then stop all 

Synth("different_tones", ["freq", 550]);

Synth("different_tones", [\freq, 660]); // same as "freq"

Synth("different_tones", ["freq", 880]);

// If no argument is specified, defaults are used (440)
Synth("different_tones")

/////////////

a = Synth("different_tones", ["freq", 64.midicps]);

b = Synth("different_tones", ["freq", 67.midicps]);

c = Synth("different_tones", ["freq", 72.midicps]);

a.set("freq", 65.midicps);

c.set("freq", 71.midicps);

a.set("freq", 64.midicps); c.set("freq", 72.midicps);

a.free; 

b.free; 

c.free;

///////////// Figure 1.7 Synth definition

(
//run this first
SynthDef("PMCrotale", {
arg midi = 60, tone = 3, art = 1, amp = 0.8, pan = 0; 
var env, out, mod, freq;

freq = midi.midicps;
env = Env.perc(0, art);
mod = 5 + (1/IRand(2, 6));

out = PMOsc.ar(freq, mod*freq, 
	pmindex: EnvGen.kr(env, timeScale: art, levelScale: tone), 
	mul: EnvGen.kr(env, timeScale: art, levelScale: 0.3));

out = Pan2.ar(out, pan);

out = out * EnvGen.kr(env, timeScale: 1.3*art, 
	levelScale: Rand(0.1, 0.5), doneAction:2);  
Out.ar(0, out); //Out.ar(bus, out);

}).add;
)

//Then run this a bunch of times:

Synth("PMCrotale", ["midi", rrand(48, 72).round(1), "tone", rrand(1, 6)])


~houston = Buffer.read(s, "sounds/a11wlk01-44_1.aiff");

~chooston = Buffer.read(s, "sounds/a11wlk01.wav");

{PlayBuf.ar(1, ~houston)}.play; // number of channels and buffer.

{PlayBuf.ar(1, ~chooston)}.play; // number of channels and buffer.

///////////// Figure 1.8 Playback buffers

[~houston.bufnum, ~houston.numChannels, ~houston.path, ~houston.numFrames];

[~chooston.bufnum, ~chooston.numChannels, ~chooston.path, ~chooston.numFrames];

(  // phasing
{
	var rate, trigger, frames;
	frames = ~houston.numFrames; // or use ~chooston.numFrames

	rate = [1, 1.01];
	trigger = Impulse.kr(rate);
	PlayBuf.ar(1, ~houston, 1, trigger, frames * Line.kr(0, 1, 60)) * 
	EnvGen.kr(Env.linen(0.01, 0.96, 0.01), trigger) * rate;
}.play;
)

/////////////

(  // speed and direction change
{
	var speed, direction;
	speed = LFNoise0.kr(12) * 0.2 + 1;
	direction = LFClipNoise.kr(1/3);
	PlayBuf.ar(1, ~houston, (speed * direction), loop: 1);
}.play;
)

///////////// Figure 1.9 Connecting controls with a bus

(
// if these haven't been used they will hold 0
~kbus1 = Bus.control; // a control bus
~kbus2 = Bus.control; // a control bus
{
	var speed, direction;
	speed = In.kr(~kbus1, 1) * 0.2 + 1;
	direction = In.kr(~kbus2);
	PlayBuf.ar(1, ~chooston, (speed * direction), loop: 1);
}.play;
)

(
// Start the controls
{Out.kr(~kbus1, LFNoise0.kr(12))}.play;

{Out.kr(~kbus2, LFClipNoise.kr(1/4))}.play;
)

// Start the second buffer with the same control input buses, 
// but send it to the right channel using Out.ar(1 etc.

(
{
	var speed, direction;
	speed = In.kr(~kbus1, 1) * 0.2 + 1;
	direction = In.kr(~kbus2);
	Out.ar(1, PlayBuf.ar(1, ~houston, (speed * direction), loop: 1));
}.play;
)

/////////////

~kbus3 = Bus.control; // a control bus

~kbus4 = Bus.control; // a control bus
  // run these one at a time, (turn down the speakers!)

{Out.kr(~kbus3, SinOsc.kr(3).scope("out3") * 100)}.play; 

{Out.kr(~kbus4, LFPulse.kr(1/3).scope("out4") * 200)}.play;

{Out.ar(0, SinOsc.ar(In.kr(~kbus3) + In.kr(~kbus4) + 440).scope("left"))}.play;

{Out.ar(1, SinOsc.ar(In.kr(~kbus3) + In.kr(~kbus4) + 880).scope("right"))}.play;

~kbus3 = Bus.control; // a control bus 

~kbus4 = Bus.control; // a control bus 

{Out.kr(~kbus3, SinOsc.kr(3).range(340, 540))}.play;

{Out.kr(~kbus4, LFPulse.kr(6).range(240, 640))}.play;

SynthDef("Switch", {arg freq = 440; Out.ar(0, SinOsc.ar(freq, 0, 0.3)) }).add;

x = Synth("Switch"); // default

x.map(\freq, ~kbus3)

x.map(\freq, ~kbus4)

///////////// Figure 1.10 Buffer modulation

(
{
	Out.ar(0,
		Pan2.ar( PlayBuf.ar(1, ~houston, loop: 1) * 
			SinOsc.ar(LFNoise0.kr(12, mul: 500, add: 600)),
		0.5)
	)
}.play
)

(
{
var source, delay; 
	source = PlayBuf.ar(1, ~chooston, loop: 1);
	delay =  AllpassC.ar(source, 2, [0.65, 1.15], 10);
	Out.ar(0,
	Pan2.ar(source) + delay
	)
}.play
)

///////////// Figure 1.11 FX routing using buses.

// Create and name buses 
~delay = Bus.audio(s, 2);
~mod = Bus.audio(s, 2);
~gate = Bus.audio(s, 2);
~k5 = Bus.control;

{Out.kr(~k5, LFNoise0.kr(4))}.play; // start the control

// Start the last item in the chain, the delay
{Out.ar(0, AllpassC.ar(In.ar(~delay, 2), 2, [0.65, 1.15], 10))}.play 

// Start the next to last item, the modulation
{Out.ar(~delay, In.ar(~mod, 2) * SinOsc.ar(In.kr(~k5)*500 + 1100))}.play

// Start the third to last item, the gate
{Out.ar([0, ~mod], In.ar(~gate, 2) * max(0, In.kr(~k5)))}.play

// Start one buffer
{Out.ar(~gate, Pan2.ar(PlayBuf.ar(1, ~houston, loop: 1), 0.5))}.play;

// Start the other
{Out.ar(~gate, Pan2.ar(PlayBuf.ar(1, ~chooston, loop: 1), -0.5))}.play

/////////////

a = ["C", "C#", "D",  "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B "];

a.at(8);

"Item at index 5 is: ".post; a.at(5).postln; // why didn't it print E?

"Item at index 0 is: ".post; a.at(0).postln; // because we start with 0

do(50, { [0, 2, 4, 5, 7, 9, 11].at(7.rand).postln})

do(50, { ["C", "D", "E", "F", "G", "A", "B"].at(7.rand).postln})

///////////// 

Task({
	50.do({
		["C", "D", "E", "F", "G", "A", "B"].at(7.rand).postln;
		1.wait;
	});
}).play

///////////// Figure 1.12 Random MIDI walk.

Task({
a = ["C", "C#", "D",  "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"];
"count, midi, pitch, octave".postln;
	do(50, {arg count;
		p = rrand(36, 72);
		[count, p, a.wrapAt(p), (p/12).round(1) - 1].postln;
	1.wait;
	})
}).play

///////////// 1.13 Random Crotale Walk

// This uses the PMCrotale synth definition
(
a = ["C", "C#", "D",  "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"];
"event, midi, pitch, octave".postln;
r = Task({
	inf.do({ arg count;
		var midi, oct, density;
		density = 1.0; // 100% of the time. Uncomment below for 70%, etc.
		// density = 0.7; 
		// density = 0.3;
		midi = [0, 2, 4, 7, 9].choose;
		// midi = [0, 2, 4, 5, 7, 9, 11].choose;
		// midi = [0, 2, 3, 5, 6, 8, 9, 11] .choose; 
		// midi = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11] .choose;
		oct = [48, 60, 72].choose;
		if(density.coin, 
			{ // true action
				"".postln;
				[midi + oct, a.wrapAt(midi), 
				(oct/12).round(1)].post;
				Synth("PMCrotale", 
					["midi", midi + oct, "tone", rrand(1, 7), 
					"art", rrand(0.3, 2.0), "amp", rrand(0.3, 0.6), "pan", 1.0.rand2]);
			}, {["rest"].post}); // false action
		0.2.wait; 
	}); 
}).start
)

r.stop; // run this to stop

/////////////

if(10 == 10, {"10 is indeed equal to 10"}, {"false"})

if((1 < 20).and(1.isInteger), {"1 is less than 20"}, {"false"})

10.do({arg count; [count, if(count.odd, {"odd"}, {"even"})].postln})

(
84.do({arg count; if([0, 4, 7].includes(count%12), 
	{count.post; " is part of a C triad.".postln}, 
	{count.post; " is not part of a C traid".postln})})
)

50.do({if(1.0.rand.round(0.01).post > 0.5,  {" > 0.5".postln}, {" < 0.5".postln})})

50.do({if(1.0.rand > 0.5,  {"play a note".postln}, {"rest".postln})})

50.do({if(0.5.coin, {"play a note".postln}, {"rest".postln})}) // same as above

if((10.odd).or(10 < 20), {"true".postln}, {"false".postln})

/////////////

Array.fill(100, {wchoose([1, 2, 3, 4], [0.5, 0.3, 0.125, 0.075])}).sort

/////////////

[0, 2, 4, 5, 7, 9, 11].do({arg each, count; ["count", count, "each", each].postln})  
// same
[0, 2, 4, 5, 7, 9, 11].do({arg whatever, blech; [blech, whatever].postln})

(
var pc;
pc = ["C", "C#", "D",  "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"];
[0, 2, 4, 5, 7, 9, 11].do({arg each; pc.wrapAt(each).postln;})
)

///////////// Figure 1.14 Nested do to generate a 12-tone matrix.

(
var row, inversion, pitchClass;
row = Array.series(11, 1).scramble.insert(0, 0); 
// or enter your own row, e.g. Webern's Op 27
// row = [0, 11, 8, 2, 1, 7, 9, 10, 4, 3, 5, 6]; 
row.postln;
inversion = 12 - row;
// I add spaces to the strings for a tidy row
pitchClass = ["C  ", "C# ", "D  ", "Eb ", 
	"E  ", "F  ", "F# ", "G  ", "Ab ", "A  ", "Bb ", "B  "];
inversion.do({arg eachInv;
	var trans;
	trans = (row + eachInv); 
	// prints just pitch class
	trans.do({arg scaleDegree; pitchClass.wrapAt(scaleDegree).post});
	//"".postln; // uncomment these line if you want to do both
	// prints just numbers
	//trans.do({arg scaleDegree; (scaleDegree%12).post; " ".post});
	"".postln;
	});
"".postln
)

/////////////

{LFNoise1.ar(5000)}.plot // random wave 

{max(0, LFNoise1.ar(5000))}.plot // return only positive values

(
{
var ampCont;
ampCont = max(0, LFNoise1.kr(12)); // slow it down for LFO control
SinOsc.ar(440, mul: ampCont)
}.scope
)

///////////// Figure 1.15 Example of additive synthesis

// Mix down a few of them tuned to harmonics:

(
{
var fund = 220;
Mix.ar(
	[
	SinOsc.ar(220, mul: max(0, LFNoise1.kr(12))),
	SinOsc.ar(440, mul: max(0, LFNoise1.kr(12)))*1/2,
	SinOsc.ar(660, mul: max(0, LFNoise1.kr(12)))*1/3,
	SinOsc.ar(880, mul: max(0, LFNoise1.kr(12)))*1/4,
	SinOsc.ar(1110, mul: max(0, LFNoise1.kr(12)))*1/5,
	SinOsc.ar(1320, mul: max(0, LFNoise1.kr(12)))*1/6
	]
)*0.3
}.play
)

///////////// Figure 1.16 Example of additive synthesis

// Try this first
Array.fill(20, {arg cnt; cnt + 1*110}); // harmonics built on 440

// And a patch
(
{Mix.ar(
	Array.fill(12, 
		{arg count; 
		var harm;
		harm = count + 1 * 110; // remember precedence; count + 1, then * 110
			SinOsc.ar(harm, 
				mul: max([0, 0], SinOsc.kr(count+1/4))
				)*1/(count+1)
		})
)*0.7}.play
)

///////////// Figure 1.18 Physically modeled bells. 

// Try this first, to illustrate the array of arrays.

Array.fill(3, {Array.fill(10, {rand(1000)})})	
// Then this patch.
(
{
var scale, specs, freqs, amps, rings, 
	numRes = 5, bells = 20, pan; 
scale = [60, 62, 64, 67, 69].midicps;
	Mix.fill(bells, {
		freqs = Array.fill(numRes, {rrand(1, 15)*(scale.choose)});
		amps = Array.fill(numRes, {rrand(0.3, 0.9)});
		rings = Array.fill(numRes, {rrand(1.0, 4.0)});
		specs = [freqs, amps, rings].round(0.01);
		// specs.postln;
		pan = (LFNoise1.kr(rrand(3, 6))*2).softclip;
		Pan2.ar( 
			Klank.ar(`specs, 
				Dust.ar(1/6, 0.03)), 
				pan)
	})
}.play;
) 

///////////// Figure 1.19 Generative sequences using arrays

( // first define the synth

SynthDef.new("SimpleBlip", { 
arg midi = 60, tone = 10, art = 0.125, amp = 0.2, pan = -1;
var out, temper;	
out =	Pan2.ar(
			Blip.ar( // play the sequence
				midi.midicps,
				tone
				) * EnvGen.kr(Env.perc(0.01, art)),
		pan // pan left, center, or right
	);
DetectSilence.ar(out, doneAction:2);
amp = amp - ((midi - 60) * 0.02);
Out.ar(0, out*amp)
	}).add;
)

/////////////
 
(
// Then run this Task
~inst = [0, 0, 0]; // Three containers for tasks
~pSeq = [0, 0, 0]; // Three containers for sequences
~scaleAdd = [4, 5, 11, nil, 10, 3, 6, nil]; // 
~notes = 
[" C", " C#", " D", " Eb", " E", " F", 
	" F#", " G", " Ab", " A", " Bb", " B"]; 
~rout = Task({
	inf.do({
		arg cnt1; 
		var steps, durSeq, harmSeq;
		steps = rrand(6, 12);
		if(cnt1%6 == 0, // every sixth iteration, add a degree
			{~scale = ~scale.add(~scaleAdd.wrapAt((cnt1/6).round(1) - 1));}); 
		"\nIteration: ".post; cnt1.post; 
		[" (center) ", " (right) ", " (left) "].wrapAt(cnt1).postln;
		if(cnt1%24 == 0, // reset all three
			{~scale = [0, 2, 7, 9]; 
			3.do({arg cnt2; 
				~pSeq.wrapPut(cnt2, 
					Array.fill(steps, 
						{~scale.choose + [48, 60].choose}))})});
		"scale: ".post; ~scale.postln;
		~pSeq.wrapPut(cnt1, // fill array with scale steps
			Array.fill(steps, {~scale.choose + [48, 60].choose}));
		"MIDI seq: ".post; (~pSeq.wrapAt(cnt1)%12).postln;
		"Sequence (notes): ".post; 
		~pSeq.wrapAt(cnt1).do( // print the sequence using note names
			{arg thisItem; ~notes.at(thisItem%12).post}); 
		"".postln;
		// create harmonic and duration arrays
		harmSeq = Array.fill(steps, {rrand(1.0, 5.0)});
		durSeq = Array.fill(steps - 1, {rrand(0.01, 0.9)});
		// stop the previous task at this array position
		~inst.wrapAt(cnt1).stop;
		~inst.wrapPut(cnt1, 
			Task({
				inf.do({arg cnt3; // each sequence
					Synth("SimpleBlip", 
						[\midi, ~pSeq.wrapAt(cnt1).wrapAt(cnt3), 
						\tone, harmSeq.wrapAt(cnt3), 
						\art, durSeq.wrapAt(cnt3), 
						\amp, rrand(0.1, 0.3), 
						\pan, cnt1.wrap(-1, 2)]);
					0.125.wait; // tempo of each note
				})}).start;
		);
		12.wait;})
}).start; // time between each new sequence
)

~rout.stop; // stop new sequences

~inst.at(0).free; // at any time, stop center sequence

~inst.at(1).free; // stop right sequence

~inst.at(2).free; // stop center sequence

/////////////

Server.internal.prepareForRecord; // or Server.local

Server.internal.record;

Server.internal.stopRecording;

// Run these variations before to change file formats.
Server.internal.recSampleFormat = "int16"; // change format

Server.internal.recChannels = 1; // change from the default stereo
// saves file in the SC folder, will be overwritten if repeated

Server.internal.prepareForRecord("myAudio.aif"); 

///////////// Figure 1.20 Offset and scale

(
	{
	var trigger, wave, label, scale, offset;
	trigger = Impulse.kr(10);
	wave = SinOsc.kr(1/10) ; // change to 400
	scale = 1; offset = 0;
//	wave = wave * scale + offset; 
	label = "scale = " ++ scale.asString ++ ", offset = " ++ offset.asString;
	wave.round(0.01).poll(label: label);
	}.scope(1)
)

///////////// Figure 1.21 SinOsc offset and scaled for control

(
	{
	var trigger, control, scale, offset;
	scale = 300; // try other values, but not greater than offset
	offset = 600; // try other values
	trigger = Impulse.kr(10);
	control = SinOsc.ar(1/4).scope("control"); // LFO
	control = control  * scale + offset;
	SinOsc.ar(freq: abs(control).poll).scope("audio")
	}.play
) 

// Speaking of vibrato, I can't resist this faux Theremin. Play it with the Mouse: 

{SinOsc.ar(SinOsc.ar(8, 0, 10, MouseX.kr(400, 1000, 1)))}.play

///////////// Figure 1.22 Test you skills on a patch

(
{
	var carrier, rate, trigger, modRatio, index, control, env;
	rate = 3;
	trigger = Impulse.kr(rate);
	control = LFNoise0.kr(rate);
	carrier = 62;
	modRatio = 4.125;
	index = 10;
	carrier = carrier.midicps;
	carrier.poll(trigger, "carrier"); 
	index.poll(trigger, "index"); 
	modRatio.poll(trigger, "modRatio");
	PMOsc.ar(carrier, carrier*modRatio, index)
}.play
)

///////////// Figure 1.23 PMOsc with offset and scale. 

(
a = SynthDef("PMOsc_ex", 
{
	arg left = 10, right = 10, indexLow = 4, indexHigh = 12;
	var pitch, timbre, trigger, env, index, out;
	trigger = Impulse.kr([left, right]); // trigger
	pitch = TRand.kr(36, 72, trigger).round(1); // C2 to C6
	timbre = LFNoise0.kr(1/20, mul: 0.2, add: 2); // mod control
	env = Linen.kr(trigger, releaseTime: 1/[left, right]); // envelope
	index = env * indexHigh + indexLow; // env scaled and offset for index
	pitch = pitch.midicps; // midi converted to freq
	out = PMOsc.ar(pitch, pitch*timbre, index, mul: env);
	Out.ar(0, out);
}).play
)

a.set("left", 4)

a.set("right", 5)

a.set("indexLow", 1)

a.set("indexHigh", 4)

///////////// Figure 1.24 PMOsc with sample and hold (latch).

(
// run this first
a = SynthDef("Latch_demo",
{
arg rate = 9; 
var freq, latchrate, index, ratio, env, out;
latchrate = rate*LFNoise0.kr(1/10, mul: 0.03, add: 1.6);
index = Latch.kr(
	LFSaw.kr(latchrate, mul: 5, add: 6),
	Impulse.kr(rate)
	);
freq = Latch.kr(
	LFSaw.kr(latchrate, 
	mul: max(0, LFNoise1.kr(1/5, mul: 24, add: 10)), 
	add: LFNoise0.kr(1/7, mul: 12, add: 60)),
	Impulse.kr(rate)
	).round(1).midicps;

ratio = LFNoise1.kr(1/10, mul: 2.0, add: 5.0);

env = EnvGen.kr(
	Env.perc(0, LFNoise0.kr(rate, mul: 1, add: 1.5)/rate), 
	Impulse.kr(rate), 
	LFNoise1.kr([5, 5], 2, 1).max(0).min(0.8));
out = PMOsc.ar(
	[freq, freq * 1.5],
	freq*ratio,
	index,
	mul: env
);
Out.ar(0, out);
}
).play
)

a.set("rate", 10)

a.set("rate", 15)

a.set("rate", 6)

a.free;

///////////// Figure 1.25 It's just a bell.

(
{ // it's just a bell
var burst, burstEnv, bell, delay, dry, 
burstFreq = 500, freqs, amps, rings;
burstEnv = EnvGen.kr(Env.parc(0, 0.05),
				Dust.kr(1/5), 0.1);
// burstEnv.poll(100, "env");
burst = SinOsc.ar(freq: burstFreq,
	mul: burstEnv);
// burst.poll(100, "burst");
freqs = Array.fill(10, {exprand(100, 1000)});
amps = Array.fill(10, {rrand(0.01, 0.1)});
rings = Array.fill(10, {rrand(1.0, 6.0)});
// [freqs, amps, rings].round(0.01).postln;
// "safe" values
// freqs = [100, 200, 300, 400];
// amps = [1, 1, 1, 1];
// rings = [1, 1, 1, 1];

bell = Pan2.ar(
	Klank.ar(`[freqs, amps, rings], burst), 
	rrand(-1.0, 1.0)
);

delay = AllpassN.ar(bell, 2.5, 
	[LFNoise1.kr(7, 1.5, 1.6), LFNoise1.kr(7, 1.5, 1.6)], 
	1, mul: 0.8);
bell 
+ delay
// + SinOsc.ar(mul: LFPulse.kr(1) * 0.05);
}.play
)

