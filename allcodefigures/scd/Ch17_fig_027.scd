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
}).add;
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
}).add;
)

// re-evalute the function to create new harmonics (update the SynthDef afterwards)
~harms=~harmsfunc.value(7);
//Send the SynthDef function again, as we have done earlier to obtain new amplitudes for each harmonic

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

