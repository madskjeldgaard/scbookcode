// figure 8.7 - text color animation

z.txtView.stringColor_(Color.red);	// try a single color
(
z.animate = { |z, dt=0.2, n = 6| 
	var colors = [Color.red, Color.green, Color.black];
	Task { 
		n.do { |i| 
			dt.wait; 
			z.txtView.stringColor_(colors.wrapAt(i))
		}
	}.play(AppClock)
};
)
z.animate;			// test with default values 
z.animate(0.1, 24); 	// and test with arguments given



