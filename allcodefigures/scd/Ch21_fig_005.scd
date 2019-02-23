
// Figure 5. A GUI with vertical sliders controlling the frequency and amplitude
(
var synth, win;
synth = Synth(\simpleSynth, [\freq, 100, \amp, 0]);
win = Window("", Rect(100, 100, 94, 200), false);
StaticText(win, Rect(20, 170, 160, 20)).font_(Font("Helvetica", 9)).string_("freq");
Slider(win, Rect(10, 10, 30, 160))
	.action_({|sl| synth.set(\freq, [100, 2000, \exp].asSpec.map(sl.value)) });
StaticText(win, Rect(60, 170, 160, 20)).font_(Font("Helvetica", 9)).string_("amp");
Slider(win, Rect(50, 10, 30, 160))
	.action_({|sl| synth.set(\amp, [0, 1.0, \amp].asSpec.map(sl.value)) });
win.onClose_({ synth.free }).front; // we add a "onClose" message to the window and "front" it.
)

