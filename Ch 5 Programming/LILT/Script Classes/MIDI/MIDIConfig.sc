/* (IZ 2005-08-23) { (SC3)
	
	TO BE REPLACED!
	
2007-01-26: MidiConfig is to be replaced by another scheme that records directly the bindings of each Script of a Session from the Script's gui, and then saves the bindings of all Scripts together with the Session as script. An option for saving alternative MIDI-bindings of a Session separately from the session should also be provided. 

Utility for saving the MIDI command types sent by all controls on a given device and assigning these to controls of scripts.

MIDIConfig.new(<name>, row1, row2 ... rown);
Each row is a number stating the number of items in the row. 
The rows items are named automatically as follows: 

<row number><item number> where: 
<row number> is numbered alphabetically A-Z, a-z (max 52 rows possible).
<item number> is numbered numerically
Thus 'B5' is the 5th item on the 2nd row. 

The items are represented by buttons named as follows:
<item name> <midi assignment>
Where <midi assignment> is named as follows: 
<channel><midi type><number>
Midi types are:
NN : note on
NO : note off
NX : note on-off
CC : Control change
PB : Pitch bend
AT : After touch

w = SCWindow("MIDI Device Configuration", Rect(0, 200, 12 * 74 + 8, 200).fromTop);
w.view.decorator = FlowLayout(w.view.bounds);
{ SCButton(w, Rect(0,0,70, 20)).states_([["B99 16CC127"]]).font_(Font("Helvetica", 10)); } ! 12;
w.front;
} */

MIDIConfig : Model {
// {
	var <>name;	// name for display on GUI window
	var <>path;	// path for saving data
	var <rows;	// row specs: [name, size]
	var <items;	// nested array of MIDIConfigItem
	var <window, learnButton, noteOnOffButton;
	var <>autoProgress = true;	// whether to select next item when MIDIListener notifies "next"
	var <rowDict; // store rows by name for easy access. See method 'at'
// }
	*new { | name ... specs |
		^super.newCopyArgs(nil, name ? "MIDI Device Configuration").init(specs);
	}
	init { | specs |
		if (specs.size == 0) { specs = [8] };
		rows = specs.separate { |a, b|
			not(a.isKindOf(Number).not and: { b.isKindOf(Number) })
		}.collect { |spec, i |
			if (spec[0].isKindOf(Number)) {
				["(" ++ (1+i).asString ++ ")", spec[0]]
			}{
				if (spec[1].isKindOf(Number)) {
					spec
				}{
					[spec[0].asString, 8]
				}
			}
		};
		items = rows.flop[1].collect { | rowSize, rowNum |
			{ | itemNum | MIDIConfigItem(rowNum, itemNum + 1, this) } ! rowSize
		};
		this.connectItems;
	}
	connectItems {
		var flat;
		flat = items.flat;
		flat.do { | item, pos |
			item.nextItem = flat@@(pos + 1);
			item.previousItem = flat@@(pos - 1);
			item.midiConfig = this;
		};
		// also create row dictionary for access by name. See method 'at'.
		rowDict = ();
		rows.do { |r, i|
			rowDict[r[0].asSymbol] = items[i];
		};
	}
	*withAll { | items |
		^this.newCopyArgs(items).connectItems;
	}
	makeGui {
		var itemButton, item, menu;
		if (window.notNil) { ^window.front };
		window = SCWindow(name,
			Rect(0, 0, 74 * (items collect: _.size).sort.top + 108,
				1 + items.size * 24 + 8).fromTop);
		window.view.decorator = FlowLayout(window.view.bounds);
		learnButton = SCButton(window, Rect(0, 0, 129, 20))
			.states_([["Start controller capture", Color.blue],
				["Stop controller capture", Color.red]])
			.action_ {|me| if (me.value > 0) { this.startLearn } { this.stopLearn }};
		SCStaticText(window, Rect(0, 0, 85, 20)).string = "Auto-Progress:";
		SCButton(window, Rect(0, 0, 30, 20)).states_([["yes"],["no"]])
			.action_ { | me |
				autoProgress = if (me.value > 0) { false } { true };
			};
		SCStaticText(window, Rect(0, 0, 100, 20)).string = "Note-On/Off pairs:";
		noteOnOffButton = SCButton(window, Rect(0, 0, 30, 20)).states_([["no"],["yes"]])
			.action_ { | me |
				MIDIListener.noteOnOffPairs = if (me.value > 0) { true } { false };
			};
		SCButton(window, Rect(0, 0, 40, 20)).states_([["Save"]])
			.action_ { this.save };
		SCButton(window, Rect(0, 0, 55, 20)).states_([["Save as"]])
			.action_ { this.saveDialog };
		SCButton(window, Rect(0, 0, 40, 20)).states_([["Load..."]])
			.action_ { |me| me.focus(false); this.loadDialog({|new| new.makeGui; }); };
		SCButton(window, Rect(0, 0, 55, 20)).states_([["Revert"]])
			.action_ { this.class.load(path, {|new| new.makeGui; window.close }); };
		SCButton(window, Rect(0, 0, 40, 20)).states_([["Name"]])
			.action_ { this.nameDialog; };
		SCButton(window, Rect(0, 0, 40, 20)).states_([["Clear"]])
			.action_ { this.clear };
		window.view.decorator.nextLine;
		items do: { | item, count |
			SCStaticText(window, Rect(0, 0, 97, 20)).string = rows[count][0];
			item do: { | item | item.makeView(window) };
			if (count + 1 < items.size ) { window.view.decorator.nextLine };
		};
		window.front;
		learnButton.focus(true);
		window.onClose = {
			this.stopLearn;
			MIDIListener.removeDependant(this);
			window = nil;
			learnButton = nil;
			noteOnOffButton = nil;
			MIDIListener.addReceiver(nil);
			items.flat.do {|i| i.view = nil};
			MIDIConfigItem.midiConfigClosed(this);
		};
		MIDIListener.addDependant(this);
	}
	toggleLearnMode {
		if (this.isLearning) { this.stopLearn } { this.startLearn }
	}
	isLearning {
		^learnButton.notNil and: { learnButton.value > 0 }
	}
	update { | sender, args |
		var status;
		#status, args = args.asArray;
		switch (status,
			\isLearning, {
				if (learnButton.notNil) { learnButton.value = args.binaryValue; };
				MIDIConfigItem.setListeningStatus;
			},
			\noteOnOffPairs, {
				this.noteOnOffPairs = args;
			},
			\next, {
				if (autoProgress) { MIDIConfigItem.selectNextItem }
			}
		);
	}
	startLearn {
		MIDIConfigItem.startLearn(items.first.first);
		window.front;
	}
	stopLearn { MIDIListener.closeWindow; }
	noteOnOffPairs_ { | flag |
		if (noteOnOffButton.notNil) { noteOnOffButton.value = flag.binaryValue };
	}
	midiResponderKeys { ^items.flat collect: _.name }
	nameDialog {
		TextDialog("Enter a name for this Configuration", "MIDI Config", { | ok, input |
			if (ok) { name = input;
				if (window.notNil) { window.name = name }
			};
		});
	}
	clear { items.flat do: _.clear }
	//////// SAVING AND LOADING
	saveDialog {
		CocoaDialog.savePanel({ | argPath |
			path = argPath;
			this.save;
		}, {
			"save cancelled".postln;
		});
	}
	save {
		if (path.isNil) { ^this.saveDialog };
//		this.writeArchive(path);
		this.getSaveData.writeArchive(path);
	}
	getSaveData {
		^[name, rows, items.collect { | itemRow |
			itemRow.collect { |item | item.getSaveData; }
		}];
	}
	loadDialog { | okFunc |
		CocoaDialog.getPaths({ | argPath |
			this.stopLearn;
			this.class.load(argPath.first, okFunc, this)
		}, {
			"load cancelled".postln;
		})
	}
	*load { | path, okFunc, previous |
		var data, new;
		if (path.isNil) { ^Warn("MIDIConfig cannot load: Empty path")};
		data = Object.readArchive(path);
		// TODO: Simplify this. Probably replace save/load using a Script to create a MIDIConfig
		okFunc.(new = this.fromSaveData(data, path));
		previous.changed(\midiConfig, new);
		^new;
	}
	*fromSaveData { | data, path |
		^this.newCopyArgs(nil, data[0], path, data[1],
			data[2].collect { |row|
				row.collect { |item|
					MIDIConfigItem.fromSaveData(*item) };
				}).connectItems
	}
	close { if (window.notNil) { window.close } }
	at { | row, column |
		// get an item by row and column index - for binding parameters of Scripts
		// with humanly readable code
		row = row ? 0;
		column = column ? 0;
		if (row.isKindOf(Integer)) {
			^items[row][column];
		} {
			^rowDict[row.asSymbol][column];
		}
	}
}

MIDIConfigItem {
// {
	classvar <selectedItem; 	// currently selected item for re-assignment or binding to script controls
	classvar <rowNames; 		// for naming row items after alphabetic characters
	classvar <midiEventCodes; 	// for adding suffix to row item by its responder name.
	classvar <>font;			// uniform Helvetica 10 point font for views
	var <>name;				// basic name string (without midi description)
	var <>responder;			// responder
	var <>funcSourceCode;		// source code for the action of the responder
								// see Parameter about how this is used in Script Parameters
	var <>key;					// key where this responder may be stored (not applicable here?)
	var <>view;				// drag source view displaying MIDIConfigItem 
	var <>nextItem;			// for automatically moving to next item when midi assigned
	var <>previousItem;		// for moving to previous item for corrections;
	var <>midiConfig;			// MIDIConfig instance, for getting status: learning/not learning
//	}
	*initClass {
		font = Font("Helvetica", 10);
		// for some reason (some required class?) this does not work here, 
		// so it is evaluated lazily in *new
//		rowNames = _.asAscii ! 128 select: _.isAlpha collect: _.asString;
		midiEventCodes = (
			NoteOnResponder: "NN", NoteOffResponder: "NF", CCResponder: "CC",
			NoteOnOffResponder: "NX", TouchResponder: "AT", BendResponder: "BD"
		);
	}
	*startLearn { | item |
		if (selectedItem.isNil) { selectedItem = item };
		MIDIListener.makeGui;
		selectedItem.select;
		MIDIListener.addReceiver(selectedItem);
	}
	*new { | rowNum, itemNum |
		// lazy initialization of rowNames because not reliable in initClass:
		rowNames = rowNames ?? { _.asAscii ! 128 select: _.isAlpha collect: _.asString; };
		^super.new.name_((rowNames[rowNum] ++ itemNum.asString).asSymbol);
	}
	makeName { | argRowNum, argItemNum |
		^(rowNames[argRowNum] ++ argItemNum.asString).asSymbol;
	}
	makeView { | window |
		view = SCDragSource(window, Rect(0, 0, 70, 20))
			.font_(font)
			.action_ { this.select; }
			.object_(responder)
			.string_(this.makeViewName)
			.align_(\center)
			.canFocus_(true)
			.keyDownAction_ { | me, char, mod, uni, key |
//				[this, char].postln;
				switch(char,
					127.asAscii, { this.clear },
					// not yet done: 
					13.asAscii, { this.sendResponder }, // send responder to receiver
//					$\t, { this.selectNextItem },		// tab (not available to keyDownAction!)
// NOTE: tab is not caught by keyDownAction - instead the focus always moves
// to the next item! 
					0.asAscii, { this.selectNextItem },	// cursor keys: 
					1.asAscii, { this.selectPreviousItem },
					2.asAscii, { this.selectPreviousItem },
					3.asAscii, { this.selectNextItem }
//					,
//					$ , { midiConfig.toggleLearnMode; } // this crashes!
//					{ me.defaultKeyDownAction(char, mod, uni, key)}
				)
			};
	}
	makeViewName {
		var chan = "", num = "";

		^name ++ if (responder.isNil) {
			" --- "
		}{
			" " ++
			responder.matchEvent.chan.asString ++
			midiEventCodes[responder.class.name] ++
			(responder.matchEvent.b ? "").asString;
		}
	}
	addMIDIResponder { | argResponder, actionString, argKey |
		responder = argResponder;
		responder.function = actionString.compile;
		key = argKey;
		if (view.notNil) {
			view.object = responder;
			view.string = this.makeViewName;
/*			{
				view.states[0][0] = this.makeviewName;
				view.states = view.states; // both lines obligatory
				view.refresh;					// for change of color to take effect
			}.defer;
*/		};
	}
	select {
		// avoid superfluous double selection:
		if (selectedItem === this) { ^this };
		if (selectedItem.notNil) {selectedItem.deselect};
		selectedItem = this;
		if (midiConfig.isLearning) { MIDIListener.addReceiver(this) };
		this.setListeningStatus;
	}
	*midiConfigClosed { | argMidiConfig |
		if (selectedItem.notNil and: { selectedItem.midiConfig == argMidiConfig }) {
			selectedItem.deselect;
		}
	}
	setListeningStatus {
		if (view.notNil) {
			view.background = if (midiConfig.isLearning) { Color(1,0.3,0.3) } { Color(0.3,0.7,1) };
			view.focus(true);
/*			view.states[0][2] = if (midiConfig.isLearning) { Color(1,0.3,0.3) } { Color(0.3,0.7,1) };
			view.focus(true);
			view.states = view.states; // both this and the next line are
			view.refresh;			// needed for color change to take effect
*/		};
	}
	*setListeningStatus {
		if (selectedItem.notNil) { selectedItem.setListeningStatus };
	}
	deselect {
		if (view.notNil) {
			view.background = Color.clear;
/*			view.states[0][2] = Color.clear;
			view.states = view.states; // otherwise change does not take effect!
			view.refresh; 					 // this also needed to refresh view color!
*/		};
		selectedItem = nil;
	}
	*selectNextItem {
		if (selectedItem.notNil) { selectedItem.selectNextItem }
	}
	selectNextItem {
		if (selectedItem.notNil) { selectedItem.nextItem.select }
	}
	*selectPreviousItem {
		if (selectedItem.notNil) { selectedItem.selectPreviousItem }
	}
	selectPreviousItem {
		if (selectedItem.notNil) { selectedItem.previousItem.select }
	}
	asCompileString {
		^"MIDIConfigItem(" ++ name.asString ++ ")";
	}
	midiResponder { ^responder }
	getSaveData {
		^[name, if (responder.isNil) {nil} {responder.getSaveData}, key];
	}
	*fromSaveData { | argName, respData, argKey |
		^this.newCopyArgs(argName,
			MIDIResponder.fromSaveData(respData),
			argKey
		);
	}
	clear { // clear responder and update gui
		responder = nil;
		if (view.notNil) {
			view.string = this.makeViewName;
/*			view.states[0][0] = this.makeviewName;
			view.states_(view.states).refresh; // both needed for refresh
*/		}
	}
	sendResponder { "send responder not yet implemented".postln }
}
