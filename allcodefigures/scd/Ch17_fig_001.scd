(
// read a whole sound into memory
s = Server.local;
b = Buffer.read(s,"sounds/a11wlk01.wav"); // remember to free the buffer later.
)
(
SynthDef("help_PlayBuf", { arg out=0,bufnum=0, rate=1;
Out.ar(out,
Pan2.ar(
PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum)*rate, loop: 1),
0)
)
}).add;
)
p=Synth(\help_PlayBuf, [\rate, 0.midiratio,\out, 0, \bufnum, b.bufnum]); // original pitch
p.set(\rate, 12.midiratio);	// one octave up
p.set(\rate, 7.midiratio);	// seven semitones up (fifth interval)

p.free;
p=nil;
b.free;
b=nil;

