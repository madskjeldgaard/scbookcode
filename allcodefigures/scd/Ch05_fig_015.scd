Figure 5.15
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




