//	figure 8.18 - keeping Shout out of the way

Shout.close;
Shout("blabla");	// now typing is impossible, because the new window is in front.

Shout("blabla blabla");	// now one can type, because Shout window was already there.
Shout.close; 

	// this does not work, because the shout window gets put in front later:
d = Document.current; Shout(\bla); d.front; 
 

	// This can be wrapped around the call to this.makeWin in Shout:new:
Shout { 
	...
	*new { |message="¡Shout'er!"| 
		var currDoc;

		if (win.isNil or: { win.isClosed }) { 
			currDoc = Document.current;
			top = this.basicNew(message); 
				// wait a little before restoring front window
			defer ({ currDoc.front }, 0.1);
		} {
			top.setMessage(message);
		};
	}
	...	
}



