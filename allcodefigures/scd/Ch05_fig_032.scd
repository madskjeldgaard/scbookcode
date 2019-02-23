Figure 5.32
Server.default.boot;	// boot the server first;
(
var degrees, window, button;
window = Window("melodies?", Rect(400, 400, 200, 200));
button = Button(window, window.view.bounds.insetBy(10, 10));
button.states = [["click me to add a note"]];
button.action = { 
	degrees = degrees add: 0.rrand(15);
	Pbind(\degree, Pseq(degrees), \dur, Prand([0.1, 0.2, 0.4], inf)).play;
};
window.front;
)


