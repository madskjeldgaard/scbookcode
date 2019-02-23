Figure 16.25: Playing Wavesets from buffers.

(
	// A wavesets loads the file into a buffer by default.
		b = w.buffer;		
	// Wavesets.prepareSynthDefs loads this synthdef: 
		SynthDef(\wvst0, { arg out = 0, buf = 0, start = 0, length = 441, playRate = 1, sustain = 1, amp=0.2, pan; 
			var phasor = Phasor.ar(0, BufRateScale.ir(buf) * playRate, 0, length) + start;
			var env = EnvGen.ar(Env([amp, amp, 0], [sustain, 0]), doneAction: 2);
			var snd = BufRd.ar(1, buf, phasor) * env;
			
			OffsetOut.ar(out, Pan2.ar(snd, pan));
		}, \ir.dup(8)).add;
)

// play from frame 0 to 440, looped for 0.1 secs, so ca 10 repeats. 
(instrument: \wvst0, bufnum: b.bufnum, start: 0, length: 440, amp: 1, sustain: 0.1).play;

	// get data from waveset
(
var start, length, sustain, repeats = 20; 
#start, length, sustain = w.frameFor(150, 5);

(	instrument: \wvst0, bufnum: b.bufnum, amp: 1, 
	start: start, length: length, sustain: sustain * repeats
).play;
)

	// or even simpler: 
w.eventFor(startWs: 150, numWs: 5, repeats: 20, playRate: 1).put(\amp, 0.5).play;

