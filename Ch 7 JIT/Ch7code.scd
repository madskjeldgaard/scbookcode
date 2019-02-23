
// in the following examples, all code that needs to be evaluated as one block is set in parentheses
// any such part can be evaluated in any order, e.g. starting from the middle or the end,
// and it may usually be rewritten at runtime.


//////////////////////////////////////////////////////////////////////////////
///////////////////////////// 1 Changing state   ////////////////////////////
////////////////////////////////////////////////////////////////////////////

// Figure 7.1: A modulo algorithm that operates over states of variables
(
Task {
	x = 4; y = 13;
	loop {
		x = (x * y) % 11;
		(note: x.postln, dur: 0.125).play;
		0.125.wait;
	}
}.play
);		// creates a loop of values

// change x and y
x = 5;	// new initial value
y = 4;	// new multiplication factor



// Figure 7.2: Synthesis graph

(
{
	x = SinOsc.kr(4); 
	y = SinOsc.kr(13);
	SinOsc.ar(x * y % 0.4 * 500 + 600) * 0.2
}.play;
)

// change x and y?
x = SinOsc.kr(4); // no effect.
y = SinOsc.kr(4); // no effect either.


// Figure 7.3: Dynamic synthesis graph

p = ProxySpace.push;
~x = { SinOsc.kr(4) }; 
~y = { SinOsc.kr(13) };
~z = { SinOsc.ar(~x * ~y % 0.4 * 500 + 600) * 0.2 };
~z.play;

	
// now ~x and ~y can be replaced
~x = { SinOsc.kr(0.4) }; 
~y = { SinOsc.kr(1.3) }; 

p.clear(2).pop; // release environment (2 sec fadeout)




//////////////////////////////////////////////////////////////////////////////
///////////////////  3 ProxySpace, Ndef, and NodeProxy  /////////////////////
////////////////////////////////////////////////////////////////////////////


// Figure 7.4: Refactoring a synthesis graph at runtime

p = ProxySpace.push;	// if needed

~a = { Lag.ar(LFClipNoise.ar(2 ! 2, 0.5, 0.5), 0.2) };
(
~b = {
	var c, d;
	c = Dust.ar(20 ! 2);
	d = Decay2.ar(c, 0.01, 0.02, SinOsc.ar(11300));
	d + BPF.ar(c * 5, ~a.ar * 3000 + 1000, 0.1)
}
);

~b.play;

// the refactored code from above

(
~a = { 
	var a;
	a =  Lag.ar(LFClipNoise.ar(2 ! 2, 0.5, 0.5), 0.2);
	BPF.ar(~c.ar * 5, a * 3000 + 1000, 0.1) 
}
);
~c = { Dust.ar(20 ! 2) };
~d = { Decay2.ar(~c.ar, 0.01, 0.02, SinOsc.ar(11300)) };
~b = ~a + ~d;

~b.play;



// Figure 7.6: A dynamic graph of a chaotic linear congruence. 
// self reference (~x) constructs a loop at control rate 

~out.play;
~x = 0.2; ~a = 1.1; ~c = 0.13;
~x = (~a * ~x) + ~c % 1.0;  // leaving out the .kr message assumes a control rate ugen.

~out = { Pan2.ar(SinOsc.ar(~x * 4000 + 200) * 0.1, ~x) };



// Figure 7.7: Creating a proxy object explicitly and changing its source
n = NodeProxy.new;
x = { SinOsc.ar(n.kr * 200 + 300) * 0.1 }.play;
n.source = { LFPulse.kr([1.3, 2.1, 3.2]).sum };
n.clear; x.free;

// Figure 7.8: Unified creation and access syntax with Ndef

Ndef(\out, { SinOsc.ar(Ndef.kr(\x) * 200 + 300) * 0.1 }).play;
Ndef(\x, { LFPulse.kr([1.3, 2.1, 3.2]).sum });
Ndef.clear;

// Figure 7.9: Unified creation and access syntax within an environment
p = ProxySpace.push; // if needed
~out = { SinOsc.ar(~x.kr * 200 + 300) * 0.1 }
~out.play;
~x = { LFPulse.kr([1.3, 2.1, 3.2]).sum };
p.clear.pop;



// Figure 7.10: Initialisation of node proxies in the proxy space

p.clear; // neutralize space, free all buses and synths
p.push; // if needed
~a.ar(3); // 3 channels, audio rate
~b.kr(8); // 8 channels, control rate
~c.play; // playing an uninitialized proxy assumes (per default) 2 channels, audio rate
~d = { LFNoise0.kr([1, 1, 1, 1]) }; // 4 channels, control rate




// Figure 7.11: Parameter mapping and setting

~out.play; ~out.fadeTime = 3;
(
// name with a_ represents audio rate argument
~out = { |freq=440, mod=0.4, detune=0.1, a_in = #[1,1]|
	freq = freq * ([0, detune] + 1);
	LFTri.ar(LFTri.ar(mod * freq).range(freq * mod, freq)) * a_in * 0.2
}
);

~mod2 = { LFNoise1.kr(1).range(0, 1) };
~mod1 =  { LFPulse.kr(~mod2.kr * 30 + 1, 0, 0.3) };
~freq1 = { ~mod1.kr * 13100 + 100 };
~freq2 = { LFTri.kr(30) * 200 + 300 };
~audio1 = { BrownNoise.ar(LFClipNoise.kr(10.dup), 1) };
~audio2 = { SinOsc.ar(LFNoise2.kr(1.dup).exprange(4, 1000)) };

~out.map(\freq, ~freq2, \mod, ~mod1);
~out.set(\detune, 0.01);
~out.map(\freq, ~freq1, \mod, ~mod1);
~out.xmap(\freq, ~freq1, \mod, ~mod2); // xmap crossfades over fade time to new value.
~out.xmap(\freq, ~freq2, \mod, ~mod1, \a_in, ~audio2);
~out.map(\a_in, ~audio1);


//////////////////////////////////////////////////////////////////////////////
//////////  4 Structured waiting and rewriting: TaskProxy, Tdef  ////////////
////////////////////////////////////////////////////////////////////////////



// Figure 7.12: Rewriting a synth def and a task def while running
// this synthdef is used in the subsequent figures
(
SynthDef(\wave, { |out, freq=440, amp=0.1, sustain=0.1, mod=0.2|
	OffsetOut.ar(out,
		EnvGen.ar(Env.perc(ExpRand(0.001, 0.05), sustain, amp), doneAction: 2)
		*
		SinOsc.ar(freq, SinOsc.ar(sustain.reciprocal * 8, [0, Rand(0, pi)], mod))
	)
}).add
);
(
Tdef(\x, {
	x = 4; y = 13;
	loop {
		x = (x * y) % 11;
		(instrument: \wave, note: x.postln, sustain: 0.5, octave: 6).play;
		0.125.wait;
	}
}).play
);


// Figure 7.13: Embed and fork of different tasks

(

Tdef(\a, { 10.do { (instrument: \wave, freq: 50.rand + 1500).play; 0.03.wait } });
Tdef(\b, { [1, 5, 1, 2, 8, 4, 12].do { |x| (instrument: \wave, note: x + 8).play; 0.1.wait } });
Tdef(\c, { "c is just a waiting message".postln; 2.wait; });


Tdef(\x, {
	loop {
		Tdef(\a).embed; // play in sequence
		1.wait;
		Tdef(\b).embed;
		2.wait;
		Tdef(\a).fork; // play in parallel
		Tdef(\b).fork;
		Tdef(\c).embed;
	}
}).play
);

// rewrite with infinite loop
Tdef(\a, { inf.do { (instrument: \wave, freq: 50.rand + 500).play; 0.1.wait } }); 
// rewrite with finite loop
Tdef(\a, { 10.do { (instrument: \wave, freq: 50.rand + 500).play; 0.1.wait } });


// Figure 7.14: Passing an environment into a task proxy when embedding
(
Tdef(\a, { |in|
	in.at(\n).do { |i| 
		in = (instrument: \wave, detune: 5.rand2).putAll(in);
		in.postln.play; 
		in.delta.wait;
	} 
})
);

(
Tdef(\x, { |inevent|
	loop {
		Tdef(\a).embed((note: [15, 17], dur: 0.01, n: 13));
		1.wait;
		Tdef(\a).embed((note: 9, dur: 0.4, n: 4));
		1.wait;
	}
}).play;
)



//////////////////////////////////////////////////////////////////////////////
/////////////////////   5 Empty Patterns    /////////////////////////////////
////////////////////////////////////////////////////////////////////////////


// Figure 7.15: A pattern proxy as an entry point into a stream

Pdefn(\x, Pseq([0, 2, 0, 7, 6, 5, 4, 3], inf));
(
Task {
	var stream = Pdefn(\x).asStream;
	var val;
	loop { 
		val = stream.next; 
		(instrument: \wave, note: val).play; 
		0.2.wait 
	}
}.play
);

Pdefn(\x, Pseq([0, 2, 0, 8, 6, 5, 2, 3, 4, 5], inf)); // rewrite the definition at runtime.
Pdefn(\x, Pseq([0, 2, 0, 7, 6, 5, 4, 3].scramble + 4, inf));



// Figure 7.16: Deriving variations fom nonexisting streams by mathematical operations

Pdefn(\y, Pdefn(\x) + 2); // derive a transposition
Pdefn(\z, Pdefn(\x) + Pseq([0, 5, 0, 7, 2], inf)); // derive a variation
Pdefn(\a, Ptuple([Pdefn(\y), Pdefn(\z)])); // combine them in a stream of arrays
(
Task {
	var stream = Pdefn(\a).asStream;
	var val;
	loop { 
		val = stream.next.postln; 
		(instrument: \wave, note: val, sustain: rrand(0.5, 0.9)).play; 
		0.2.wait 
	}
}.play
);

// rewriting the definitions causes all derivations to vary
Pdefn(\x, Pseq([0, 11], inf));
Pdefn(\x, Pseq([0, 2, 0, 7, 6, 5, 4, 3].scramble + 5, inf));
Pdefn(\z, Pdefn(\x) + Pseq([1, 5, 1, 11, 1], inf)); // change a variation

Pdefn(\a, 5); // a number as a source
Pdefn.clear; // clearing all - the empty pattern returns a series of 1. 



// Figure 7.17: Pdef - play, pause and resume.

Pdef(\a).play; // play silence in sequence
Pdef(\a, Pbind(\instrument, \wave)); // insert a sequence of notes
Pdef(\a, Pbind(\instrument, \wave, \dur, Pseq([1, 3, 2, 3], inf) / 6)); // add some rhythm
Pdef(\a).pause;
Pdef(\a).resume;
Pdef(\a).stop;



// Figure 7.18: A larger combination of Pdefs
(
(
Pdef(\x,
	Pbind(
		\instrument, \wave,
		\mod, Pseq([1, 0, 1, 0], inf), 
		\dur, Pn(1/2, 8),
		\note, 7
	)
)
);

(
Pdef(\y,
	Pbindf(
		Pdef(\x),
		\amp, 0.2,
		\note, Pshuf([0, 2, 3, 5], 2) + Prand([0, 5, [0, 4]], inf),
		\dur, Pseq([1, 3, 2, 3], inf) / 6
	)
)
);

(
Pdef(\z, Pbindf(Pdef(\y), \dur, 1/4))
);

// the combination of all placeholders into a new placeholder
(
Pdef(\a,
	Pmul(\dur, Pwhite(-0.02, 0.02) + 1,
		Pseq([
			Ppar([Pdef(\x), Pdef(\y)]),
			Pdef(\x),
			Pdef(\y),
			Pdef(\z),
			Ppar([Pdef(\x), Pbindf(Pdef(\y), \ctranspose, 2)])
		], inf)
	)
);
))

Pdef(\a).play; // play it

// go into a looping vamp
(
Pdef(\x,
	Pbind(
		\instrument, \wave, 
		\dur, Pseq([1, 3, 2, Prand([3, 2])], inf) / 6,
		\octave, [6, 4]
	)
)
);

// release a break
(
Pdef(\x,
	Pbind(
		\instrument, \wave, 
		\dur, Pseq([1, 3, 2, Prand([3, 2])], 1) / 6,
		\octave, [6, 4]
	)
)
);

Pdef(\a).stop; // stop the player


//////////////////////////////////////////////////////////////////////////////
/////////////////  6 Symbol streams and recursive patterns  /////////////////
////////////////////////////////////////////////////////////////////////////


// Figure 7.19: Simplifying the code in Figure 12.18 using Psym

// the combination of all placeholders into a new placeholder
(
Pdef(\b, Pbindf(Pdef(\y), \ctranspose, 2));
Pdef(\a,
	Pmul(\dur, Pwhite(-0.02, 0.02) + 1,
		Psym(Pseq([[\x, \y], \x, \y, \z, [\x, \b]], inf).trace) // trace it to post which
	)
).play;
)

// Figure 7.20: Using a Pdefn for the sequence of symbols itself
(
Pdefn(\sequence, Pseq([[\x, \y], \x, \y, \z, [\x, \b]], inf));
Pdef(\a,
	Pmul(\dur, Pwhite(-0.02, 0.02) + 1,
		Psym(Pdefn(\sequence).trace)
	)
).play;
)

// rewrite the sequence
Pdefn(\sequence, Pseq([\x], inf));
Pdefn(\sequence, Pseq([\x, \y, \x, [\x, \y]], inf));

Pdef(\a).stop; // stop playing


// Figure 7.21: Event type 'phrase'

(instrument: \x, type: \phrase).play; // a single phrase from Pdef(\x)

// a pattern of overlapping phrases
(
Pbind(
	\type, \phrase, 
	\instrument, \x, 
	\legato, 2.5, 
	\note, Pseq([0, 5, 7], inf)
).play
);


// Figure 7.22: Recursive phrasing
(
Pdef(\x, { |note=0, n=6, step=3, modulo=15, sustain=1|  
	Pbind(
		\instrument, \wave,
		\note, note.value + (Pseries(1, step, n) % modulo) + 7, 
		\dur, sustain.value / n
	) 
})
);

(
Pdef(\a, 
	Pbind(
		\type, \phrase,
		\instrument, \x,
		\note, Pseq([0, 5, 4, 8, 0], inf),
		\n, 5,
		\modulo, Pseq([3, [4, 3, 5], [13, 15]], inf),
		\dur, Pseq([1, 2, 0.5, 1.5], inf)
	)
).play
);

(
Pdef(\a, 
	Pbind(
		\type, \phrase,
		\instrument, \x,
		\note, Pseq([0, 5, 4, 8, 0], inf),
		\n, 5,
		\modulo, Prand([3, [4, 3, 5], [13, 15]], inf),
		\recursionLevel, 1,
		\dur, Pseq([1, 2, 0.5, 1.5], inf) * 2
	)
).play
);




//////////////////////////////////////////////////////////////////////////////
/////////////////////////  8 Combinatorics  /////////////////////////////////
////////////////////////////////////////////////////////////////////////////


// Figure 7.25: Combinations between patterns and ugen graphs


(
SynthDef(\train, { |out, xfreq=15, sustain=1.0, amp=0.1, pan|
	Line.ar(1, 1, sustain, doneAction:2);
	OffsetOut.ar(out, Pan2.ar(Impulse.ar(xfreq), pan, amp));
}).add
);

p = ProxySpace.push;
~z.play;

// A pattern in an audio rate node proxy ...
(
~x = Pbind(
	\instrument, \train, 
	\xfreq, Pseq([50, Pwhite(30, 800, 1), 5, 14, 19], inf), // only non-standard keys, i.e. xfreq 
	\sustain, Pseq([Pwhite(0.01, 0.1, 1), 0.1, 1, 0.5, 0.5], inf),
	\pan, Prand([-1, 1], inf) * 0.1
)
);
~y = { Ringz.ar(~x.ar, 5000 * [1, 1.2], 0.01) }; // resonant filter on the impulses from ~x
~mod = { LFNoise1.kr(0.1).exprange(200, 5000) }; // a modulator
~z = { ~y.ar * (SinOsc.ar(~mod.kr) + 1) }; // ring modulation with frequency ~mod

// ... and a control rate node proxy in a pattern.
// To pass on modulation like this, the standard event parameters like freq cannot be used.
// Here, we use xfreq instead.

(
~x = Pbind(
	\instrument, \train, 
	\xfreq, Pseq([50, ~mod, 5, ~mod, 19], inf), // read from the ~mod proxy bus. 
	\sustain, Pseq([Pwhite(0.01, 0.1, 1), 0.1, 1, 0.5, 0.5], inf),
	\pan, Prand([-1, 1], inf) * 0.1
)
);

// Figure 7.26 The same functionality, using Ndef instead of ProxySpace.
(
SynthDef(\train, { |out, xfreq=15, sustain=1.0, amp=0.1, pan|
	Line.ar(1, 1, sustain, doneAction:2);
	OffsetOut.ar(out, Pan2.ar(Impulse.ar(xfreq), pan, amp));
}).add
);

Ndef(\z).play;

// a pattern in an audio rate node proxy ...
(
Ndef(\x, Pbind(
	\instrument, \train, 
	\xfreq, Pseq([50, Pwhite(30, 800, 1), 5, 14, 19], inf), 
	\sustain, Pseq([Pwhite(0.01, 0.1, 1), 0.1, 1, 0.5, 0.5], inf),
	\pan, Prand([-1, 1], inf) * 0.1
))
);
Ndef(\y, { Ringz.ar(Ndef(\x).ar, 5000 * [1, 1.2], 0.01) });
Ndef(\mod, { LFNoise1.kr(0.1).exprange(200, 5000) });
Ndef(\z, { Ndef(\y).ar * (SinOsc.ar(Ndef(\mod).kr) + 1) }); // ring modulation with Ndef(\mod)

// ... and a control rate node proxy in a pattern

(
Ndef(\x, Pbind(
	\instrument, \train, 
	\xfreq, Pseq([50, Ndef(\mod), 5, Ndef(\mod), 19], inf), // read from the Ndef(\mod) proxy bus
	\sustain, Pseq([Pwhite(0.01, 0.1, 1), 0.1, 1, 0.5, 0.5], inf),
	\pan, Prand([-1, 1], inf) * 0.1
))
);


// Figure 7.27: Using a Tdef to create overlapping synths within a node proxy


Ndef(\x).play; // here an Ndef is used, the same can be done within a ProxySpace
Ndef(\x, { |freq=5, detune=0| Impulse.ar(freq * [detune, 1 - detune]) * 0.2 });
(
Tdef(\c, {
	loop {
		 Ndef(\x).fadeTime = rrand(0.1, 3.5);
		 Ndef(\x).send([\freq, exprand(2, 400), \detune, [0.0, 1.0].choose.rand]);
		2.wait;
	}
}).play
);





