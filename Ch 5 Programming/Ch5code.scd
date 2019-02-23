  //Figure 5.1
1					// the Integer number 1 
1.234 				// the floating-point (Float) number 1.234
$a 					// the character (Char) a
"hello"				// a String (an array of characters) 
\alpha				// a Symbol (a unique identifier) 
'alpha 1'				// another notation for a Symbol
100@150 				// a Point defined by coordinates x, y 
[1, \A, $b] 				// an Array containing 3 elements
(a: 1, b: 0.2) 			// an Event 
{ 10.rand }				// a Function
String 				// the Class String 
Meta_String 			// the Class of Class String



//Figure 5.3
// Boot the default server first:
Server.default.boot;
// Then select all lines between the outermost parentheses and run: 
( 	
{
	Resonz.ar(GrayNoise.ar,
		XLine.kr(100, 1000, 10, doneAction: 2), 
		XLine.kr(0.5, 0.01, [4, 7], doneAction: 0)
	)
}.play
)
// further examples:
{ WhiteNoise.ar(EnvGen.kr(Env.perc, timeScale: 3, doneAction: 2)) }.play;
{ WhiteNoise.ar(EnvGen.kr(Env.perc, timeScale: 0.3, doneAction: 2))}.play;



 //Figure 5.4
((1 + 2).asString).interpret 		// = 3
"1" ++ "2". interpret			// 12: 2 is translated to string by ++
("1" ++ "2").interpret			// 12
(1.asString ++ 2.asString).interpret 	// 12
"1+2". interpret 				// 3
(1.asString ++ "+2"). interpret		// 3
(1 + 2).interpret				// error: interpret not understood by Integer 3





 //Figure 5.5
(
a = 5;
5 do: { a = a + 10; a.postln };
Post << "The value of variable 'a' is now " << a << "\n";
)

//Figure 5.8
(
// A window with a button that posts: "hello there!"
var window, button;
// create a GUI window and store it in variable window
window = Window.new("OLA!", Rect(200, 200, 120, 120)); 
// create a button in the window and store it in variable button
button = Button.new(window, Rect(10, 10, 100, 100));
button.states = [["'ALLO"]];	// set one single label for the button
button.action = { "hello there!".postln }; // set the action of the button
window.front;		  	  // show the window
)
 
//Figure 5.9

( 
// execute this first to boot the server and load the synth definition
Server.default.waitForBoot({ 
	SynthDef("ping", { | freq = 440 |
		Out.ar(0, 
			SinOsc.ar([freq, freq * (4/3)], 0,
				EnvGen.kr(Env.perc(0.05, 0.3, 0.1, -4), doneAction: 2)
			)
		)
	}).send(Server.default);
});
)

(
// execute this next to create the sounds
var countdown = 100;
var note = 50;
var increment_func, decrement_func;
var action;
increment_func = {
	note = note + [2, 5, 7, 12].choose;
	if (note > 100) { action = decrement_func };
};
decrement_func = {
	note = note - [1, 2, 5, 7, 12].choose;
	if (note < 50) { action = increment_func };
};
action = increment_func;
{
	countdown do: { 
		Synth("ping", [\freq, note.midicps]);
		action.value;
		0.1.wait;
	}
}.fork;
)


 //Figure 5.11
// run each line separately: 
currentEnvironment;	// empty if no environment variables have been set
~alpha = pi;			// set env. variable ~alpha to pi
currentEnvironment;	// see current Environment again: ~alpha is set
~freq = 800;			// set another environment variable
Server.local.boot;
{ LFNoise0.ar(~freq, 0.1) }.play; // use an environment variable
// setting an environment variable to nil is equivalent to removing it: 
~alpha = nil;
currentEnvironment;	// alpha is no longer set


 //Figure 5.12
(
~q = "TOP";							// store "TOP" in ~a, top environment
(a: "INNER") use: { // run function in environment with ~a = "INNER"
	currentEnvironment.postln; // show the current environment
	topEnvironment.postln;		// show the top environment (different!)
	~a.postln				// show ~a's value in current environment
};	
~a;						// show ~a's value in top environment
)


 //Figure 5.13
(
var alpha, beta, gamma;
gamma = alpha;	// storing variable alpha in gamma only stores nil 
alpha = 10; 	// store 10 in alpha ...
gamma.postln;	// but the value of gamma remains unchanged
alpha = beta;	// so one cannot use gamma as 'joker'
beta = 20;		// to switch between variables alpha and beta.
gamma.postln;	// gamma is still nil.
)


 //Figure 5.15
Server.default.boot; 		// (boot Server before running example)
(
// Define a function and call it in different contexts
var synth;				// Synth creating the sound that is changed
var freq = 220; 			// frequency of the sound
var change_freq;			// function that changes the frequency of the sound
var window;			// window holding buttons for changing the sound
var button1, button2, button3; // buttons changing the sound

// Create a synth that plays the sound to be controlled:
synth = { | freq = 220 | LFTri.ar([freq, freq * 2.01], 0, 0.1) }.play;
// Create frequency changing function and store it in variable change_freq
change_freq = {						// start of function definition
		freq = freq * [0.9, 0.9.reciprocal].choose; // change freq value
		synth.set(\freq, freq);			// set synth's frequency to new value
};								// end of function definition

// Create 3 buttons that call the example function in various ways
window = Window("Buttons Archaic", Rect(400, 400, 340, 120));
//  ------------------------- Example 1 -------------------------
button1 = Button(window, Rect(10, 10, 100, 100));
button1.states = [["I"]]; // set the label of button1
// button1 calls the function each time that it is pressed 
button1.action = change_freq;	// make button1 change freq once
//  ------------------------- Example 2 -------------------------
button2 = Button(window, Rect(120, 10, 100, 100));
button2.states = [["III"]];
// Button2 creates a routine that calls the example function 3 times
button2.action = { 			// make button2 change freq 3 times
	{ 3 do: { change_freq.value; 0.4.wait } }.fork; // play as routine
};
//  ------------------------- Example 3 -------------------------
button3 = Button(window, Rect(230, 10, 100, 100));
button3.states = [["VIII"]];
button3.action = { 			// like example 2, but 8 times
	{ 8 do: { change_freq.value; 0.1.wait } }.fork; // play as routine
};
// use large size font for all buttons:
[button1, button2, button3] do: _.font_(Font("Times", 32));
// stop the sound when the window closes: 
window.onClose = { synth.free };
window.front; // show the window
)




 //Figure 5.18
(
// a function that calculates the square of the mean of two numbers
var sq_mean;
sq_mean = { arg a, b; 	// arguments a, b defined in arg statement form
	(a + b / 2).squared;
};
// calculate the square of the mean of 3 and 1:
sq_mean.value(3, 1);
)


 //Figure 5.19
(
// a function that calculates the square of the mean of any numbers
var sq_mean_all;
sq_mean_all = { | ... numbers | // using ellipsis and | | argument form
	(numbers.sum / numbers.size).squared;
};
// calculate the square of the mean of [1, 3, 5, -7]:
sq_mean_all.(1, 3, 5, -7); // short form: omit message 'value'
)


 //Figure 5.20
(
var w_func;
w_func = { arg message = "warning!", bounds = Rect(200, 500, 500, 100);
	var window;
	window = Window("message window", bounds).front;
	TextView(window, window.view.bounds.insetBy(10, 10))
		.string = message;	
};
 // provide text, use default bounds
w_func.(String.new.addAll(Array.new.addAll(" Major news! ").pyramid(7)));
)


 //Figure 5.21
(
var window, button; 
window = Window("Server Button", Rect(400, 400, 200, 200));
button = Button(window, Rect(5, 5, 190, 190));
button.states = [["boot!"], ["quit!"]];
button.action = { |me| Server.default perform: [\quit, \boot][me.value] };
window.front;
)


 //Figure 5.22
(
var window, button; 
window = Window("Server Button", Rect(400, 400, 200, 200));
button = Button(window, Rect(5, 5, 190, 190));
button.states = [["boot"], ["quit"]];
button.action = { | me | 
	[{ "QUITTING THE DEFAULT SERVER".postln;
		Server.default.quit;
	},{ "BOOTING THE DEFAULT SERVER".postln;
		Server.default.boot;
	}][me.value].value;
};
window.front;
)



 //Figure 5.23
Server.default.boot // boot default server before running example
(
var buffer;
buffer = Buffer.read(path: "sounds/a11wlk01.wav",
	action: { | buffer |
		format("loaded % at: %", buffer, Main.elapsedTime).postln;
	});
format("Reached this after 'Buffer.read' at: %", Main.elapsedTime).postln;
buffer;
)


 //Figure 5.24
Server.default.boot;  // do this first
(						// then the rest of the program
var window, routine;
window = Window("close me to stop").front;
window.onClose = { routine.stop };
routine = { 
	loop {
		(degree: -10 + 30.xrand, dur: 0.05, amp: 0.1.rand).play;
		0.05.rand.wait;
	}
}.fork;
)


 //Figure 5.25
_.isPrime ! 10
_.squared ! 10
_@_.(30, 40) // equivalent to: { | a, b | Point(a, b) }.value(30, 40)
Array.rand(12, 0, 1000).clump(4) collect: Rect(*_)
(1..8).collect([\a, \b, _]);
(a: _, b: _, c: _, d: _, e: _).(*Array.rand(5, 0, 100));


 //Figure 5.26
(
var iterative_factorial;
iterative_factorial = { | n |
	var factorial = 1;	// initialize factorial as factorial of 1
	// calculate factorial n times, updating its value each time
	n do: { | i | factorial = factorial * (i + 1) };
	factorial;	// return the final value of factorial;
};
iterative_factorial.(10).postln;	// 10 factorial: 3628800
)



//Figure 5.27
// Define the factorial function and store it in variable f:
f = { | x | if ( x > 1) { x * thisFunction.value(x - 1) } { x } };
 f.value(10);			// 10 factorial: 3628800


 //Figure 5.28
(
/* a function that recursively prints all folders and files
   found in a path and its subfolders */
{ | path |
	// store function here for use inside the if's {}:
	var thisFunc = thisFunction;
	format("====== now exploring: %", path).postln;
	// for all items in the path:
	path.pathMatch do: { | p | 
		// if the item is a folder, run this function on its contents
		// otherwise print the file found
		if (p.last == $/) { thisFunc.(p ++ "*") }{ p.postln }
	}
}.("*") // run function on home path of SuperCollider
)


//Figure 5.29
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


//Figure 5.31
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




//Figure 5.32
Server.default.boot;	// boot the server first;
(
var degrees, window, button;
window = Window("melodies?", Rect(400, 400, 200, 200));
button = Button(window, window.view.bounds.insetBy(10, 10));
button.states = [["click me to add a note"]];
button.action = { 
	degrees = degrees add: 0.rrand(15);
	Pbind(\degree, Pseq(degrees), \dur, Prand([0.1, 0.2, 0.4], inf)).play;
};
window.front;
)


 //Figure 5.34
1.class 		// the class of Integer 1: Integer
1.class.class 	// the Class of the Class of Integer 1: Meta_Integer
// the Class of the Class of the Class of Integer 1: 
1.class.class.class  					// Class
// the Class of the Class of the Class of the Class of Integer 1
1.class.class.class.class				// Meta_Class
// the Class of the Class of the Class of the Class of the Class of 1
1.class.class.class.class.class 		// Class 
Class.class				// the Class of Class is Meta_Class
Meta_Class.class		// the Class of Meta_Class is Class



 //Figure 5.36
Counter { 
	// variables: maximum count, current count
	var <>max_count, <>current_count = 1;
	// class method for creating a new instance
	*new { | max_count = 10 |
		^super.new.max_count_(max_count)
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
	// reset count
	reset {
		current_count = 1;
		this.changed(\reset);
	}
}


 //Figure 5.37
Server.default.boot;
(
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
)


 //Figure 5.38
(
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
)
 //Figure 5.39
(
~make_display = { | counter |
	var window, label, adapter, stagger;
	window = Window(
		"counting to " ++ counter.max_count.asString, 
		Rect(stagger = UniqueID.next % 20 * 20 + 400, stagger, 200, 50)
	);
	label = StaticText(window, window.view.bounds.insetBy(10, 10));
	adapter = { | counter, what, count |
			{ label.string = counter.current_count.asString }.defer
	};
	counter addDependant: adapter;
	/* remove the adapter when window closes to prevent error in 
	   updating non-existent views: */
	window.onClose = { counter removeDependant: adapter };
	window.front
};
)










