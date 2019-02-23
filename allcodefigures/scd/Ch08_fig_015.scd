	//	figure 8.15 - converting animate to a class method.

z.animate = { |z, dt=0.2, n = 6| 
	var colors = [Color.red, Color.green, Color.black];
	Task { 
		n.do { |i| 
			dt.wait; 
			z.txtView.stringColor_(colors.wrapAt(i))
		}
	}.play(AppClock)
};

Shout { 
	...
	animate { |dt=0.2, n=6|
		var colors = [Color.red, Color.green, Color.black]; 
		Task { 
			n.do { |i| 
				txtView.stringColor_(colors.wrapAt(i)); 
				dt.wait 
			};
			txtView.stringColor_(Color.black); // make sure we end black
		}.play(AppClock);
	}
	...	
}

// tests: 
a = Shout("Test 1, 2");
Shout.top.animate;



