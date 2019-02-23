// better solution for fade times: Make fadeins fadein * stretch.reciprocal. This will make the time constant so that onset phase is correct. Should be able to optimise it so it only happens once!

LorisPartialEnvs {

	*ar{|partials, stretch = 1, pitch = 1, bw, ioff = 0|
		var phases, envs;
		phases = Array.new(partials.size);
		partials.partialList.do({ arg item, i;
			var time, theseEnvs;
			//item.postln;
			time = item[0];
			phases.add(item[1]);
			// freq, amp, bw
			theseEnvs = [Env(item[4], item[2]), Env(item[3], item[2]), Env(item[5], item[2])];
			theseEnvs = theseEnvs
				.collect({|env, i|
					var levelScale = 1;
					if(i == 0, {levelScale = pitch.value});
					if(i == 2, {levelScale = bw.value}); 
				
					EnvGen.ar(env.delay(time + (i * ioff)), levelScale: levelScale, 
						timeScale: stretch.value); 
			});
			theseEnvs = theseEnvs.add(item[1]); // initial phase
			envs = envs.addAll(theseEnvs);
		});

		^envs.unlace(4);
	}
}
