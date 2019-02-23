1.11 FX Routing using Buses

// Create and name buses 
~delay = Bus.audio(s, 2);
~mod = Bus.audio(s, 2);
~gate = Bus.audio(s, 2);
~k5 = Bus.control;

~controlSyn= {Out.kr(~k5, LFNoise0.kr(4))}.play; // start the control

// Start the last item in the chain, the delay
~delaySyn = {Out.ar(0, AllpassC.ar(In.ar(~delay, 2), 2, [0.65, 1.15], 10))}.play(~controlSyn, addAction: \addAfter) 

// Start the next to last item, the modulation
~modSyn = {Out.ar(~delay, In.ar(~mod, 2) * SinOsc.ar(In.kr(~k5)*500 + 1100))}.play(~delaySyn, addAction: \addBefore);

// Start the third to last item, the gate
~gateSyn = {Out.ar([0, ~mod], In.ar(~gate, 2) * max(0, In.kr(~k5)))}.play(~modSyn, addAction: \addBefore);

// make a group for the PlayBuf synths at the head of the chain
~pbGroup = Group.before(~controlSyn);

// Start one buffer. Since we add to the group, we know where it will go
{Out.ar(~gate, Pan2.ar(PlayBuf.ar(1, ~houston, loop: 1), 0.5))}.play(~pbGroup);

// Start the other
{Out.ar(~gate, Pan2.ar(PlayBuf.ar(1, ~chooston, loop: 1), -0.5))}.play(~pbGroup);



