

// =====================================================================
// - SuperCollider Basics -
// =====================================================================

// Tutorial 08 - Buffers and Samples

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

//	1) Allocating a buffer
// 	2) Reading a buffer
//	3) Streaming a buffer
// 	4) Record into buffer
// 	5) Fill a buffer
//	6) Pitch and Time changes in playback
// 	7) Using BufWr and BufRd






// 1) =========  Allocating a buffer  ==========

/* 
A buffer is a globally available array of floating point numbers stored
on the server. It can hold all kinds of data, most typically sampled audio.
*/

// we create a buffer: (server, frames, number of channels)
// (allocating space on the server for the information to enter the buffer)
b = Buffer.alloc(s, 44100 * 8.0, 2); // 4 seconds of sound on a 44100 Hz system, 2 channels

// in the post window we get this information:
//  - > Buffer(10, 352800, 2, 44100, nil) // bufnum, samples, channels, sample-rate, path

b = Buffer.alloc(s, 44100 * 8.0, 4); // 2 seconds of sound on a 44100 Hz system, 4 channels

// and we can get to this information by calling the server:
b.bufnum.postln;

// we can check the number of frames (samples)
b.numFrames.postln;

// and the number of channels
b.numChannels.postln;

//which means that the following should give us the length of the sample in seconds:
(b.numFrames / b.numChannels ) / 44100

// to free the buffer from the server:
b.free;

// Buffers are loaded into RAM, so it depends on your system how many you can load.

// open the terminal, type top, run the server and then run this line.
a = Array.fill(10, {Buffer.alloc(s,44100 * 8.0, 2)});

// you see how the memory of scsynth increases

// now, run the following line and the memory is de-allocated.
10.do({arg i; a[i].free;})

// or simply
a.collect(_.free;)

///////////// sidestep.... try this:
a = [1,2,3,4,5,6];
b = a.collect(_ + 100);
("a is : "+a).postln;
("b is : "+b).postln;
//////////////////////




// 2) =========  Reading a buffer  ==========

b = Buffer.read(s, "sounds/a11wlk01.wav");
b.bufnum.postln; // let's check its bufnum

// now play it
(
SynthDef(\playBuf,{ arg out = 0, bufnum;
	var signal;
	signal = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum));
	Out.ar(out, signal ! 2)
}).load(s)
)
x = Synth(\playBuf, [\bufnum, b.bufnum]) // we pass in the number of the buffer

x.free; // free the synth 
b.free; // free the buffer
		
// for many buffers, the typical thing to do is to load them into an array:
b = Array.fill(10, {Buffer.read(s, "sounds/a11wlk01.wav")});

// and then we can access it from the index in the array
x = Synth(\playBuf, [\bufnum, b[2].bufnum])




// 3) =========  Streaming a buffer  ==========


// To use longer files (say you want to create a sequencer like Logic)
// it is better to use DiskIn (which reads the file from disk, ie. not loaded into RAM)

// Then we still need a buffer (but we are cueing it, i.e. not filling)
b = Buffer.cueSoundFile(s, "sounds/a11wlk01-44_1.aiff", 0, 1);

(
SynthDef(\playcuedBuf,{ arg out = 0, bufnum;
	var signal;
	signal = DiskIn.ar(1, bufnum);
	Out.ar(out, signal ! 2)
}).load(s)
)

x = Synth(\playcuedBuf, [\bufnum, b.bufnum], s)

// NOTE: As of July 2007, you can also just pass the buffer and
// not the bufnum to the DiskIn or the PlayBuf. 
// See: http://www.create.ucsb.edu/pipermail/sc-users/2007-July/035622.html




// 4) =========  Recording into a buffer  ==========

b = Buffer.alloc(s, 44100 * 8.0, 1); // 8 second mono buffer

(
SynthDef(\recBuf,{ arg out=0, bufnum=0;
	var in;
	in = AudioIn.ar(1);
	RecordBuf.ar(in, bufnum);
}).load(s);
)

// we record into the buffer
x = Synth(\recBuf, [\out, 0, \bufnum, b.bufnum]);
x.free;

// and we can play it back using the playBuf synthdef we created above
z = Synth(\playBuf, [\bufnum, b.bufnum])
z.free;

// if we like it, we can write it to disk as a soundfile:
b.write("myBufRecording.aif", "AIFF", 'int16');

// TIP: play with the recLevel and preLevel of a buffer
// to overdub into the buffer, creating layers of sound.
// or perhaps "I'm Sitting in a Room" exercise a la Lucier.




// 5) =========  Fill a buffer  ==========

// The Osc UGen is a wavetable look-up oscillator
(
SynthDef(\oscplayer,{ arg out = 0, bufnum;
	var signal;
	signal = Osc.ar(bufnum, MouseX.kr(60,300)); // mouse x to control pitch
	Out.ar(out, signal ! 2)
}).load(s)
)


b = Buffer.alloc(s, 512, 1); // we allocate 512 samples (the buffer size must be the power of 2)
b.sine1(1.0/[1,2,3,4], true, true, true);
b.plot // notise somthing strange?

// check this:
(
b.getToFloatArray(action: { |array| Ê{ array[0, 2..].plot }.defer });
)

// let's listen to it
a = Synth(\oscplayer, [\bufnum, b.bufnum])
a.free;


// a saw wave:

b = Buffer.alloc(s, 512, 1);
b.sine1(1.0/Array.series(90,1,1)*2, false, true, true);
b.getToFloatArray(action: { |array| Ê{ array[0, 2..].plot }.defer });

// play it
a = Synth(\oscplayer, [\bufnum, b.bufnum])
a.free;


// fill it with random numbers
b = Buffer.alloc(s, 512, 1);
b.sine1(Array.fill(50, {1.0.rand}), true, true, true);
b.getToFloatArray(action: { |array| Ê{ array[0, 2..].plot }.defer });


// let's listen to it
a = Synth(\oscplayer, [\bufnum, b.bufnum])
a.free;



// use an envelope to fill a buffer
a = Env([0, 1, 0.2, 0.3, -1, 0.3, 0], [0.1, 0.1, 0.1, 0.1, 0.1, 0.1], \sin).plot;

// ENVELOPE turned into a SIGNAL and then into a WAVETABLE
c = a.asSignal(512).asWavetable;
c.size; // the size of the wavetable is twice the size of the signal... 1024

// now we neet to put this wavetable into a buffer:
b = Buffer.alloc(s, 512);
b.setn(0, c);

// play it
a = Synth(\oscplayer, [\bufnum, b.bufnum])
a.free;



// and for the sake of exhaustibility, check out Signal as well:

x = Signal.sineFill(512, [0,0,0,1]);
[x, x.neg, x.abs, x.sign, x.squared, x.cubed, 
x.asin.normalize, x.exp.normalize, x.distort].flop.flat.plot(numChannels: 9);

c = x.exp.normalize.asWavetable; // try the other unary operators on the signal

b = Buffer.alloc(s, 512);
b.setn(0, c); // put the wavetable into the buffer so Osc can read it.

// play it
a = Synth(\oscplayer, [\bufnum, b.bufnum])
a.free;



// 6) =========  Pitch and time changes  ==========


b = Buffer.read(s, "sounds/a11wlk01-44_1.aiff");

// The most common way
// here double rate (and pitch) results in half the length (time) of the file

(
SynthDef(\playBuf,{ arg out = 0, bufnum;
	var signal;
	signal = PlayBuf.ar(1, bufnum, MouseX.kr(0.2, 4), loop:1);
	Out.ar(out, signal ! 2)
}).load(s)
)

x = Synth(\playBuf, [\bufnum, b.bufnum])
x.free


// we could use PitchShift to change the pitch without changing the time
// PitchShift is a granular synthesis pitch shifter (other techniques include Phase Vocoders)

(
SynthDef(\playBufWPitchShift,{ arg out = 0, bufnum;
	var signal;
	signal = PlayBuf.ar(1, bufnum, 1, loop:1);
	signal = PitchShift.ar(
		signal,	// stereo audio input
		0.1, 			// grain size
		MouseX.kr(0,2),	// mouse x controls pitch shift ratio
		0, 				// pitch dispersion
		0.004			// time dispersion
	);
	Out.ar(out, signal ! 2)
}).load(s)
)

x = Synth(\playBufWPitchShift, [\bufnum, b.bufnum])
x.free


// for time streching check out the Warp0, Warp1 Ugens.




// 7) =========  BufRd and BufWr  ==========

// Here we use BufRd (Buffer Read) to play the contents of a buffer at a given index
// We use Phasor as index to move between the start and the end of the buffer.

{ BufRd.ar(1, b.bufnum, Phasor.ar(0, 1, 0, BufFrames.kr(b))) }.play;

// use SinOsc to modulate the playrate
{ BufRd.ar(1, b.bufnum, Phasor.ar(0, SinOsc.ar(1).range(0.5, 1.5), 0, BufFrames.kr(b))) }.play;

// Write into buffer:
(
y = { arg rate=1; 
	var signal;
	signal = SinOsc.ar(LFNoise1.kr(2, 300, 400), 0, 0.1);
	BufWr.ar(signal, b.bufnum, Phasor.ar(0, BufRateScale.kr(0) * rate, 0, BufFrames.kr(0)));
	0.0 //quiet
}.play;
)

// play it back
{ BufRd.ar(1, b.bufnum, Phasor.ar(0, 1, 0, BufFrames.kr(b.bufnum))) }.play;

y.free;



// Scratching the buffer with the mouse

b = Buffer.read(s, "sounds/a11wlk01.wav");

SynthDef(\xiiscratch, {arg bufnum, pitch=1, start=0, end;
	var signal;
	signal = BufRd.ar(1, bufnum, Lag.ar(K2A.ar(MouseX.kr(1, end)), 0.4));
	Out.ar(0, signal!2);
}).play(s, [\bufnum, b.bufnum, \end, b.numFrames]);

	



