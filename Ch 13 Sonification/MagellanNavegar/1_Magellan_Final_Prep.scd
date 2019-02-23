/*

Required Quarks: 

	MathLib - includes nSorted method

Recommended Quarks:
	SpeakersCorner - includes SpatioScope class for demonstrating spatialisation.


Quarks.install("MathLib");
Quarks.install("SpeakersCorner");

*** recompile if needed ****

1_Magellan_Final_Prep.scd
	Reads in all data for the magellan sonification, 
	opens other text docs.

2_PlaySingleCountries.scd
	play contries individually
3_Magellan_Reise.scd
	load timeline data (comments in German)
	
4_windDev.scd
	the motion noise, wind and storm sound

5_FinalPlayer.scd
	play the full piece (8 channels)
		
6_VariousTests.scd
	individual steps of the piece, for debugging
	
*/

(
q = q ? ();
q.dir = Document.current.path.dirname;
//	q.filename = q.dir ++ "/ICADConcertData_Extend2.txt";
	q.filename = q.dir ++ "/ICADConcertData_Magellan.txt"; // this data file for magellan

q.text = TabFileReader.read(q.filename, true, false);

		// read the definitive timeline:
(q.dir ++ "/3_Magellan_Reise.scd").loadPaths;

q.names1 = q.text[0];

q.chanNames = q.names1.collect(_.asSymbol);

// Document.new("chanNames:", q.chanNames.asCompileString); 
Document.open(q.dir ++ "/5_FinalPlayer.scd"); 
// Document.open(q.dir ++ "/4_windDev.scd"); 
Document.open(q.dir ++ "/2_PlaySingleCountries.scd"); 

q.dataChans = q.text
	.drop(1)		// drop first line, chanNames
	.flop;				// swap rows and columns

	// read using sonenvir class SEData: 

q.worlddata = SEData.fromNamesData(
	\world,
	q.chanNames, 
	q.dataChans
);

		// convert channels with names to symbols
[ 'Country', 'Capital', 'Region' ].do { |name, i| 
	var chandata = q.worlddata.atChan(name).data;
	chandata.do { |str, i| chandata.put(i, str.asSymbol) };
};

	// convert channels with numbers to floats for better math precision:
q.numberChannels = q.chanNames.reject(
	[ 'Country', 'Capital', 'Region', 'Latitude_deg', 'Longitude_deg' ].includes(_)
); 

q.numberChannels.do { |name, i| 
	var data = q.worlddata.atChan(name).data;
	data.do { |str, i| 
		var num = if (str.every({ |ch| ch.isDecDigit.not }), nil, str.asFloat);
		data.put(i, num);
	};
};

q.worlddata.analyse; 

//	two utilities for distance and angle on the sphere:

q.rad2Deg = 180/pi; q.deg2Rad = pi / 180; 
q.earthRadius = 6372.8; 

q.sphDist = { |q, lat1, long1, lat2, long2, format=\rad| 
	var res;
	lat1 = lat1 * q.deg2Rad; long1 = long1 * q.deg2Rad;
	lat2 = lat2 * q.deg2Rad; long2 = long2 * q.deg2Rad;

	res = 2 * asin( (
		sin(lat2 - lat1 * 0.5).squared 
		+ (cos(lat1) * cos(lat2) * sin((long2 - long1).abs * 0.5).squared)
		).sqrt
	); 
	if (format == \rad, res, { res * q.rad2Deg });
}; 

q.sphAngle = { |q, lat1, long1, lat2, long2, format=\rad| 
	// shorten angle by the ratio of higherLat / lowerLat to 
	
	var highLat, lowLat, shorten; 
	#highLat, lowLat = [lat1, lat2].sort { |a, b| a.abs > b.abs }; 
	shorten = (1-highLat.abs) / (1-lowLat.abs); 
	
	(long2 - long1 @ (lat2 - lat1 * shorten)).theta 
	* (if (format == \rad, 1, q.rad2Deg))
}; 

q.getTopN = {Ê|q, n = 40, curlat=0, curlong=0| 
	q.vectors.collect({ |vect| vect ++ q.sphDist(vect[1], vect[2], curlat, curlong) })
		.nSorted(n, {Ê|a, b| a.last < b.last  })
		.collect({ |iVect| 
			var i, vect; #i, vect = iVect; 
			vect.add(  q.sphAngle(vect[1], vect[2], curlong, curlong) );
		});
};

q.vectors = q.worlddata.vectorsCollect([\Country, \Latitude, \Longitude], { |vect, i| vect ++ i }); 

q.setWind = { |q, newCoord, event|
	var speed, angle, dirPoint;
	q.lastCoord = q.lastCoord ? newCoord; 
	q.lastAngle = q.lastAngle ? -0.75pi;	// southwest; 
	[q.lastCoord, q.lastAngle ];
	speed = q.sphDist(q.lastCoord[0], q.lastCoord[1], newCoord[0], newCoord[1]) 
		* 0.33 * q.earthRadius;		// stimmt das so? muss ein durch timestep!!!
		
	angle = if (speed > 0, { 
		q.sphAngle(q.lastCoord[0], q.lastCoord[1], newCoord[0], newCoord[1]);
	}, { q.lastAngle });
	dirPoint = Polar(1, angle).asPoint; 
	q.lastCoord = newCoord; 
	q.lastAngle = angle;
// 	[ \speed, speed, \dirPoint, dirPoint, \event, event].postln;

	event = event ? q.lastEvent ? \normal;
	
	event.switch(
		\storm, { 
			~wind.set(
				\x, dirPoint.x, \y, dirPoint.y,
				\freq, rrand(450, 600), 
				\dRate, rrand(12, 15), \drift, rrand(7, 10), \q, 2.5, 
				\focus, rrand(0.2, 0.4)
			) }, 
		\calm, { 
			~wind.set(
				\x, dirPoint.x, \y, dirPoint.y,
				\freq, rrand(50, 60), 
				\dRate, 1, \drift, 2, 
				\q, 1, 
				\focus, rrand(0.2, 0.5)
			) }, 
		\normal, {	
			\default.postln;
			~wind.set(
				\x, dirPoint.x, \y, dirPoint.y,
				\freq, speed * 3 + 70, 
				\dRate, 3, \drift, rrand(2.5, 4), 
				\q, speed * 0.05 + 1, 
				\focus, ((speed + 5) * 0.2)
			) }
	);
	q.lastEvent = event;
};
)
/*
	// some tests to see that the data loaded OK: 
q.worlddata.chanNames.postcs;
		// some name channels
q.worlddata.atChan('Country').postcs;
q.worlddata.atChan('Capital').postcs;
q.worlddata.atChan('Region').postcs;

		// some number channels
q.worlddata.atChan('Population').postcs;
q.worlddata.atChan('GDP_dollar').postcs;
q.worlddata.atChan('GDP_per_capita').postcs;
q.worlddata.atChan('Housing_total').postcs;

q.worlddata.atChan('Latitude').data.postcs.plot;
q.worlddata.atChan('Longitude').data.postcs.plot;
q.worlddata.atChan('Latitude').data;
q.worlddata.atChan('Longitude').data.do { |d, i| [i, d.round(0.1)].postln };


q.worlddata.channels.do { |chan| [chan.name, chan.min, chan.max, chan.avg, chan.numNil].postln };
*/

/* test for geometry

a = q.worlddata.atVector(5, [ 'Country', 'Latitude', 'Longitude' ]);
q.worlddata.atChan('Country').data.indexOf('Spain');

b = q.worlddata.atVector(158, [ 'Country', 'Latitude', 'Longitude' ]);

c = [ 'Victoria', b[1], b[2] ];

q.sphDist(a[1], a[2], b[1], b[2]) * q.earthRadius;

	// angles: 0 is west, 0.5pi is north, pi is east, -0.5pi is south:
q.sphAngle(50, 0, 50, 20);		// ca London -> Kiew
q.sphAngle(50, 0, 60, 0);		// ca London -> Hebrides
q.sphAngle(50, 0, 50, -120);	// ca London -> Vancouver
q.sphAngle(50, 0, 5, 0);		// ca London -> Accra

		// Antigua_and_Barbuda -> Spain
q.sphAngle(a[1], a[2], b[1], b[2], \deg); //  ca 45 deg NW, OK!

q.sphAngle(a[1], a[2], b[1], b[2]); 

*/

// ten closest countries to sevilla: 
/*
q.vectors = q.worlddata.vectorsCollect([\Country, \Latitude, \Longitude], { |vect, i| vect ++ i }); 

q.vectors;
q.currPos = [\victoria, 37.5, -6];


q.topN = q.getTopN(10, 37.5, -6); q.topN.round(0.01).printAll;

q.topN = q.getTopN(10, -37.5, 174); q.topN.round(0.01).printAll;

q.topN.do { |list| [ list[4] * q.earthRadius, list].round(0.01).postln; };

*/
