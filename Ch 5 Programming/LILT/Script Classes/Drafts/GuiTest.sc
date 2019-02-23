/* IZ 2007-06-22 { SC3
Test class for new gui approach utilizing experiences gained so far with ListModel, WindowHandler etc. 

=== Meta - technique: 
Make a spec-driven technique for spliting a window into panes, where each pane has its name and dimensions (and optionally maybe dim flex specs)
Suggest using messages: 
.split(pane = \top, \horizontal, name1, fraction | pixels, name2, fraction | pixels, name3 ...)
.split(pane = \top, \vertical, name1, fraction, name2, fraction ...)
Then to use a pane, send usePane to the GuiMaker class to make it active

GuiMaker.usePane(paneName = top, window);
Defaulte pane is window.view (top view of the current window);

Rejected: 
(
	top: 0@0.5,
	buttons: 0.3@0,
	display: 0@0
)
Where 0 means take all the space left in this dimension
and number where 0 < number < 1 means take number fraction of that dimension. 

=== Things to implement macros for creating guis in easier ways: 
Single item interaction and editing: 

Number
	NumberBox
	Slider
	Knob
String
Button
Menu

Arrays of items: 
	Array of Number Boxes
	Array of Check-Boxes
	Array of Knobs
	Array of Sliders

Lists of Items
	List editor for Array
	List editor for Dictionary (Event)
	[List editor for Set?]

=== To test this:
Create a class that has several of the above kinds of objects as instance variables, and create an interface for it. 
-Use 2 windows for different subsets of the variables
-Demonstrate how a common variable in both windows gets updated when it changes in one of them
-Demonstrate how changing a *link* from one instance of this test class to another one
 will update the guis of both instances.

=================


} */
/*
GuiTest {
	classvar <all;	// all instances of this class: for finding source links
	var <>name;
	var <>num;
	var <>envir;
	var <>targets; // array of GuiTest instances

	getSources {
		^all select: { | g | g.targets.asArray includes: this }
	}

	makeGui {
		WindowHandler(this, \main, Rect(200, 200, 400, 300), { | window, model, handler |
			// window's top pane is already stored in WindowHandler
			this.numbox(
				Rect(0, 0, 200, 20), // dimensions
				\num, // value getter action and update key
				'num_', // value setter action
				\freq, // optional: spec for mapping
				// optional: custom keyboard commands
				// 
				
			);
			
		}
		).makeGui;
		
	}
	makeLinksGui {
		
	}
}

*/