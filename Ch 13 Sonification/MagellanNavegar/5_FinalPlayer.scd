/*** 
	This script prepares a proxy for every country, and a Tdef to play the piece.
	It takes a while to load, tells you when ready!

***/ 
(
q.numSpeakers = q.numSpeakers ? 8; 
s.recChannels_(q.numSpeakers);
	
	// settings for the concert version: 
Tdef(\navegar).set(
	\steptime, 1/3, 	// a day is scaled into 0.3333 seconds
	\numCountries, 15, // the 15 nearest countries are heard at any time
	\timelineOff, 0, 	// timeline starts from the first day
	\postEveryN, 10	// every tenth day gets posted.
);
	// testing values
Tdef(\navegar).set(
	\steptime, 1/3, 
	\numCountries, 1, // few countries
	\timelineOff, 4, 
	\postEveryN, 1	// post every day
); 

	// load the wind sound;
(q.dir ++ "/4_windDev.scd").loadPaths;

	// If you have the AmbIEM quark installed, you can render the speakers
	// as sources in a virtual room, to stereo headphones.
	
	// for 8 chans uncomment these:
//Kemar.path_("KemarHRTF/")		// for 8 chan
//	.initBuffers(
//		[25, 70, 115, 160, 200, 245, 290, 335].rotate(1), 
//		[0, 0, 0, 0,  0, 0, 0, 0]
//	);

	// for 12 chans uncomment these:
//Kemar.path_("KemarHRTF/")		// for 12 chans
//	.initBuffers(
//		[	 15,  45,  75, 105, 135, 165, 
//			195, 225, 255, 285, 315, 345 ].rotate(1), 
//		0 ! q.numSpeakers
//	);

	// set up a player nodeproxy for every country;  
	// these are all countries that are being 'touched' by the travel route when q.numCountries = 20.
q.usedNames = 
[ 'Algeria', 'Andorra', 'Angola', 'Antigua_and_Barbuda', 'Argentina', 'Australia', 'Bangladesh', 'Barbados', 'Belgium', 'Belize', 'Benin', 'Bhutan', 'Bolivia', 'Botswana', 'Brazil', 'Brunei', 'Burkina_Faso', 'Burma', 'Burundi', 'Cambodia', 'Cameroon', 'Cape_Verde', 'Central_African_Republic', 'Chad', 'Chile', 'China', 'Colombia', 'Comoros', 'Congo_Democratic_Republic', 'Congo_Republic', 'Costa_Rica', 'Cote_d_Ivoire', 'Cuba', 'Djibouti', 'Dominica', 'Dominican_Republic', 'East_Timor', 'Ecuador', 'El_Salvador', 'Equatorial_Guinea', 'Ethiopia', 'Fiji', 'France', 'Gabon', 'Gambia', 'Ghana', 'Grenada', 'Guatemala', 'Guinea', 'Guyana', 'Haiti', 'Honduras', 'Hong_Kong', 'India', 'Indonesia', 'Ireland', 'Italy', 'Jamaica', 'Japan', 'Kenya', 'Kiribati', 'Korea_North', 'Korea_South', 'Laos', 'Lesotho', 'Liberia', 'Libya', 'Liechtenstein', 'Luxembourg', 'Madagascar', 'Malawi', 'Malaysia', 'Maldives', 'Mali', 'Malta', 'Mauritania', 'Mauritius', 'Mayotte', 'Mexico', 'Monaco', 'Morocco', 'Mozambique', 'Namibia', 'Nepal', 'Netherlands', 'New_Zealand', 'Nicaragua', 'Niger', 'Nigeria', 'Oman', 'Palau', 'Panama', 'Papua_New_Guinea', 'Paraguay', 'Peru', 'Philippines', 'Portugal', 'Puerto_Rico', 'Reunion', 'Rwanda', 'Saint_Helena', 'Samoa', 'San_Marino', 'Senegal', 'Seychelles', 'Sierra_Leone', 'Singapore', 'Slovenia', 'Solomon_Islands', 'Somalia', 'South_Africa', 'Spain', 'Sri_Lanka', 'Suriname', 'Swaziland', 'Switzerland', 'Taiwan', 'Tanzania', 'Thailand', 'Togo', 'Tonga', 'Trinidad_and_Tobago', 'Tunisia', 'Tuvalu', 'Uganda', 'United_Arab_Emirates', 'United_Kingdom', 'Uruguay', 'Vanuatu', 'Venezuela', 'Vietnam', 'Yemen', 'Zambia', 'Zimbabwe' ];

p.clear;

~allChans.ar(q.numSpeakers);
~allChans.bus; 

q.synthfunc = { |rootFreq = 220, outProp = 4.5, inProp = 2.2, 
	attack=0.0, decay = 1.0, dens = 2,
	 amp = 0.2, x = 0, y = 1, step=0.33333| 
	 
	var freqs, exciter, resonator, ampcomp, pos; 
	
	freqs = rootFreq * [ 1/outProp, 1/inProp, 1, inProp, outProp]; 
	
	exciter = (Dust2.ar(dens * [ 1,2,4,2,1] * 0.07) 	// five individual rd triggers for 
												// each component, weighted for center
			+ Dust2.ar(dens * 0.3))					// + some common attacks
				.clip2(0.5)	
				.lag(0.0003) 
				* (dens ** -0.5)				// amplitude compensation for density
			+ PinkNoise.ar(0.002)				// soft background noise 'halo' for fusion
			* (decay ** -0.5);					// amplitude compensation for decay time
				
	resonator = Formlet.ar( exciter, freqs, 
		Ramp.kr(attack, step), decay, 
		AmpComp.kr(freqs.max(50))
	).softclip.sum; 
	
		// convert x, y to angle for panaz, 
		// and rotate to look west.
	pos = atan2(Ramp.kr(y, step), Ramp.kr(x, step)) * (-pi.reciprocal) + 0.5; 

	PanAz.ar(q.numSpeakers, LeakDC.ar(resonator, 0.95), pos, Ramp.kr(amp, step));
};

Spec.specs.put('rootFreq', [100, 2000, \exp].asSpec);
Spec.specs.put('dens', [1.8, 16000, \exp].asSpec);
Spec.specs.put('attack', [0.0003, 0.1, \exp].asSpec);
Spec.specs.put('decay', [0.2, 3, \exp].asSpec);
Spec.specs.put('inProp', [0, 8].asSpec);
Spec.specs.put('outProp', [0, 12].asSpec);
Spec.specs.put('pos', \pan.asSpec);

~wind.clear.ar(q.umSpeakers);

Spec.specs.put('wfreq', [40, 1000, \exp].asSpec);
Spec.specs.put('dRate', [0.1, 30, \exp].asSpec);
Spec.specs.put('drift', [0, 12].asSpec);
Spec.specs.put('q', [0.01, 100, \exp].asSpec);
Spec.specs.put('x', [-100, 100].asSpec);
Spec.specs.put('y', [-100, 100].asSpec);
Spec.specs.put('focus', [0.1, 10, \exp].asSpec);
Spec.specs.put('step', [0.03, 3, \exp].asSpec);

q.usedNames.do { |name| p[name] = nil; p[name].ar(q.numSpeakers); p[name].bus_(~allChans.bus); };


fork {
	var syndef; 
	
	q.usedNames.do { |name, i| 
		0.05.wait; 
		p[name].set(\amp, 0).pause; 
		if (i == 0, { 
			p[name].source_(q[\synthfunc]); 
			syndef = p[name].objects.first.synthDef; 
		}, { 
			p[name].source_(syndef)
		}); 
	};
	
	q.dataVectors = q.usedNames.collect({ |name| 
		var cntIndex = q.worlddata.atChan('Country').data.indexOf(name); 
		var dataVect = q.worlddata.atVector(cntIndex, [\GDP_per_capita, \Density]);
		[name, cntIndex, dataVect]; 
	});
	
		// get static values once from dataset!
	q.usedNames.do { |name| 
		var cntIndex = q.worlddata.atChan('Country').data.indexOf(name); 
		var dataVect = q.worlddata.atVector(cntIndex, 
			[\GDP_per_capita, \Density, \Drinking_total, \prop10, \prop20]);
		
		var gdp, dens, drink, prop10, prop20; 
		[name, cntIndex, dataVect].postln; 
		#gdp, dens, drink, prop10, prop20 = dataVect; 
		
		gdp = gdp.expexp(400, 58900, 200, 2200); 
		dens = dens.expexp(1.8, 16204.5, 1.8, 170); 
		drink = (drink ? 100).linlin(13, 100, 0.2, 1.5); // 1.5 secs max
		
		p[name].set(			
			\rootFreq, gdp, 
			\outProp, (prop10 ? 1.2).sqrt, 	// unknown ginis form quartertone cluster
			\inProp, (prop20 ? 1.1).sqrt, 	// this was wrong in the concert version, fixed for the book
			\decay, drink, 				// wrong in the concert version, fixed for the book
			\dens, dens, 
			
			\attack, 0.1, 	// all distant, off, and in the west
			\amp, 0.0, 
			\x, 1, \y, 0
		);
		0.02.wait;
	}; 
	"\n\nREADY!\n\n\n".postln;
};
);
/*	
	p[\Spain].resume;
	p[\Spain].pause;
*/

	// Final Player sequencer
(
q.allUsed = Set.new(180);

Tdef(\navegar, {Ê|e| 
	var daysToNext, newStreams = [], oldStreams = [], dyingStreams = []; 
	var finalN; 
	
	p.wakeUp; 
	0.5.wait;
	p.do(_.pause); 
	0.5.wait;
	~allChans.wakeUp; 
	~wind.set(\amp, 0.1).wakeUp.resume;
	
		// orig uses AdCVerb, available in Sonenvir svn
	~out = { var snd = ~allChans.ar(q.numSpeakers) + ~wind.ar(q.numSpeakers); 
		snd // + (AdCVerb.ar(snd, 2.1, 0.6, q.numSpeakers) * 0.01)
	}; 
//		// substitute FreeVerb if you like
//	~out = { var snd = ~allChans.ar(q.numSpeakers) + ~wind.ar(q.numSpeakers); 
//		snd + (FreeVerb.ar(snd, 1, 0.75, 0.8) * 0.01)
//	}; 

		// render to headphone stereo, with a slight bass boost
//	~out = { MidEQ.ar(Kemar.ar(~allChans.ar) + ~wind.ar(q.numSpeakers), 100, 2, 6) };

		// or mix down to plain stereo for testing
//	~out = { Splay.ar(~allChans.ar(q.numSpeakers)[0, 7, 1, 6, 2, 5, 3, 4]) }; 

	~out.wakeUp; 

	0.5.wait;
	~out.resume.play(fadeTime: 10);
	~wind.resume.play(fadeTime: 6);

	q.timeline.drop(e.timelineOff).doAdjacentPairs { |line, line2, i| 
		line.postln; 
		daysToNext = (((line2[2] * 365) + (line2[1] * 30) + line2[0]) 
			- ((line[2] * 365) + (line[1] * 30) + line[0])); 
			
		[ "TIMELINE:", line, line2, q.lastEvent ].postcs;
	//	("	" ++ daysToNext).postln;
		daysToNext.do { |i| 
		
			var posLat = blend(line[3], line2[3], i / daysToNext); 
			var posLong = blend(line[4], line2[4], i / daysToNext);
			
			var topN = q.getTopN(e.numCountries, posLat, posLong); 
			var cntNames = topN.collect(_.first); 
			var poster = i % e.postEveryN == 0;
			
			q.setWind([posLat, posLong], line[5]);
			
			newStreams = cntNames.difference(oldStreams);
	//		[ 'oldStreams', oldStreams ].postln; 
			if (newStreams.notEmpty) { [ 'newStreams', newStreams ].postln };
			
			dyingStreams = oldStreams.difference(cntNames);
			if (dyingStreams.notEmpty) { [ 'dyingStreams', dyingStreams ].postln };

			oldStreams = cntNames; 
			
					// turn off old proxies;
			dyingStreams.do { |name| 
				var px = p[name]; 
				px.set(\amp, 0);
				SystemClock.sched(e.steptime, { px.pause });
			}; 
					// turn on new ones
			newStreams.do { |name| 
				var px = p[name]; 
				px.resume;				
			}; 
			
			if (poster, { [ \SHIP, posLat, posLong, q.lastEvent].round(0.01).postln });

					// update the playing ones
			topN.do { |list|Ê
				var dist, angle, dirVect, x, y, amp, attack;
				
				dist = list[4] * q.earthRadius; 
				angle = q.sphAngle(posLat, posLong, list[1], list[2]);
				dirVect = Polar(1, angle).asPoint; 
				x = dirVect.x; y = dirVect.y; 	// send x, y as direction vector, 
				
				amp = (dist.max(100) ** -1) * 50;
				attack = dist.max(100).linexp(0, 2000, 0.001, 0.1); 
				
				if (poster, { 
					[ list, \dist, dist, \panAz, angle / pi, \x, x, \y, y, \atan, atan2(y, x), \amp, amp, \att, attack]
						.round(0.001).postln; 
				}); 
				
				p[list.first].set(
					\amp, amp,
					\attack, attack,
					\x, x, 
					\y, y
				);
			};
			// "".postln; 
			finalN = topN; 	// so I can end them;
			
			q.allUsed.addAll(newStreams); 
					
			e.steptime.wait;
		};
		"".postln;
	//	0.5.wait;
	};
	
	("" + q.allUsed.size + "countries touched.").postln;
	~out.end(20);
});
)
/*
~allChans.stop;
~allChans = nil;

~allChans.stop;
~allChans.vol_(0.1);


p[\Spain].resume;

~out = { ~allChans.ar };
~out.play;

// for use with HEADPHONES use these

~out = { Splay.ar(~allChans.ar) };

	// HRTF rendering if you have AmbIEM installed:
~out = { MidEQ.ar(Kemar.ar(~allChans.ar), 100, 2, 6) };
~out = { ~allChans.ar };

// ~out = { Splay.ar(~allChans.ar[0,7,1,6,2,5,3,4]) };


~out.wakeUp.resume.play;

~out.vol_(4);

NodeProxyEditor(~out);
*/