// figure 13.12 - Rotational Grain Train
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

