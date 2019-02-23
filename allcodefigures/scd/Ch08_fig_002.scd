// figure  8.2 - a puppet modeled as an event.

m = (); 			// make an empty event
m.myfreq_(50);	// put something in it with a setter method: a pseudo-instance variable
m.myfreq;			// look it up with a getter method
				// put a function into it with a setter: 
				// this becomes a pseudo-method
m.blip_({ |ev| { Blip.ar(ev.myfreq, 11) * XLine.kr(1, 0.01, 0.6, doneAction: 2) }.play; });
m.blip;		// execute the function with a pseudo-method call (same name)


