// figure  8.3 - add more instance variables, change the blip method.
(
m.numHarms_(20); 	// a new instvar
m.decay_(0.3); 	// and another
				// update the blip method to use them:
m.blip_({ |ev| 
	{ Blip.ar(ev.myfreq, ev.numHarms) 
	* XLine.kr(1, 0.01, ev.decay, doneAction: 2) }.play; 
});
)
m.blip;	// test


