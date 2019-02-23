Figure 7.21

(instrument: \x, type: \phrase).play; // a single phrase from Pdef(\x)

// a pattern of overlapping phrases
(
Pbind(
	\type, \phrase, 
	\instrument, \x, 
	\legato, 2.5, 
	\note, Pseq([0, 5, 7], inf)
).play
);

