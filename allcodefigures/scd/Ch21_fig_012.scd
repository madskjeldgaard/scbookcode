
// Figure 12. A sketch of snow triggering a bell sound on landing
(
SynthDef(\snowBell, { | freq=440, amp=0.4, pan=0 |
	var x, env;
	env = EnvGen.kr(Env.perc(0.001, Rand(550,650)/freq, amp), doneAction:2);
	x = Mix.fill(6, {SinOsc.ar(freq*Rand(-10,10), 0, Rand(0.1,0.2))});
	x = Pan2.ar(x, pan, env);
	Out.ar(0, x);
}).add;
)

(
var win, msl, trigAction, snowloc, speeds, speed, layers=4, snowcount = 62;

// fill an array with arrays (number of layers) of locations
snowloc = {{rrand(0.38,1.5)} ! snowcount} ! layers;
// fill an array with arrays (number of layers) of step size (speed)
speeds = {{rrand(0.01,0.018)} ! snowcount} ! layers;

speed = 0.1;

win = Window("snow", Rect(11, 311, 520, 240), border: false).front;
win.view.background = Color(0.14,0.17,0.24);

msl = Array.fill(layers, {|i|
		MultiSliderView(win, Rect(-1, -1, 522, 242))
			.strokeColor_( Color.new255(rrand(22,35),rrand(22,35),rrand(22,35)) )
			.fillColor_( Color.new255(rrand(222,255),rrand(222,255),rrand(222,255)) )
			.valueThumbSize_(rrand(2.8,3.8))
			.indexThumbSize_(rrand(2.8,3.8))
			.gap_(5)
	});

// when the snow falls this happens. (pitch is mapped to index and amplitude to speed)
trigAction = {arg drop, amp; Synth(\snowBell, [\freq, 400+(drop*20), \amp, amp, \pan, rrand(-0.8, 0.8)])};

t = Task({
	loop({
		snowloc = snowloc.collect({|array, i| 
			array = array.collect({|val, j| 
				val = val-speeds[i][j]; 
				if(val< 0.0, {val = 1.0; trigAction.(j, speeds[i][j]*10 )});
				val
			});
			array
		});
		/* 
		Task uses the TempoClock by default so we need to "defer" the GUI updating 
		(Function:defer uses AppClock) This means that the Task is essentially using 
		the SystemClock and therefore the timing is better on the sound front. 
		The AppClock (used for GUI updates) has worse timing.
		*/
		{ layers.do({|i| msl[i].value_(snowloc[i]) }) }.defer;
		speed.wait;
	});
}).start;

// on stopping the program (Command/Ctrl + dot) the task will stop and the window close
CmdPeriod.add({ t.stop; win.close; });
)

