

SynthDef("PV_LocalMax_Amp", { arg in=0, out=0, bufnum=0, 
	// Threshold: 0 - 50
	threshold=25, amp=0.5;
	var chain;
 	chain = FFT(bufnum, In.ar(in));
	chain = PV_LocalMax(chain,  threshold);
	Out.ar(out, amp*IFFT(chain));
});
