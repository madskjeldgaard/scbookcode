/* IZ 2007-02-03 { SC3
To see the codes for all those keyboard strokes at any time ...
Also demonstrates how to use a custom GUI function to replace the standard GUI.

Still being tested: inconsistencies when restarting the script from the session gui,
with its previous gui having been closed
} */

var window, view, script;
script = ~script;
script.guiFunc = {
	if (window.notNil) {
		window.front
	}{
		window = SCWindow("Type any key to see its codes", Rect(50, 50, 300, 70));
		window onClose: { window.isNil; script.stopped; };
		window.view.decorator = FlowLayout(window.view.bounds, 2@2, 2@2);
		script.makeMainButton(window.view, window);
		window.view.decorator.nextLine;
		view = SCTextField(window, Rect(0, 0, *window.view.decorator.rest));
		view.string = "Start the key with the button above first.";
		view.keyDownAction = { | me ... args |
			args = format("char:  %  modifier:  %  unicode:  %  key:  %", *args).postln;
			me.string = args;
		};
		window.front;
	}
};

~start = {
	~script.makeGui;
//	{ view.string = " Type here ..."; }.defer(0.1);
	view.focus(true);
};

~stop = {
	view.string = " THE END "
};
