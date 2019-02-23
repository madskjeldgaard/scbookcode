Figure 5.29
(
// a function that creates a function that counts to any number
var counter_maker;
var window, button1, button2; // gui for testing the function

// the function that makes the counting function
counter_maker = { | max_count |
	// current_count is used by the function created below
	// to store the number of times that it has run
	var current_count = 0;  
	{	// start of definition of the counting function
		if (current_count == max_count) {
			format("finished counting to %", max_count).postln; 
			max_count;		// return max count for eventual use
		}{
			current_count = current_count + 1; // increment count
			format("counting % of %", current_count, max_count).postln; 
			current_count	// return current count for eventual use
		}
	}	// end of definition of the counting function 
};

//  ----- Test application for the counter_maker function ----- 
// window displaying 2 buttons counting to different numbers
window = Window("Counters", Rect(400, 400, 200, 80));
// make a button for triggering the counting: 
button1 = Button(window, Rect(10, 10, 180, 20));
button1.states = [["counting to 10"]];	// labels for button1
// make a function that counts to 10 and store it as action in button1
button1.action = counter_maker.(10);
button2 = Button(window, Rect(10, 40, 180, 20));
button2.states = [["counting to 5"]];	// labels for button2
// make a function that counts to 5 and store it as action in button2
button2.action = counter_maker.(5);
window.front;			// show the window
)

