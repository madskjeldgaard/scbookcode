/* (IZ 2005-10-20) {

ListModel provides interface for displaying a list of items on a SCListView. The actions of adding, removing, and selecting items in the list can be customized by setting corresponding instance variables of the ListModel. Furthermore, variables are provided for functions that convert data loaded from a file to a new list or that convert the list to data for saving on file.

Subclass ListWithGui adds name, makeGui function and window for storing a unique window and restoring it when required, if it was previously closed. 

ListWithGui should be used for lists that require one single list gui, but may want to add additional panels to that gui. ListModel can be used as dependant of another ListWithGui to create sub-guis of nested sublists of a list. 

To add an action to be performed when an item is selected, use method onSelect. When a new element is selected, actions performed in the sselectActions array are evaluated. See details in method. 

When the contents of the list model change (addition or removal of an element in the list), the dependants of the list are sent the message update with the ListModel, the symbol \list and the names of the new list items as argument. 
Further update messages can be used by custom functions to update any gui or other element in any way. 


Variables: 

var <>list;			// The actual collection of items to be managed
// functions for converting data items etc. 
var >add, >remove;	// functions for adding or removing items, depending on class of list
var >makeNames;	// make name list for namesItems for display on list views
var >fromData; 	// create new list from data (loaded from file)
var >toData;		// convert list to data for saving on file
// internal caches for display and selection 
var <namesItems;	// array of name-item pairs for deleting by index
var <selection;	// the currently selected item

} */

// EXAMPLE 1. ListModel
(
w = SCWindow.new;
l = SCListView(w, Rect(0, 0, 200, 300));
d = (0..7);
m = ListModel(d);
l.keyDownAction = { |me, char, mod, uni, key|
	switch (char,
		$\r, { if (m.selection.notNil) { m.selection.squared.postln; } },
		127.asAscii, {
			m.removeAt(me.value);
		},
		me.defaultKeyDownAction(char, mod, uni, key);
	);
};
m.addListView(l);
m.onSelect(l, {|i| ["my square root is:", i.sqrt].postln; }, { "no item selected".postln; });
w.front;
)


// EXAMPLE 2. ListWithGui
// Using pop-up menu instead of a list view.
(
l = ListWithGui("Patterns from Numbers", { | l |
	var window, listview, itemview;
	window = SCWindow(l.name, Rect(300, 300, 250, 200));
	listview = SCPopUpMenu(window, Rect(0, 0, 250, 20));
	window.view.keyDownAction = { | me, char, mod, uni, key |
		['keyboard input:', char, mod, uni, key].postln;
		switch (uni,
			13, { l.selectAt(listview.value) },	// return
			3, { l.add(1 + 7.rand); l.selectLast; }, // enter
			127, { l.removeAt(listview.value); },	// backspace
			{ listview.defaultKeyDownAction(char, mod, uni, key); }
		);
	};
	l.addListView(listview);
	itemview = SCTextView(window, Rect(0, 23, 250, 173));
	l.onSelect(itemview,
		{ | item, name, index |
			var beat, amp;
			beat = 0.5 / item;
			amp = 0.1 / item;
			itemview.string =
				"Pick an item from the menu to play.\n\nAll: "
				++ l.names.asString ++ "\n\n\tCurrent element: " ++ name ++
				"\n\tnote amplitude is: " ++ amp.round(0.001).asString
				++ "\n\tnote duration is: " ++ beat.round(0.001).asString ++
				"\n\nbackspace = remove\nreturn = select again\nenter = add new item.";
			listview.focus(1);
			Pbind(\dur, beat, \amp, amp, \degree,
				Pseq((1..item) * [1, -1].choose +.1 Array.rand(item, 1, 3).integrate)
			).play(SystemClock)
		}, {
			itemview.string = " --- no selection ---\nPress enter to add item."
	});
	window;
	},
	(1..10)
);
l.makeGui;
)