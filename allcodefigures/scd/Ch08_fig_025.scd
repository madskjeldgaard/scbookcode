// figure  8.25	-  tests for the cloud
	
Tdef(\cloud0).play;

	// try changing various things from outside the loop. 
	// change its playing settings 

Tdef(\cloud0).envir.current.put('densRange', [ 50, 200 ]); // dense, async
Tdef(\cloud0).envir.current.put('densRange', [ 1, 10 ]);  // sparse, async
Tdef(\cloud0).envir.current.put('densRange', [ 30, 30 ]); // synchronous

	// for faster access, call the tdef's envir d
d = Tdef(\cloud0).envir;
d.current.put('freqRange', [ 800, 1200 ]);
d.current.put('durRange', [ 0.02, 0.02 ]);

d.current.put('ampRange', [ 0.1, 0.1 ]);

d.current.put('panRange', [ 1.0, 1.0 ]);
d.current.put('panRange', [ -1.0, 1.0 ]);

d.current.put('densRange', [ 30, 60 ]);
d.synName = \percSin;
d.synName = \gab1st;
d.current.put('durRange', [ 0.001, 0.08 ]);


