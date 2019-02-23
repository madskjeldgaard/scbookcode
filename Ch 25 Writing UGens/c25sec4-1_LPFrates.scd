// Demonstrates the effect of using checkSameRateAsFirstInput
s.boot;
x = {LPF.ar(WhiteNoise.kr) }.play(s); // Error
x = {LPF.ar(WhiteNoise.ar) }.play(s); // OK
x.free;
x = {LPF.kr(WhiteNoise.ar) }.play(s); // Error
x = {LPF.kr(WhiteNoise.kr) }.play(s); // OK
x.free;
