/* SynthDef("martianWinter", { arg out, dt=6, freq=500;
        var v, dist, level, asig;
 //       dist = 0; //XLine.kr(0.02, 0.02, dt) - 0.02;
//        level = LFPar.ar(dist.reciprocal).abs;
        v = Resonz.ar(PinkNoise.ar(LFNoise1.kr(3*[1,1], 1, 8)),  freq, 0.04);
        asig = (Line.kr(0, 1, dt * 0.3) * v)*0.2;
        Out.ar(out, [asig, asig, asig, asig, asig, asig, asig, asig] );
});
*/
/*
SynthDef("martianWinter", { arg out, attack=2, release = 3, freq=500,
		vibFreq = 3, vibAmp = 1;
        var source, level;
        source = Resonz.ar(PinkNoise.ar(LFDNoise1.kr(vibFreq, vibAmp, 2)),  freq, 0.04);
        level = Line.kr(0, 5 attack);
        Out.ar(out, source * level);
});
*/

SynthDef("martianwinter", { arg out = 0, freq=500, amp = 1, attack=2, release = 3, 
		vibFreq = 3, vibAmp = 1, gate = 1;
        var source, env;
        env = Env([0,20,0], [attack, release], releaseNode: 1);
        source = Resonz.ar(PinkNoise.ar(LFDNoise1.kr(vibFreq, vibAmp, 2)),  
        	freq, 0.04, EnvGen.kr(env, gate, doneAction: 2));
         Out.ar(out, source * amp);
}, [\ir]);



/*

a = Synth("martianwinter", [\attack, 0.01, \release, \10]);
a = Synth("martianwinter", [\attack, 0.01, \release, \10]);
a.set(\vibFreq, 10);
a.set(\vibAmp, 10);
a.set(\vibFreq, 100);
a.set(\vibAmp, 1);
a.set(\freq, 1000);
a.set(\gate, 0);
*/