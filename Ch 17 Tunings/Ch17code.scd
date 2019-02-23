//////////////////////////////////////////////////////
Figure  17.1 

(
// read a whole sound into memory
s = Server.local;
b = Buffer.read(s,"sounds/a11wlk01.wav"); // remember to free the buffer later.
)
(
SynthDef("help_PlayBuf", { arg out=0,bufnum=0, rate=1;
Out.ar(out,
Pan2.ar(
PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum)*rate, loop: 1),
0)
)
}).send(s,[\out, 0, \bufnum, b.bufnum]);
)
p=Synth(\help_PlayBuf, [\rate, 0.midiratio]); // original pitch
p.set(\rate, 12.midiratio);	// one octave up
p.set(\rate, 7.midiratio);	// seven semitones up (fifth interval)

b.free;
b=nil;
p.free;
p=nil;
//////////////////////////////////////////////////////
 Figure  17.2 

(
Pbind(
\midinote, Pseq([0, 2, 3, 5, 7]+60, inf),
\dur, 0.3
).play
)
//////////////////////////////////////////////////////
 Figure  17.3 

(
Pbind(
\midinote, Pseq([0, 2, 3, 5.25, 7.5]+60, inf),
\dur, 0.3
).play
)
//////////////////////////////////////////////////////
 Figure  17.4 

(
Pbind(
\note, Pseq([0,2,4,5,7,9,11,12], inf),
\dur, 0.3,
\octave, 5
).play
)
//////////////////////////////////////////////////////
 Figure  17.5 

(
Pbind(
\degree, Pseq([0,1,2,3,4,5,6,7], inf),
\dur, 0.3,
\octave, 5
).play
)
//////////////////////////////////////////////////////
 Figure  17.6 

(
Pbind(
\degree, Pseq([0,1,2,3,4,5,6,7], inf),
\dur, 0.3,
\octave, 5,
\scale, (0..11)
).play
)
//////////////////////////////////////////////////////
 Figure  17.7 

(
Pbind(
\degree, Pseq([0, 2, 2.1, 2.05], inf),
\dur, 0.3,
\scale, (0..11),
\octave, 5
).play
)
//////////////////////////////////////////////////////
 Figure  17.8 

(
Pbind(
\note, Pwhite(-6,9),
\dur, 0.3,
\sustain, 1.1,
\stepsPerOctave, 7
).play
)
//////////////////////////////////////////////////////
 Figure  17.9 
(
e=Pbind(
\degree, Pwhite(-3, 7),
\dur, 0.25,
\stepsPerOctave, 21,
\sustain, 1.1,
\scale, [0, 4, 8, 11, 14, 17]
).play;
)
//////////////////////////////////////////////////////
 Figure  17.10 

(
// previous example should still be running
e.stream=Pbind(
\degree, Pwhite(-3, 7),
\dur, 0.25,
\stepsPerOctave, 21,
\sustain, 1.1,
\scale, [0, 3, 5, 8, 10, 13]
).asStream;
)
//////////////////////////////////////////////////////
 Figure  17.11 

(
var stepsperoctave=3;
Array.fill(stepsperoctave, {arg i; 2.pow(i/stepsperoctave)});
)

Using a function to calculate the value at a chosen degree
(
f = {|degree, steps|
2.pow(degree/steps)
};
)
// 	degree 0
f.(0, 3);
// 	degree 1
f.(1, 3);
// 	degree 2
f.(2, 3);

The function is modified to multiply the value by a root frequency in Hertz
(
f = {|degree, steps, root=440|
2.pow(degree/steps)*root
};
)
//	12 notes per octave, degrees 0,1 and 12
f.(0,12)
f.(1, 12)
f.(12, 12)

//	14 notes per octave, degrees 0,1, 12 and 14
f.(0,14)
f.(1, 14)
f.(12, 14)
f.(14,14)
//////////////////////////////////////////////////////

 Figure 17.12 

(
SynthDef("tone2", { arg freq = 440, amp=0.5, gate=1, envdur=1.5;
var sound, env;
env = EnvGen.kr(Env.perc(0.01, envdur), doneAction:2);
sound = Pan2.ar(SinOsc.ar(freq, 0, amp)*env, 0);
Out.ar(0, sound);
}).load(s);
)
(
a=[ 1, 1.030303030303, 1.0606060606061, 1.1212121212121, 1.3636363636364, 1.6060606060606, 2 ]*220;

// Play the all the notes of the tuning
e=Pbind(
\freq, Pseq( a, inf),
\dur, 0.2,
\amp, 0.5,
\sustain, 0.6,
\instrument, \tone2
).play
)
// Choose the notes randomly
(
e.stream=Pbind(
\freq, Pn(Prand( a, 1)),
\dur, 0.2,
\amp, 0.5,
\sustain, 0.6,
\instrument, \tone2
).asStream
)
//////////////////////////////////////////////////////
 Figure 17.13 

(
~rationames=[1/1, 8/7, 7/6, 6/5, 5/4, 4/3, 7/5, 10/7, 3/2, 8/5, 5/3, 12/7, 7/4];
~scale=[0,3,5,8,10,12];
e = Pbind(
\freq, Pseq([
Pfunc({
(~rationames.wrapAt(~scale).[~scale.size.rand])*440
})
],inf),
\dur, 0.25,
\amp, 0.5,
\instrument, \tone2
).play; // returns an EventStream
)
// set a new scale
~scale=[0,2,5,7,9,11];
~scale=[0,1,3,5,6,8,9];
~scale=[0,3,5,8,10,12];
//////////////////////////////////////////////////////
 Figure 17.14 

(
var n, buts, synths, ratios, rationames;
w = SCWindow("tonality diamond", Rect(200,500,420,150));
w.view.decorator = FlowLayout(w.view.bounds);

rationames=[
"7/4", "3/2","5/4","1/1",
"7/5","6/5","1/1","8/5",
"7/6","1/1","5/3","4/3",
"1/1","12/7","10/7","8/7"
];

n=rationames.size;

n.do({ |i|
SCButton(w, Rect(20,20+(i*30),100,30))
.states_([[rationames[i], Color.black, 
if((rationames[i])=="1/1", {Color.red},{Color.yellow})
]
])
.action_({ arg butt;
Synth(\tone2, [\freq, ((rationames[i]).interpret)*440]);

})
});
w.front;
)
//////////////////////////////////////////////////////
 Figure 17.16 

(
a=Pbind(
\degree, Pwhite(0, 12),
\dur, 0.5,
\octave, 5,
\amp, 0.4,
\stepsPerOctave, 12,
\instrument, \tone2
);
b=Pbind(
\degree, Pwhite(0, 14),
\dur, 0.25,
\octave, 4,
\amp, 0.4,
\stepsPerOctave, 14,
\instrument, \tone2
);
Ppar([a, b]).play;
)
//////////////////////////////////////////////////////
 Figure 17.17 

(
a=Pbind(
\degree, Pfunc({
[
[0, 6, 12].choose, 12.rand
].choose;
}),
\dur, 0.5,
\octave, 4,
\amp, 0.4,
\stepsPerOctave, 12,
\instrument, \tone2
);
b=Pbind(
\degree, Pfunc({
[
[0, 7, 14].choose, 14.rand
].choose;
}),
\dur, 0.25,
\octave, 5,
\amp, 0.3,
\stepsPerOctave, 14,
\instrument, \tone2
);
Ppar([a, b]).play;
)
//////////////////////////////////////////////////////
 Figure 17.18 

(
~tolerance={|a, b, t, max|
var c, d;
c=[];
d=[];
a.do({ |aitem, aindex|
b.do({ |bitem, bindex|
var x;
x = (aitem-bitem).abs;
if( (x > t) && (x < max),
{
c=c.add(aindex);
d=d.add(bindex);
//[aitem, bitem].post; " out of tune ".post; [aindex, bindex].postln;
//" ".postln;
})
})
});
[(0..a.size).difference(c), (0..b.size).difference(d)];
};
)

(
// use the function function with two tunings
var mintreshold, maxtreshold, int;

// two different equal tunings expressed linearly
a=Array.fill(12, { |i| (1/12)*(i) });
b=Array.fill(21, { |i| (1/21)*(i) });

int=1/21;				// smallest interval
mintreshold=int*0.15;
maxtreshold=int*0.85;
/*
intervals inferior to mintreshold are in tune
intervals between mintreshold and maxtreshold are out of tune
intervals superior to maxtreshold are in tune
*/

// print a list of notes from the two tunings which form a dissonant interval
~tolerance.value(a, b, mintreshold, maxtreshold);
)
//////////////////////////////////////////////////////
 Figure 17.19 

(
a=Pbind(
\degree, Pfunc({
// notes which clash with the other tuning have been removed
[0,4,8,12].choose
}),
\dur, 0.5,
\octave, 5,
\amp, 0.4,
\sustain, 0.85,
\stepsPerOctave, 12,
\instrument, \tone2
).play;
b=Pbind(
// notes which clash with the other tuning have been removed
\degree, Pfunc({
[0,7,14,21].choose
}),
\dur, 0.25,
\octave, 4,
\amp, 0.35,
\sustain, 0.85,
\stepsPerOctave, 21,
\instrument, \tone2
).play;
)

(
a.stream=Pbind(
// introducing more notes from that tuning after having changed the threshold
\degree, Pfunc({
[ 0, 1, 4, 7, 8, 9, 9, 12 ].choose
}),
\dur, 0.75,
\octave, 5,
\amp, 0.4,
\sustain, 0.85,
\stepsPerOctave, 12,
\instrument, \tone2
).asStream;
)
//////////////////////////////////////////////////////
 Figure 17.20 

(
f = {|steps| Array.fill(steps, { |i| 2.pow(i/steps) }) };
//	Calculation of the twelve equal-note temperament
x =  f.(12);
//	mapping the tuning to a new range beyond an octave
y = x.linlin(1, 2, 1, 2.25);
//	multiplying by a root frequency
a=y*440;

Pbind(
\freq, Pfunc({ a.choose }),
\dur, 0.25,
\octave, 5,
\amp, 0.5,
\sustain, 1.1,
\instrument, \tone2
).play
)
//////////////////////////////////////////////////////
 Figure 17.21 

(
Pbind(
\degree, Pwhite(0, 18),
\dur, 0.3,
\sustain, 1.0,
\amp, 0.5,
\sustain, 1.1,
\instrument, \tone2,
\stepsPerOctave,  18.809
).play;
)
//////////////////////////////////////////////////////



 Figure 17.22 

(
a=[ 1, 1.09375, 1.1875, 1.28125, 1.375, 1.46875, 1.5625, 1.65625];
b=a*440;
e=Pbind(
\freq, Pseq( b, inf),
\dur, 0.2,
\amp, 0.5,
\instrument, \tone2,
\sustain, 0.6
).play
)
// play in a different order
(
e.stream=Pbind(
\freq, Pn(Pshuf( b, 1)),
\dur, 0.2,
\amp, 0.5,
\instrument, \tone2,
\sustain, 0.6
).asStream
)
//////////////////////////////////////////////////////
 Figure 17.23 

(
// F. Mogini pattern-based Tuning - 2000.
x=880;

Pbind(
\freq, Pn(
Plazy({
if(x<=150, {x=x*2});
if(x>=2000, {x=x/2});
x=[

x*[1.1428,  1.36, 1.26].choose,
x/[1.1428,  1.36, 1.26].choose

].choose
})
),
\dur, 0.14,
\sustain, 0.8,
\cutoff, Pfunc({ 1.0.rand})
).play;
)
//////////////////////////////////////////////////////




 Figure 17.24 

a=(1..16)*100
(
e=Pbind(
\freq, Pseq( a, inf),
\dur, 0.2
).play
)

// a beautiful tuning system can be created from the harmonic series.
(
e.stream=Pbind(
\freq, Pn(Pshuf( a, 1)),
\dur, 0.2,
\sustain, 0.8
).asStream
)
//////////////////////////////////////////////////////
 Figure 17.25

a=(1..11);
(
a.size.do({ |i|
var x=a[i];
while({x>2},{x=x/2});
a.put(i, x)
});
)

b=a.asSet.asArray.sort;
(
e=Pbind(
\freq, Pn(Pshuf( b*440, 1)),
\dur, 0.2,
\sustain, 0.8
).play
)
//////////////////////////////////////////////////////
 Figure 17.26 

a=(1..8);
b=(9..16);
(
a.size.do({ |i|
var x=a[i];
var y=b[i];
// harmonics below 8 remain in the first octave
while({x>2},{x=x/2});
// harmonics above 9 remain in the second octave
while({y>4},{y=y/2});
a.put(i, x);
b.put(i, y);
});
)
a;
b;
c=(a++b).asSet.asArray.sort;
c;

(
e=Pbind(
\freq, Pn(Pshuf( c*200, 1)),
\dur, 0.2,
\sustain, 1.1
).play
)
//////////////////////////////////////////////////////
 Figure 17.27 

(
// 	a function to expand the tuning from one octave to four octaves
~harmsfunc={arg stepsperoctave=7;
var harms;
// calculate each note from the tuning
harms=Array.fill(stepsperoctave, {arg i; 2.pow(i/stepsperoctave)});
harms.size.do({ |i|
if( 0.6.coin, {
// multiply some of the notes to create higher harmonics
harms.put(i, (harms[i])*[1,2,4,8].choose )
})
});
harms.sort;
};
)

//  create an array of virtual harmonics, seven equal-note temperament
~harms=~harmsfunc.value(7);

(
// send a synth definition with some partials and the current value of ~harms

SynthDef(\cfstring1, { arg out=0, freq = 360, gate = 1, pan, amp=0.8;
var sound, eg, fc, osc, a, b, w;
var harms, amps;

// use the harmonics previously calculated
harms=~harms;
//	create new amplitudes for each harmonic
amps=Array.fill(harms.size,{1.0.rand}).normalizeSum*0.1;

osc = Array.fill(harms.size, { |i|
SinOsc.ar(freq * harms[i], 0, amps[i] );
})++[SinOsc.ar(freq , 0, amp*(0.5.rand+0.2) ), SinOsc.ar(freq*2 , 0, amp*(0.5.rand+0.15) )];

eg = EnvGen.kr(Env.asr(0.02,1,1), gate, doneAction:2);

sound = Pan2.ar(eg * Mix.ar(osc), pan);
Out.ar(0, sound);
}).store;
)

(
e=Pbind(
\instrument, \cfstring1,
// frequencies are rpeated so we can notice the effect of harmonics
\degree, Pseq([0,1,2,3,4,5,6,7],inf),
\dur, 0.25,
\stepsPerOctave, 7,
\octave, 4,
\pan, Pfunc({0.5.rand2 })
).play;
)

Send the SynthDef function again to obtain new amplitudes for each harmonic
(
// send a synth definition with some partials and the current value of ~harms

SynthDef(\cfstring1, { arg out=0, freq = 360, gate = 1, pan, amp=0.8;
var sound, eg, fc, osc, a, b, w;
var harms, amps;

// use the harmonics previously calculated
harms=~harms;
//	create new amplitudes for each harmonic
amps=Array.fill(harms.size,{1.0.rand}).normalizeSum*0.1;

osc = Array.fill(harms.size, { |i|
SinOsc.ar(freq * harms[i], 0, amps[i] );
})++[SinOsc.ar(freq , 0, amp*(0.5.rand+0.2) ), SinOsc.ar(freq*2 , 0, amp*(0.5.rand+0.15) )];

eg = EnvGen.kr(Env.asr(0.02,1,1), gate, doneAction:2);

sound = Pan2.ar(eg * Mix.ar(osc), pan);
Out.ar(0, sound);
}).store;
)

// re-evalute the function to create new harmonics (update the SynthDef afterwards)
~harms=~harmsfunc.value(7);
Send the SynthDef function again, as we have done earlier to obtain new amplitudes for each harmonic

// finally playing a random melody to make it less repetitive
(
e.stream=Pbind(
\instrument, \cfstring1,
// frequencies are repeated so we can notice the effect of harmonics
\degree,  Pwhite(0, 7),
\dur, 0.25,
\stepsPerOctave, 7,
\octave, 4,
\pan, Pfunc({0.5.rand2 })
).asStream;
)
// we could develop further and re-write the SynthDef with a partial argument 
// and also change the partials directly from Pbind

//////////////////////////////////////////////////////
 Figure 17.28 

(
var w, keys, steps, octaves;

w = SCWindow.new.name="Custom keyboard: 7 steps per octave";
steps = 7;
octaves= 2;
// seven steps per octave;
a=Array.fill(7, { |i| (1/7)*(i) })+1;
b= a++(a*2);

c=Synth(\default, [\amp, 0]);

keys=Array.fill(steps*octaves,{ |i|

SCButton(w, Rect(20+(i*22),20,20,50))
.states_([
if(i.mod(steps)==0,{
[i.asString, Color.black, Color.red]},{
[i.asString, Color.black, Color.yellow]});

])
.action_({ arg butt;
c.set(\freq,b[i]*220, \amp, 0.25)
});
});

w.front;
)
//////////////////////////////////////////////////////

