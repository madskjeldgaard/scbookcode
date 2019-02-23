Figure 20.7
(
	// define PR prototype - Proto(...) => PR(\name)
Proto({
	~sayhi = { |name|
			// ~greeting.value is an internal pseudomethod call
		"%, %.\n".postf(~greeting.value, name ? ~name);
	};
	~name = "Monique";		// default name
	~greeting = "Hello";		// default greeting
}) => PR(\greeter);
)

// make working instance as BP
PR(\greeter) => BP(\greeter);
BP(\greeter).sayhi("Jacqueline");
BP(\greeter).name = "Zsa Zsa";
BP(\greeter).sayhi;
	// changing name in BP does not affect PR
PR(\greeter).name;

// override default at chuck time
// now this greeter belongs just to Bob
PR(\greeter).chuck(BP(\greetBob), parms: (name: "Bob"));
BP(\greetBob).name;	// ~name variable is overridden
BP(\greetBob).sayhi;

// remove both instances from the repository
BP([\greeter, \greetBob]).free;


