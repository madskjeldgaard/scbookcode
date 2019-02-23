Figure 18.3
(
//In this example, we use the higher-level server abstraction classes, Group and Synth to handle the
// node IDs. At least as important though is the use of variables. Now that the relationships are
// specified rather than the specific values, we can change the gesture dramatically by changing
// just one or two variables. To transpose everything, we only need to change the value of 
// ~baseNote. To adjust the duration, we only need to change the ~dur variable, and this is now 
// independent of the deltaOn (i.e. independent of the amount of time between the start of one
// note and the start of the next note).
var score;
var deltaOn = 0.2;  //amount of time between the start of one note and the start of the next note
var dur = 0.4;      //try changing dur to 0.3, 1.4, 3.4, or whatever you like
var baseNote = 75;  //transpose the entire fragment up or down
var firstPitch  = (baseNote + 0).midicps;  //alter the relationship between any of the pitches
var secondPitch = (baseNote - 4).midicps;  // without effecting the others
var thirdPitch  = (baseNote + 7).midicps;

score = Score.new([
	[t =  0.0,
		(g = Group.basicNew(s)).newMsg,
		//we use environment variables here (identified by the preceding ~) 
		// since we might add or remove events; hence we don't know ahead of 
		// time how many events we have, and therefore how many variables we'll need
		(~s01 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, firstPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s01.freeMsg
	],
	[t = t + deltaOn,
		(~s02 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, firstPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s02.freeMsg
	],
	[t = t + deltaOn,
		(~s03 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, firstPitch, \dur, dur], \addToHead)
	],
	[t + dur,
		~s03.freeMsg
	],
	[t = t + deltaOn,
		(~s04 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, firstPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s04.freeMsg
	],
	[t = t + deltaOn,
		(~s05 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, secondPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s05.freeMsg
	],
	[t = t + deltaOn,
		(~s06 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, secondPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s06.freeMsg
	],
	[t = t + deltaOn,
		(~s07 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, thirdPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s07.freeMsg
	],
	[t = t + deltaOn,
		(~s08 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, thirdPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s08.freeMsg
	],
	[t = t + deltaOn,
		(~s09 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, thirdPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s09.freeMsg
	],
	[t = t + deltaOn,
		(~s10 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, thirdPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s10.freeMsg,
		g.freeMsg
	],
]
);

score.sort;
score.play(s);
)


