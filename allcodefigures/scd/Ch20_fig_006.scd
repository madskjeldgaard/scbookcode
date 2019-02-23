Figure 20.6
// "Hello World" greeter classes

(
~greeter = Proto({
	~sayhi = { |name|
		"%, %.\n".postf(~greeting.value, name ? ~name);
	};
	~name = "Monique";		// default name
	~greeting = "Hello";		// default greeting
});

// make a subclass with .clone
~frenchGreeter = ~greeter.clone({
	~greeting = "Bonjour";
});

~timeAwareFrenchGreeter = ~frenchGreeter.clone({
	~greeting = {
		var	hour = Date.getDate.hour;
		if(hour < 18) { ~dayGreeting } { ~eveningGreeting };
	};
	~dayGreeting = "Bonjour";
	~eveningGreeting = "Bon soir";
});
)

// Use the objects:
~greeter.sayhi;
~greeter.sayhi("Bob");
~frenchGreeter.sayhi("Isabelle");
~timeAwareFrenchGreeter.sayhi("Eric");


