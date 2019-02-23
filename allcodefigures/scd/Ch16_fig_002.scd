Figure 16.2: Perception of short silences.

(
p = ProxySpace.push;

~source = { SinOsc.ar * 0.1 };
~silence = { |silDur=0.01| 
	EnvGen.ar(
		Env([0, 1, 1, 0, 0, 1, 1, 0], [0.01, 2, 0.001, silDur, 0.001, 2, 0.01]), 
		doneAction: 2) ! 2
};
~listen = ~source * ~silence;
~listen.play;
)

~silence.spawn([\silDur, 0.001]); // sounds like an added pulse 
~silence.spawn([\silDur, 0.003]);
~silence.spawn([\silDur, 0.01]);
~silence.spawn([\silDur, 0.03]);	  // a pause in the sound

	// try the same examples with noise:
~source = { WhiteNoise.ar * 0.1 };	

p.pop

