Figure 5.8
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
 
