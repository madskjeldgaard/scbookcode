Figure 6.1.  Example of a SynthDef
SynthDef(					
	"sine", 				// name of SynthDef	
	{				// function begins with a brace
	 arg gate = 1, out = 0, 		// arguments serve as Control declarations
 freq = 400, amp = 0.4,
 pan = 0, ar = 1, dr = 1;

	var audio;			
	audio = SinOsc.ar(freq, 0, amp);		// start with a SinOsc
	audio = audio * Linen.kr(gate, ar, 1, dr, 2);	// apply an envelope
	audio = Pan2.ar(audio, pan);			// stereo pan, 
	OffsetOut.ar(out,audio);			// to bus out and out+1 
	}
).add;				// make and store a SynthDesc and SynthDef


