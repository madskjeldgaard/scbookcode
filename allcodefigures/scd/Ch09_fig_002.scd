figure 9.2

(
var window, slider, mySynth;
mySynth = Synth(\default);
window = SCWindow("myWindow", Rect(100,100, 220, 40));
slider = SCSlider(window, Rect(8,4, 200, 20))
		.value_(440.explin(20,20000,0,1))
		.action_({|view| 
			mySynth.set(\freq,
				view.value.linexp(0,1,20,20000).postln
			)	
		})
		.onClose_({mySynth.release;});
window.front;
CmdPeriod.doOnce({window.close});
)

