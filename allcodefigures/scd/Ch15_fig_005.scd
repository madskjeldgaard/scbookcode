Figure 15.5
b = Buffer.alloc(s,1024,1); //for sampling rates 44100 and 48000
//b = Buffer.alloc(s,2048,1); //for sampling rates 88200 and 96000

//track audio in (try clapping a beat or beatboxing, but allow up to 6 seconds for tracking to begin); events will be spawned at quarter, eighth and sixteenth note rates
(
SynthDef(\beattrack,{	
	var trackb,trackh,trackq,tempo;
	var source;
	var bsound,hsound,qsound;
	
	source = SoundIn.ar(0);
	
	#trackb,trackh,trackq,tempo = BeatTrack.kr(FFT(b.bufnum, source));
	
	bsound = Pan2.ar(LPF.ar(WhiteNoise.ar*(Decay.kr(trackb,0.05)),1000),0.0);
	
	hsound = Pan2.ar(BPF.ar(WhiteNoise.ar*(Decay.kr(trackh,0.05)),3000,0.66),-0.5);
	
	qsound = Pan2.ar(HPF.ar(WhiteNoise.ar*(Decay.kr(trackq,0.05)),5000),0.5);
	
	Out.ar(0, bsound+hsound+qsound);
}).add;
)

x = Synth(\beattrack); // Go!

x.free;
b.free; // Free the buffer

