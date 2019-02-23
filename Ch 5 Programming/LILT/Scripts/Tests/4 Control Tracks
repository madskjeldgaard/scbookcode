/* (IZ 2007-01-17) { 
For further development, from envelope automation example in examples folder of SC3.
The same example is also in the EnvGen help file.

Thinking of limiting the size of the env synthdef to ca 256 nodes, then
possibly chaining envelopes to achieve playback of tracks with more control nodes.
Meaning as soon as the synth of the control-envelope is stopped, the onEnd command is used to start a new envelope with different data. There is going to be some time jitter because of the time it takes to receive the end notification and to send the 256 or so new numbers for starting a new envelope / track shape. 


======================================================
SynthDef("env", { arg i_outbus=0;
	var env, envctl;
	
	// make a dummy 8 segment envelope
	env = Env.newClear(800);
	
	// create a control argument array
	envctl = Control.names([\env]).kr( env.asArray );
	
	ReplaceOut.kr(i_outbus, EnvGen.kr(envctl, doneAction: 2));
}).load(s);


SynthDef("sine", {
	Out.ar(0, SinOsc.ar(In.kr(0), 0, 0.2));
}).load(s);


s.sendMsg(\c_set, 0, 800);

s.sendMsg(\s_new, \sine, 2001, 1, 0);

e = Env([700,900,900,800], [1,1,1]*0.4, \exp).asArray;
s.sendBundle(nil,[\s_new, \env, 2002, 2, 2001],[\n_setn, 2002, \env, e.size] ++ e);

f = Env([2000,2000,800,2000,900,2000], [1,1,1,1,1]*0.3, \step).asArray;
s.sendBundle(nil,[\s_new, \env, 2003, 2, 2001],[\n_setn, 2003, \env, f.size] ++ f);

s.sendMsg(\n_free, 2001);

f = Env(scramble((1..40) * 200), { 0.1 } ! 40, \step).asArray;
s.sendBundle(nil,[\s_new, \env, 2003, 2, 2001],[\n_setn, 2003, \env, f.size] ++ f);

f = Env(scramble((1..400) * 20), { 0.1 } ! 400, \step).asArray;
s.sendBundle(nil,[\s_new, \env, 2003, 2, 2001],[\n_setn, 2003, \env, f.size] ++ f);

f = Env(scramble((1..400) * 20), { 0.05 } ! 400, \step).asArray;
s.sendBundle(nil,[\s_new, \env, 2003, 2, 2001],[\n_setn, 2003, \env, f.size] ++ f);


f = Env(scramble((1..400) * 20), { 0.1 } ! 400, \sine).asArray;
s.sendBundle(nil,[\s_new, \env, 2003, 2, 2001],[\n_setn, 2003, \env, f.size] ++ f);

f = Env(scramble((1..200) * 20), { 0.05 rrand: 0.5 } ! 200, \linear).asArray;
s.sendBundle(nil,[\s_new, \env, 2003, 2, 2001],[\n_setn, 2003, \env, f.size] ++ f);

f = Env(scramble((1..200) * 20), { 0.05 rrand: 0.5 } ! 200, \step).asArray;
s.sendBundle(nil,[\s_new, \env, 2003, 2, 2001],[\n_setn, 2003, \env, f.size] ++ f);





} */

