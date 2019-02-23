1.24 PMOsc With Sample and Hold (Latch) 

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


