Figure 11.7
// MIDI Example
(
SynthDef(\midi, { arg out=0, midinote=60, dur=0.1;
	var source = Blip.ar(midinote.midicps, 4, 0.2);
	var env = EnvGen.kr(Env.perc(0.1, dur), 1.0, doneAction: 2);
	Out.ar(out, (source * env) ! 2);

}).add;

c = NoteOnResponder( { |src, chan, num, vel|
	[num, vel].postln;
	Synth.new(\midi, [\midinote, num, \dur, vel/512]);
});
)
c.remove;

