Counter : Model { 
	// variables: maximum count, current count
	var <>max_count, <>current_count = 1;
	// class method for creating a new instance
	*new { | max_count = 10 |
		^super.new.max_count_(max_count)
	}
	// test method: returns array of 5 Counter instances
	*make5 { 
		^(5, 10 .. 25) collect: this.new(_)
	}
	// if maximum count not reached, increment count by 1
	count1 {
		if (current_count >= max_count) {
			this.changed(\max_reached)
		}{
			current_count = current_count + 1;
			this.changed(\count, current_count);
		}
	}
	// reset count to 1
	reset {
		current_count = 1;
		this.changed(\reset);
	}
}

/*

SynthDef("ping", { | freq = 440 | 
	Out.ar(0, 
		SinOsc.ar(freq, 0, 
			EnvGen.kr(Env.perc(level: 0.1), doneAction: 2)
	))
}).send(Server.default);

SynthDef("wham", {
	Out.ar(0, BrownNoise.ar(
		EnvGen.kr(Env.perc(level: 0.1), doneAction: 2)
	))
}).send(Server.default); 


~sound_adapter =  { | counter, what, count |
	switch (what, 
		\reset, { Synth("wham"); },
		\max_reached, { counter.reset },
		\count, { Synth("ping", 
			[\freq, count.postln * 10 + counter.max_count * 20]
			)
		}
	)
};

~counters = (6, 11 .. 26) collect: Counter.new(_);
~counters do: _.addDependant(~sound_adapter);

// Make a routine that increments all counters every 1/2 seconds
~counter = { loop { ~counters do: _.count1; 0.25.wait } }.fork;

~make_display = { | counter |
	var window,label, adapter, stagger;
	window = GUI.window.new(
		"counting to " ++ counter.max_count.asString, 
		Rect(stagger = UniqueID.next % 20 * 20 + 400, stagger, 200, 50)
	);
	label = GUI.staticText.new(window, window.view.bounds.insetBy(10, 10));
	adapter = { | counter, what, count |
		{ label.string = counter.current_count.asString }.defer
	};
	counter addDependant: adapter;
	window.onClose = { counter removeDependant: adapter };
	window.front
};


// make displays or close their windows at any time: 
~make_display.(~counters[0]);
~make_display.(~counters[4]); 

~counter.stop;

~counters do: ~make_display.(_);


*/




/* a function that creates an event that counts to any number, 
   and resets: */
  
/* 
counter_maker = { | max_count |
	var current_count = 0; 
	(	// the counter object is an event with 3 functions: 
		count1: // function 1: increment count (stored as count1)
		{	// start of definition of the counting function
			if (current_count == max_count) {
				format("finished counting to %", max_count).postln; 
			}{
				current_count = current_count + 1; // increment count
				format("counting % of %", current_count, max_count).postln; 
			}
		},	// end of definition of the counting function
		reset_count: { // function 2: reset count (stored as reset_count)
			format("resetting % counter", max_count).postln;
			current_count = 0
		},
		max_count: { max_count } // function 3: return value of max_count
	)
};
// Function that makes several counters and a GUI to control them
make_counters_gui = { | ... counts |
	var window, counter;
	window = GUI.window.new("Counters", 
			Rect(400, 400, 200, 50 * counts.size + 10));
	// enable automatic placement of new items in window: 
	window.view.decorator = FlowLayout(window.view.bounds, 5@5, 5@5);
	counts collect: counter_maker.(_) do: { | counter |  
		GUI.button.new(window, Rect(0, 0, 190, 20))
			.states_([["Counting to: " ++ counter.max_count.asString]])
			.action = { counter.count1 };
		GUI.button.new(window, Rect(0, 0, 190, 20))
			.states_([["Reset"]])
			.action = { counter.reset_count };
	};
	window.front;
};
make_counters_gui.(5, 10, 27); // example use of the GUI test function
)
*/