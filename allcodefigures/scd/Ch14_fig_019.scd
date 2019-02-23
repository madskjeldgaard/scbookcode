Figure 14.19


Server.default = s = Server.internal;
s.boot;
(
n = 512; // number of bins
b = Buffer.alloc(s, n, 1);
c = Buffer.alloc(s, n, 1);

// create arrays of magnitude scalars and load them to buffers
d = Array.fill(n, {1.0.linrand});
e = 1.0 - d;
d = Buffer.loadCollection(s, d);
e = Buffer.loadCollection(s, e);

f = Buffer.read(s,"sounds/a11wlk01.wav");
)

(
x = SynthDef("spectral diffusion", { arg out=0, analBuf, analBufCopy, scalBuf1, scalBuf2, soundBuf;
 var chain1, chain2;
 chain1 = FFT(analBuf, PlayBuf.ar(1, soundBuf, BufRateScale.kr(soundBuf), loop: 1));
 chain2 = PV_Copy(chain1, analBufCopy); // copy the initial analysis
 chain1 = PV_MagMul(chain1, scalBuf1);
 chain2 = PV_MagMul(chain2, scalBuf2); 
 Out.ar(out,  0.5 * IFFT([chain1, chain2]));
}).play(s,[out: 0, analBuf: b, analBufCopy: c, scalBuf1: d, scalBuf2: e, soundBuf: f]);
s.scope; // compare the two channels
)

// execute this multiple times to change the distribution
(
g = Array.fill(n, {1.0.linrand});
h = 1 - g;
d.loadCollection(g);
e.loadCollection(h);
)

x.free; [b, c, d, e, f].do(_.free);


