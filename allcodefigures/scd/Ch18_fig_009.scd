Figure 18.9

(
var score, grain, now, thisdur;
var ampmap, double;

grain = CtkNoteObject(
	SynthDef(\NRT_grain, {arg freq, amp, dur, pan = 0;
		var src, env;
		env = EnvGen.ar(
			Env([0, 1, 0], [0.5, 0.5], \sin),
			timeScale: dur, doneAction: 2, levelScale: amp);
		src = SinOsc.ar(freq, 0, env);
		OffsetOut.ar(0, Pan2.ar(src, pan));
		})
	);
	
score = CtkScore.new;

now = 0;

// create a 3 second granular gesture

while({
	thisdur = 0.05.rrand(0.1);
	score.add(
		grain.new(now, thisdur).freq_(440.rrand(880)).amp_(0.05).dur_(thisdur).pan_(0));
	now = now + 0.01;
	now < 3;
	});

// a function to later map the amplitude to a given shape
// envtimes should be scaled to 1
ampmap = {arg aScore, env;
	// scaled the envs times by the CtkScore's duration
	env.times = env.times * aScore.endtime;
	aScore.notes.do({arg thisNote;
		var curtime;
		curtime = thisNote.starttime;
		thisNote.amp_(env[curtime]);
		});
	};

// returns a new copy of the CtkScore with notes
// double an octave higher
double = {arg aScore, shift = 2;
	var thisScore;
	thisScore = aScore.copy;
	thisScore.notes.do({arg thisNote;
		thisNote.freq_(thisNote.freq * shift)
		});
	thisScore;
	};
		
// a Routine to play the examples
Routine.run({
	var scoreDouble;
	// play the CtkScore;
	score.play;
	score.endtime.wait;
	// remap the amplitudes
	ampmap.value(score, Env([0, 0.2, 0], [0.1, 0.9], [4, -2]));
	1.wait; // pause for a moment
	// play it again!
	score.play;
	score.endtime.wait;
	// add the CtkScore that octaveDouble returns
	scoreDouble = double.value(score, 19.midiratio);
	ampmap.value(scoreDouble, Env([0, 0.25, 0], [0.6, 0.4], [4, -2]));
	score.add(scoreDouble);
	1.wait;
	score.play;
	score.endtime.wait;
	// don't like the second version? remove double
	score.ctkscores.remove(scoreDouble);
	ampmap.value(score, Env([0.15, 0.05], [1]));
	1.wait;
	score.play;
	}).randSeed_(123)
)


