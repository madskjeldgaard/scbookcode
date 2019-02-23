// figure 13.13 - Rotation Trigger
(
~planeTicker = {|saw2sin = 1, filterFreq = 2000, fSpread = 0.4, amp = 0.1|
	
	var freq = 3000 * fSpread * (~height.kr * 4 - 1).range(0.5, 2);

	var src = SelectX.ar(
		saw2sin, 
		[LFSaw.ar(freq) , SinOsc.ar(freq)]
	) 
	* Decay2.ar(
		Trig1.ar(~zeroCrossing.ar, 0.001) * 0.1, 
		0.001,
		0.3
	);

	LPF.ar(src, filterFreq, mul: amp * 0.1)
};
)


