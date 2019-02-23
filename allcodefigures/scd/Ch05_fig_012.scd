Figure 5.12
(
~q = "TOP";							// store "TOP" in ~a, top environment
(a: "INNER") use: { // run function in environment with ~a = "INNER"
	currentEnvironment.postln; // show the current environment
	topEnvironment.postln;		// show the top environment (different!)
	~a.postln				// show ~a's value in current environment
};	
~a;						// show ~a's value in top environment
)


