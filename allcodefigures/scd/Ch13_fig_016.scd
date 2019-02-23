// figure 13.16 - Rain on Bells
(

~clackUp = { |amp = 0.1|
	Formlet.ar(
		~trigsUp.ar.lag(0.0004), 
		(~height.kr).exprange(25, 2500), 
		0.002,
		0.05
	)
	* (~height.kr * 4 ** 2) 
	* amp
};
)

