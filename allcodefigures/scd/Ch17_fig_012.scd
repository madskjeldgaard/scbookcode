Figure 17.12 

(
SynthDef("tone2", { arg freq = 440, amp=0.5, gate=1, envdur=1.5;
var sound, env;
env = EnvGen.kr(Env.perc(0.01, envdur), doneAction:2);
sound = Pan2.ar(SinOsc.ar(freq, 0, amp)*env, 0);
Out.ar(0, sound);
}).add;
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

