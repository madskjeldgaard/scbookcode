
	// figure 8.26 - making random settings, and 8 random presets to switch between
(
	// make the Tdef's envir a global variable for easier experimenting
d = Tdef(\cloud0).envir;
	// a pseudo-method to make random settings, kept in the Tdef's environment
		// randomize could also do limited variation on existing setting. 
d.randSet = { |d|
	var randSet = ();	
	q.paramRNames.do { |pName, i| 
		randSet.put(pName, 
			q.paramNames[i].asSpec.map([1.0.rand, 1.0.rand].sort)
		);
	};
	randSet;
};

/* 	test randSet: 
d.current = d.randSet;
*/

// make 8 sets of parameter range settings: 
d.setNames = (1..8).collect { |i| ("set" ++ i).asSymbol };
d.setNames.do { |key| d[key] = d.randSet; }

/*	test switching to the random presets
d.current = d.set1.copy;	// copy to avoid writing into a stored setting when it is current.
d.current = d.set3.copy;	
d.current = d.set8.copy;
*/
);


