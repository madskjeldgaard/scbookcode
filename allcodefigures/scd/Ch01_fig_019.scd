1.19 Generative Sequences using Arrays

( // first define the synth

SynthDef.new("SimpleBlip", { 
arg midi = 60, tone = 10, art = 0.125, amp = 0.2, pan = -1;
var out, temper;	
out =	Pan2.ar(
			Blip.ar( // play the sequence
				midi.midicps,
				tone
				) * EnvGen.kr(Env.perc(0.01, art)),
		pan // pan left, center, or right
	);
DetectSilence.ar(out, doneAction:2);
amp = amp - ((midi - 60) * 0.02);
Out.ar(0, out*amp)
	}).add;
)
 
(
// Then run this Task
~inst = [0, 0, 0]; // Three containers for tasks
~pSeq = [0, 0, 0]; // Three containers for sequences
~scaleAdd = [4, 5, 11, nil, 10, 3, 6, nil]; // 
~notes = 
[" C", " C#", " D", " Eb", " E", " F", 
	" F#", " G", " Ab", " A", " Bb", " B"]; 
~rout = Task({
	inf.do({
		arg cnt1; 
		var steps, durSeq, harmSeq;
		steps = rrand(6, 12);
		if(cnt1%6 == 0, // every sixth iteration, add a degree
			{~scale = ~scale.add(~scaleAdd.wrapAt((cnt1/6).round(1) - 1));}); 
		"\nIteration: ".post; cnt1.post; 
		[" (center) ", " (right) ", " (left) "].wrapAt(cnt1).postln;
		if(cnt1%24 == 0, // reset all three
			{~scale = [0, 2, 7, 9]; 
			3.do({arg cnt2; 
				~pSeq.wrapPut(cnt2, 
					Array.fill(steps, 
						{~scale.choose + [48, 60].choose}))})});
		"scale: ".post; ~scale.postln;
		~pSeq.wrapPut(cnt1, // fill array with scale steps
			Array.fill(steps, {~scale.choose + [48, 60].choose}));
		"MIDI seq: ".post; (~pSeq.wrapAt(cnt1)%12).postln;
		"Sequence (notes): ".post; 
		~pSeq.wrapAt(cnt1).do( // print the sequence using note names
			{arg thisItem; ~notes.at(thisItem%12).post}); 
		"".postln;
		// create harmonic and duration arrays
		harmSeq = Array.fill(steps, {rrand(1.0, 5.0)});
		durSeq = Array.fill(steps - 1, {rrand(0.01, 0.9)});
		// stop the previous task at this array position
		~inst.wrapAt(cnt1).stop;
		~inst.wrapPut(cnt1, 
			Task({
				inf.do({arg cnt3; // each sequence
					Synth("SimpleBlip", 
						[\midi, ~pSeq.wrapAt(cnt1).wrapAt(cnt3), 
						\tone, harmSeq.wrapAt(cnt3), 
						\art, durSeq.wrapAt(cnt3), 
						\amp, rrand(0.1, 0.3), 
						\pan, cnt1.wrap(-1, 2)]);
					0.125.wait; // tempo of each note
				})}).start;
		);
		12.wait;})
}).start; // time between each new sequence
)

~rout.stop; // stop new sequences
~inst.at(0).stop; // at any time, stop center sequence
~inst.at(1).stop; // stop right sequence
~inst.at(2).stop; // stop center sequence

