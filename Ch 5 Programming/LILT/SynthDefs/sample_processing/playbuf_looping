/* IZ 070220

New version: With FreeSelfWhenDone

Play a buffer loaded from audio file, looping whenever loop control input is 1. 
NOTE: Only takes 1 channel files - no stereo or multi track

Doc not done yet : 

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

SynthDef('playbuf_looping', { | out=0, bufnum=0, rate=1.0, trigger=1.0, 
	startPos = 0.0, loop = 0.0, amp = 1 |
	var src;		// numChannels cannot be variable! Set to 1, that is: Mono
	src = PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum), 
		trigger, startPos * BufFrames.kr(bufnum), loop);
	FreeSelfWhenDone.kr(src);
	Out.ar(out, amp * src);
});
