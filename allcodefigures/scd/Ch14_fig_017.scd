Figure 14.17


s.boot;
(
b = Buffer.alloc(s,2048,1);
c = Buffer.read(s,"sounds/a11wlk01.wav");
d = Buffer.alloc(s,2048,1);
)

(
//make stereo from mono
// MouseX controls decorrelation
x = SynthDef("PV_DecorrelateStereo", { arg out=0, bufnum=0, bufnum2, soundBufnum=2;
 var in, chain, chain2;
 in = PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum), loop: 1);
 chain = FFT(bufnum, in);
 chain2 = PV_Copy(chain, bufnum2);
 chain = PV_Decorrelate([chain, chain2], 1,  MouseX.kr);
 Out.ar(out, 0.5 * IFFT(chain));
}).play(s,[\out, 0, \bufnum, b, \bufnum2, d, \soundBufnum, c]);
)

x.free; [b, c, d].do(_.free);



