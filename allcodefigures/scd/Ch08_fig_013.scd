
	// figure 8.13 - converting makeWin.

z.makeWin = { |message="Shout this!"|
	z.win = GUI.window.new("Shout", Rect(0, 900,1200, 100)).front;
	z.win.alpha_(0.7);	
	z.win.view.background_(Color.clear);	
	z.win.alwaysOnTop_(true);

	z.txtView = GUI.textView.new(z.win, Rect(0, 0,1200, 100));
	z.txtView.background_(Color.clear);
	z.setMessage(message);
};


Shout {	
	...
	*new { |message| ^super.new.makeWin(message); }
	
	makeWin { |message="Shout this!"| 
	
		win = Window("Shout'er", Rect(20, 800, width, 80)).front;
		win.alpha_(0.7);
		win.view.background_(Color.clear);
		win.alwaysOnTop_(true);
		
		txtView = TextView(win, win.bounds.moveTo(0,0));
		txtView.background_(Color.clear);
		txtView.font_(Font.new("Monaco", 32));
		this.setMessage(message);
	}

	setMessage { |message| 
		txtView.string_(message.asString)
	}
}

// tests:
Shout.new;
a = Shout.new("Blabla");
a.setMessage("Otto");



