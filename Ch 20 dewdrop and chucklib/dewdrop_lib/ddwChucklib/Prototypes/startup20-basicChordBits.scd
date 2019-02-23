
// other basic functions

// make a set of wavetables for vosc3map to avoid aliasing
// user's responsibility to set numbufs and lowMidi appropriately

{ |numbufs, server, numFrames, lowMidi, spectrumFunc|
	numbufs = numbufs ? 8;
	server = server ? Server.default;
	numFrames = numFrames ? 2048;
		// default is sawtooth
	spectrumFunc = spectrumFunc ? { |numharm| (1..numharm).reciprocal };
	lowMidi = (lowMidi ? 48) / 12;
	
	Buffer.allocConsecutive(numbufs, server, numFrames, 1, { |buf, i|
		var	base = (i + lowMidi) * 12,
			numharm = (20000 / base.midicps).asInteger;
		buf.sine1Msg(spectrumFunc.(numharm));
	});
} => Func(\makeWavetables);


// basic arpeggiators, macro- and micro-rhythms

// C major
ModalSpec(#[0, 2, 4, 5, 7, 9, 11], 12, 0) => Mode(\default);
ModalSpec(#[0, 2, 3, 5, 7, 8, 10], 12, 0) => Mode(\cmin);

// arpeggiation patterns

#{ |notes| Pseq(notes, inf) } => ArpegPat(\asis);
#{ |notes|
	notes.isArray.if({
		Pn(notes.asChord, 1)
	}, {
		Pn(notes, 1)
	});
} => ArpegPat(\block);
#{ |notes| Pxrand(notes, inf) } => ArpegPat(\xrand);
#{ |notes| Pseq(notes.sort, inf) } => ArpegPat(\up);
#{ |notes| Pseq(notes.sort({ |a, b| a > b }), inf) } => ArpegPat(\down);
#{ |notes| Pseq([notes.sort, notes+7, notes+7, notes+14, notes+14].flat, 1) } => ArpegPat(\bubbleup);
#{ |notes| Pseq([(notes = notes.sort.reverse) + 14, notes+7, notes].flat, 1) } => ArpegPat(\bubbledown);

#{ |notes| Pseq(notes.sort, 1) } => ArpegPat(\up1);
#{ |notes| Pseq(notes.sort({ |a, b| a > b }), 1) } => ArpegPat(\down1);

{ |notes| Pshuf(notes, 1) } => ArpegPat(\shuf1);
{ |notes| Pshuf(notes, inf) } => ArpegPat(\shuf);

// microrhythms

#{ |notes, event|
	var topNote, gateIndex, gate;
	topNote = event[\top];
	Pn([event.delta, event[\length], topNote.tryPerform(\gate) ? 0.5], 1)
} => MicRh(\blockFollow);

#{ |notepat| Pn(#[0.25, 0.2, 0.5], notepat.estimateLength) } => MicRh('16th');
#{ |notepat| Proutine({
	var delta;
	notepat.estimateLength.do({ |i|
		[delta = 0.2 - ((i * 2pi/25).sin * 0.125), delta, 0.75-delta].yield;
	});
}) } => MicRh(\sine);

// macrorhythms
Pn(12, inf) => MacRh(\m3);


// pattern-based topnote process
// usage:
// ... make chord process in BP(\ch) ...
// BP(\ch).v.topNote = PR(\patternTop).v.copy.make({
//	~deg = pattern;
//	~delta = pattern;	// may be omitted if you use macrorhythm
//	~length = pattern;	// may be omitted if you use macrorhythm
// });

PR(\abstractProcess).v.clone({
	~mode = \default;
	~delta = 1;	// need defaults (will be ignored for macrorhythm chord processes)
	~length = 1;
	~gate = 0.5;
	~asPattern = { 
		Pbind(
			\freq, BPStream(\deg),
			\delta, BPStream(\delta),
			\length, BPStream(\length),
			\mode, Pfunc({ ~mode }),
			\gate, BPStream(\gate)
		);
	};
}) => PR(\patternTop);

