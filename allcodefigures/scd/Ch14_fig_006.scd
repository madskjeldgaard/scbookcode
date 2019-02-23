Figure 14.6

 // Create a buffer.
b=Buffer.alloc(s, s.sampleRate, 1); //enough space for one second of mono audio 

// Write to the Buffer with BufWr, read using several taps and mix them together:
(
SynthDef(\helpTap, {|bufnum|
 var source, capture;
 source= Impulse.ar(1);
 capture= BufWr.ar(source, bufnum, Phasor.ar(0,1, 0, BufFrames.ir(bufnum),1));
	 Out.ar(0, Mix.new([1,0.95,0.94,0.93,0.8,0.4,0.4]*Tap.ar(bufnum, 1, [0.04,0.1,0.22,0.88,0.9,0.91,0.93])));
}).add;
)

x=Synth(\helpTap,[\bufnum, b.bufnum]);
x.free;

( // alternate source; use headphones to avoid feedback
SynthDef(\helpTap2, {|bufnum|
 var source, capture;
 source= SoundIn.ar(0);
 capture= BufWr.ar(source, bufnum, Phasor.ar(0,1, 0, BufFrames.ir(bufnum),1));
	 Out.ar(0, Mix.new([1,0.95,0.94,0.93,0.8,0.4,0.4]*Tap.ar(bufnum, 1, [0.04,0.1,0.22,0.88,0.9,0.91,0.93])));
}).add;
)

x=Synth(\helpTap2,[\bufnum, b.bufnum]);
x.free;

// free buffer:
b.free;


