Figure 5.31
(
var counter_maker;		// creator of counters
var make_counters_gui;	// function making counters + a gui
/* a function that creates an event that counts to any number, 
   and resets: */ 
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
	window = Window("Counters", 
			Rect(400, 400, 200, 50 * counts.size + 10));
	// enable automatic placement of new items in window: 
	window.view.decorator = FlowLayout(window.view.bounds, 5@5, 5@5);
	counts collect: counter_maker.(_) do: { | counter |  
		Button(window, Rect(0, 0, 190, 20))
			.states_([["Counting to: " ++ counter.max_count.asString]])
			.action = { counter.count1 };
		Button(window, Rect(0, 0, 190, 20))
			.states_([["Reset"]])
			.action = { counter.reset_count };
	};
	window.front;
};
make_counters_gui.(5, 10, 27); // example use of the GUI test function
)




