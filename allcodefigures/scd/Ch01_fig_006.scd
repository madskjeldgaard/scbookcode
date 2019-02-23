1.6 Phase Modulation With Modulator as Ratio

(
{ // carrier and modulator not linked
	r = Impulse.kr(10);
	c = TRand.kr(100, 5000, r);
	m = TRand.kr(100, 5000, r);
	PMOsc.ar(c, m, 12)*0.3
}.play
)
 
(
{
	var rate = 4, carrier, modRatio; // declare variables
	carrier = LFNoise0.kr(rate) * 500 + 700;
	modRatio = MouseX.kr(1, 2.0);
	// modulator expressed as ratio, therefore timbre
	PMOsc.ar(carrier, carrier*modRatio, 12)*0.3
}.play
)
 


