/* IZ 050626
 
Play a buffer loaded from audio file on a single shot. 
NOTE: Only takes 1 channel files - no stereo or multi track

out: The channel out of which to play back.
bufnum: Number of buffer to play back.
rate: rate of playback. Cannot be changed after start
	1 = original rate of buffer
	2 = twice as fast
	0.5 = half that fast 
	etc. 
amp: scaling of playback amplitude. Dynamically settable
stop: setting stop to 1 will immediately stop the playback. 
trigAt: notify client when portion of file has been played
	acceptable values: Any number between 0 and 1. 
	0: notify immediately
	0.5: notify exactly midways in the playback of the buffer
	0.75: notify 3/4 along the way.
	1: notify exactly at the end. 
	etc.

To chain a buffer after the end of this one, use notification by 
NodeWatcher.register(aSynth), or use the Node-onEnd utility method
that is found in the Lilt library by IZ. 

Example: 
s = Server.local;
s.sendMsg("/b_allocRead", 0, "sounds/a11wlk01.wav");
a = Synth("PlayBuf");
a = Synth("PlayBuf", [\rate, 0.5]);
a = Synth("PlayBuf", [\rate, 1.5]);

20.do { |i| Synth("PlayBuf", [\rate, 0.1 + (i/500)]); }

-----

TrigWatcher(Synth("PlayBuf", [\trigAt, 0.5]), 0, { 
	"done".postln; });


////////////////// HISTORY: 
- 050627 Adding argument "trigAt" and Trig to enable notification of 
  client when a certain portion of the buffer has been played.

*/

SynthDef("playbuf_1_const", { arg out=0, bufnum=0, rate = 1, amp = 1, 
	stop = 0, trigAt = 1, trigID = 0;
	var env, src, index, dur;
	env = Env([1, 0], [0.01], releaseNode: 0);
	dur = BufDur.ir(bufnum) / rate;
	index = Line.ar(0, 1, dur);
	SendTrig.kr(trigAt <= A2K.kr(index), trigID, (dur) * (1 - trigAt));
	src = BufRd.ar(1, bufnum, index * BufFrames.ir(bufnum));
	Out.ar(out, src * EnvGen.kr(env, (index + stop) < 1, doneAction: 2));
}, [\kr, \ir, \ir, \kr, \kr, \ir,  \ir]);

/* OLD STUFF - for scrapping 

a = {	| amp = 0.1, dur = 1, stop = 0 |
	var env, gate = 0;
	var index;
	env = Env([1, 0], [0.01], releaseNode: 0);
	index = Line.ar(0, 1, dur);
	Out.ar(0, SinOsc.ar(440, 0, amp * EnvGen.kr(env, (index + stop) < 1, doneAction: 2)));
}.play;

a.set(\stop, 0);

////////////// 
s.sendMsg("/b_allocRead", 0, "sounds/a11wlk01.wav");
b = Synth("playbuf", [\bufnum, ]);
a = {	| amp = 0.1, rate = 1, stop = 0 |
	var env, src;
	var index;
	env = Env([1, 0], [0.01], releaseNode: 0);
	index = Line.ar(0, 1, rate * BufDur.ir(0));
	src = BufRd.ar(1, 0, index * BufFrames.ir(0));
	Out.ar(0, src * EnvGen.kr(env, (index + stop) < 1, doneAction: 2));
}.play;

a.set(\rate, 4);
a.set(\stop, 1);

//////////////////////////////////////////////////////////////////////

SynthDef("PlayBuf",{ arg out=0,bufnum=0, duration = 1, amp = 1;
	var env; 
	env = Env([1,1,
	Out.ar(out,
		PlayBuf.ar(1,bufnum, BufRateScale.kr(bufnum))
	)
});

*/