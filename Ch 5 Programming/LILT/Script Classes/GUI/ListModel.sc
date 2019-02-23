/* (IZ 2005-10-19) {

ListModel: list management functionality, including adding of multiple views for displaying the same list of items. 

Configurable actions for adding, removing, selecting, displaying lists of data.

ListWithGui: ListModel with additional name, window and window making function. Used little, will be removed when the few classes using it start using ListModel with WindowHandler instead.

} */

ListModel : Model {
	var <list;	// The list of items to be managed, or object that can provide such a list. 
	// functions for converting data items etc. :
	var >add, >remove;	// functions for adding or removing items, depending on class of list
	var >makeNames;	// make name list for namesItems for display on list views
	// internal caches for display and selection 
	var <namesItems;	// array of name-item pairs for deleting by index
	var <selection;		// the currently selected item
	var <selectActions; // array of actions to do whenever selection changes
	*new { | list, add, remove, makeNames /*, fromData, toData */ |
		^this.newCopyArgs(nil,
			list ? [],	// initialize contents of list
			add ?? {#{ |l, i| l.list.add(i) }},	// action for adding an item
			remove ?? {#{ |l, i| l.list.remove(i); l.list }}, // action for removing an item
			// action for making names of the items for display.
			makeNames ?? {#{ |l| l.list.collect { |i| [i.asString, i] }}}
		).makeNamesItems; // initialize the names - items array for display and access
	}
	onSelect { | ... args |
		// Add an action to perform whenever an item is selected.
		// Any number of actions can be added by repeated calls of this method. 
		// accepts args: view, itemAction, nilAction, or itemAction, nilAction. 
		// creates adapter that executes itemAction whenever the selection of this
		// list changes to a non nil item, and nilAction whenever the newly selected
		// item is nil. If first argument is an SCView, then the adapter is removed 
		// when that view closes.
		var view, itemAction, nilAction, adapter, oldOnClose;
		if (args[0].isKindOf(SCView)) {
			#view, itemAction, nilAction = args;
			adapter = this.makeSelectAdapter(itemAction, nilAction);
			oldOnClose = view.onClose;
			view.onClose = {
				oldOnClose.value;
				selectActions.remove(adapter);
			};
		}{
			#itemAction, nilAction = args;
			adapter = this.makeSelectAdapter(itemAction, nilAction);
		};
		selectActions = selectActions.add(adapter);
		^adapter;
	}
	makeSelectAdapter { | itemAction, nilAction |
		^{ | item, name, index |
			if (item.isNil) {
				nilAction.(item, name, index, this)
			}{
				itemAction.(item, name, index, this)
			}
		}
	}
	// perform add action with item and store the result in list:
	add { | item | this.list = add.(this, item) }
	// add and item as above and select it: 
	addSelect { | item |
		this.add(item);
		this.selectItem(item);
	}
	list_ { | argList |
		// when a new list becomes my contents, update items' names and views
		list = argList;
		this.updateNames;
	}
	updateNames {
		this.makeNamesItems;
		this.listChanged;
	}
	makeNamesItems {
		// create names for the items for display on list views
		namesItems = makeNames.(this);
	}
	listChanged {
		// if list is changed, update list views, also giving the current selection
		this.changed(\list, this.indexOf(selection) ? 0, this.names);
		// TODO: this to be replaced by this.changed(\selection?)
		// perhaps the onSelect should use update mechanism after all! ...
		if (selection.notNil) {
			this.selectItem(selection);
		};
	}
	indexOf { | item |
		// find and return the index of an item.
		// [[1, 2]].slice(nil, 1) should return [2], not 2, so
		// namesItems.slice(nil, 0) does not work consistently for [[a, b]], so use:
		if (namesItems.size == 0) {
			^nil
		}{
			^namesItems.flop[1].indexOf(item);
		}
	}
	names {
		// return a list of the items as strings for display in a list view.
		// namesItems.slice(nil, 0) does not work consistently for [[a, b]], so use:
		if (namesItems.size == 0) {
			^[]
		}{
			^namesItems.flop[0];
		}
	}
	addListView { | listview |
		// Make listview represent your contents and handle selection actions.
		// Many views can be added to the same list model.
		// DOES NOT SELECT THE FIRST ITEM BECAUSE YOU MAY NEED TO SELECT
		// SOME OTHER ITEM INITIALLY. YOU *MUST* MAKE THE INITIAL SELECTION
		// OUTSIDE THIS METHOD!
		var adapter, oldOnClose;
		adapter = { | who, what, index, data |
			switch (what,
				// list changed: update items and index
				\list, {
					listview.items = data; listview.value = index ? 0; }
			)
		};
		this.addDependant(adapter);
		this.onSelect(listview, {| a, b, i |
			listview.value = i?0;
		});
		oldOnClose = listview.onClose;
		if (oldOnClose.notNil) {
			listview.onClose = { oldOnClose.value; this.removeDependant(adapter); };
		}{
			listview.onClose = { this.removeDependant(adapter); };
		};
		listview.items = this.names;
		listview.action = { | me | this.selectAt(me.value.asInteger) };
		^adapter; // can be used to remove adapter to support multiple views or models
	}
	selectAt { | index |
		// select the item at index in the list and update views.
		// The index is usually given as a result of a user selection on a view
		// If the list is empty, then the selection becomes nil.
		var itemName;
		index = index.asInteger;
		#itemName, selection = if (namesItems.size > 0) {namesItems[index]} {#["", nil]};
		this.selectionChanged(index, itemName, selection);
		^selection;
	}
	selectLast {
		// select the last item in the list
		if (list.size == 0) { ^nil };
		this.selectAt(list.size - 1);
	}
	selectItem { | item |
		// mark item as current selection and update views.
		// Use selectItem as name for this method, not select, because: 
		// select is used for selecting in collections and patterns. 
		var index;
		selection = item;
		index = this.indexOf(item) ? 0;
		this.selectionChanged(index, (namesItems[index] ? [])[0], item);
	}
	selectionChanged { | index, itemName, item |
		// do all actions that are required when the selection changes
		selectActions do: { | foo | foo.(item, itemName, index); };
//		this.changed(\selection, index, itemName, item);
	}
	removeItem { | item |
		// remove item and select the next item in the list.
		// Must go over removeAt to get that index
		var index;
		index = list.indexOf(item);
		if (index.notNil) { this.removeAt(index); };
	}
	removeAt { | index |
		// remove at index sent by list view. Select the new
		// item at the index of a list view after removal.
		index = index.asInteger;
		this.remove(this.atIndex(index));
		this.list = this.list; 	// update list contents of dependants
		this.selectAt(if (index >= list.size) { 0 } { index });
	}
	// remove item from the list by performing the remove action
	remove { | item |
		// does not select new item in place of removed selection
		// see removeAt
		this.list = remove.(this, item);
	}
	atIndex { | index |
		^(namesItems[index.asInteger] ? [])[1];
	}
	itemNamed { | name |
		^namesItems.detect({|i| i.first == name}).asArray.wrapAt(1);
	}
	size { ^list.size }
}


NamedListModel : ListModel {
	var <>name;	// provide name variable. 
	*new { | name, list, add, remove, makeNames, fromData, toData |
		^super.new(list, add, remove, makeNames, fromData, toData)
			.name_(name);
	}
	asString { ^this.class.name.asString ++ "(" ++ name.asString ++ ")" }
}

ListWithGui : NamedListModel {
// { provide customizeable makeGui function, window and bounds variables.
	// Thus recreate a window gui for this list at the place last closed. 
	var >makeGui;
	var <window;
	var <>bounds;	// bounds of window before closing: reopen at last position
// }
	*new { | name, makeGui, list, add, remove, makeNames, fromData, toData |
		^super.new(name, list, add, remove, makeNames, fromData, toData)
			.makeGui_(makeGui);
	}
	name_ { | argName |
		name = argName;
		this.changed(\name, name);
	}
	makeGui {
		if (window.notNil) { ^window.front };
		window = makeGui.(this).onClose_({
			bounds = window.bounds;
			this.changed(\windowClosed);
			window = nil;
		}).front;
		this.listChanged;
//		this.selectItem(selection);
		this.selectAt(this.indexOf(selection) ? 0);
		^window;
	}
	closeGui {
		if (window.notNil) { window.close }
	}
	toggleGui {
		if (window.isNil) { this.makeGui } { window.close }
	}
}
