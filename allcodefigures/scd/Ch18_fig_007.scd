Figure 18.7


// environment variables are used for real-time examples of Ctk objects

n = CtkNoteObject(
	SynthDef(\NRT_grain, {arg gate = 1, freq, amp;
		var src, env;
		src = SinOsc.ar(freq, 0, amp);
		env = EnvGen.kr(Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction:2);
		OffsetOut.ar(0, src * env);
		})
	);
	
// create a new note based on 'n', start to play it in 0.1 seconds
a = n.new(0.1).freq_(440).amp_(0.1).gate_(1).play;
// the release method will set 'gate' to 0.0, and free this node
a.release;

// create another note
a = n.new(0.1).freq_(440).amp_(0.1).play;
// alter the freq argument in real time
a.freq_(550);
// alter the freq with a CtkControl that describes an Env
// CtkControl.env(Env)
a.freq_(CtkControl.env(Env([550, 440, 550], [1, 2], \exp)));
// apply a random control to the amp parameter, with an envelope applied to the range. All 
// parameters to the CtkControl can themselves be CtkControls
// CtkControl.lfo(KRUGen, freq, low, high, phase)
a.amp_(CtkControl.lfo(LFNoise2, 0.5, CtkControl.env(Env([0.1, 0.9], [5])), 0.1));
a.amp_(0.1);

// release the note
a.release;


