// Figure 3.7 Executing one line at a time

(
// here's a synthdef that allows us to play from a buffer, with a fadeout
SynthDef("playbuf", { arg out = 0, buf, gate = 1;
	Out.ar(out,
		PlayBuf.ar(1, buf, BufRateScale.kr(buf), loop: 1.0) 
			* Linen.kr(gate, doneAction: 2); // release synth when fade done
	)
}).add;
// load all the paths in the sounds/ folder into buffers
~someSounds = "sounds/*".pathMatch.collect{ |path |  Buffer.read(s, path)}; 
)
// now here's the score, so to speak
// execute these one line at a time
~nowPlaying = Synth("playbuf", [buf: ~someSounds[0]]);
~nowPlaying.release; ~nowPlaying = Synth("playbuf", [buf: ~someSounds[1]]);
~nowPlaying.release; ~nowPlaying = Synth("playbuf", [buf: ~someSounds[2]]);
~nowPlaying.release;
// free the buffer memory
~someSoundsBuffered.do(_.free);
 
 

