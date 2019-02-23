// figure 13.6 - Four regions in parallel
(
// four channels playing, ordered by total number. 
// On stereo systems, only the first 2 channels play.
Tdef(\execs, { 
	var yearDur = 2;	// one year is 2 seconds
	q.execdata.years.do { |year, i| 
	var region, numExecs, numyears = q.execdata.years.size; 
		// ordered by total number, on stereo, only first 2 play.
	[\South, \Midwest,  \West, \Northeast ].do { |regName, j| 

		region = q.execdata.regions[regName].postln; 
			numExecs = region[i + 1]; 
			[regName, year, numExecs].postln;
			(instrument: \tick).play;
			if (numExecs > 0) {
				(instrument: \noisepulses, legato: 1, 
dur: yearDur, 
					numPulses: numExecs, 
					pan: j * 0.5 - 0.25 	// to 4 channels
				).play;
			};
		};
		yearDur.wait;
	};
}).play;
)


