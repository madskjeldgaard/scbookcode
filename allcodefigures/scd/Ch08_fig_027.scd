
	// ex. 8.27 - crossfading between different settings with a taskproxy
	
(
	// and some parameters for controlling the fade
d.stopAfterFade = false;
d.xfadeTime = 5; 

d.morphtask = TaskProxy({
	var startSet = d[\current], endSet = d[\target];
	var stepsPerSec = 20;
	var numSteps = d.xfadeTime * stepsPerSec;
	var blendVal, morphSettings;
	
	if (d.target.notNil) { 
		(numSteps).do { |i| 
		//	["numSteps", i].postln;
			blendVal = (i + 1) / numSteps; 
			morphSettings = endSet.collect({ |val, key| 
				(startSet[key] ? val).blend(val, blendVal) 
			});
			d.current_(morphSettings);
			(1/stepsPerSec).wait;
		};
		d.current_(d.target.copy);
		"morph done.".postln;
		if (d.stopAfterFade) { Tdef(\cloud0).stop; };
	};
}).quant_(0);		// no quantization so the task starts immediately

/* test morphing
(
Tdef(\cloud0).play;
d.target = d.set6.copy;
d.morphtask.play;
)
Tdef(\cloud0).stop;

	// playing a finite cloud with tendency mask: 
(
Tdef(\cloud0).play;		// begin playing
d.stopAfterFade = true; 	// end cloud when crossfade ends
d.xfadeTime = 10; 			// set fade time
d.target = d.set8.copy;		// and target
d.morphtask.play;			// and start crossfade.
)
*/

	// put fading into its own method, with optional stop.
d.fadeTo = { |d, start, end, time, autoStop| 
	d.current = d[start] ? d.current;
	d.target = d[end]; 
	d.xfadeTime = time ? d.xfadeTime;
	if (autoStop.notNil) { d.stopAfterFade = autoStop };
	d.morphtask.stop.play;
};

/* 	// tests fadeTo:
Tdef(\cloud0).play;
d.fadeTo(\current, \set2, 20);
d.fadeTo(\current, \set6, 10);
d.fadeTo(\current, \set5, 3, true);

Tdef(\cloud0).play;
d.fadeTo(\current, \set1, 3, false);
*/ 
);



