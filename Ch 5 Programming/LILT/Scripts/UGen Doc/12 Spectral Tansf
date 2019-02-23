/* (IZ 2005-09-03)

Spectral Transforms

	PV_BinScramble
	PV_MagFreeze
	PV_Diffuser
	PV_RandWipe
	PV_RectComb 
	PV_RectComb2 
	PV_RandComb
	PV_MagMul
	PV_Add	PV_Mul
	PV_Min	PV_Max
	PV_CopyPhase
	PV_BinWipe
	PV_BrickWall
	PV_PhaseShift
	PV_MagSquared
	PV_PhaseShift270
	PV_PhaseShift90
	PV_MagNoise
	PV_BinShift
	PV_MagShift
	PV_MagSmear
	PV_MagAbove
	PV_LocalMax
	PV_MagClip
	PV_MagBelow
	IFFT	FFT
	Convolution
	Convolution2
	PV_ConformalMap
*/
/* Collected examples modified from help files: 

(
//
SynthDef("help-binScramble", { arg out=0, bufnum=0, soundBufnum=2;
	var in, chain;
	in = LFPulse.ar(440, 0, 0.5, 0.1);
	chain = FFT(bufnum, in);
	chain = PV_BinScramble(chain, MouseX.kr , MouseY.kr, Impulse.kr(50) ); 
	Out.ar(out, 0.5 * IFFT(chain).dup);
}).play(s,[\out, 0, \bufnum, b.bufnum, \soundBufnum, c.bufnum]);
)


*/
