Figure 18.2
s = Server.local;
s.boot;

// a sample SynthDef
SynthDef(\NRT_beep, {arg freq, dur, amp = 0.1;
	var half;
	half = dur * 0.5;
	Out.ar(0, SinOsc.ar(freq, 0, 
		EnvGen.kr(Env.new([0, amp, 0], [half, half], [4, -4]))));
	}).load(s);

(
var score;

// A Score, created from a note-list of time-stamped events.
score = Score.new([
    [0.0,
        [\g_new, 1000],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 75.midicps, \dur, 0.2]
    ], 
    [0.2,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 75.midicps, \dur, 0.2]
    ], 
    [0.4,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 75.midicps, \dur, 0.2]
    ], 
    [0.6,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 75.midicps, \dur, 0.2]
    ], 
    [0.8,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 71.midicps, \dur, 0.2]
    ], 
    [1.0,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 71.midicps, \dur, 0.2]
    ], 
    [1.2,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 82.midicps, \dur, 0.2]
    ], 
    [1.4,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 82.midicps, \dur, 0.2]
    ], 
    [1.6,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 82.midicps, \dur, 0.2]
    ], 
    [1.8,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 82.midicps, \dur, 0.2]
    ], 
    [2.0,
        [\n_free, 1001, 1000]
    ],
	[2.00001, [0]]
]);

score.play(s); // play the Score in real-time...

// ... or render in Non-Real-Time
score.recordNRT("/tmp/trashme", "~/test.aiff".standardizePath, 
	options: ServerOptions.new.numOutputBusChannels_(1));
)
