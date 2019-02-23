/* (IZ 2007-10-27) { 
Play a selected buffer with single - shot playbuf SynthDef, triggering it multiple times at random interval and random rates, random starting point and random duration, within controllable range parameters. 

b = Buffer.read(Server.local, "./sounds/a11wlk01.wav");

(
a = SynthDef("playbuf_segment", { |
	out = 0, 
	bufnum = 0, 
	startPos = 0, 
	duration = 1,
	rate = 1,
	amp = 1
	|
	var src, env;
	src = PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum),
		1, startPos * BufFrames.kr(bufnum), 0);
	env = EnvGen.kr(Env([0, 1, 1, 0], [0.03, BufDur.kr(bufnum) / rate * duration - 0.06, 0.03]),
		timeScale: 1 / rate, doneAction: 2); 
//	FreeSelfWhenDone.kr(src);
	Out.ar(out, amp * env * src);
}).play(Server.local, [\bufnum, b.bufnum, \startPos, 0.5, \duration, 0.03, \rate, 0.5]);
)




} */

var samples;
samples = Samples.all[~script.session.server];

~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'bufnum', 0, 1023, 'linear', 1, 0 ],
[ 'rateMin', 0.125, 8, 'exp', 0, 1 ],
[ 'rateRange', 0.0, 8, 'linear', 0, 1 ],
[ 'ampMin', 0, 1, 'amp', 0, 0.1 ],
[ 'ampRange', 0, 1, 'amp', 0, 0.5 ],
[ 'waitMin', 0.1, 120, 'exponential', 0, 0.5],
[ 'waitRange', 0.0000001, 120, 'exponential', 0, 1.5],
[ 'durMin', 0.001, 10, 'exponential', 0, 0.1],	// max 10 repetitions
[ 'durRange', 0, 10, 'linear', 0, 0.2],	// + max 10 repetitions random variation

];


~script.routine {
	var sample, sampleRate, sample_dur, play_dur, bufnum;
	loop {
		bufnum = ~bufnum;
		Synth("playbuf_segment", [
			'out', ~out,
			'bufnum', ~bufnum,
			'rate', ~rateMin.asFloat rrand: (~rateMin + ~rateRange),
			'startPos', 0.0 rrand: 1.0,
			'length', ~durMin rrand: (~durMin + ~durRange),
			'amp', ~ampMin rrand: (~ampMin + ~ampRange)
		]);
		wait(~waitMin rrand: (~waitMin + ~waitRange));
	};

	
};

