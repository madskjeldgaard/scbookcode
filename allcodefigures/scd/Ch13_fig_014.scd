// figure 13.14 - Distances to the Head
(
~backCross = {|amp = 1|
	var 	numObj = 3;
	var 	in = ~isLeft.kr(numObj);
	var 	height = ~height.kr(numObj), 
		front = ~isFront.kr(numObj); 
	var aEnv, fEnv, aEnvNoise;

	var trig = Trig1.ar((in - Delay1.kr(in)).abs - 1, 0.00001) > 0.5;
	// only trigger if behind the body and near ground
	trig = trig * (front < 0) * (height < 0.26);
	aEnv = EnvGen.kr(Env.perc(0.05, 2), gate: trig);
	aEnvNoise = EnvGen.kr(Env.perc(0.01, 0.1), gate: trig);
	fEnv = EnvGen.kr(
		Env.perc(0.01, 0.1), gate: trig, 
		levelScale: 900, levelBias: 50
	);
	
	aEnv * ((0.2 * WhiteNoise.ar * aEnvNoise) + 
	SinOsc.ar(fEnv * (height * 8).squared * 0.4, 0, 1.5).softclip) * amp
};
)
