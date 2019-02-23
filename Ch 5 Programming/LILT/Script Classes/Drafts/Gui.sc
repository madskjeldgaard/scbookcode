/* (IZ 2007-01-18) {
	NOT YET TESTED!
	some code for managing windows that has been used in several places in Lilt.
So this code will be at some point replaced by references to this Classes code.

Gui contains a window and handles incoming update messages to it in simple controller manner. Also stores default and last stored bounds for restoring window in last closed position. 

One makes a new window by creating a Gui instance: 

Gui(model, windowId = 'gui', name, bounds, commands)

model is the object that will be sending the update messages. The handler will add itself as dependant to the model when a window is made and remove itself when it the window closes.

id is the unique id under which the Gui instance will be saved for that particular object. This enables one to open multiple Guis for one model. The handler will check the id, and if ther is still an old handler under this id for this model, it will recreate the window with the bounds and other stuff saved in the handler. 

buildFunc is the function that builds the contents of the window. It is only called
when the window is made (instead of the model having to check in its code whether the window was made this time or not).

commands is an event holding symbols pointing to actions. It acts as simple controller. 

Examples: 

// 1. Empty window
a = Gui(1);
a.makeGui;
a.close;
//////////////////////////////////////////////////////////////
// 2. Number box for setting the value of a Ref
a = Gui(`1, { | gui, model, window | 
	gui.numbox('value');
});
a.makeGui;
a.close;
//////////////////////////////////////////////////////////////
// 3. Number box, sets the value and updates the name of the window

a = Gui(`1, { | gui, model, window | 
	gui.numbox('value', setterAction: { | view | 
		model.value = view.value; 
		window.name = "`(" ++ view.value.asString ++ ")";
	});
});
a.makeGui;
a.close;

} */

Gui {
	classvar <all;	// Library storing any number of gui's for any object under separate id's
	classvar <currentPane;	// pane currently selected for adding gui items
	var <model;		// the object represented by the gui
	var buildAction;	// function for creating the gui items in the window
	var <defaultBounds;	// default bounds for the window
	var saveBounds = true;	// whether to reopen a window in position where it last closed
	var <id;		// symbol under which this gui is stored for the model in "all"
	var <window;	// the window
	var <updateActions;	// envir of keys bound to actions responding to values sent by the model
	var <getterActions;	// envir of keys bound to actions that get the values from the model
	var <savedBounds; // bounds of window at last time it was closed
	var <panes; 	// Panes instance holding this window's panes
	*initClass { all = Library.new }
	*new { | model, buildAction, defaultBounds, saveBounds = true, id |
		var old;
		id = id ?? { model.asString.asSymbol };
		old = all[model, id];
		if (old.isNil) {
			^this.newCopyArgs(model, buildAction,
				defaultBounds ?? { Rect(100, 100, 200, 200) }, saveBounds, id).init;
		}{
			^old.makeGui;
		}
	}
	init {
		// store yourself under your model and id for access
		all[model, id] = this;
		updateActions = ();
		updateActions[id] = { this.updateAll };
		getterActions = ();
	}
	updateAll {
		getterActions keysValuesDo: { | key, action |
			this.update(model, key, action.(model))
		}
	}
	update { | who, what ... moreArgs |
	//		thisMethod.report(who, what, *moreArgs);
			updateActions[what].(who, what, *moreArgs);
	}
	makeGui {
		if (window.notNil) { ^window.front; };
		model.addDependant(this);
//		thisMethod.report(model, this, model.dependants);
		window = SCWindow(id.asString, savedBounds ? defaultBounds);
		panes = Panes(window);
		this.usePane(\top);
		buildAction.(this, model, window); // build action may rename window...
		this.updateAll;					// prompt update of all aspects of model
		window.onClose = { this.closed };
		window.front;
	}
	closed {
		if (saveBounds) { savedBounds = window.bounds };
		model.removeDependant(this);
		window = nil;
	}
	closeWindowOnClose {
		updateActions[\closed] = { this.close };
	}
	close { if (window.notNil) { window.close } }
	usePane { | paneName |
		currentPane = panes.getPane(paneName);
	}
	// ============ METHODS FOR CONSTRUCTING VARIOUS WIDGETS ===============
	makeActions { | key, setterAction, getterAction |
		// Private method: construct actions for updating and setting values: 
		var setterKey;
		if (setterAction.isKindOf(Array)) {
			setterKey = setterAction;
			setterAction = { | me | model.perform(*(setterKey ++ [me.value]))}
		};
		if (setterAction.isKindOf(Function).not) {
			setterKey = (key.asString ++ "_").asSymbol;
			// if (model)
			setterAction = { | me |
				model.perform(setterKey, me.value);
				model.changed(key, me.value);
			};
		};
		if (getterAction.isKindOf(Function).not) {
			getterAction = { model.perform(key) }
		};
		^[setterAction, getterAction];
	}
	numbox { | key = \num, rect, setterAction, getterAction |
		var widget;
		widget = SCNumberBox(currentPane, rect ?? { Rect(0, 0, 100, 20) });
		updateActions[key] = { | who, what, value |
			widget.value = value;
		};
		#setterAction, getterAction = this.makeActions(key, setterAction, getterAction);
		widget.action = setterAction;
		getterActions[key] = getterAction;
		^widget;
	}

	label { | key = \num, rect, setterAction, getterAction |
		var widget;
		widget = SCStaticText(currentPane, rect ?? { Rect(0, 0, 100, 20) });
		updateActions[key] = { | who, what, value |
			widget.string = value;
		};
		#setterAction, getterAction = this.makeActions(key, setterAction, getterAction);
		widget.action = setterAction;
		getterActions[key] = getterAction;
		^widget;
	}

	text { | key = \num, rect, setterAction, getterAction |
		var widget;
		widget = SCTextField(currentPane, rect ?? { Rect(0, 0, 100, 20) });
		updateActions[key] = { | who, what, value |
			widget.string = value;
		};
		#setterAction, getterAction = this.makeActions(key, setterAction, getterAction);
		widget.action = setterAction;
		getterActions[key] = getterAction;
		^widget;
	}
 	slider { | key = \num, rect, setterAction, getterAction |
		var widget;
		widget = SCSlider(currentPane, rect ?? { Rect(0, 0, 100, 20) });
		updateActions[key] = { | who, what, value |
			widget.value = value;
		};
		#setterAction, getterAction = this.makeActions(key, setterAction, getterAction);
		widget.action = setterAction;
		getterActions[key] = getterAction;
		^widget;
	}
	/* 
	*/
	list { | key = \num, rect, setterAction, getterAction // ,
//		selectAction, 
		 |
		var widget;
		widget = SCNumberBox(currentPane, rect ?? { Rect(0, 0, 100, 20) });
		updateActions[key] = { | who, what, value |
			widget.value = value;
		};
		#setterAction, getterAction = this.makeActions(key, setterAction, getterAction);
		widget.action = setterAction;
		getterActions[key] = getterAction;
		^widget;
	}
	// SCKnob does not work? 
/*	knob { | key = \num, rect, setterAction, getterAction |
		var widget;
		widget = SCKnob(currentPane, rect ?? { Rect(0, 0, 70, 70) });
		updateActions[key] = { | who, what, value |
			widget.value = value;
		};
		#setterAction, getterAction = this.makeActions(key, setterAction, getterAction);
		widget.action = setterAction;
		getterActions[key] = getterAction;
		^widget;
	}
	rangeSlider { | key = \num, rect, setterAction, getterAction |
		var widget;
		widget = SCNumberBox(currentPane, rect ?? { Rect(0, 0, 100, 20) });
		updateActions[key] = { | who, what, value |
			widget.value = value;
		};
		#setterAction, getterAction = this.makeActions(key, setterAction, getterAction);
		widget.action = setterAction;
		getterActions[key] = getterAction;
		^widget;
	}
	slider2D { | key = \num, rect, setterAction, getterAction |
		var widget;
		widget = SCNumberBox(currentPane, rect ?? { Rect(0, 0, 100, 20) });
		updateActions[key] = { | who, what, value |
			widget.value = value;
		};
		#setterAction, getterAction = this.makeActions(key, setterAction, getterAction);
		widget.action = setterAction;
		getterActions[key] = getterAction;
		^widget;
	}
	envelope { | key = \num, rect, setterAction, getterAction |
		var widget;
		widget = SCNumberBox(currentPane, rect ?? { Rect(0, 0, 100, 20) });
		updateActions[key] = { | who, what, value |
			widget.value = value;
		};
		#setterAction, getterAction = this.makeActions(key, setterAction, getterAction);
		widget.action = setterAction;
		getterActions[key] = getterAction;
		^widget;
	}
	signal { | key = \num, rect, setterAction, getterAction |
		var widget;
		widget = SCNumberBox(currentPane, rect ?? { Rect(0, 0, 100, 20) });
		updateActions[key] = { | who, what, value |
			widget.value = value;
		};
		#setterAction, getterAction = this.makeActions(key, setterAction, getterAction);
		widget.action = setterAction;
		getterActions[key] = getterAction;
		^widget;
	}
*/
}
