
/* Figure 2.1 */

s = Server.internal.boot;
z = s.scope(4);

// a) mono output
(
a = SynthDef(\UGen_ex1a, {
	Out.ar(0, SinOsc.ar(440, 0, 0.1))
}).play(s);
)	
a.free;

// b) freq input is an Array of 4 items - outputs to busses 0-3
(
a = SynthDef(\UGen_ex1b, {
	Out.ar(0, SinOsc.ar([440, 446, 448.5, 882], 0, 0.1))
}).play(s);
)	
a.free;

// c) Array is added to the 'mul' arg to show mapping
(
a = SynthDef(\UGen_ex1c, {
	Out.ar(0, SinOsc.ar([440, 446, 448.5, 882], 0, [0.1, 0.2, 0.3, 0.4]))
}).play(s);
)	
a.free;

// d) The output of the SinOsc above is actually an Array of four SinOscs. Sum them together
// for an additive synthesis example.
(
a = SynthDef(\UGen_ex1d, {
	Out.ar(0, SinOsc.ar([440, 446, 448.5, 882], 0, [0.1, 0.2, 0.3]).sum);
}).play(s);
)	
a.free;

z.window.close;

/* Figure 2.2 */

Server.default = s = Server.internal.boot;

z = s.scope;
(
SynthDef(\UGen_ex2, {arg freq = 440;
	var src, compressor, limiter, out;
	// 10 SinOsc's, mixed together. Output amplitude is controlled with an Dust UGen
 	// wrapped in a Decay2 UGen to create a spike with an Exponential Decay
	src = SinOsc.ar(
		// a harmonic series based on freq
		Array.series(10, freq, freq),
		0, // phase
		Array.fill(10, {Decay2.ar( 
			// Dust will create an impulse about every 2 seconds, with values between 0
			// and 5
			Dust.ar(0.1, 5),
			// Decay2, attach time of 0.01 seconds and a decay time of 5 seconds to
			// allow for a build up of signal
			0.01, 5)});
		).sum;
	// compress signal about 0.5
	compressor = Compander.ar(src, src, 0.5, 1, 0.1);
	limiter = Limiter.ar(compressor, 0.5);
	// out is the compressed only signal on the left, the compressed and limited on the 
	// right
	out = [DelayN.ar(compressor, 0.02, 0.02), limiter];
	// use Peak and poll to track the highest output values. Updates every second
	Peak.ar(out ++  src, Impulse.kr(1)).poll(1, ["compressed", "limited", "src"]);
	Out.ar(0, out);
}).add;
)	
a = Synth(\UGen_ex2, [\freq, 440]);

a.free; z.window.close;

/* Figure 2.3 */
(
SynthDef(\UGen_ex3, {arg gate = 1, amp = 1, rate = 10;
	var trigger, dur, carfreq, modfreq, index, pan, env;
	trigger = Impulse.ar(rate);
	dur = rate.reciprocal;
	carfreq = LFNoise2.kr.range(100, 110);
	modfreq = LFTri.kr(0.1).exprange(200, 840);
	index = LFCub.kr(0.2).range(4, 10);
	pan = WhiteNoise.ar.range(-0.1, 0.1);
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1),
		gate,
		levelScale: amp,
		doneAction: 2);
	Out.ar(0,
		GrainFM.ar(2, trigger, dur, carfreq, modfreq, index,
			pan, -1) * env)
}).add;
)
a = Synth(\UGen_ex3, [\rate, 80, \amp, 0.2]);

b = Synth(\UGen_ex3, [\rate, 42, \amp, 0.2]);

c = Synth(\UGen_ex3, [\rate, 121, \amp, 0.2]);

[a, b, c].do({arg thisSynth; thisSynth.set(\gate, 0)});

/* Figure 2.4 */

(
SynthDef(\UGen_ex4a, {arg id, limit = 1;
	var src, pitch, hasPitch, keynum, outOfTune;
	// read input
	src = SoundIn.ar(0);
	// analyze the frequency of the input
	#pitch, hasPitch = Pitch.kr(src);
	// convert to a midi keynum, but don't round! This value will be used later.
	pitch = pitch.cpsmidi;
	// if you are within an eighth tone of an equal tempered pitch, send a trigger
	outOfTune = (pitch - pitch.round).abs < 0.25;
	// if outOfTune is true, send a trigger. Limit to 1 trigger every 'limit' seconds  
	SendTrig.kr(Trig.kr(outOfTune, limit), id, pitch.round);
}).add;
	
SynthDef(\UGen_ex4b, {arg id1, id2, limit = 1, thresh = 0.5;
	var src, amp, amptrig, timer;
	src = SoundIn.ar(0);
	// analyze the amplitude input, cause a trigger if the output is over the thresh
	amp = Amplitude.kr(src);
	amptrig = Trig.kr(amp > thresh, limit);
	// use amptrig to see how long it is between triggers.
	timer = Timer.kr(amptrig);
	// send the values back with two different ids
	SendTrig.kr(amptrig, id1, amp);
	SendTrig.kr(amptrig, id2, timer);
}).add;

// plays a SinOsc of the pitch you were closest to
SynthDef(\UGen_ex4c, {arg freq;
	Out.ar(1, SinOsc.ar(freq, 0, XLine.kr(0.1, 0.00001, 0.5, doneAction: 2)))
}).add;

// modulated noise to respond to amp spikes
SynthDef(\UGen_ex4d, {arg freq;
	Out.ar(1, LFNoise1.ar(200) * SinOsc.ar(freq, 0, 
		XLine.kr(0.1, 0.00001, 0.5, doneAction: 2)));
}).add;

// allocate three unique ids for the trigger ids
a = UniqueID.next;
b = UniqueID.next;
c = UniqueID.next;	

// an envelope to poll for amp values later
e = Env([440, 880], [1], \exp);

// add the responder
o = OSCresponderNode(s.addr, '/tr', {arg time, responder, msg;
	// the msg is an array with 4 values... post them
	msg.postln;
	// the id sent back from the SendTrig is msg[2]... use it to decide what to do
	case 
		// pitch trigger
		{msg[2] == a}
		// msg[3] is the rounded keynum
		{Synth(\UGen_ex4c, [\freq, msg[3].midicps])}
		// amp trigger
		{msg[2] == b}
		// play a noise burst, higher the amp value, higher the freq (polls the 
		// Env 'e')
		{Synth(\UGen_ex4d, [\freq, e[msg[3]]])}
		// use the Timer value to play a delayed noise burst at 2000 Hz
		{msg[2] == c}
		{SystemClock.sched(msg[3], {
			Synth(\UGen_ex4d, [\freq, 2000]);
			})}
}).add;

// schedule the start our listening synths...
// then sing or tap away on the input.
SystemClock.sched(1.0, {
	Synth(\UGen_ex4a, [\id, a, \limit, 1]);
	Synth(\UGen_ex4b, [\id1, b, \id2, c, \limit, 0.2, \thresh, 0.25]);
});
	
// add a command period function to stop the synths and remove the responder
CmdPeriod.doOnce({
	o.remove; "Removed the responder".postln;
})
)

/* Figure 2.5 */
(
SynthDef(\UGen_ex5, {arg gate = 1, seed = 0, id = 1, amp = 1;
	var src, pitchbase, freq, rq, filt, trigger, env;
	RandID.ir(id);
	RandSeed.ir(1, seed);
	env = EnvGen.kr(Env([0, 1, 0], [1, 4], [4, -4], 1), gate, doneAction: 2);
	src = WhiteNoise.ar;
	trigger = Impulse.kr(Rand.new(2, 5));
	pitchbase = IRand.new(4, 9) * 12;
	freq = TIRand.kr(pitchbase, pitchbase + 12, trigger).midicps;
	rq = LFDNoise3.kr(Rand.new(0.3, 0.8)).range(0.01, 0.005);
	filt = Resonz.ar(src, Lag2.kr(freq), rq);
	Out.ar(0, Pan2.ar(filt, LFNoise1.kr(0.1)) * env * amp)
}).add;
)	
a = Synth(\UGen_ex5, [\seed, 123]);

a.release;

// Using the same seed, we get the same gesture
b = Synth(\UGen_ex5, [\seed, 123]);

b.release;

// passing in different seeds
(
r = Routine.run({
	thisThread.randSeed_(123);
	10.do({
		a = Synth(\UGen_ex5, [\seed, 10000.rand.postln, \amp, 3.dbamp]);
		1.wait;
		a.release;
	})
});
)		
/* Figure 2.6 */
(
SynthDef(\UGen_ex6, {arg gate = 1, roomsize = 200, revtime = 450;
	var src, env, gverb;
	env = EnvGen.kr(Env([0, 1, 0], [1, 4], [4, -4], 1), gate, doneAction: 2);
	src = Resonz.ar(
			Array.fill(4, {Dust.ar(6)}),
			1760 * [1, 2.2, 3.95, 8.76] + 
				Array.fill(4, {LFNoise2.kr(1, 20)}), 
			0.01).sum * 30.dbamp;
	gverb = GVerb.ar(
		src,
		roomsize, 
		revtime, 
		// feedback loop damping
		0.99,
		// input bw of signal
		LFNoise2.kr(0.1).range(0.9, 0.7),
		// spread 
		LFNoise1.kr(0.2).range(0.2, 0.6),
		// almost no direct source 
		-60.dbamp,
		// some early reflection
		-18.dbamp, 
		// lots of the tail
		3.dbamp,
		roomsize);
	Out.ar(0, gverb * env)
}).add;
)	
a = Synth(\UGen_ex6);

a.release;

/* Figure 2.7 */
(
SynthDef(\UGen_ex7a, {arg gate = 1, freq = 440, amp = 0.1, rate = 0.2;
	var src, pos, env;
	src = SinOsc.ar(freq, 0);
	pos = LFNoise2.ar(rate);
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, levelScale: amp, doneAction: 2);
	Out.ar(0, Pan2.ar(src, pos) * env);
}).add;
			
SynthDef(\UGen_ex7b, {arg gate = 1, freq = 440, amp = 0.1, rate = 0.2;
	var src, pos, env;
	src = SinOsc.ar(freq, 0);
	pos = LFNoise2.kr(rate);
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, levelScale: amp, doneAction: 2);
	Out.ar(0, Pan2.ar(src, pos) * env);
}).add;
	
SynthDef(\UGen_ex7c, {arg gate = 1, freq = 440, amp = 0.1, rate = 0.2;
	var src, pos, env;
	src = SinOsc.ar(freq, 0);
	pos = LFNoise2.kr(rate);
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, levelScale: amp, doneAction: 2);
	Out.ar(0, Pan2.ar(src * env, pos));
}).add;
)

// 56% on my machine
(
a = Group.new;
250.do({
	Synth(\UGen_ex7a, [\freq, 440.0.rrand(1760.0), \amp, 0.001, \rate, 0.2], a)
});
)
a.release;

// 39%
(
a = Group.new;
250.do({
	Synth(\UGen_ex7b, [\freq, 440.0.rrand(1760.0), \amp, 0.001, \rate, 0.2], a)
});
)
a.release;

// 35%
(
a = Group.new;
250.do({
	Synth(\UGen_ex7c, [\freq, 440.0.rrand(1760.0), \amp, 0.001, \rate, 0.2], a)
});
)
a.release;

/* Figure 2.8 */
(
SynthDef(\UGen_ex8a, {arg gate = 1, freq = 440, amp = 0.1, rate = 0.2;
	var w, x, y, out, env, decode;
	#w, x, y = PanB2.ar(
		SinOsc.ar(freq, 0), LFNoise2.kr(rate));
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, levelScale: amp, doneAction: 2);
	decode = DecodeB2.ar(2, w, x, y);
	Out.ar(0, decode * env)
}).add;
			
SynthDef(\UGen_ex8b, {arg outbus, freq = 440, rate = 0.2;
	var w, x, y;
	#w, x, y = PanB2.ar(
		SinOsc.ar(freq, 0), LFNoise2.kr(rate));
	Out.ar(outbus, [w, x, y])
}).add;
			
SynthDef(\UGen_ex8c, {arg inbus, gate = 1, amp = 0.1;
	var w, x, y, env, decode;
	#w, x, y = In.ar(inbus, 3);
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, levelScale: amp, doneAction: 14);
	decode = DecodeB2.ar(2, w, x, y) * env;
	ReplaceOut.ar(0, decode);
}).add;
)

(
a = Group.new;
250.do({
	Synth(\UGen_ex8a, [\freq, 440.0.rrand(1760.0), \amp, 0.001, \rate, 0.2], a)
});
)
a.release;

(
a = Group.new;
z = Bus.audio(s, 3);

// the 'catch-all' synth for decoding and enveloping
Synth(\UGen_ex8c, [\inbus, z, \amp, 0.001], a, \addAfter); // add it after the Group containing the encoding synths

250.do({
	Synth(\UGen_ex8b, [\freq, 440.0.rrand(1760.0), \outbus, z, \rate, 0.2], a)
});
)

a.release;	
/* Figure 2.9 */
(
// pass in amp in db
SynthDef(\UGen_ex9a, {arg gate = 1, freq = 440, amp = 0;
	var src, pos, env;
	src = SinOsc.ar(freq, 0, amp.dbamp);
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	Out.ar(0, Pan2.ar(src * env, Rand(-1.0, 1.0)));
}).add;
	
// pass in linear amplitude
SynthDef(\UGen_ex9b, {arg gate = 1, freq = 440, amp = 1;
	var src, env;
	src = SinOsc.ar(freq, 0, amp);
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	Out.ar(0, Pan2.ar(src * env, Rand(-1.0, 1.0)));
}).add;
	
SynthDef(\UGen_ex9c, {arg gate = 1, freq = 440, amp = -3, pos = 0;
	var src, env;
	src = SinOsc.ar(freq, 0, amp);
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	Out.ar(0, Pan2.ar(src * env, pos));
}).add;
)	

// 45% on my machine
(
a = Group.new;
250.do({
	Synth(\UGen_ex9a, [\freq, 440.0.rrand(1760.0), \amp, -60], a)
});
)
a.release;

// 36%
(
a = Group.new;
250.do({
	Synth(\UGen_ex9b, [\freq, 440.0.rrand(1760.0), \amp, -60.dbamp], a)
});
)
a.release;

// 36% (no difference from b)
(
a = Group.new;
250.do({
	Synth(\UGen_ex9c, [\freq, 440.0.rrand(1760.0), \amp, -60.dbamp, \pos, 1.0.rand2], a)
});
)
a.release;

/* Figure 2.10 */
(
SynthDef(\UGen_ex10a, {arg gate = 1;
	var src, delay, env;
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	src = Decay.ar(Impulse.ar(1), 1.0, PinkNoise.ar(0.1));
	delay = CombN.ar(src, 0.1, Line.kr(0.0001, 0.001, 10));
	Out.ar(0, (delay * env).dup);
}).add;

SynthDef(\UGen_ex10b, {arg gate = 1;
	var src, delay, env;
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	src = Decay.ar(Impulse.ar(1), 1.0, PinkNoise.ar(0.1));
	delay = CombL.ar(src, 0.1, Line.kr(0.0001, 0.001, 10));	
	Out.ar(0, (delay * env).dup);
}).add;

SynthDef(\UGen_ex10c, {arg gate = 1;
	var src, delay, env;
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	src = Decay.ar(Impulse.ar(1), 1.0, PinkNoise.ar(0.1));
	delay = CombC.ar(src, 0.1, Line.kr(0.0001, 0.001, 10));	
	Out.ar(0, (delay * env).dup);
}).add;
)

a = Synth(\UGen_ex10a); // no interpolation
a.release;

a = Synth(\UGen_ex10b); // linear interpolation
a.release;

a = Synth(\UGen_ex10c); // cubic interpolation
a.release;

(
SynthDef(\UGen_ex10d, {arg gate = 1, deltime = 0.001;
	var src, delay, env;
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	src = Decay.ar(Impulse.ar(1), 1.0, PinkNoise.ar(0.1));
	delay = CombN.ar(src, 0.1, deltime);
	Out.ar(0, (delay * env).dup);
}).add;

SynthDef(\UGen_ex10e, {arg gate = 1, deltime = 0.001;
	var src, delay, env;
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	src = Decay.ar(Impulse.ar(1), 1.0, PinkNoise.ar(0.1));
	delay = CombL.ar(src, 0.1, deltime);
	Out.ar(0, (delay * env).dup);
}).add;
	
SynthDef(\UGen_ex10f, {arg gate = 1, deltime = 0.001;
	var src, delay, env;
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	src = Decay.ar(Impulse.ar(1), 1.0, PinkNoise.ar(0.1));
	delay = CombC.ar(src, 0.1, deltime);
	Out.ar(0, (delay * env).dup);
}).add;
)

// tune to a specific pitch
a = Synth(\UGen_ex10d, [\deltime, 100.midicps.reciprocal]); // no interpolation
a.release;

a = Synth(\UGen_ex10e, [\deltime, 100.midicps.reciprocal]); // linear interpolation
a.release;

a = Synth(\UGen_ex10f, [\deltime, 100.midicps.reciprocal]); // cubic interpolation
a.release;

// a much longer delay
a = Synth(\UGen_ex10d, [\deltime, 0.1]); // no interpolation
a.release;

a = Synth(\UGen_ex10e, [\deltime, 0.1]); // linear interpolation
a.release;

a = Synth(\UGen_ex10f, [\deltime, 0.1]); // cubic interpolation
a.release;
