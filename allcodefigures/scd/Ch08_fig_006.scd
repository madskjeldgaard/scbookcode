// figure 8.6 - more pseudo-methods.

z.setMessage = { |z, str| z.txtView.string_(str) };

z.setMessage("Does this update?");	// test
(
z.shout = { |z, str|
	if (z.win.isNil or: { z.win.isClosed }) { z.makeWin };
	z.setMessage(str);
};
)
z.shout("Do we get this?"); // test

z.win.close;
z.shout("Do we get this too?"); // also when window has closed?


