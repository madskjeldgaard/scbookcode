1.5 Variables

(
// run this first
p = { // make p equal to this function
r = Line.kr(1, 20, 60); // rate
// r = LFTri.kr(1/10) * 3 + 7;
t = Impulse.kr(r); // trigger
// t = Dust.kr(r);
e = Linen.kr(t, 0, 0.5, 1/r); // envelope uses r and t
f = TRand.kr(1, 10, t); // triggered random also uses t
// f = e + 1 * 4;
Blip.ar(f*100, f, e) // f, and e used in Blip
}.play
)

p.free;  // run this to stop it


