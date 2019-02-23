Figure 7.10

p.clear; // neutralize space, free all buses and synths
p.push; // if needed
~a.ar(3); // 3 channels, audio rate
~b.kr(8); // 8 channels, control rate
~c.play; // playing an uninitialized proxy assumes (per default) 2 channels, audio rate
~d = { LFNoise0.kr([1, 1, 1, 1]) }; // 4 channels, control rate


