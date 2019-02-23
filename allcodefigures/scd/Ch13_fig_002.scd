// figure 13.2 - Mapping the data to pitch in discrete events
(
Pbindef(\exec, 
	\note, Pseq(q.getReg(\Midwest)), 
	\octave, 3, 
	\dur, 5 / q.execdata.years.size
).play;
)
	// the different regions
Pbindef(\exec, \note, Pseq(q.getReg(\Northeast)));
Pbindef(\exec, \note, Pseq(q.getReg(\West)));
Pbindef(\exec, \note, Pseq(q.getReg(\South)));

