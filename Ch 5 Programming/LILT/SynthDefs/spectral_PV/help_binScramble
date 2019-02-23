/* (IZ 2005-10-28) { 
} */

SynthDef("help-binScramble", { arg out=0, bufnum=0, fftBufnum=2;
	var in, chain;
	in = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), loop: 1);
	chain = FFT(fftBufnum, in);
	chain = PV_BinScramble(chain, MouseX.kr , 0.1, MouseY.kr > 0.5 ); 
	Out.ar(out, 0.5 * IFFT(chain).dup);
});
