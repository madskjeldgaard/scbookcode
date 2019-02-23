Figure 7.6

// self reference (~x) constructs a loop at control rate 

~out.play;
~x = 0.2; ~a = 1.1; ~c = 0.13;
~x = (~a * ~x) + ~c % 1.0;  // leaving out the .kr message assumes a control rate ugen.

~out = { Pan2.ar(SinOsc.ar(~x * 4000 + 200) * 0.1, ~x) };


