		// figure 13.5 - Four regions in sequence
(
Tdef(\execs, { 
	var yearDur = 2;	// one year is 2 seconds
	var region, numExecs, numyears = q.execdata.years.size; 
	[\Northeast, \Midwest, \West, \South].do { |regName, i| 

		region = q.execdata.regions[regName].postln; 
		q.execdata.years.do { |year, i| 
			numExecs = region[i + 1]; 
			[regName, year, numExecs].postln;
			(instrument: \tick).play;
			if (numExecs > 0) {
				(instrument: \noisepulses, legato: 1, 
numPulses: numExecs, dur: yearDur).play;
			};
			yearDur.wait;
		};
		yearDur.wait;
	};
}).play;
)

