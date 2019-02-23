1.23 PMOsc With Offset and Scale

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

