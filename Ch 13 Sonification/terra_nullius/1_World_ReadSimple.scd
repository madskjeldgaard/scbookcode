/*
simple textfile reader for reading in ICAD concert data files in SC3
*/
		// 
(
q = q ? ();
q.dir = Document.current.path.dirname;
q.filename = q.dir ++ "/WorldExtended2.txt";
q.text = TabFileReader.read(q.filename, true, false);
q.names1 = q.text[0];

q.chanNames = q.names1.collect(_.asSymbol);

Document.new("chanNames:", q.chanNames.asCompileString); 

q.dataChans = q.text
	.drop(1)		// drop first line, chanNames
	.drop(-1)		// drop last line, total population
	.flop;								// swap rows and columns

q.worlddata = (); 

q.chanNames.do { |name, i| 
	q.worlddata.put(name, q.dataChans.at(i));
};

		// convert channels with names to symbols
[ 'Country', 'Capital', 'Region' ].do { |name, i| 
	var chandata = q.worlddata.at(name);
	chandata.do { |str, i| chandata.put(i, str.asSymbol) };
};

	// convert channels with numbers to floats for better math precision:
q.numberChannels = q.chanNames.reject(
	[ 'Country', 'Capital', 'Region', 'Latitude_deg', 'Longitude_deg' ].includes(_)
); 

q.numberChannels.do { |name, i| 
	var chandata = q.worlddata.at(name);
	chandata.do { |str, i| 
		var num = if (str.every({ |ch| ch.isDecDigit.not }), nil, str.asFloat);
		chandata.put(i, num);
	};
};
)

/**** 
//	code that was used to convert latitude_deg and longitude_deg to float values:

		// latitude and longitude need special conversion to degrees:
[ 	'Latitude', 'Longitude' ].do { |name, i| 
	var data = q.worlddata.atChan(name).data;
	data.do { |str, i| 
		var sign, degrees, sep, minutes;
		#sign, degrees, sep, minutes = str.select(_.isPrint).split($ ).reject(_.isEmpty);
		
		sign = (N: 1, S: -1, E: 1, W: -1)[sign.asSymbol];
		degrees = degrees.asFloat + ((minutes ? 0).asFloat / 60) * sign;
		
		data.put(i, degrees);
	};
};
	// write it out so you can copy/paste it into Excel
q.worlddata.atChan('Latitude').size.do { |index| 
	var lat, long;
	#lat, long = q.worlddata.atVector(index, [ 'Latitude', 'Longitude' ]).round(0.0001);
	("" ++ lat ++ Char.tab ++ long).postln;
}; "";

****/