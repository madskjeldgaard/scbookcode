
	//	figure 8.10 - updated setMessage flashes text.
(
z.setMessage = { |z, str| 
	var messSize = str.size; 
	var fontsize = (1.64 * z.txtView.bounds.width) / max(messSize, 32);
	z.txtView.font_(GUI.font.new("Monaco", fontsize));
	z.txtView.string_(str);
	z.animate;
};
)
//!! a long comment gets scaled down to a rather smaller font size, minimally fontsize 32! 

//!! short is big!
(
z.makeWin = { |q, message="Shout this!"|
	z.win = Window("Shout", Rect(0, 900,1200, 100)).front;
	z.win.alpha_(0.7);	
	z.win.view.background_(Color.clear);	
	z.win.alwaysOnTop_(true);

	z.txtView = TextView(z.win, Rect(0, 0,1200, 100));
	z.txtView.background_(Color.clear);
	z.setMessage(message);
};
)
z.makeWin("shout.");


