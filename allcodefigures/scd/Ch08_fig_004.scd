//	figure 8.4 - A minimal shout window sketch.

z = z ? (); 	// make an empty event as a pseudo-object
z.win = Window("Shout", Rect(0, 900,1200, 100)).front;
z.txtView = TextView(z.win, Rect(0, 0,1200, 100));
z.txtView.string_("Shout this!");
z.txtView.font_(Font("Monaco", 32));

	// tune appearances
z.win.alpha_(0.7);		// make the window slightly transparent
z.win.view.background_(Color.clear);	// make the window's top view,
z.txtView.background_(Color.clear);	// and textview fully transparent
z.win.alwaysOnTop_(true);	// make sure it is always on top .

z.win.close;	// close when done



