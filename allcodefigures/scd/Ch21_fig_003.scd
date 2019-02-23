
// Figure 3. A GUI to control the frequency and amplitude of our synth
(
var synth, win;
// we initialize the synth
synth = Synth(\simpleSynth, [\freq, 100, \amp, 0]);
// specify the GUI window
win = Window("simpleSynth", Rect(100,100, 230, 90), false);
// and place the frequency and amplitude sliders in the window
StaticText(win, Rect(10,10, 160, 20)).font_(Font("Helvetica", 9)).string_("freq");
Slider(win, Rect(40,10, 160, 24))
	.action_({|sl| synth.set(\freq, [100, 2000, \exp].asSpec.map(sl.value)) });
StaticText(win, Rect(10,46, 160, 20)).font_(Font("Helvetica", 9)).string_("amp");
Slider(win, Rect(40,46, 160, 24))
	.action_({|sl| synth.set(\amp, [0, 1.0, \amp].asSpec.map(sl.value)) });
win.onClose_({ synth.free }).front; // we add a "onClose" message to the window and "front" it.
)

