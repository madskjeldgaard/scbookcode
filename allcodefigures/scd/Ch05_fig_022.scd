Figure 5.22
(
var window, button; 
window = Window("Server Button", Rect(400, 400, 200, 200));
button = Button(window, Rect(5, 5, 190, 190));
button.states = [["boot"], ["quit"]];
button.action = { | me | 
	[{ "QUITTING THE DEFAULT SERVER".postln;
		Server.default.quit;
	},{ "BOOTING THE DEFAULT SERVER".postln;
		Server.default.boot;
	}][me.value].value;
};
window.front;
)



