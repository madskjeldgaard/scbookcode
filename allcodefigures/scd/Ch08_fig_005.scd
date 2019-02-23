// figure 8.5 - add a pseudo-method.
(
z.makeWin = { |z, message="Shout this!"|
	z.win = Window("Shout", Rect(0, 900,1200, 100)).front;
	z.win.alpha_(0.7);	
	z.win.view.background_(Color.clear);	
	z.win.alwaysOnTop_(true);

	z.txtView = TextView(z.win, Rect(0, 0,1200, 100));
	z.txtView.string_(message);
	z.txtView.font_(GUI.font.new("Monaco", 32));
	z.txtView.background_(Color.clear);
};
)
z.makeWin("Try showing that."); 

