Figure 14.1

(
/// basic setup
s = Server.local.boot;

s.doWhenBooted({
	SynthDef( \nicepoc, { |out=0,freq=440,amp=0.1,dur=0.3|
		Out.ar( out, SinOsc.ar( freq, mul: amp )*EnvGen.kr( Env.perc(0.05,1), timeScale: dur, doneAction:2 ) )
	}).add;
});
)

// mono, 1 channel:
(
p = Pbind(
     \degree, Pseq([0, 3, 5, 6, 7],5),
     \dur, 0.2,
     \instrument, \nicepoc
).play;
)

p.stop;

// multiple mono:
// the melody gets played on both channels, the second note in the pattern differs,
// so when listening to it, the space "spreads" out
(
p = Pbind(
     \degree, Pseq([0 ,[3,4], 5, 6, 7],5),
     \out, [0,1],
     \dur, 0.2,
     \instrument, \nicepoc
).play;
)

p.stop;

