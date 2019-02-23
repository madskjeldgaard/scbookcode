Figure 6.1.  Example of a SynthDef
SynthDef(					
	"sine", 				// name of SynthDef	
	{				// function begins with a brace
	 arg gate = 1, out = 0, 		// arguments serve as Control declarations
 freq = 400, amp = 0.4,
 pan = 0, ar = 1, dr = 1;

	var audio;			
	audio = SinOsc.ar(freq, 0, amp);		// start with a SinOsc
	audio = audio * Linen.kr(gate, ar, 1, dr, 2);	// apply an envelope
	audio = Pan2.ar(audio, pan);			// stereo pan, 
	OffsetOut.ar(out,audio);			// to bus out and out+1 
	}
).add;				// make and store a SynthDesc and SynthDef

Figure 6.2. Example of a key/value Array and a note Event=
a = [ 
	type:			\note,		
	instrument: 		'sine', 
	freq:			400, 
	amp:			0.1, 
	pan:			0, 
	ar: 			2, 
	dr: 			4, 
	sustain:		2
];

e = (
	type:			\note,		
	instrument: 		'sine', 
	freq:			400, 
	amp:			0.1, 
	pan:			0, 
	ar: 			2, 
	dr: 			4, 
	sustain:		2
);
e.play;					// play the note

Figure  6.3. The OSC commands created by the note event
e.asOSC.do{ | osc | osc.postcs };

// the result that would be posted:		
//  [ 0.0, ['s_new', 'sine', 1000, 0, 1, 'out', 0, 'freq', 400.0, 'amp', 0.1, 'pan', 0, 'ar', 2, 'dr', 4] ]
//  [ 2.0, [ 'n_set', 1000, 'gate', 0 ] ] 



Figure 6.4. Two ways of writing the same Event Pattern
	1. Pbind( *[ 
		dur:	0.2,
freq: 	Pseq([100, 200, 300, 400, 500, 600, 700, 800]) 
	] );

	2. Pbind( 
		\dur, 0.2,
		\freq, 	Pseq([100, 200, 300, 400, 500, 600, 700, 800]) 
	);

Figure 6.5. A more elaborate Event Pattern
p = Pbind(*[ 
		instrument:	\default,
		detune:		[0,1,3],
		freq: 		Pseq( (1..11) * 100,  4 * 5 * 7),
		db:		Pseq([-20, -40, -30, -40], inf),
		pan:		Pseq([-1,0,1,0], inf),
		dur:		Pseq([0.2,0.2,0.2,0.2, 0.4,0.4,0.8], inf),
		legato:		Pseq([2,0.5,0.75,0.5,0.25], inf)	
	] );
// render 40 seconds of the pattern in the file named "sf.aif"	
	p.render("sounds/sf.aif", 40)	
// now play the pattern in real-time
	p.play;									
Figure 6.6.  Using Event types
(
	(type: 	\group, 	
id: 2
).play;					// create a group with nodeID 2

	(	type:		\note, 		// play note		
	 	sustain:	100, 		// lasting 100 seconds				
	 	group: 		2		// in group 2

	 ).play;	
	 										
)
(
	(type: 	\off, 	id: 2).play;		// release all notes in the group
	(type:	 \kill,	id: 2,  lag: 3).play;	// and free the group  3 seconds later	
)

Figure 6.7. Chord events
// 2nd inversion - e loudest
( degree:  [-3,0,2],	sustain: 2, 	db: [-20, -20, -10] ).play			
// 2nd inversion - c loudest
 ( degree:  [-3,0,2],	sustain: 2, 	db: [-20, -10, -20] ).play	
// note "fattened" by three detuned copies
 ( degree: 0,		sustain: 2, 	detune: [0,3, 5] ).play			
// each detune is assigned to a different pitch, fat free.
 ( degree:  [-3,2,4],	sustain: 2, 	detune: [0,3, 5] ).play			
// detune rotates through each note in the chord
 ( degree:  [-3,2,4],	sustain: 2, 	detune: [0,0,0,3,3,3,5,5,5] ).play 	

Figure 6.8.  Interdependent key values in a Pattern
	Pbind(*[
		stretch:		Pseg([0,0.1,0.2,1],8).linexp(0,1, 1,0.125),
		midinote: 		100.cpsmidi,
		harmonic:		Pwhite(1, 16),
		legato:			Pkey(\stretch) * Pkey(\harmonic)/2 ,
		db:			-10 - Pkey(\harmonic),
		detune:			Pwhite(0.0,3.0),
		dur:			0.2,
	]).play

Figure 6.9. Chaining Event Patterns
	a = Pbind(*[
		scale: 	Pn( Pstep([[0,2,4,5,7,9,11], [0,1,3,5,6,8,11] ], 5 ) ),
		db:	Pn(Pseg([-20, -30, -25, -30], 0.4))
	]);
	b = Pbind(*[ 
degree: Pbrown(0, 6, 1), 
mtranspose: Prand([\rest, Pseq([0], 5.rand)],inf), 
dur: 0.2, 
octave: 6
]);
	c = Pbind(*[ 
degree: [0,2,4],  
mtranspose: Pbrown(0, 6, 1), 
dur: 0.4, 
db: -35
]);
	d = Pchain(Ppar([b, c]),a);
d.play;

Figure 6.10. Using Proutine to define and play patterns on the fly
	Proutine({| ev |
		var pat, refPat;

		refPat = Pbind(*[dur: 0.2, note: Pseq([0,0, 0, 7,0, 7])]);

		loop {
			ev = refPat.embedInStream(ev);

			pat = Pbind(*[
				dur: [0.2, 0.4].choose,
				note: Pseq(Array.fill(5, { 10.rand }), 3.rand )
			]);	
ev = pat.embedInStream(ev);
		}
	
}).play



Figure 6.11.  Using Proutine to define value and event patterns
~patA = Pbind(*[
	dur:	0.2,
	degree: Proutine({ | ev |
		var noteArray  = (0..5);
		loop {
			ev = Pseq(noteArray ).embedInStream(ev);
			noteArray[6.rand] = 7.rand;
		}		
	})
]);


~patB = Proutine({ | ev |
	var pat, pats= [
	 	Pbind(*[ degree: Pseq([  0, 7]), dur: 0.2   ]),
	 	Pbind(*[ degree: Pseq([11, 7]), dur: 0.2 ]),
		Pbind(*[ degree: Pseq([16, 7]), dur: 0.2 ]).
		(type: \rest, delta: 1)
	];
			
	loop {
		pat = pats.choose;
		ev = pat.embedInStream(ev);
	}
});

Pchain(
	Pbind(*[
		db: 	Pn(Pstep([-15, -25,-25, -20, -30, -25], 0.2) )
			+ Pseg([-30, -5,-10, -40], 12)
	]), 
	Ptpar([
		0, ~patA, 
		0,  ~patA, 
		12,  ~patB
	]) 
).play;

Figure 6.12.  Rendering and playing a pattern
	~pattern = Pbind(*[ 
		instrument:	"default",
		freq: 	Pseq([100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100], 5),
		db:	Pseq([-10, -30, -20, -30], inf),
		dur:	Pseq([0.2,0.2,0.2,0.2, 0.4,0.4,0.8],inf),
		legato: Pseq([2,0.5,0.75,0.5,0.25], inf)	
	] );

	~score = ~pattern.asScore(24 * 11/7);
	~score.render("recordings/test.aif");
	SoundFile("recordings/test.aif").play;
Figure 6.13. Soundfile granulation with a Pattern 
SynthDef("playbuf", { | out=0, bufnum = 0, rate = 1, 
startPos = 0, amp = 0.1, sustain = 1, 
pan = 0, gate = 1|
	var audio, env;
	rate = rate * BufRateScale.kr(bufnum);
	startPos = startPos * BufFrames.kr(bufnum);
	env = EnvGen.ar(Env.sine, 1, timeScale: sustain, doneAction: 2);
	audio = PlayBuf.ar(1, bufnum, rate, 1, startPos, 0);
	audio = env * audio;
	audio = Pan2.ar(audio, pan, amp);
	OffsetOut.ar(out, audio);
}).add;
		
	Pseq([
		(	type:		\load,
			filename:	"sounds/a11wlk01.wav",
			bufnum:	1,
			delta:		0
		),
		
		Pbind(*[
			instrument:	"playbuf",
			type:		\on,
			id:		-1,
			dur:		 Pseg([0,1],21).linexp(0,1,0.01,2),
			legato:		4,
			startPos:	Pn(Pseg([0,1], 10)),
			bufnum:	1,		
		]),
		(	type:		\free, 
			bufnum: 	1
		)
	]).play(quant: 0) 
// the result: [ ( 'midinote': 60 ), ( 'midinote': 64 ), ( 'midinote': 67 ) ]
Figure 6.14.  Yield versus EmbedInStream
	r = Routine{ 
		Pseq([1,2,3]).yield; 
		Pseq([1,2,3]).embedInStream;
		123445.embedInStream; 
		123445.embedInStream; 
	};

	[next(r), next(r), next(r), next(r), next(r), next(r)];
	// the result: [ a Pseq, 1, 2, 3, 123445, 123445, nil]

Figure 6.15. The definition of the stream created by Pseq.
	Routine({
		repeats.value.do({
			list.size.do({ arg i;
				item = list.wrapAt(i + offsetValue);
				inval = item.embedInStream(inval);
			});
		});
	});

 Figure 6.16.  The definition of Event’s play method.
	play {
		if (parent.isNil) { parent = defaultParentEvent };
		this.use { ~play.value };
	}

Figure 6.17. Definition of the key \play in the default event
{
	var tempo, server;
	
	~finish.value;						// user callback 
	server = ~server ?? { Server.default };	 
	tempo = ~tempo;					// assigning to a variable
									// saves repeated look ups
	if (tempo.notNil) {					// if not nil, change tempo of
		thisThread.clock.tempo = tempo;	// the clock playing the pattern
	};
	~eventTypes[~type].value(server);		// select play function from ~type
}

Figure 6.18.  Implementation of the event type \bus.
	{ |server|
		var lag, array;
		lag = ~lag + server.latency;
		array = ~array.asArray;
		server.sendBundle(lag, 
			[\c_setn, ~out.asUGenInput, array.size] ++ array);
	}	


