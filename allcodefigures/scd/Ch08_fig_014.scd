
	//	figure 8.14 - converting z.shout to Shout.new.

z.shout = { |z, str|
	if (z.win.isNil or: { z.win.isClosed }) { z.makeWin };
	z.setMessage(str);
};

Shout { 
	classvar <top;
	...
	*new { |message="¡Shout'er!"| 

		if (top.isNil or: { top.win.isClosed }) { 
			top = this.basicNew(message); 
		} {
			top.setMessage(message);
		};
	}
		// the method formerly known as *new
	*basicNew { |message="Shout this!"| ^super.new.makeWin(message) } 

	*close { try { top.win.close } }
	
	...
}

// tests: 
Shout("Test 1, 2");
Shout("Test 1, 2, 3, 4");	// same window
Shout.close;

Shout("Test 1, 2");		// new window






