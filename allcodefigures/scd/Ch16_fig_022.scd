Figure 16.22: A Wavesets object

w = Wavesets.from("sounds/a11wlk01.wav");

w.xings;			// all integer indices of the zero crossings found
w.numXings;		// the total number of zero crossings
w.lengths;		// lengths of all wavesets
w.amps;			// peak amplitude of every waveset
w.maxima;			// index of positive maximum value in every waveset
w.minima;			// index of negative minimum value in every waveset

w.fracXings;		// fractional zerocrossing points
w.fracLengths;	// and lengths: allows more precise looping.
				
w.lengths.plot;	// show distribution of lengths
w.amps.plot;

	// get data for a single waveset: frameIndex, length (in frames), dur
w.frameFor(140, 1);	
w.ampFor(140, 1);		// peak amplitude of that waveset or group
	
	// extract waveset by hand
w.signal.copyRange(w.xings[150], w.xings[151]).plot("waveset 150");
w.plot(140, 1);	// convenience plotting
w.plot(1510, 1);	

	// plot a group of 5 adjacent wavesets
w.plot(1510, 5)	


