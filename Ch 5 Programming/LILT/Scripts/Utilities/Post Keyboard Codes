/* IZ 2007-02-03 { SC3
To see the codes for all those keyboard strokes at any time ...
} */

var window, view, script;
script = ~script;
~start = {
	window = SCWindow("Type any key to see its codes", Rect(50, 50, 300, 50));
	window.onClose = { script.stopped };
	view = SCTextField(window, window.view.bounds.insetBy(5, 5));
	view.keyDownAction = { | me ... args |
		args = format("char:  %  modifier:  %  unicode:  %  key:  %", *args).postln;
		me.string = args.asString;
	};
	window.front;
	view.focus(true);
};

~stop = { window.close; window = nil; }