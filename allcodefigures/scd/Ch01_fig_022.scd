1.22 Test Your Skills

(
{
	var carrier, rate, trigger, modRatio, index, control, env;
	rate = 3;
	trigger = Impulse.kr(rate);
	control = LFNoise0.kr(rate);
	carrier = 62;
	modRatio = 4.125;
	index = 10;
	carrier = carrier.midicps;
	carrier.poll(trigger, "carrier"); 
	index.poll(trigger, "index"); 
	modRatio.poll(trigger, "modRatio");
	PMOsc.ar(carrier, carrier*modRatio, index)
}.play
)


