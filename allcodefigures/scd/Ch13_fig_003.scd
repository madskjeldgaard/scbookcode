			// figure 13.3 - Continuous data sonification
 
b = Buffer.sendCollection(s, q.getReg(\Northeast), 1); 
(
Ndef(\exec, { |dur = 5, scale=50, offset=200| 
	var vals = PlayBuf.ar(1, b, dur / SampleRate.ir );
	Pan2.ar(
		SinOsc.ar(vals  * scale + offset), 0, 
		EnvGen.kr(Env.linen(0.01, dur, 0.01, 0.2), doneAction: 2)
	);
}).play;
)
	// load other regions into buffer
b.sendCollection(q.getReg(\Midwest));   Ndef(\exec).send;
b.sendCollection(q.getReg(\West));   Ndef(\exec).send;
b.sendCollection(q.getReg(\South));   Ndef(\exec).send;

