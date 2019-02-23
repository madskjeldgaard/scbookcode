/// figure 13.18: Navegar – sound design for a single country
(
~single = { |rootFreq = 220, outProp = 4.5, inProp = 2.2, attack=0.00, decay = 1.0, 
	dens = 2, amp=0.2, x=1, y = 0, step=0.33333| 
	
	var numChans = 4; 
	
	var freqs = rootFreq * [ 1/outProp, 1/inProp, 1, inProp, outProp]; 
	
	var exciter = (Dust2.ar(dens * [ 1,2,4,2,1] * 0.07)  	// five individual rd triggers for 
										// each component, weighted for center
			+ Dust2.ar(dens * 0.3))				// + some common attacks
				.clip2(0.5)	
				.lag(0.0003) 				// slightly filtered
				* (dens ** -0.5)				// amplitude comp for dust density
			+ PinkNoise.ar(0.002)				// some fused background noise
			* (decay ** -0.5);				// amplitude comp. for decay
				
	var resonator = Formlet.ar( exciter, freqs, 
		Ramp.kr(attack, step), decay, 
		AmpComp.kr(freqs.max(50))
	).softclip.sum; 
	
	var pos = atan2(Ramp.kr(y, step), Ramp.kr(x, step)) * (pi.reciprocal) + 1; // look west.

	PanAz.ar(numChans, LeakDC.ar(resonator, 0.95), pos, Ramp.kr(amp, step));
};
~single.play;
)

