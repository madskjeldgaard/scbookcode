Figure 18.10
s = Server.local;
s.boot;

~sinosc = CtkSynthDef.new(\NRT_sinosc, 
	{arg outbus = 0, freq = 622.254, phase = 0, amp = 1, offSet = 0;
		Out.ar(outbus, SinOsc.ar(freq, phase, amp, offSet));
	}
);

(
var score;
var baseNote = 75;
var slopeTime = 0.25;
var curve = \sine;
var firstPitch = (baseNote + 0).midicps;
var firstStart = 0.0;
var firstDur = 5.0;
var firstAttackTime = slopeTime * 0.5;
var firstDecayTime  = slopeTime - firstAttackTime;
var firstVibDepth = 0.21;
var firstVibRate = 2.3;
var firstPeakAmp = 0.25;
var firstDecayAmp = 0.01;
var secondPitch = (baseNote - 4).midicps;
var secondStart = 2.2;
var secondDur = 4.0;
var secondAttackTime = slopeTime * 0.5;
var secondDecayTime  = slopeTime - secondAttackTime;
var secondVibDepth = 0.15;
var secondVibRate = 1.7;
var secondPeakAmp = 0.25;
var secondDecayAmp = 0.01;
var thirdPitch = (baseNote + 7).midicps;
var thirdStart = 3.1;
var thirdDur = 3.75;
var thirdAttackTime = slopeTime * 0.5;
var thirdDecayTime  = slopeTime - thirdAttackTime;
var thirdVibDepth = 0.21;
var thirdVibRate = 4;
var thirdPeakAmp = 0.2;
var thirdDecayAmp = 0.25;

score = CtkScore.new(
	~firstGroup = CtkGroup.new(firstStart, firstDur, server: s),
	~sinosc.new(firstStart, firstDur, \tail, ~firstGroup, server: s)
		.freq_(CtkControl.lfo(SinOsc, firstVibRate, 
			(firstPitch - ((firstPitch / (firstPitch.log2)) * (firstVibDepth * (1/3)))), 
			(firstPitch + ((firstPitch / (firstPitch.log2)) * (firstVibDepth * (2/3)))), 
			duration: firstDur, addAction: \head, target: ~firstGroup, server: s))
		.amp_(CtkControl.env(
			Env.new([0, firstPeakAmp, firstDecayAmp, 0], [firstAttackTime, 
				firstDur - (firstAttackTime + firstDecayTime), firstDecayTime], curve), 
			addAction: \head, target: ~firstGroup, server: s)),
	~secondGroup = CtkGroup.new(secondStart, secondDur, server: s),
	~sinosc.new(secondStart, secondDur, \tail, ~secondGroup, server: s)
		.freq_(CtkControl.lfo(SinOsc, secondVibRate, 
			(secondPitch - ((secondPitch / (secondPitch.log2)) * (secondVibDepth * (1/3)))), 
			(secondPitch + ((secondPitch / (secondPitch.log2)) * (secondVibDepth * (2/3)))), 
			duration: secondDur, addAction: \head, target: ~secondGroup, server: s))
		.amp_(CtkControl.env(
			Env.new([0, secondPeakAmp, secondDecayAmp, 0], [secondAttackTime, 
				secondDur - (secondAttackTime + secondDecayTime), secondDecayTime], curve), 
			addAction: \head, target: ~secondGroup, server: s)),
	~thirdGroup = CtkGroup.new(thirdStart, thirdDur, server: s),
	~sinosc.new(thirdStart, thirdDur, \tail, ~thirdGroup, server: s)
		.freq_(CtkControl.lfo(SinOsc, thirdVibRate, 
			(thirdPitch - ((thirdPitch / (thirdPitch.log2)) * (thirdVibDepth * (1/3)))), 
			(thirdPitch + ((thirdPitch / (thirdPitch.log2)) * (thirdVibDepth * (2/3)))), 
			duration: thirdDur, addAction: \head, target: ~thirdGroup, server: s))
		.amp_(CtkControl.env(
			Env.new([0, thirdPeakAmp, thirdDecayAmp, 0], [thirdAttackTime, 
				thirdDur - (thirdAttackTime + thirdDecayTime), thirdDecayTime], curve), 
			addAction: \head, target: ~thirdGroup, server: s))
);

score.play;
)

