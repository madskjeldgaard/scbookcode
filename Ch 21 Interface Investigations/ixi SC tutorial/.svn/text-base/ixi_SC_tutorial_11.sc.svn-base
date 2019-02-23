

// =====================================================================
// - SuperCollider Basics -
// =====================================================================

// Tutorial 11 - Fast Fourier Transform (FFT)

// =====================================================================
// - ixi audio tutorial - www.ixi-audio.net
// =====================================================================


/*		
		---------------------------------------------------------------
 		Copyright (c) 2005-2008, ixi audio.
 		This work is licensed under a Creative Commons 
		Attribution-NonCommercial-ShareAlike 2.0 England & Wales License.
 		http://creativecommons.org/licenses/by-nc-sa/2.0/uk/
		---------------------------------------------------------------
*/



// ========== Contents of this tutorial ==========

// 	1) Fast Fourier Transform examples
//	2) Language manipulation of bins


/*
Most processing of digital audio takes place in the time domain, typically varying samples in amplitude (ring modulation, waveshaping, distortion) or time (filters or delays). FFT allows us to manipulate sound in the frequency domain, performing various calculations on the independent frequency bins of the signal.

In FFT, windows are taken from the sound signal and analysed one by one. (The window size is typically 512 or 1024 samples creating list of 256 or 512 bins: values of magnitude and phase). The processing (using the PV plugins of SC) is done in the frequency domain and then converted back to the time domain before playback. The windows are normally overlapping with a hanning window to prevent smearing between frequencies.

This way:  FFT -> PV_Ugens -> IFFT

where FFT translates the signal from the time domain into the frequency domain, the PV_UGens perform some functions on the sound and then we use Inverse Fast Fourier Transform (IFFT) to translate the signal back to the time domain.

Frequency bins are a sets of magnitude and phase. The larger the windows, the better pitch resolution we have, but worse precision in time. The smaller the windows, the worse pitch resolution but better precision in time.

sample rate/window size
44100/512  = 86.1328125 	// so the first (lowest) frequency of a 512 window is 86.13 CPS
44100/1024 = 43.06640625 	// so the first (lowest) frequency of a 1024 window is 43.06 CPS

For a  window size of 1024 samples we get 512 bins. 
These are the frequencies of which we will get the mag and phase:
Post << 512.collect({|i| (22050/512)*(i+1)})
(And we would need a 1024 frame Buffer to store that (mag and phase for each freq))

<<< Dan Stowell says:
 The full list of frequencies, including DC, that a 1024-point FFT theoretically generates:
a = 1024.collect({|i| (44100/1024)*i});
Except we ignore the bins above nyquist since they're redundant:
a = a[..512];
Resulting in:
a.postcs;""
>>>

NOTE : some of the examples below might use the FFT plugins from the library of Bhob Rainey
http://bhobrainey.net

*/



// 1) =========  Fast Fourier Transform examples  ==========



// load the buffers (and place your sounds into the buffers)
(
b = Buffer.alloc(s,2048,1);
c = Buffer.alloc(s,2048,1);
//d = Buffer.read(s,"sounds/oceanMONO.aif");
//d = Buffer.read(s,"sounds/insand/camina.aif");
d = Buffer.read(s,"sounds/digireedoo.aif");
e = Buffer.read(s,"sounds/holeMONO.aif");
f = Buffer.read(s, "sounds/a11wlk01.wav");
)




// magnitude above... - Passes only bins whose magnitude is above a threshold
(
SynthDef(\pvmagabove, { arg out=0,bufnum=0, soundBufnum1;
	var in, chain;
	in = PlayBuf.ar(1, soundBufnum1, BufRateScale.kr(soundBufnum1), loop: 1);
	//in = WhiteNoise.ar(0.2);
	chain = FFT(bufnum, in);
	chain = PV_MagAbove(chain, MouseY.kr(1, 300)); 
	Out.ar(out, 0.5 * IFFT(chain));
}).play(s,[\out,0,\bufnum,b.bufnum, \soundBufnum1, e.bufnum]);
)

// brickwall - Clears bins above or below a cutoff point (works as lowpass or highpass filters)
(
SynthDef(\pvbrickwall, { arg out=0,bufnum=0, soundBufnum1;
	var in, chain;
	in = PlayBuf.ar(1, soundBufnum1, BufRateScale.kr(soundBufnum1), loop: 1);
	//in = WhiteNoise.ar(0.2);
	chain = FFT(bufnum, in);
	chain = PV_BrickWall(chain, MouseX.kr(-1,1)); 
	Out.ar(out, 0.5 * IFFT(chain));
}).play(s,[\out,0,\bufnum,b.bufnum, \soundBufnum1, e.bufnum]);
)

// rectcomb - Makes a series of gaps in a spectrum
(
SynthDef(\pvrectcomb, { arg out=0,bufnum=0, soundBufnum1;
	var in, chain;
	in = PlayBuf.ar(1, soundBufnum1, BufRateScale.kr(soundBufnum1), loop: 1);
	//in = WhiteNoise.ar(0.2);
	chain = FFT(bufnum, in);
	chain = PV_RectComb(chain, 8, LFTri.kr(0.097,0,0.4,0.5), 
		LFTri.kr(0.24,0,-0.5,0.5)); 
	Out.ar(out, 0.5 * IFFT(chain));
}).play(s,[\out,0,\bufnum,b.bufnum, \soundBufnum1, e.bufnum]);
)

// rectcomb - controllable with mouse
(
SynthDef(\pvrectcomb, { arg out=0,bufnum=0, soundBufnum1;
	var in, chain;
	in = PlayBuf.ar(1, soundBufnum1, BufRateScale.kr(soundBufnum1), loop: 1);
	//in = WhiteNoise.ar(0.2);
	chain = FFT(bufnum, in);
	chain = PV_RectComb(chain,  MouseX.kr(0, 32), MouseY.kr, 0.2); 
	Out.ar(out, 0.5 * IFFT(chain));
}).play(s,[\out,0,\bufnum,b.bufnum, \soundBufnum1, e.bufnum]);
)


// magfreeze - Freezes magnitudes at current levels when freeze > 0
(
SynthDef(\pvmagfreeze, { arg out=0,bufnum=0, soundBufnum1;
	var in, chain;
	in = PlayBuf.ar(1, soundBufnum1, BufRateScale.kr(soundBufnum1), loop: 1);
	//in = WhiteNoise.ar(0.2);
	chain = FFT(bufnum, in);
	chain = PV_MagFreeze(chain, MouseX.kr(-1, 1) ); // on the right side it freezes
	Out.ar(out, 0.5 * IFFT(chain));
}).play(s,[\out,0,\bufnum,b.bufnum, \soundBufnum1, f.bufnum]);
)


// copy phase - Combines magnitudes of first input and phases of the second input.

(
SynthDef(\pvcopy, { arg out=0, bufnumA=0, bufnumB=1, soundBufnum=2;
	var inA, chainA, inB, chainB, chain;
	inA = PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum), loop: 1);
	inB =  SinOsc.ar(SinOsc.kr(SinOsc.kr(0.08, 0, 6, 6.2).squared, 0, 100, 800));
	chainA = FFT(bufnumA, inA);
	chainB = FFT(bufnumB, inB);
	chain = PV_CopyPhase(chainA, chainB); 
	Out.ar(out,  0.5 * IFFT(chain).dup);
}).play(s,[\out, 0, \bufnumA, b.bufnum, \bufnumB, c.bufnum, \soundBufnum, d.bufnum]);

)

// magnitude smear - Average a bin's magnitude with its neighbors. 

(
SynthDef(\pvmagsmear, { arg out=0, bufnum=0, soundBufnum=2;
	var in, chain;
	in = PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum), loop: 1);
	chain = FFT(bufnum, in);
	chain = PV_MagSmear(chain, MouseX.kr(0, 100)); 
	Out.ar(out, 0.5 * IFFT(chain).dup);
}).play(s,[\out, 0, \bufnum, b.bufnum, \soundBufnum, e.bufnum]);
)



// morph - morphs between two buffers
(
SynthDef(\pvmorph, { arg out=0, bufnumA=0, bufnumB=1, soundBufnum1=2, soundBufnum2=3;
	var inA, chainA, inB, chainB, chain;
	inA = PlayBuf.ar(1, soundBufnum1, BufRateScale.kr(soundBufnum1), loop: 1);
	inB = PlayBuf.ar(1, soundBufnum2, BufRateScale.kr(soundBufnum2), loop: 1);
	chainA = FFT(bufnumA, inA);
	chainB = FFT(bufnumB, inB);
	chain = PV_Morph(chainA, chainB, MouseX.kr); 
	Out.ar(out,  IFFT(chain).dup);
}).play(s,[\out, 0, \bufnumA, b.bufnum, \bufnumB, c.bufnum, \soundBufnum1, d.bufnum, \soundBufnum2, e.bufnum]);
)


// softwipe - Copies low bins from one input and the high bins of the other
(
SynthDef(\pvsoftwipe, { arg out=0, bufnumA=0, bufnumB=1, soundBufnum1=2, soundBufnum2=3;
	var inA, chainA, inB, chainB, chain;
	inA = PlayBuf.ar(1, soundBufnum1, BufRateScale.kr(soundBufnum1), loop: 1);
	inB = PlayBuf.ar(1, soundBufnum2, BufRateScale.kr(soundBufnum2), loop: 1);
	chainA = FFT(bufnumA, inA);
	chainB = FFT(bufnumB, inB);
	chain = PV_SoftWipe(chainA, chainB, MouseX.kr); 
	Out.ar(out,  IFFT(chain).dup);
}).play(s,[\out, 0, \bufnumA, b.bufnum, \bufnumB, c.bufnum, \soundBufnum1, d.bufnum, \soundBufnum2, e.bufnum]);
)


// magminus - subtracting spectral energy - Subtracts buffer B's magnitudes from buffer A.
(
SynthDef(\pvmagminus, { arg out=0, bufnumA=0, bufnumB=1, soundBufnum1=2, soundBufnum2=3;
	var inA, chainA, inB, chainB, chain;
	inA = PlayBuf.ar(1, soundBufnum1, BufRateScale.kr(soundBufnum1), loop: 1);
	inB = PlayBuf.ar(1, soundBufnum2, BufRateScale.kr(soundBufnum2), loop: 1);
	chainA = FFT(bufnumA, inA);
	chainB = FFT(bufnumB, inB);
	chain = PV_MagMinus(chainA, chainB, MouseX.kr(0, 1)); 
	Out.ar(out,  IFFT(chain).dup);
}).play(s,[\out, 0, \bufnumA, b.bufnum, \bufnumB, c.bufnum, \soundBufnum1, d.bufnum, \soundBufnum2, e.bufnum]);
)




// 2) =========  Language manipulation of bins  ==========

/*

The PV_ UGens are blackboxes. We can read their helpfiles, but we don't see clearly 
what they do unless we look at their C++ sourcecode. But what if we want to manipulate 
the bins on the language side?

A new addition to SuperCollider allows this, so instead of:
 
	FFT -> PV_Ugens -> IFFT

as we looked at above, we can now do:

	FFT -> our bin calculations -> IFFT

we do this through pvcollect (Phase Vocoder Collect - see the collect method in 
the Collection helpfile)

pvcollect processes each bin of an FFT chain separatedly. (see pvcollect helpfile)


pvcollect takes a function and it is inside this function that we can have fun with
the magnitude and the phase of the signal (as taken into the frequency domain).

We have magnitude, phase and index to play with. The pvcollect returns an array of
[mag, phase]
We can then use all kinds of algorithms to play with the mag and the phase, for
example using the index as a parameter in the calculations.

*/





(
s.boot.doWhenBooted{
b = Buffer.alloc(s, 1024, 1);
c = Buffer.read(s,"sounds/a11wlk01.wav");
}
)

// spectral delay - here we use a DelayN UGen to delay the bins according to MouseX location
(
{
	var in, chain, v;
	in = PlayBuf.ar(1, c, BufRateScale.kr(c), loop: 1);
	chain = FFT(b, in);
	
	v = MouseX.kr(0.1, 1);
	
	chain = chain.pvcollect(b.numFrames, {|mag, phase, index|
		mag + DelayN.kr(mag, 1, v);
	}, frombin: 0, tobin: 256, zeroothers: 1);
	
	Out.ar(0, 0.5 * IFFT(chain).dup);
}.play(s);
)

// another type of spectral delay where the high frequencies get longer delay times
// this is the trick: 250.do({|i|(i*(250.reciprocal)).postln;})
(
{
	var in, chain, v;
	in = PlayBuf.ar(1, c, BufRateScale.kr(c), loop: 1);
	chain = FFT(b, in);
	
	v = MouseX.kr(0.1, 2);
	
	chain = chain.pvcollect(b.numFrames, {|mag, phase, index|
		mag + DelayN.kr(mag, 1, v*(index*256.reciprocal));
	}, frombin: 0, tobin: 256, zeroothers: 0);
	
	Out.ar(0, 0.5 * IFFT(chain).dup);
}.play(s);
)



// yet another spectral delay where the each bin gets a random delay time
(
{
	var in, chain, v;
	in = PlayBuf.ar(1, c, BufRateScale.kr(c), loop: 1);
	chain = FFT(b, in);
	
	v = MouseX.kr(0.1, 2);
	
	chain = chain.pvcollect(b.numFrames, {|mag, phase, index|
		mag + DelayN.kr(mag, 1, v*1.0.rand);
	}, frombin: 0, tobin: 256, zeroothers: 0);
	
	Out.ar(0, 0.5 * IFFT(chain).dup);
}.play(s);
)

// spectral delay where the delaytimes are modulated by an oscillator
(
{
	var in, chain, v;
	in = PlayBuf.ar(1, c, BufRateScale.kr(c), loop: 1);
	chain = FFT(b, in);
	
	v = MouseX.kr(0.1, 2);
	
	chain = chain.pvcollect(b.numFrames, {|mag, phase, index|
		mag + DelayN.kr(mag, 1, v*SinOsc.ar(0.5).range(0.1,1));// play with Tri or LFSaw, etc.
	}, frombin: 0, tobin: 256, zeroothers: 0);
	
	Out.ar(0, 0.5 * IFFT(chain).dup);
}.play(s);
)



// amplitude controlled with MouseX and phase manipulation with MouseY
(
{
	var in, chain, v;
	in = PlayBuf.ar(1, c, BufRateScale.kr(c), loop: 1);
	chain = FFT(b, in);
		
	chain = chain.pvcollect(b.numFrames, {|mag, phase, index|
		[mag * MouseX.kr(0.5, 2), phase / MouseY.kr(0.5, 30)]
	}, frombin: 0, tobin: 250, zeroothers: 0);
	
	Out.ar(0, 0.5 * IFFT(chain).dup);
}.play(s);
)



// here we add noise to the phase
(
{
	var in, chain, v;
	in = PlayBuf.ar(1, c, BufRateScale.kr(c), loop: 1);
	chain = FFT(b, in);
		
	chain = chain.pvcollect(b.numFrames, {|mag, phase, index|
		[mag, LFNoise0.kr.range(0, 3.14)];
	}, frombin: 0, tobin: 250, zeroothers: 1);
	
	Out.ar(0, 0.5 * IFFT(chain).dup);
}.play(s);
)


// square the magnitude and put a random phase (from 0 to pi (3.14))
(
{
	var in, chain, v;
	in = PlayBuf.ar(1, c, BufRateScale.kr(c), loop: 1);
	chain = FFT(b, in);
	
	chain = chain.pvcollect(b.numFrames, {|mag, phase, index|
		[mag.sqrt, pi.rand];
	}, frombin: 0, tobin: 256, zeroothers: 1);
	
	Out.ar(0, 0.5 * IFFT(chain).dup);
}.play(s);
)

// here we use the index and we subtract it with a LFPar on a slow sweep
(
{
	var in, chain, v;
	in = PlayBuf.ar(1, c, BufRateScale.kr(c), loop: 1);
	chain = FFT(b, in);
	
	chain = chain.pvcollect(b.numFrames, {|mag, phase, index|
		if((index-LFPar.kr(0.1).range(2, b.numFrames/20)).abs < 10, mag, 0); // swept bandpass
	}, frombin: 0, tobin: 250, zeroothers: 0);
	
	Out.ar(0, 0.5 * IFFT(chain).dup);
}.play(s);
)



// now play with it yourself!!!

