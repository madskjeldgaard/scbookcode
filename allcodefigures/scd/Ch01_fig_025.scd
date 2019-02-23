1.25 It's Just a Bell

(
{ // it's just a bell
var burst, burstEnv, bell, delay, dry, 
burstFreq = 500, freqs, amps, rings;
burstEnv = EnvGen.kr(Env.perc(0, 0.05),
				Dust.kr(1/5), 0.1);
// burstEnv.poll(100, "env");
burst = SinOsc.ar(freq: burstFreq,
	mul: burstEnv);
// burst.poll(100, "burst");
freqs = Array.fill(10, {exprand(100, 1000)});
amps = Array.fill(10, {rrand(0.01, 0.1)});
rings = Array.fill(10, {rrand(1.0, 6.0)});
// [freqs, amps, rings].round(0.01).postln;
// "safe" values
// freqs = [100, 200, 300, 400];
// amps = [1, 1, 1, 1];
// rings = [1, 1, 1, 1];

bell = Pan2.ar(
	Klank.ar(`[freqs, amps, rings], burst), 
	rrand(-1.0, 1.0)
);

delay = AllpassN.ar(bell, 2.5, 
	[LFNoise1.kr(7, 1.5, 1.6), LFNoise1.kr(7, 1.5, 1.6)], 
	1, mul: 0.8);
bell 
+ delay
// + SinOsc.ar(mul: LFPulse.kr(1) * 0.05);
}.play
)

