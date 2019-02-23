
// Decaying bell. Run several concurrently. 

(
{
var aenv, fenv, out, trig, dur, base;
dur = rrand(1.0, 6.0);
base = exprand(100, 1000);
trig = Impulse.kr(1/6);
out = Mix.ar(
	Array.fill(15,{
		arg count;
		var thisDur;
		thisDur = dur * rrand(0.5, 1.0);
		aenv = EnvGen.kr(
			Env.new([0, 1, 0.4, 1, 0], [0, 0.5, 0.5, 0]), trig, 
			timeScale: thisDur);
		fenv = EnvGen.kr(
			Env.new([0, 0, 0.5, 0.5, 0], [0.25, 0.5, 0.25, 0]), 
				trig, timeScale: thisDur);
		Pan2.ar(SinOsc.ar( Rand(base, base * 12) * 
			LFNoise1.kr(10, mul: 0.1 * fenv, add: 1), // freq
			mul: aenv // amp
		), ([1, -1].choose) * fenv)
	})
) * EnvGen.kr(Env.linen(0, dur, 0), Impulse.kr(6), timeScale: dur, 
		levelScale: 0.05, doneAction: 2);
out*0.3;
}.play;
)


// Repeating gestures using PM crotales.

(
SynthDef("crotale", {
arg param = #[500, 3, 2, 0, 6, 5, 0, 0.9]; 
var factor, env, out, freq, index, dur;
var bus, ratioa, ratiob, attack, decay, panCont;
freq = param.at(0); index = param.at(1); dur = param.at(2);
bus = param.at(3); ratioa = param.at(4); ratiob = param.at(5);
attack = param.at(6); decay = param.at(7);

env = Env.perc(attack, decay);
factor = gcd(ratioa, ratiob);
ratioa = div(ratioa, factor); 
ratiob = div(ratiob, factor);

panCont = (EnvGen.kr(env, timeScale: dur*1.1, 
	levelBias: -1, levelScale: 2))
			* (IRand(0, 1) * 2 - 1);  // 0*2-1 = -1, 1*2-1 = 1

out = PMOsc.ar(
	ratioa*freq, //or try ratioa*freqCont,
	ratiob*freq, //or try ratioa*freqCont,
	pmindex: EnvGen.kr(env, timeScale: dur, 
		levelBias: 1, levelScale: index), 
	mul: EnvGen.kr(env, timeScale: dur, levelScale: 0.3));

out = Pan2.ar(out, panCont);

out = out * EnvGen.kr(env, timeScale: 1.3*dur, 
	levelScale: Rand(0.1, 0.5), doneAction:2);  
Out.ar(0, out); //Out.ar(bus, out);

}).add;
)

// Define the instrument by running the code
// above, then run this task to actually play 
// the instrument. Run several concurrently.


(
r = Task({
	var freq, indexDepth, indexRange, synthIndex, dur, repeat;
	var next, count, countDown, offset, ratioa, ratiob, envs, env;
	var range = 60, outBus = 0;
	count = 0; countDown = 0; offset = 0;
	envs = [[0, 0.9], [0.01, 0.9], [0.1, 0.8], [0.8, 0.01]];
	repeat = Array.fill(10, 
		{[rrand(range, range+24).round(1).midicps, 3, 
			2.1 - exprand(0.1, 2.0), 0, 1, 1, 0, 0.9]});
	next = Array.fill(10, {[3, 0.75, 0.5, 0.25, 0.125].choose});
	freq = rrand(range, range*2); // these two are just starting points
	indexDepth = 1;
	
	inf.do({
		if(countDown <= 0,
			{
			env = envs.choose;
			next.put(count%10, [3, 0.5, 0.25, 0.125, 0.125].choose); 
			repeat.put(count%10, [
				rrand(range, range + 24).round(1).midicps, 
				rrand(0.1, 12.0),
				2.1 - exprand(0.1, 2.0), outBus, rrand(1, 12),  
				rrand(1, 12), env.at(0), env.at(1)]);
			});
			
			Synth("crotale").setn(\param, repeat.wrapAt(count));
			next.wrapAt(count).wait;   
			if((count > 10).and(countDown <= 0), 
				{offset = countDown = [0, 3.rand, 6.rand].choose; 
				count = count - offset});
	count = count + 1;
	countDown = countDown - 1;
	});
}).play(SystemClock);
)



