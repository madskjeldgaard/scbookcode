Figure 23.5 
(
SynthDef(\FreqMod, { |car=440, carFine=1, mod=100, modFine=1, index=4, amp=0.3, pan=0| 
	Out.ar(0, Pan2.ar(PMOsc.ar(car * carFine, mod * modFine, index), pan, amp)); 
}).add; 		

c = Controller.new(s, 256);
c.makeInterface(2, 3, 0, "Freq Mod controller");

c[0].setup("carFreq", [50, 4800, \exp].asSpec, 440);
c[1].setup("carFreqFine", [0.95, 1.05, \exp].asSpec, 1);
c[2].setup("modFreq", [10, 4800, \exp].asSpec, 10);
c[3].setup("modFreqFine", [0.95, 1.05, \exp].asSpec, 1);
c[4].setup("mIndex", [0, 24, \lin].asSpec, 1);
c[5].setup("amp", [0, 1, \lin].asSpec, 0.2);
);

a = Synth(\FreqMod) // start the synth
6.do({|i| a.map(i, c[i].index)}); // map the synth control to the controller.
a.free // when finished


