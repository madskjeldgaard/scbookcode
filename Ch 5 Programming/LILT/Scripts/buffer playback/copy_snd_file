/* Hack for copying files - developed to overcome problems 
with protected (?) files that I want to give as assignments ...
*/


/*

SynthDef("copy_audio_file", { | inbuf = 0, outbuf = 1 |
	var src;
	src = DiskIn.ar(2, inbuf);
	DiskOut.ar(outbuf, src);
	Out.ar(0, src);
}).send(s);

~inbuf = 0;

~outbuf =  Buffer.cueSoundFile(s,"recordings/diskouttest.aiff", 0, 2, 
	completionMessage:  { | buf | x.addToHeadMsg(nil, [\bufnum, buf.bufnum]) }
);

*/


~start = { Synth("copy_audio_file") };
