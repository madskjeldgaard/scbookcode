

// =====================================================================
// - SuperCollider Basics -
// =====================================================================

// Tutorial 15 - Musical Patterns in the SCLang

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

// 	1) The SynthDefs
// 	2) A survey of patterns usage
// 	3) TempoClock and patterns
// 	4) Popcorn
// 	5) Clocks in SuperCollider
// 	6) Using the TempoClock





// 1) ========= The SynthDefs ==========

/*
In this tutorial we'll use two synth definitions.
We store them on the server now instead of loading them.
This way we don't need to read the SynthDescLib when using Patterns.
*/

(
SynthDef(\sine, {arg freq=440, phase=0, amp=0.5, envdur=1, pan=0.0;
	var signal;
	signal = Pan2.ar(SinOsc.ar(freq, phase, amp).cubed, pan); // note the pan
	signal = signal * EnvGen.ar(Env.perc(0.01, envdur), doneAction:2);
	Out.ar(0, signal);
}).store; 

SynthDef(\synth1, {arg out=0, freq=440, envdur=1, amp=0.4, pan=0;
    var x, env;
    env = EnvGen.kr(Env.perc(0.001, envdur, amp), doneAction:2);
    x = Mix.ar([FSinOsc.ar(freq, pi/2, 0.5), Pulse.ar(freq,Rand(0.3,0.7))]);
    x = RLPF.ar(x,freq*4,Rand(0.04,1));
    x = Pan2.ar(x,pan);
    Out.ar(out, x*env);
}).store; // you can also load the synthdefs, but then you need to run the following line

// if you are not using .store above, you'd have to use the next line:
SynthDescLib.global.read; // let's read the synthdef to use with Patterns
)



// 2) ========= A survey of patterns usage ==========


// we can try to play these synthdefinitions using our patterns
// it plays default arguments of patterns (see Event sourcefile (apple+j))

Pdef(\test1, Pbind(\instrument, \sine)).play; // it plays our synthdef

Pdef(\test2, Pbind(\instrument, \synth1)).play; // it plays our synthdef


(
Pdef(\scale, Pbind( \instrument, \synth1,
				 \freq, Pseq([62,64,67,69,71,74], inf).midicps
)); 
)

a = Pdef(\scale).play;
a.pause 	// pause the stream
a.resume 	// resume it
a.stop 	// stop it (resets it)
a.play 	// start again

// then we can set variables in our instrument using .set
Pdef(\scale).set(\out, 20); // outbus 20 
Pdef(\scale).set(\out, 0); // outbus 0 

// here we set the duration of the envelope in our instrument
Pdef(\scale).set(\envdur, 0.1);

// NOTE: Patterns use default keywords defined in the Event class, so take care
// not to use those keywords in your synth definitions. 
// If we had used dur instead of envdur for the envelope in our instrument, this would happen:
Pdef(\scale).set(\dur, 0.1);
// because dur is a keyword of Patterns
// (the main ones are \dur, \freq, \amp, \out, \midi)

// resetting the freq info is not possible however :
Pdef(\scale).set(\freq, Pseq([72,74,72,69,71,74], inf).midicps);

// one solution would be to resubmit the Pattern Definition:
(
Pdef(\scale, Pbind( \instrument, \synth1,
				 \freq, Pseq([72,74,72,69,71,74], inf).midicps // different sequence
)); 
)
// and it's still in our variable "a", it's just the definition that's different
a.pause
a.resume

/////////// Patterns and environmental variables
// or we could use Pdefn (read the helpfiles to compare Pdef and Pdefn)
// (here we are using envrionment variables to refer to patterns)

// we use a Pdefn to hold the scale
Pdefn(\scaleholder, { |arr| Pseq(arr.freqarr) });
// and we add an array to it
Pdefn(\scaleholder).set(\freqarr, Array.fill(6, {440 +(300.rand)} ));

// then we play a Pdef with the Pdefn
Pdef(\scale, 
		Pbind( 	\instrument, \synth1,
				\freq, Pn(Pdefn(\scaleholder), inf), // loop
				\dur, 0.4
			)
			
); 
a = Pdef(\scale).play;

// and we can reset our scale 
Pdefn(\scaleholder).set(\freqarr, Array.fill(3, {440 +(300.rand)} ));


// another example
(
Pdefn(\deg, Pseq([0, 3, 2],inf));

Pset(\instrument, \synth1, 
	Ppar([
		Pbind(\degree, Pdefn(\deg)),
		Pbind(\degree, Pdefn(\deg), \dur, 1/3)
])
).play;
)

Pdefn(\deg, Prand([0, 3, [1s, 4]],inf));
Pdefn(\deg, Pn(Pshuf([4, 3, 2, 7],2),inf));
Pdefn(\deg, Pn(Pshuf([0, 3],2),inf));

(
Pdefn(\deg, Plazy { var pat;
				pat = [Pshuf([0, 3, 2, 7, 6],2), Pshuf([3, 2, 6],2), Pseries(11, -1, 11)].choose;
				Pn(pat, inf)
		});
)


/////////////// p


(
Pdef(\player).set(\instrument, \sine);

Pdef(\player,
	Pbind(
		\instrument, 	Pfunc({ |e| e.instrument }),
		\midinote, 	Pseq([45,59,59,43,61,43,61,61,45,33,31], inf),
		\dur, 		Pseq ([0.25,1,0.25,0.5,0.5,0.5,0.125,0.125,0.5,0.25,0.25], inf),
		\amp, 		Pseq ([1,0.1,0.2,1,0.1125,0.1125,1,0.1125,0.125,0.25,1,0.5], inf)
	)
);
)

Pdef(\player).play;

Pdef(\player).set(\instrument, \synth1);
Pdef(\player).set(\envdur, 0.1);
Pdef(\player).set(\envdur, 0.25);
Pdef(\player).set(\envdur, 1);
Pdef(\player).set(\instrument, \sine);


///////////////////////////////////////////////////////


(
~scale = [62,67,69, 77];

c = Pdef(\p04b, 
		Pbind(\instrument, \synth1, 
					\freq, (Pseq.new(~scale, inf)).midicps, // freq arg
					\dur, Pseq.new([1, 1, 1, 1], inf);  // dur arg
		)
);

c = Pdef(\p04c, 
		Pbind(\instrument, \synth1,
					\freq, (Pseq.new(~scale, inf)).midicps, // freq arg
					\dur, Pseq.new([1, 1, 1, 1], inf);  // dur arg
		)
);
)

Pdef(\p04b).quant_([2, 0, 0]);
Pdef(\p04c).quant_([2, 0.5, 0]); // offset by half a beat
Pdef(\p04b).play;
Pdef(\p04c).play;

// (quant can't be reset in real-time, so we use align to align patterns).
// align takes the same arguments as quant (see helpfile of Pdef)

Pdef(\p04c).align([4, 0, 0]);
Pdef(\p04c).align([4, 0.75, 0]); // offset by 3/4 a beat





// another useful pattern is Tdef (Task patterns)

Tdef(\x, { loop({ Synth(\sine, [\freq, 200+(440.rand)]); 0.25.wait; }) });

TempoClock.default.tempo = 2; // it runs on the default tempo clock

Tdef(\x).play(quant:1);
Tdef(\x).stop;

// and we can redefine the definition "x" in realtime whilst playing
Tdef(\x, { loop({ Synth(\synth1, [\freq, 200+(440.rand)]); 1.wait; }) });

Tdef(\y, { loop({ Synth(\synth1, [\freq, 1200+(440.rand)]); 1.wait; }) });
Tdef(\y).play(quant:1);

Tdef(\y).stop;



// 3) ========= TempoClock and Patterns ==========





// to chage the tempo of the above Patterns, you can use the default TempoClock
// (as you didn't register a TempoClock for the pattern)


TempoClock.default.tempo = 1.2

// But if you want to have each pattern playing different TempoClocks, 
// you need to create 2 clocks and use them to drive each pattern.
// (this way one can do some nice phasing/polyrhytmic stuff)

(
t = TempoClock.new;
u = TempoClock.new;
Pdef(\p04b).play(t);
Pdef(\p04c).play(u);
u.tempo = 1.5
)

// it's hard to get this clear as they are running the same pitch patterns so let's
// redefine one of the patterns:
(
Pdef(\p04c, 
		Pbind(\instrument, \synth1,
					\freq, (Pseq.new(~scale.scramble, inf)).midicps*2, // freq arg
					\dur, Pseq.new([1, 1, 1, 1], inf);  // dur arg
		)
)
)
// and try to change the tempo 
u.tempo = 1;
u.tempo = 1.2;
u.tempo = 1.8;
u.tempo = 3.2;




// 4) ========= Popcorn ==========



SynthDescLib.global.read;
// the poppcorn 

(
~s1 = [72, 70, 72, 67, 64, 67, 60];
~s2 = [72, 74, 75, 74, 75, 74, 72, 74, 72, 74, 72, 70, 72, 67, 64, 67, 72];

~t1 = [0.25, 0.25, 0.25, 0.25, 0.125, 0.25, 0.625];
~t2 = [0.25, 0.25, 0.25, 0.125, 0.25, 0.125, 0.25, 0.25, 0.125, 0.25, 0.125, 0.25, 0.25, 0.25, 0.125, 0.25, 0.5 ];

c = Pdef(\moogy, 
		Pbind(\instrument, \synth1, // using our synth1 synthdef
					\freq, 
						Pseq.new([
							Pseq.new([
								Pseq.new(~s1.midicps, 2),
								Pseq.new(~s2.midicps, 1)
								], 2),
							Pseq.new([
								Pseq.new((~s1+7).midicps, 2),
								Pseq.new((~s2+7).midicps, 1)
								], 2)	
							], inf),
					\dur, Pseq.new([ 
							Pseq.new(~t1, 2),
							Pseq.new(~t2, 1)
							], inf)
		)
);
Pdef(\moogy).play
)



// 5) ========= Clocks in SuperCollider ==========


/*
There are 3 clocks in SuperCollider:
	- SystemClock
	- TemploClock (same as SystemClock but counts in musical tempi)
	- AppClock (musically unreliable, but good for communicating with GUI's)

Routines, Tasks and Patterns can all run by these 3 different clocks.
You pass the clocks as arguments to them.
*/

// Let's have a quick look at the SystemClock:
(
SystemClock.sched(2.0,{ arg time;  
	time.postln; 
	0.5 // wait between next scheduled event
});
)

(
SystemClock.sched(2.0,{ arg time;  
	"HI THERE! Long wait".postln; 
	nil // no wait - no next scheduled event
});
)

// You can also schedule an event for an absolute time:
(
SystemClock.schedAbs( (thisThread.seconds + 4.0).round(1.0),{ arg time;
	("the time is exactly " ++ time.asString 
		++ " seconds since starting SuperCollider").postln;
});
)

// --- The AppClock works pretty much the same but uses different source clocks.




// 6) ========= Using the TempoClock ==========

// We create a TempoClock

t = TempoClock(2); // tempo is 2 beats per second (120 bpm);

// the clock above is now in a variable "t"
// we can now use it to schedule events (at a particular beat in the future):


t.schedAbs(t.beats.ceil, { arg beat, sec; [beat, sec].postln; 1});
t.schedAbs(t.beats.ceil, { arg beat, sec; "ho ho --".post; [beat, sec].postln; 1 });


// and we can change the tempo:
t.tempo_(4)

t.beatDur // we can ask the clock the duration of the beats
t.beats // the beat time of the clock

t.clear



// polyrhythm of 3/4 and 4/4
(
t = TempoClock(4);
t.schedAbs(t.beats.ceil, { arg beat, sec;
	beat.postln;
	if (beat % 2==0, {Synth(\sine, [\freq, 444])});
	if (beat % 4==0, {Synth(\sine, [\freq, 333])});
	if (beat % 3==0, {Synth(\sine, [\freq, 888])});
	1; // repeat
});
)
t.tempo_(6)

// polyrhythm of 5/4 and 4/4
(
t = TempoClock(4);
t.schedAbs(t.beats.ceil, { arg beat, sec;
	if (beat % 2==0, {Synth(\sine, [\freq, 444])});
	if (beat % 4==0, {Synth(\sine, [\freq, 333])});
	if (beat % 5==0, {Synth(\sine, [\freq, 888])});
	1; // repeat
});

)

// polyrhythm of 5/4 and 4/4
(
t = TempoClock(4);

t.schedAbs(t.beats.ceil, { arg beat, sec;
	if (beat % 2==0, {Synth(\sine, [\freq, 60.midicps])});
	if (beat % 4==0, {Synth(\sine, [\freq, 64.midicps])});
	if (beat % 5==0, {Synth(\sine, [\freq, 67.midicps])});
	if (beat % 5==3, {Synth(\sine, [\freq, 72.midicps])});
	1; // repeat
});
)

// polyrhythm of 5/4 and 4/4
(
t = TempoClock(4);

t.schedAbs(t.beats.ceil, { arg beat, sec;
	if (beat % 4==0, {"one".postln; Synth(\sine, [\freq, 60.midicps])});
	if (beat % 4==2, {"two".postln; Synth(\sine, [\freq, 72.midicps])});
	if ((beat % 4==1) || (beat % 4==3), {Synth(\sine, [\freq, 84.midicps])});
	
	if (beat % 5==0, {Synth(\synth1, [\freq, 89.midicps, \amp, 0.2])});
	if (beat % 5==2, {Synth(\synth1, [\freq, 96.midicps, \amp, 0.2])});
	1; // repeat
});
)
//////////

(
SynthDef( \klanks, { arg freqScale = 1.0, amp = 0.1;
	var trig, klan;
	var  p, exc, x, s;
	trig = Impulse.ar( 0 );
	klan = Klank.ar(`[ Array.fill( 16, { linrand(8000.0 ) + 60 }), nil, Array.fill( 16, { rrand( 0.1, 2.0)})], trig, freqScale );
	klan = (klan * amp).softclip;
	DetectSilence.ar( klan, doneAction: 2 );
	Out.ar( 0, Pan2.ar( klan ));
}).store;
)

// polyrhythm of 4/4 and 7/4
(
t = TempoClock(4);

t.schedAbs(t.beats.ceil, { arg beat, sec;
	if (beat % 4==0, {"one".postln; Synth(\klanks, [\freqScale, 40.midicps])});
	if (beat % 4==2, {"two".postln; Synth(\klanks, [\freqScale, 52.midicps])});
	if ((beat % 4==1) || (beat % 4==3), {Synth(\klanks, [\freqScale, 43.midicps])});
	
	if (beat % 7==0, {Synth(\synth1, [\freq, 88.midicps, \amp, 0.2])});
	if (beat % 7==3, {Synth(\synth1, [\freq, 96.midicps, \amp, 0.2])});
	if (beat % 7==5, {Synth(\synth1, [\freq, 86.midicps, \amp, 0.2])});

	1; // repeat
});

)

t.tempo_(8)



// an example showing tempo changes 

(
t = TempoClock(80/60); // 80 bpm
// schedule an event at next whole beat
t.schedAbs(t.beats.ceil, { arg beat, sec; 
	"beat : ".post; beat.postln;
	if (beat % 4==0, { Synth(\sine, [\freq, 60.midicps]) });
	if (beat % 4==2, { Synth(\sine, [\freq, 67.midicps]) });
	if (beat % 0==0, { Synth(\sine, [\freq, 72.midicps]) });
	1 // 1 here means that we are repeating/looping this
});
t.schedAbs(16, { arg beat, sec; 
	" ****  tempochange on beat : ".post; beat.postln; 
	t.tempo_(150/60); // 150 bpm
});
5.do({ |i| // on beats 32, 36, 40, 44, 48 
	t.schedAbs(32+(i*4), { arg beat, sec;
		" ****  tempo is now : ".post; (150-(10*(i+1))).post; " BPM".postln; 
		t.tempo_((150-(10*(i+1)))/60); // going down by 10 bpm each time
	});
});
t.schedAbs(60, { arg beat; t.tempo_(200/60) }); // 200 bpm
t.schedAbs(76, { arg beat;
	t.clear;
	t.schedAbs(t.beats.ceil, { arg beat, sec; 
		"beat : ".post; beat.postln;
		if (beat % 4==0, { Synth(\sine, [\freq, 67.midicps]) });
		if (beat % 4==2, { Synth(\sine, [\freq, 74.midicps]) });
		if (beat % 0==0, { Synth(\sine, [\freq, 79.midicps]) });
		1 // 1 here means that we are repeating/looping this
	});
	t.schedAbs(92, { arg beat; t.stop }); // stop it!
}); // 200 bpm
t.schedAbs(92, { arg beat; t.stop }); // if we tried to stop it here, it would have been "cleared"
)


