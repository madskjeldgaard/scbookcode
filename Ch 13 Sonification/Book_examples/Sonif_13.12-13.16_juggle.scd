/**** 
	Note: all these examples require tracking data to produce sounds.
	As the subtleties of the tracking data are very hard to simulate, 
	we decided not to provide a (misleading) simulation. To hear the 
	examples in action, please see and hear the videos on the book website.
****/

// example juggling 12 - Rotational Grain Train

(
~rotater = {|amp = 1|
	var rotVel = ~rotVel.kr;	// rotVel and height from tracking data proxies
	BPF.ar(
		Impulse.ar((rotVel > 0.5) * rotVel * 5).lag(0.0001),
		(~height.kr * 120 + 36 + [[0, 7], [0, 12], [0, 16]]).midicps, 
		0.2
	).collect({ |pair| (pair * [1, 0.125]).sum }) 	* 6 * amp
};
)



// example juggling 13 - Rotation Trigger
(
~planeTicker = {|saw2sin = 1, filterFreq = 2000, fSpread = 0.4, amp = 0.1|		var freq = 3000 * fSpread * (~height.kr * 4 - 1).range(0.5, 2);
		var src = SelectX.ar(		saw2sin, 		[LFSaw.ar(freq) , SinOsc.ar(freq)]	) 	* Decay2.ar(		Trig1.ar(~zeroCrossing.ar, 0.001) * 0.1, 		0.001,		0.3	);
		LPF.ar(src, filterFreq, mul: amp * 0.1)};
)



// example juggling 14 - Distances to the Head
(
~backCross = {|amp = 1|	var 	numObj = 3;	var 	in = ~isLeft.kr(numObj);	var 	height = ~height.kr(numObj), 		front = ~isFront.kr(numObj); 
	var aEnv, fEnv, aEnvNoise;	var trig = Trig1.ar((in - Delay1.kr(in)).abs - 1, 0.00001) > 0.5;	// only trigger if behind the body and near ground	trig = trig * (front < 0) * (height < 0.26);	aEnv = EnvGen.kr(Env.perc(0.05, 2), gate: trig);	aEnvNoise = EnvGen.kr(Env.perc(0.01, 0.1), gate: trig);	fEnv = EnvGen.kr(		Env.perc(0.01, 0.1), gate: trig, 		levelScale: 900, levelBias: 50	);
		aEnv * ((0.2 * WhiteNoise.ar * aEnvNoise) + 	SinOsc.ar(fEnv * (height * 8).squared * 0.4, 0, 1.5).softclip) * amp};
)


// example juggling 15 -  Left-right Trigger
(
~distances = {|amp = 0.2795| 	LFSaw.ar(min((~dist.kr * 2.5 * 90 + 20).midicps, 44100)) * amp};
)


// example juggling 16 - Rain on Bells
(
~clackUp = { |amp = 0.1|	Formlet.ar(		~trigsUp.ar.lag(0.0004), 		(~height.kr).exprange(25, 2500), 		0.002,		0.05	)	* (~height.kr * 4 ** 2) 	* amp};)