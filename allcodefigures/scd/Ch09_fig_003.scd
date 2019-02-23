
figure 9.3

(
var window, slider, mySynth, spec;
mySynth = Synth(\default);
spec = \freq.asSpec;
window = SCWindow("myWindow", Rect(100,100, 220, 40));
slider = SCSlider(window, Rect(8,4, 200, 20))
		.value_(spec.unmap(440))
		.action_({|view| 
			mySynth.set(\freq,			
				spec.map(view.value).postln
			)
		})
		.onClose_({mySynth.release;});
window.front;
CmdPeriod.doOnce({window.close});
)

