/* IZ 050705ff {
UNDER DEVELOPMENT

A script object holds a text script that creates a process that can be started and stopped.
It creates a button as a gui for starting and stopping the script plus sliders for controlling parameters.
See more doc in "Scripts/About Scripts/the Script class"

// TODO: Create instance of script from its code-text, do not reload different code into same script instance. (Use ScriptPrototype for this, see existing draft.)

// TODO: ??? rewrite color scheme to use one global dictionary for all classes and instances so as to avoid generating so many color instances for each gui of each script.??? 

}*/

Script : Model {
	// {
	classvar <>globalParentEnvir;
	classvar <>lastLoadedScripts;	// list of all scripts from last opened folder
	classvar <globalScripts;		/* dict (event) of scripts entered in globalParentEnvir 
			by addGlobalScript(globalname, script). Used to display these scripts in: */
	classvar <globalScriptsWindow; // display list of globally known scripts
	classvar enterKey, escapeKey;	// needed to these key as character in keyDownActions
	const <snapshotViewWidth = 150;

	var <name;		// this name will be displayed on the gui button
	var <>path;		// the path from which the script was loaded
	var <>script;		// the script as text (can be edited and resaved)
	var <>envir;		// the environment within which the script will be evaluated
	var <>session;	// session I belong to: For getting a group in order-of-execution
	var <>scriptGroup;	// scriptGroup I belong to: for grouping ...
	var <>groupIndex = 0;	// index of Group (in Session) where nodes should be created

	// following are set by the script:
	var <>startFunc;	// Function put by script in envir.start.  Upon this.start, it
					// creates the playing process. 
	var <>stopFunc; // put by script in envir.stop - defaults to 
					// { process.perform(*(envir.stopMessage ?? { process.stopMessage })) }
	var <>process;	// The process that is playing while this script is running.
					// Created from startFunc. Can be: Synth, Routine, EventStream.
	var <krInputBusses;		/* array of busses which my process reads signals from by map.
		Each association is: ctlname->linkedbus. See method start */
	var <>output;	// OutputParameter that I output to - if present 
	var <parameters;	// array of Parameters (for GUI and control by MIDI or other means)
	var <parameterEnvir; // 070121: always set a parameter by sending it .set. for that 
	 						// you need to access it. parameterEnvir is for fast access
	// -------------------- GUI stuff
	var <>guiFunc;		// if not nil, this replaces the default makeGui method's way
	// TODO: 070121 gui, linksWindow and guiBounds to be removed - window management
	// will be delegated to WindowHandler class
	var <gui;			// control gui window. Only one is created
	var linksWindow;	// window displaying links of inputs and outputs
	var scriptConnectionsWindow; // window displaying targets / sources for this script
	var >guiBounds; // rect of last closed guiWindow: for reopening on same place

	/* ---------------------------------------------------------------------------------
	   following describe the type of Script based on the presence of keys in controlSpecs
	   and are used for coloring its main button and deciding can-receive-drag */
	var <>hasBufferInput = false;
	var <>hasAudioInput = false;
	var <>hasAudioOutput = false;
	var <>hasControlOutput = false;
	// original [folder, scriptName] for reference in saving / reloading in Session:
	var <folder, <originalName;
	var <>snapshots;	// snapshots of full script states that can be saved on disc.
	const none = 256, alt = 524576, shiftalt = 655650, ctrl = 262401, altctl = 786721;
	// }
	*initClass {
		Class.initClassTree(Event);
		// make the usual patterns playable with default parent event
		globalParentEnvir = Event.parentEvents[\default].copy;
		globalScripts = ();
		enterKey = 3.asAscii;
		escapeKey = 27.asAscii;
	}
	*loadFromBrowser { | folder, scriptName, ownName, guiLeft, guiTop, makeGui = true |
		// load from folder - scriptname pair as found in ScriptBrowser,
		// but use ownName if given. Used to load Sessions from scripts
		var path;
		path = ScriptBrowser.getScriptPath(folder.asSymbol, scriptName.asSymbol);
		if (path.isNil) {
			^Post << "Script not found: " << folder << " : " << scriptName << "\n"
		}{
			^Script.load(path, ownName ? scriptName).initBounds(guiLeft, guiTop, makeGui);
		}
	}
	*load { | path, name, session, scriptGroup |
		^this.new(name ?? { path.basename.splitext.first }, path,
			File.use(path, "r", { | f | f.readAllStringRTF }),
			session: session ? Session.current,
			scriptGroup: scriptGroup ? Session.currentScriptGroup
		);
	}
	*new { | name, path, script, envir, session, scriptGroup, groupIndex = 0 |
		^super.newCopyArgs(nil, name ? 'empty script', path, script, envir,
			session, scriptGroup, groupIndex).init;
	}
	init {
		// store folder and filename - needed to write/load scripts in Sessions
		// Using ScriptBrowser methods to ensure compatibility if code changes.
		folder = ScriptBrowser.getFolderFromPath(path);
		originalName = ScriptBrowser.getNameFromPath(path);
		envir = envir ?? {(
		// note: because a MIDIResponderArray cannot contain the handle itself in its  
		// objects' array, I am using a "unique ID" instead of the script
		// itself inside the objects array of the MIDIResponderArray
			start: #{ Routine({ 5.do { "empty script".postln; 1.wait }}).play };
		)};
		envir.script = this;
		session = session ?? { Session.current };
		scriptGroup = scriptGroup ?? { session.currentScriptGroup };
		envir.target = session.getGroup(groupIndex);
		krInputBusses = ();
		this.initScript;
	}
	initScript {
		var key, action, spec;
		var stopMessage;
		if (this.isRunning) { this.stop; };
//		this.close;		// (close and) update GUtIs 
		// compile the actual script text and evaluate the resulting function within the 
		// environment.	 This sets up the script instance for starting and stopping:
		parameterEnvir = (script: `this); // make available to script for modifications
		// script's parameter must be Ref for MIDIResponderArray activate/deactivate to work
		// make globalParentEnvir available to script for stuff that may depend on it:
		envir.proto /* parent */ = globalParentEnvir; //
		if (script.notNil) { envir.use(script.compile); };
		startFunc = envir[\start];
		if (startFunc.isNil) {
			warn("Script " ++ name.asString ++ " does not set the ~start field");
		};
		stopFunc = envir[\stop] ?? {
			stopMessage = envir.stopMessage ? \stop;
			{ process.perform(*stopMessage); };
		};
		/* - set input-output type properties for linking and coloring 
		   - initialize parameter values, otherwise synths wont work properly */
		// first re-init in case any of these have been set by previous script contents: 
		hasBufferInput = hasAudioInput = hasAudioOutput = hasControlOutput = false;
		// initialize control spec variables:
		parameters = envir.controlSpecs.collect { | spec |
			Parameter.forScript(this, spec);
		};
		parameters do: { | p | parameterEnvir[p.name] = p };
		session.addScript(this, scriptGroup);
		if (session.script.isNil) { // if not a Session-making script ...
		}{ // then inherit your Session's Script's environment. 
			envir.parent = session.script.envir;
		};
		snapshots = ListModel(
			makeNames: { | l | l.list },
			remove: { | l, item |
				l.list remove: l.list.detect { | i | i.array[1] === item };
				l.list;
			}
		);
		this.changed(\initDone); // so one can do something when loading is complete
//		snapshots.postln;
	}
	doWhenInited { | action |
		if (this.isInited) { ^action.(this) };
		this  addDependant: { |  who, what  |
			if (what == \initDone) {
				action.(this);
				this removeDependant: thisFunction;
			}
		};
	}
	isInited { // kludge: check whether snapshots have been inited.
		^snapshots.isNil.not;
	}
	initBounds { | argLeft, argTop, makeGui = false |
		if (argLeft.notNil) {
			guiBounds = this.guiBounds.moveTo(argLeft, argTop);
		};
		if (makeGui) { this.makeGui }
	}
	include { | folder, scriptName |
		// a script may include another script - meaning evaluate it within envir
		// at the point of the include. 
		// useful for standardized pieces of code used for automatically 
		// generated scripts such as Session loading scripts. 
		var path, string;
		path = ScriptBrowser.getScriptPath(folder.asSymbol, scriptName.asSymbol);
		if (path.isNil) {
			^Post << "Script not found: " << folder << " : " << scriptName << "\n|"
		}{
			string = File.use(path, "r", { |f| f.readAllStringRTF });
			envir.use(string.compile);
		}
	}
	fromSynthDesc { | synthDesc |
		// { clear this script and replace with script made from synthdesc 
		// Uses script already made in class SynthDescs instead of remaking the script
		// FIX ME: fromSynthDesc should be class method? }
		envir = nil;
		script = SynthDefs.currentScript;
		path = "./" ++ SynthDefs.selectedDef.name.asString;
		this.name_(SynthDefs.selectedDef.name);
		this.init;
	}
	test {	// a gui for safely testing a script - stops when the gui closes
		// create a gui + make yourself stop when the gui closes
		this.makeGui;
		gui.addDependant { | who, state |
			if (state == \closed) {
				this.stop;
			}
		};
//		if (session.server.serverRunning.not) { session.server.boot };
	}
	*loadRaw { | folder, file, envir |
		// load a file as raw code, as found from folder/file names in ScriptBrowser
		// interpret the file as is (do not create a script instance)
		// provide access to an environment. Default: currentParentEnvir
		^this.loadRawEnvir(ScriptBrowser.getScriptPath(folder, file), envir);
	}
	*loadRawEnvir { | path, envir |
		// load a file as is, without script instance
		// provide environment to the code as context. Default: globalParentEnvir
		var currentEnvirBackup, result;
		currentEnvirBackup = currentEnvironment;	// backup current environment
		envir = envir ? globalParentEnvir;		// use custom environment
		currentEnvironment = envir;				// (default is globalParentEnvir
		result = path.load;					// load within environment
		currentEnvironment = currentEnvirBackup;	// restore original current environment
		^result;		// also return result of evaluation for further use
	}
	openDialog {
		// load one script from path selected by user into his instance 
		CocoaDialog.getPaths({ | paths |
			this.load(paths.first)
		}, {
			"load cancelled".postln;
		})
	}
	*openFolderDialog {
		// load all scripts in folder selected by user and make new gui
		CocoaDialog.getPaths({ | paths |
			paths = paths.first.dirname;
			this.load(paths ++ "/*");
			this.makeGui(name: paths.basename);
		}, {
			"load cancelled".postln;
		})
	}
	name_ { | argName |
		name = argName.asSymbol;
		this.changed(\name, name);
	}
	renameDialog {
		TextDialog("Edit name for this Script:", name.asString, { | ok, string |
			if (ok) {
				this.name = string.asSymbol;
				scriptGroup.list = scriptGroup.list;
			}
		})
	}
	start { | ... args | // args may be passed e.g. by MIDIResponder functions 
		// do not restart if started by a script whose output you receive
		var server = session.server;
		if (this.isRunning) { ^this };
		CmdPeriod.add(this);
		process = envir.use({startFunc.(*args)});
		envir.process = process;
		// always notify readers, so that BusLinks may start / stop accordingly
		this.writerStarted;
		if (process.respondsTo(\onEnd)) {
			process.onEnd({
				this.stopped;
				process.removeServer(server);
			}, server);
			// if understands onEnd also understands onStart!
			// notify when node *really* started so that aNode.map(...) always works:
			process.onStart({
				krInputBusses keysValuesDo: { | param, index |
//					thisMethod.report(this, param, param.krInputRunning);
					if (param.krInputRunning) { process.map(param.name, index) }
				};
				this.changed(\started);
			}, server);
		}{	// if process not a node, notify started immediately.
			this.changed(\started);
		};
		^process;
	}
	onStart { | action |
	// do something when you start
		this.addDependantAction(action, \started);
	}
	onStop { | action |
	// do something when you stop
	// useful for stopping multiple nodes when a routine or stream type Script stops
		this.addDependantAction(action, \stopped);
	}
	writerStarted {
		if (this.isRunning) { this.notifyReadersOfStart };
	}
	writerStopped {
		// do nothing. Only notify when YOU stop
	}
	notifyReadersOfStart {
		if (output.notNil) { output.writerStarted };
	}
	stop { | ... args | // args may be passed e.g. by MIDIResponder functions
		if (this.isRunning) {
			envir.use({stopFunc.(*args)});
			if (process.isKindOf(Node)) {
				this.changed(\released)
			}{
				this.stopped;
			};
		}
	}
	kill { process.free }
	resetParams {

/*		var key, action;
		envir[\controlSpecs].do { |s|
			key = s[0];
			if ([\in, \out, \bufnum].includes(s[0]).not) {
				// set each parameter except for in, out, bufnum to default
				if ((action = s[6]).isNil) {
				// use default action if no action is provided
					this.set(key, s[5] ?? {s[1]})
				}{
				// use custom action for setting if provided
					envir.use { action.(s[5]) };
				}
			}
		}
*/	}
	stopped {
		process = nil;
		envir[\process] = nil;
		if (output.notNil) { output.writerStopped;
//			[this, "informed: ", output, "that it stopped"].postln;
		};
		// defer so that when stopped from synth notification or other
		// osc message, gui items will still update OK
		{ this.changed(\stopped); }.defer
	}
	toggle {
		if (this.isRunning) { this.stop } { this.start }
	}
	isRunning { ^process.notNil }
	cmdPeriod {
		// only stop with things that CmdPeriod stops:
		if (process.isKindOf(Node) or: { process.isKindOf(AbstractFunction) }) {
			CmdPeriod.remove(this);
			envir.removeAt(\stream); // Required for restart in method stream. 
			this.stopped;
		}
	}
	/*	CONSTRUCTING START FUNCTIONS FOR SYNTHS, FUNCTIONS, ENVIRONMENTS, PATTERNS	*/
	synth { | defname |
		// make start func for creating synth. Untested!
		defname = defname ?? { originalName };
		envir[\start] = {
			Synth(defname, parameters.collect({ | p |
				[p.name, envir[p.name.asSymbol]]}).flat,
				envir[\target], \h	// \h == \addToHead
			)
		}
	}
	routine { | function |
		// Make start func for playing routine made from a function
		envir[\start] = {
			Routine({
				envir.use(function);
				this.stopped;
			}).play(envir[\clock] ? SystemClock)
		}
	}
	stream { | function |
		// make start for playing stream created by function
		// store the stream returned by func in ~stream.
		// when starting, resume ~stream as long as it has not ended
		// (this means resuming stream from where it stopped instead of beginning)
		envir[\start] = {
			if (~stream.isNil) {
				~stream = function.value;
			}{
				if (~stream.wasStopped.not) { ~stream.reset };
				~stream.resume;
			};
		};
		envir[\stopMessage] = \pause;
	}
	playEnvir {
		// The envir Event: 
		envir[\start] = { Pbindf(envir).play }
	}
	asPbind { | ... keysValues |
		// Pbindf with the envir Event
		envir[\start] = { Pbindf(envir, *keysValues).play }
	}
	asSession { // Construct stop message to close session stored in process when 
		// script is stopped. More code may be added to the present method?
		envir[\stopMessage] = \remove;
	}
	/* { LISTENING TO BROADCAST MESSAGES FROM OTHER OBJECTS 
	Create a start function which adds me as dependent to object stored in ~sender, 
	reacting to messages whose second argument is equal to <message> } */
	listenTo { | action, sendersKey = \sender, messageKey = \vertex |
		~script.routine { // can this be just: this.routine ... ? 
			var adapter;
			envir.use(envir[\startScore]);
			adapter = { | sender, message ... args |
				if (message == messageKey) {
					envir.use { action.(*args) };
				};
			};
			envir[sendersKey].addDependant(adapter);
			this.yield;
			envir[sendersKey].removeDependant(adapter);
			envir.use(envir[\stopScore]);
		};
		~stopMessage = \next;
	}
	// simpler version, used as utility for other macros like onStart, onClose etc.
	addDependantAction { | action, message |
		// add action as dependent to be performed when update message matches message
		this addDependant: { |  who, what  |
			if (what == message) { action.(who, what) }
		};
	}
	makeGui {
		var label, slider, numbox, controlspec, views, dragsink, adapterEnvir, adapter;
		var mainPanel, snapshotPanel, snapshotNameEditView, snapshotListView, decorator;
		if (guiFunc.notNil) { ^guiFunc.(this, envir) };  // if present, use guiFunc instead
		if (gui.notNil) { ^gui.front };
		gui = SCWindow(name.asString, this.guiBounds);
		mainPanel = SCCompositeView(gui,
			Rect(0, 0, gui.view.bounds.width, gui.view.bounds.height));
		mainPanel.decorator = decorator = FlowLayout(gui.view.bounds, 3@3, 1@1);
		this.makeMainButton(mainPanel, gui);
		adapterEnvir = (
			scriptClosed: { gui.close },
			waitingForMIDIbinding: { mainPanel.background = Color(1, 1, 0, 0.5) },
			midiActive: { mainPanel.background = Color(0.3, 0.4, 0.4, 0.3) },
			midiInactive: { mainPanel.background = Color.clear },
			name: { | name | gui.name = name.asString }
		);
		adapter = { | sender, state, fullVal |
			adapterEnvir[state].value(fullVal);
		};
		this.addDependant(adapter);
		if (output.notNil) { output.makeOutputDragView(mainPanel, adapterEnvir) };
		decorator.nextLine;
		gui.onClose = {
			guiBounds = gui.bounds;
			this.changed(\guiClosed);
			gui.changed(\closed);	// window notifies dependent views
			gui.release;
			gui = nil;
			this.removeDependant(adapter);
		};
		parameters do: _.makeGui(mainPanel, adapterEnvir);
		snapshotPanel = SCCompositeView(gui, Rect(gui.view.bounds.width, 0,
			snapshotViewWidth, gui.view.bounds.height));
		snapshotPanel.decorator = decorator = FlowLayout(snapshotPanel.bounds, 3@2, 2@2);
		snapshotNameEditView = SCTextField(snapshotPanel, Rect(0, 0,
			decorator.innerBounds.width, 20));
		snapshotListView = SCListView(snapshotPanel, Rect(0, 0,
			decorator.innerBounds.width,
			decorator.innerBounds.height - decorator.currentBounds.height));
		snapshots.onSelect({ | snapshot, name, index |
			snapshotNameEditView.string = if (name.size > 2) { name } {
				name ++ " (edit name here)"
			};
		}, {});
		snapshots.addListView(snapshotListView);
		gui.view.keyDownAction = { | view, char, mod, unicode, key |
			switch (char,
				$m, { if (mod == alt) {
						this.learnMIDI
					}{ this.toggleMIDI };
//					thisMethod.report(this, char, mod, alt);
				},
				$M, { if (mod == shiftalt) {
						this.saveMIDIDialog
					}{ this.loadMIDIfromFileDialog };
//					thisMethod.report(this, char, mod, alt);
				},
				$x, { this.removeMIDIArray },
				escapeKey, { MIDIHandler.stopLearning },
				$., { MIDIHandler.handler.skipNextLearningItem },
				$e, { this.edit },
				$g, { this.showControlGui },	// show control gui part
				$h, { this.hideControlGui },	// hide control gui part
				// return key: Load the currently selected snapshot
 				$\r, { this.loadSelectedSnapshot; },
				enterKey, { this.takeSnapshot },
				$ , { this.toggle; },	// space key: stop if running / start if stopped
				$k, { this.kill; }, 	// free synth immediately
				$R, { this.resetParams }, // reset param values to defaults 
				$l, { this.makeLinksGui },	 // pop up window listing existing links
				$t, { this.makeScriptConnectionsGui },		// show sources + targets - NOT YET DONE!
				$s, { this.showSnapshots(snapshotListView) },
				$S, { this.hideSnapshots(snapshotListView) },
				$p, { this.showSnippets },
				{ 	// thisMethod.report(char, mod, unicode, key);
					view.defaultKeyDownAction(char, mod, unicode, key) }
			);
		};
		snapshotListView.keyDownAction = { | view, char, mod, unicode, key |
			switch (unicode,
				127, {	// backspace: delete snapshot 
					if (snapshots.selection.notNil) {
						this.removeSnapshot(view.value);
						thisMethod.report(this, "removed snapshot");
					}
				},
				3, {	// enter: create snapshot 
	//				if (snapshots.selection.notNil) {
						this.takeSnapshot(snapshots.selection);
						thisMethod.report(this, "took a snapshot");
	//				}
				},
				13, { 	// return: load snapshot
					if (snapshots.selection.notNil) {
						this.loadSnapshot(snapshots.selection);
						thisMethod.report(this, "activated snapshot");
					}
				},
				32, {   // space: toggle script on/off
					this.toggle;
				},
				27, {   // escape: close snapshots view
					this.hideSnapshots(snapshotListView);
				},
				115, {   // s: also close snapshots view
					this.hideSnapshots(snapshotListView);
				},
				83, {   // S: also close snapshots view
					this.hideSnapshots(snapshotListView);
				},
				101, {   // e: select name edit view
					snapshotNameEditView.focus(true);
				},
				99, {	// c: make / show snapshot code
					this.makeFuncCodeForSnapshot(snapshots.selection);
				},
				{ view.defaultKeyDownAction(char, mod, unicode, key) }
			)
		};
		snapshotNameEditView.keyDownAction = { | view, char, mod, unicode, key |
			switch (unicode,
				3, {	// enter: change selections name and return to list view
						view.defaultKeyDownAction(char, mod, unicode, key);
						snapshotListView.focus(true);
				},
				13, { 	// return: change selections name and return to list view
					view.defaultKeyDownAction(char, mod, unicode, key);
					snapshotListView.focus(true);
				},
				27, { 	// escape: undo editing and return to list view
					view.valueAction = if (snapshots.selection.isNil) { ""
					}{
						snapshots.names[snapshotListView.value].postln
					};
					snapshotListView.focus(true);
				},
				{ view.defaultKeyDownAction(char, mod, unicode, key) }
			)
		};
		snapshotNameEditView.action = { | view |
			var item;
			item = snapshots.list[snapshotListView.value];
			if (item.notNil) {
				item[0] = view.value;
				snapshots.list = snapshots.list;	// remake names and update
			};
		};
		gui.front;
		this.changed(\guiOpened, gui);
	}
	removeWhenGuiCloses {
		// remove yourself from your session if gui closes
		this.addDependant { | who, what |
			if (what == \guiClosed) {
				this.removeFromSession;
				this.close;	// close closes gui. No fear: No infinite loop.
			}
		}
	}
	removeFromSession { scriptGroup.removeItem(this); }
	guiBounds {
		^guiBounds ?? { this.defaultGuiBounds }
	}
	defaultGuiBounds {
		^Rect(450, 420,
			if (parameters.size == 0) { 200 } { 300 },
			1 + parameters.size * 21 + 6
		).fromTop
	}

	placeGui { |x = 0, y = 0 |
		// move the gui window so that left top corner is at x, y. 
		guiBounds = this.guiBounds.moveTo(x, y).fromTop;
		if (gui.notNil) { gui.bounds = guiBounds };
	}
	closeGui {
		if (gui.notNil) { gui.close };
	}
	toggleGui {
		if (gui.notNil) { gui.close } { this.makeGui }
	}
	makeMainButton { | argPanel, argWindow |
		var button, adapter;
		button = SCButton(argPanel, Rect(0, 0, 145, 20))
		.font_(Font("Helvetica", 11))
		.action_({|me| [{ this.stop }, {this.start}]
			[me.value].value(this);
		})
		.keyDownAction_({ | view, char, modifiers, unicode, keycode |
			switch ( char,
				$e, { this.edit },				// open script file for editing
				$g, { this.showControlGui },	// show control gui part
				$h, { this.hideControlGui },	// hide control gui part
				$o, { this.class.openDialog },	// open new script file
				$R, { this.load },				// reload edited script
// 				$m, { this.bind2MIDIFromUser }, // make controllable by next incoming MIDI
				$\r, { this.toggle; },	// return key: stop if running / start if stopped
				$ , { this.toggle; },	// space key: stop if running / start if stopped
				$k, { this.kill; },				// free synth immediately
				$r, { this.resetParams },
				{ view.defaultKeyDownAction(char, modifiers, unicode, keycode); }
			);
		});
		this.setButtonStates(button, argWindow);
		adapter = { | sender, state ... moreArgs |
			switch (state,
				\stopped, {
					this.setButtonStates(button, argWindow);
				},
				\started, {
					this.setButtonStates(button, argWindow);
				},
				\released, { this.setReleasedButtonStates(button, argWindow); },
				\test, { [sender, state, moreArgs].postln; },
				\wait4MIDI, { button.flashWhile(*moreArgs) },
				// change button name. not yet done. 
				\name, { this.setButtonStates(button, argWindow, this.isRunning.binaryValue); },
//				\inputLink, { this.checkInputLinkIndicationColor(dragview); },
//				\outputLink, { this.checkOutputLinkIndicationColor(dragview); },
				\scriptClosed, { this.setButtonStates(button, argWindow); }
			)
		};
		this.addDependant(adapter);
		button.value = this.isRunning.binaryValue;
	}
/*	makeMenu { | argWindow |
		var menu;
		menu = SCPopUpMenu(argWindow, Rect(0, 0, 14, 20))
			.items_(["---", "make gui", "edit", "reload", "open ...", "open folder ...",
				"save", "save as ...", "bind to MIDI ...", "save config"
			])
			.action_({ |me|
				[{},
				{ this.makeGui },
				{ this.edit },
				{ this.load },
				{ this.openDialog },
				{ this.class.openFolderDialog },
				{ this.save },
				{ this.saveAs },
				{ this.bind2MIDIFromUser(true) },	// react to just 1 key
//				{ this.bind2MIDIFromUser(false) },	// react to all keys on a channel
				{ this.saveConfig }
				][me.value].value;
				me.value = 0;
			});
	}
*/	setButtonStates { | button, window |
		var backcolor, oncolor, offcolor;
		if (window.isClosed) { ^nil };
		#backcolor, offcolor, oncolor  = case
			{ hasAudioInput } { [Color(0.85, 0.0, 0.25, 0.5), Color.white, Color(0.3, 0.9, 1.0)] }
			// Audio input has precedence over buffer input in coloring
			{ hasBufferInput } { [Color(1.0, 0.9, 0.5, 0.5), Color.black, Color(1, 0.2, 0.2)] }
			{ hasAudioOutput } { [Color(0.5, 0.75, 1.0, 0.5), Color.black, Color(1, 0.2, 0.2)] }
			{ hasControlOutput } { [Color(0.5, 1.0, 0.75, 0.5), Color.black, Color(1, 0.2, 0.2)] }
			// default: no signal i/o, no buffers
			{ true } { [Color(1.0, 1.0, 1.0), Color.black, Color(1, 0.2, 0.2)] };
		{	button.states_([
				[name, offcolor, backcolor],
				["[" ++ name.asString ++ "]", oncolor, backcolor]
			]).refresh;
			button.value = this.isRunning.binaryValue;
		}.defer
	}
	setReleasedButtonStates { | button, window |
		if (window.isClosed.not) {
			{
				button.states_([
					["*" ++ name.asString]++this.releasedColors,
					["*" ++ name.asString]++this.releasedColors
				]).refresh;
			}.defer;
		};
	}
	offColors { |color| ^[Color.black, color] }
	onColors { |color| ^[Color(1, 0.2, 0.2), color] }
	releasedColors { ^[Color.red, Color.grey(0.8, 0.5)] }
	midiResponderKeys {
		^parameters.keys.asArray.sort;
	}
	// ============== Linking 
	makeLinksGui {
		var readers, writers, readersView, writersView, dependant;
		if (linksWindow.notNil) { ^linksWindow.front };
		dependant = { | who, what |
			// when this script closes, always close list
			switch (what,
				\closed, { if (linksWindow.notNil) { linksWindow.close }},
				\readers, { readers.list_(this.getReaders).selectAt(0) },
				\writers, { writers.list_(this.getWriters).selectAt(0) }
			);
		};
		linksWindow = SCWindow("Links: " ++ name.asString,
			this.guiBounds.moveBy(120, -25).resize(300, 200)
		).onClose_({
			linksWindow = nil;
			this.removeDependant(dependant);
		});
		writersView = SCListView(linksWindow, Rect(5, 0, 143, 196));
		readersView = SCListView(linksWindow, Rect(152, 0, 143, 196));
		readers = ListModel(
			this.getReaders,
			makeNames: { | l | l.list collect:
				{ | p | [p.script.name ++ ":" ++ p.name, p] }
			}
		);
		writers = ListModel(
			this.getWriters,
			makeNames: { | l | l.list collect:
				{ | p | [p[1].name ++ ":" ++ p[0].script.name, p] }
			}
		);
		readers.addListView(readersView);
		readers.selectAt(0);
//		readers.onSelect(readersView, _.postln);
		readersView.keyDownAction = { | me, char, mod, uni, key |
			var sel = readers.selection;
			if (sel.notNil) {
				switch (char,
					// return: start script of reader parameter
					$\r, { sel.script.start;
						if (mod > 256) { this.start }
					},
					// escape: stop script of reader parameter
					27.asAscii, { sel.script.stop;
						if (mod > 256) { this.stop }
					},
					// s: show script of reader parameter
					$s, { sel.script.makeGui },
					// h hide script of reader parameter
					$h, { sel.script.closeGui },
					// backspace: remove reader
					127.asAscii, { this.removeReader(sel) },
					{ me.defaultKeyDownAction(char, mod, uni, key) }
				)
			}{
				"no reader parameter selected".postln;
			}
		};
		writers.addListView(writersView);
		writers.selectAt(0);
//		writers.onSelect(writersView, _.postln);
		writersView.keyDownAction = { | me, char, mod, uni, key |
			var sel = writers.selection;
			if (sel.notNil) {
				switch (char,
					// return: start script of writer parameter
					$\r, { sel[0].script.start;
						// on shift, also start the writer
						if (mod > 256) { this.start }
					},
					// escape: stop script of writer parameter
					27.asAscii, { sel[0].script.stop;
						// on shift, also stop the writer
						if (mod > 256) { this.stop }
					},
					// s: show script of writer parameter
					$s, { sel[0].script.makeGui },
					// h hide script of writer parameter
					$h, { sel[0].script.closeGui },
					// backspace: remove writer
					127.asAscii, { sel[0].removeReader(sel[1]) },
					{ me.defaultKeyDownAction(char, mod, uni, key) }
				)
			}{
				"no writer parameter selected".postln;
			}
		};
		linksWindow.front;
		writersView.focus(true);
		this.addDependant(dependant);
	}
	getReaders {
		if (output.isNil) { ^nil } { ^output.getReaderParameters };
	}
	getWriters {
		// for display and gui-handling in a scrips link-window views
		// return list of pairs: [writer (that is, from other script),
		// readerParameter (that is, in this script)] 
		^(parameters.collect({ | p | p.getWriterParameters +++ [p] }) ? []).flatten(1);
	}
	containsCycle { | writerScript |
		// Check if I am trying to write to myself directly or via my outputs
		^writerScript === this or:
		{ output.notNil } and:
		// output: outputParameter, its output: OutputBus, its readers: params or links
		{ output.containsCycle(writerScript) }
	}
	removeReader { | reader |
		if (output.notNil) { output.removeReader(reader) }
	}
// ============================== editing
// TODO: Redo *edit and edit to avoid duplication of code
	*edit { | path |
		// edit file of a script. Use Document window if already open on this path
		var doc;
		doc = Document.allDocuments.detect { | d | d.path == path };
		if (doc.isNil) {
			if (File.exists(path)) {
				doc = Document.open(path);
				doc.path = path;
			} {
				Post << "Script File not found: " << path;
				^nil;
			};
			if ([\rtf, \rtfd].includes(doc.path.basename.splitext.last.asSymbol).not) {
				doc.setFont(Font("Monaco", 9), -1).syntaxColorize;
			};
			doc.setFont(Font("Monaco", 9), -1).syntaxColorize;
		}{
			doc.front;
		};
		^doc;
	}
	edit {
		// edit the file of this script and reload when saved
		var found, tempPath;
		// use tempPath as placeholder in case path is nil to avoid wrong match
		// or error in searching file under nil path
		tempPath = path ?? { "./" ++ name.asString };
		found = Document.allDocuments.detect { | d | d.path == tempPath };
		if (found.isNil) {
			if (File.exists(tempPath)) {
				found = Document.open(tempPath);
				found.path = tempPath;
			} {
				found = Document(name.asString, script);
			};
			found.setFont(Font("Monaco", 9), -1).syntaxColorize;
		}{
			found.front;
		};
/*		found.onClose = { | doc |	// on close: reload document
			if (doc.path.isNil) {
				("Script '" ++ doc.title ++
					"' closed without saving. Script not loaded").postln;
			} {
				("reloading script: " ++ doc.path).postln;
				this.close;
				Script.load(doc.path).makeGui;
			}
		};
*/		^found;
	}
	hideControlGui {
		if (gui.notNil) { gui.resize(24, 200) }
	}
	showControlGui {
		if (gui.notNil) { gui.resize(1 + envir.controlSpecs.size * 21 + 6, 300) }
	}
	makeSetterAction { | key, controlspec |
		^{ | fullVal, normalizedVal |
			envir[key] = fullVal;
			if (process.isKindOf(Node)) { process.set(key, fullVal) };
		};
	}
// =================== Linking to other Scripts and Synths for signal i/o
	addReaderScript { | script, parameter = \in |
		// make script read my output through input 'parameter'
		output.addReader(script.parameterEnvir[parameter]);
	}
	removeReaderScript { | script, parameter = \in |
		// stop script reading my output through input 'parameter'
		output.removeReader(script.parameterEnvir[parameter]);
	}
	moveAfter { | argGroupIndex |
		/* Set my group index to be larger than argGroupIndex. Update target
		and move Node process to target if running. Done when I am added as reader 
		accepting signal input from another script. See OutputParameter:addReader */
		var target;
		if (groupIndex > argGroupIndex) { ^this };
		groupIndex = argGroupIndex + 1;
		envir[\target] = target = session.getGroup(groupIndex);
		if (output.notNil) { output.moveReadersAfter(groupIndex, target) };
		if (process.notNil) { process.moveAfter(target.asTarget) }
	}
	addWriterSynth { | defname, input = 'in' ... args |
		/* EXPERIMENTAL! Create and return a synth which outputs to your input  
		channel given by parameter 'in'.  NOT TESTED! */
		var bus, synth;
		input = parameterEnvir[input];
		bus = input.getInputForSynth;
		bus.addReader(input);
		synth = Synth(defname, [\out, bus.index] ++ args, process, \addBefore);
		bus.addWriter(synth);
		^synth;
	}
/*	// obsolete as of Jan '07
	addInput { | source, input = \in, output = \out |
		/* { make parameter named by input arg read its input from the output of another 
		  Script, given by argument source. 
		If no other source is already writing to this parameter, create a LinkedBus and
		store it in inputs. Else if a LinkedBus already exists, set the output of that 
		bus to write to that bus. 
		If I am already running, the bus is allocated immediately and connected to the 
		inputs and the outputs } */
		var bus;
		bus = this.getInputBus(input);
		bus.addWriter(source, output);
		// if input links exist, link-dragview string color <- red, else black 
		this.changed(\inputLink);
		// set color of sources link-dragview 
		source.addOutput(this, input, output);
		if (this.isRunning) { bus.start(this); };
	}
*/
	// parameter control and parameter data binding
	set { | param, val ... moreArgs |
		// set a scripts parameter to a value and update dependants (gui etc).
		// this is done via the parameter for consistency. 
		parameterEnvir[param].set(val, *moreArgs);
	}
	setProcessParameter { | key, val ... args |
	// received by the default action of parameter, called by Parameter:set
	// Parameter:set is the standard top method for setting a parameter of a script.
		if (process.isKindOf(Node)) { process.set(key, val, *args) };
	}
/*	set { | param, val ... moreArgs |
		// old version!
		// set a scripts parameter to a value and update dependants (gui etc).
		//
		envir.put(param, val);	// store new value, in case of restart
		// if process is a Synth or Group, set its parameter on the server
		if (process.isKindOf(Node)) { process.set(param, val) };
		this.changed(param, val, *moreArgs);	// update dependants (gui etc)
	}
*/
	nset { | ... paramValPairs |
		paramValPairs pairsDo: { | name, value | this.set(name, value) };
	}
	get { | param | ^envir[param] }
	map { | param, index |
		/* make control rate link to accept input from scripts that output to this script 
		If process is node, map its value to that index */
		thisMethod.report(this, param, index);
		krInputBusses[param] = index;
		if (process.isKindOf(Node) and: { param.script.isRunning }) {
			process.map(param.name, index)
		};
	}
	bindParameter { | myParamName, script, scriptParamName |
		// Not tested yet: always set the parameter of another script to those
		// of my parameter, whenever my parameter changes. 
		// useful for sharing such things as buffers.
		this.bindParameterAction(myParamName, { | who, what ... args |
			script.set(scriptParamName, *args);
		}, script, scriptParamName)
	}
	unbindParameter { | myParam, script, scriptParam |
		this.unbindParameterAction(myParam, script, scriptParam);
	}
	bindParameterAction { | paramName, action ... key |
		this doWhenInited: {
			this addDependant: { |  who, what ... args  |
				switch (what,
					paramName, { action.value(who, what, *args) },
					\unbind, {
						if (args == key) { this removeDependant: thisFunction }
					}
				)
			};
			this.set(paramName, this.get(paramName)); // initialize
		}
	}
	unbindParameterAction { | paramName ... key |
		this.changed(\unbind, paramName, *key);
	}
	// ---------------------------- CLOSING ----------------------------
	close {
		// Entirely remove this Script from the current workspace: Stop,
		// close gui, remove links, close dependent guis, remove from Session
		this.stop;
		this.closeGui;
		// remove all synth links to other scripts:
		this.getReaders do: this.removeReader(_);
		parameters do: _.removeAllWriters;
		this.changed(\closed);	// close all dependent (sic) guis etc
		scriptGroup.removeItem(this);	// remove from ScriptGroup;
		// remove gui and script dependencies (Parameter bindings):
		this.release;
	}
	onClose { | func | this.addDependantAction(func, \closed) }
	// --------------------------- SYNTHDEFS ----------------------------
	// make my session send any synthdefs I require when the server is booted
	loadSynthDefs { | ... synthdefs | session.loadSynthDefs(*synthdefs); }
	// ---------------------------- BUFFERS ----------------------------
	allocBuffer { | bufName = 'buffer', numFrames = 2048, numChannels = 1 |
		// allocate a buffer as soon as server is booted, and store it in 
		// envir variable named by bufName. For use with PV ugens or similar
		// ugens requiring private buffers.
		this.onBoot({
			envir[bufName] = Buffer.alloc(session.server, numFrames, numChannels);
//			thisMethod.report(envir[bufName]);
		}, session.server, true);

		this onClose: {
			if (envir[bufName].notNil) {
				envir[bufName].free;
			};
			this.removeServer(session.server);
		}
	}
	loadBuffer { | bufName, param = 'bufnum' |
		// set a BufParameter's value to use a specific buffer given by name.
		var buffer;
		if ((buffer = Samples(session.server).at(bufName)).isNil) {
			^Warn("Buffer: " ++ bufName.asString ++ " not found. 
			Please load it to the system first");
		}{
			this.set(param, buffer.bufnum ? 0);
		}
		// previous path inclusive version is broken: 
/*		this.set(
			param,
			if (pathOrName.isKindOf(Symbol)) {
				Samples(session.server).at(pathOrName) ? 0;
			}{
				Samples(session.server).loadIfNotPresent(pathOrName).bufnum;
			};
		);
*/	}
	// ---------------------------- MIDI ----------------------------
	toggleMIDI {
		if (this.midiIsActive) { this.deactivateMIDI } { this.activateMIDI }
	}
	midiIsActive { ^MIDIHandler.isActive(this) }
	activateMIDI {
		this.doIfHasMIDI({ | binding |
			thisMethod.report(this, ": ACTIVATING MIDI");
			binding.activate;
		},{ this.midiLearnDialog })
	}
	deactivateMIDI {
		thisMethod.report(this, ": DEACTIVATING MIDI");
		MIDIHandler.getMIDIbinding(this).deactivate;
	}
	doIfHasMIDI { | yesFunc, noFunc |
		var binding = this.getMIDIbinding;
//		thisMethod.report(this, binding);
		(binding.notNil).if({ yesFunc.(binding) }, noFunc);
	}
	midiLearnDialog {
		Confirm(this.asString ++ " has no MIDI Bindings. Click OK to start MIDI learning",
		{ | ok | if (ok) { this.learnMIDI }
			{ thisMethod.report("MIDI learning cancelled") }
		})
	}
	learnMIDI {
		// override the default method for adding MIDI to use a MIDIresponderArray
		this.removeMIDIArray;
		MIDIResponderArray(this, this.midiObjects.flat).learnMIDI;
	}
	removeMIDIArray {
		this.getMIDIbinding.removeMIDI;
	}
	midiObjects {
		// return objects to be bound to MIDI, as array with paired objects and functions
		// for their MIDIresponders: [obj, function, obj, function ...]
		// If no function is provided for an object, function will be nil, and MIDIHandler
		// will use the default mechanism to provide the function. 
		^(envir[\midiObjects] ?? { this.defaultMIDIobjects }).collect({ | obj |
			if (obj isKindOf: Symbol) {
				[parameterEnvir[obj], nil]
			} { obj.asArray[[0,1]] };
		});
	}
	defaultMIDIobjects {
		var list = List[[this, this.midiAction]];
		parameters do: _.add2MIDIList(list);
		^list.array;
	}
	saveMIDIDialog {
		// write all your MIDI bindings to file selected by user
		// called from GUI: User selects file via CocoaDialog.
		// If there is no binding to save, then post a message and exit.
		this.doIfHasMIDI({ | midiBindings |
			CocoaDialog.savePanel({ | path |
				var file;
				file = File(path, "w");
				this.saveMIDIbindings(midiBindings, file);
				file.close;
			})
		}, { Warn(this.asString ++ " has no MIDI bindings to save.")})
	}
	saveMIDIbindingsIfPresent { | file |
		// Called by Session:saveMIDI. Writes all midi bindings
		this.doIfHasMIDI({ | midiBindings | this.saveMIDIbindings(midiBindings, file) })
	}
	saveMIDIbindings { | midiBindings, file |
		// write all parameter names and responders of this Script's MIDIArrayResponder
		this.writeArrayHeader(file);
		midiBindings.saveMIDI(file);
		this.writeArrayFooter(file);
	}
	saveMIDI { | file, resp |
		// write the single responder that controls this Script's start / stop
		// the full array of responders is written by Script:saveMIDIbindings
/*		resp = resp ?? { this.getMIDIbinding };
		if (resp.isNil) { ^this }; // dont write if nil
*/		file putAll: [
			"\n\t['script', ",
			resp.asScriptString,
			"],"
/* [ */ //			"\n\t],"
		];
	}
	loadMIDIfromFileDialog {
		CocoaDialog.getPaths({ | paths | this.loadMIDIfromFile(paths.first) })
	}
	loadMIDIfromFile { | path |
		var specs;
		specs = path.load; // .postln;
		this.midiBindings = specs[2..];
		thisMethod.report("Loaded MIDI from file");
	}
	midiBindings_ { | midiSpecs |
	/* Method used to load MIDI specs saved in Script form. 
	mra is an array in the format described in 'About Scripts' 25e MIDI spec format
	create a MIDIResponderArray and add it + its responders to MIDIHandler.
	Once loaded, the MIDI bindings are ready to be activated on request 
	(method Script:activateMIDI )*/
		var param, resp, handler, function;
		handler = MIDIHandler.handler;
		midiSpecs = midiSpecs collect: { | spec, i |
			#param, resp = spec;
			param = parameterEnvir[param];
			function = param.value.midiAction;
			resp.function = function;
			handler.register(resp, param);
			[param, function];
		};
		handler.register(MIDIResponderArray(this, midiSpecs.flat), this);
	}
	midiAction {
		^envir[\midiAction] ?? { this.defaultMIDIaction };
	}
	defaultMIDIaction {
		^envir.defaultMIDIaction ?? {{ | src, chan, key, val |
//			thisMethod.report(this, src, chan, key, val);
			if (val == 0) { this.stop } { this.start(key, val) }
		}}
	}
	// Targets and sources: 
	makeScriptConnectionsGui {
		/* Create gui for adding / removing other scripts of this session to / from a set stored in envir variable ~targets,
		so that the script may use this set to act on in any way. 
		Also for adding / removing this script to / from the ~targets variable of other scripts
		3 panes: 
		(1) list of all scripts in this session	
			- typing t adds selected script as target to this script,
			- typing s adds selected script as source to this script 
				(i.e. this script as target to the selected script)
		(2) list of targets. Typing backspace removes selection from this script's targets
		(3) list of sources. Typing backspace removes this script from selection's targets
		*/
		var targets, sources, targetsView, sourcesView, dependant;
		if (scriptConnectionsWindow.notNil) { ^scriptConnectionsWindow.front };
		dependant = { | who, what |
			// when this script closes, always close list
			switch (what,
				\closed, { if (scriptConnectionsWindow.notNil) { scriptConnectionsWindow.close }},
				\targets, { targets.list_(this.getTargets).selectAt(0) },
				\sources, { sources.list_(this.getSources).selectAt(0) }
			);
		};
		scriptConnectionsWindow = SCWindow("Links: " ++ name.asString,
			this.guiBounds.moveBy(150, -50).resize(450, 200)
		).onClose_({
			scriptConnectionsWindow = nil;
			this.removeDependant(dependant);
		});
//		scriptsView = SCListView(scriptConnectionsWindow, Rect(5, 2, 143, 196));
		sourcesView = SCListView(scriptConnectionsWindow, Rect(152, 2, 143, 196));
		targetsView = SCListView(scriptConnectionsWindow, Rect(300, 2, 143, 196));
		targets = ListModel(
			this.getTargets,
			makeNames: { | l | l.list collect:
				{ | p | [p.script.name ++ ":" ++ p.name, p] }
			}
		);
		sources = ListModel(
			this.getSources,
			makeNames: { | l | l.list collect:
				{ | p | [p[1].name ++ ":" ++ p[0].script.name, p] }
			}
		);
		targets.addListView(targetsView);
		targets.selectAt(0);
//		targets.onSelect(targetsView, _.postln);
		targetsView.keyDownAction = { | me, char, mod, uni, key |
			var sel = targets.selection;
			if (sel.notNil) {
				switch (char,
					// return: start script of reader parameter
					$\r, { sel.script.start;
						if (mod > 256) { this.start }
					},
					// escape: stop script of reader parameter
					27.asAscii, { sel.script.stop;
						if (mod > 256) { this.stop }
					},
					// s: show script of reader parameter
					$s, { sel.script.makeGui },
					// h hide script of reader parameter
					$h, { sel.script.closeGui },
					// backspace: remove reader
					127.asAscii, { this.removeReader(sel) },
					{ me.defaultKeyDownAction(char, mod, uni, key) }
				)
			}{
				"no reader parameter selected".postln;
			}
		};
		sources.addListView(sourcesView);
		sources.selectAt(0);
//		sources.onSelect(sourcesView, _.postln);
		sourcesView.keyDownAction = { | me, char, mod, uni, key |
			var sel = sources.selection;
			if (sel.notNil) {
				switch (char,
					// return: start script of writer parameter
					$\r, { sel[0].script.start;
						// on shift, also start the writer
						if (mod > 256) { this.start }
					},
					// escape: stop script of writer parameter
					27.asAscii, { sel[0].script.stop;
						// on shift, also stop the writer
						if (mod > 256) { this.stop }
					},
					// s: show script of writer parameter
					$s, { sel[0].script.makeGui },
					// h hide script of writer parameter
					$h, { sel[0].script.closeGui },
					// backspace: remove writer
					127.asAscii, { sel[0].removeReader(sel[1]) },
					{ me.defaultKeyDownAction(char, mod, uni, key) }
				)
			}{
				"no writer parameter selected".postln;
			}
		};
		linksWindow.front;
		sourcesView.focus(true);
		this.addDependant(dependant);
	}
	getTargets {
//		^envir[\targets];
		if (envir[\targets].isNil) { envir[\targets] = Set.new };
		^envir[\targets];
	}
	getSources {
		^session.allScripts select: { | s | s.envir[\targets].asArray includes: this }
	}

	showTargets {  // to be replaced by makeTargetsGui
		var targets;
		envir[\targets] = targets = envir[\targets] ?? { Set.new };
		ListWithGui(name ++ ": Targets",
		{ | list |
			var w, decorator, lv, tv, tl;
			w = SCWindow(list.name, Rect(30, 30, 350, 450).fromTop);
			w.view.decorator = decorator = FlowLayout(w.view.bounds, 3@3, 3@3);
			tl = ListModel(
				targets.asArray,
//				add ?? {#{ |l, i| l.list.add(i) }},	// action for adding an item
				{ | l, i |
					targets.add(i);
					targets.asArray.sort({ | a, b | a.name < b.name });
				},
//				remove ?? {#{ |l, i| l.list.remove(i); l.list }}, // action for removing an item
				{ | l, i |
					targets.remove(i);
					targets.asArray.sort({ | a, b | a.name < b.name });
				},
				// action for making names of the items for display.
				{ |l| l.list.collect { |i| [i.name, i] }}
			);
			tv = SCListView(w, decorator.innerBounds.width_(168));
			tv.keyDownAction = { | view, char, mod, unicode, key |
				switch (unicode,
					127, { tl.removeAt(view.value) },
					13, { tl.atIndex(view.value).makeGui; },
					{ view.defaultKeyDownAction(char, mod, unicode, key) }
				);
			};
			tl.addListView(tv);
			lv = SCListView(w, Rect(0, 0, decorator.maxRight, decorator.maxHeight));
			lv.keyDownAction = { | view, char, mod, unicode, key |
				switch (unicode,
					3, { tl.add(list.atIndex(view.value)); },
					13, { list.atIndex(view.value).makeGui; },
					{ lv.defaultKeyDownAction(char, mod, unicode, key) }
				);
			};
			list.addListView(lv);
			w;
		},
		session.scriptGroups.list.collect(_.list).flat,
		// action for making names of the items for display.
		makeNames: { |l| l.list.collect { |i| [i.name, i] }}
		).makeGui;
	}
	showSources {
		Warn("not done yet");
	}
	// ------------------ Snippets: Functions operating on this script
	showSnippets {
/*		if (envir[\snippets].isNil) {
			^Warn("Script " ++ name ++ " has no snippets")
		};
*/		// name, defaultBounds, buildAction, commands, saveBounds = true
		WindowHandler(this, 'snippets', "Snippets for: " ++ name,
			Rect(0, 0, 400, 300).moveTo(this.guiBounds.left + 40, this.guiBounds.bottom - 100),
			{ | window, script, handler |
				var snippetEnvir, snippets, snippet, mainPanel, decorator, listView;
				var renameView;
				snippetEnvir = envir[\snippets];
				if (snippetEnvir.isNil) { envir[\snippets] = snippetEnvir = () };
				snippets = ListModel(envir[\snippets].asArray,
//					remove ?? {#{ |l, i| l.list.remove(i); l.list }}, // action for removing an item
					remove: { | l, i |
						snippetEnvir.removeAt(i.name.asSymbol);
						l.list.remove(i);
						l.list;
					},
					makeNames: { | l | l.list collect: { |s| [s.name, s] }});
				mainPanel = SCCompositeView(window,
					Rect(0, 0, window.view.bounds.width, window.view.bounds.height / 2));
				mainPanel.decorator = decorator = FlowLayout(mainPanel.bounds, 3@3, 3@3);
				listView = SCListView(mainPanel, decorator.innerBounds.width_(200));
				// add, remove, makeNames
				snippets.addListView(listView);
				listView.keyDownAction = { | view, char, mod, uni, key |
					switch (uni,
						3, { "making new snippet".postln;
							snippets.add(snippet = Snippet(this));
							snippets.selectItem(snippet);
						},
						13, {
							snippet = snippets.selection;
							if (snippet.notNil) {
								Post << "executing snippet: " << snippet.name << "\n";
								snippet.run;
							}
						},
						127, { "deleting snippet".postln;
							snippets.removeItem(snippets.selection);
						},
						{ listView.defaultKeyDownAction(char, mod, uni, key)}
					)
				};
				renameView = SCTextField(mainPanel, Rect(0, 0, decorator.rest[0], 20));
				renameView.action = { | me |
//					me.string.postln;
					if (snippets.selection.notNil) {
						snippets.selection.name = me.string.asSymbol;
						snippets.updateNames; // should use update mechanism from script here? !
//						thisMethod.report(snippets.selection, snippets.selection.name);
					}
//					snippets.selection.postln;
				};
				snippets.onSelect({ | snippet, argName, index |
					argName = snippet.name.asString;
					renameView.string = if (argName.size > 2) { argName } {
						argName ++ " (edit name here)"
					};
				}, {});
//				snippets.selectAt(0);
			}
		).makeGui;
	}
	// ------------------ GLOBAL SCRIPT ITEMS
	*addGlobalScript { | globalName, script |
		// { Add and remove scripts inside the globalScripts classvar for global access
		//	Update display of global scripts on globalScriptsWindow } */
		globalScripts[globalName] = script;
		{ this.changed(\addGlobalScript, globalName, script); }.defer;
	}
	*removeGlobalScript { | globalName |
		var script;
		script = globalScripts[globalName];
		if (script.notNil) { script.stop };
		globalScripts.removeAt(globalName);
		{ this.changed(\removeGlobalScript, globalName); }.defer;
	}
	*makeGlobalsGui {
		// (at this stage:) Make window displaying the global scripts
		if (globalScriptsWindow.notNil) { ^globalScriptsWindow.front; };
		^this.makeGlobalsWindow;
	}
	*makeGlobalsWindow {
		var scriptList, adapter;
		globalScriptsWindow = SCWindow.new("Global Scripts", Rect(0, 420, 150, 150).fromTop);
		globalScriptsWindow.onClose = { globalScriptsWindow = nil };
		scriptList = SCListView(globalScriptsWindow, Rect(3, 3, 144, 144))
			.resize_(5)
			.font_(Font("Helvetica", 10));
		/* ( no action defined yet when selecting an item. This is just a passive display) */
		adapter = { | sender, state |
			switch (state,
				\removeGlobalScript,
					{ scriptList.items = globalScripts.keys.asArray.sort; },
				\addGlobalScript,
					{ scriptList.items = globalScripts.keys.asArray.sort; }
			)
		};
		this.addDependant(adapter);
		scriptList.onClose = { this.removeDependant(adapter) };
		{ this.changed; }.defer;
		^globalScriptsWindow.front;
	}
// { REMOTE SCRIPT CONTROL: Methods for creating and controlling scripts via OSC
//	all these methods use the prefix r } */
	*rAdd { | globalName, folder, scriptName |
		// create new script and store it in globalParentEnvir under globalName
		this.addGlobalScript(globalName, ScriptBrowser.load(folder, scriptName));
	}
	*rRemove { | globalName |
		this.removeGlobalScript(globalName);
	}
	*rLoad { | globalName, folder, scriptName |
		// load script into an existing script (thereby replacing the previous
		// script loaded in that instance)
		globalScripts[globalName].load(ScriptBrowser.at(folder, scriptName));
		{ this.changed(\loadScript, globalName, folder, scriptName); }.defer;
	}
	*rStart { | globalName |
		{
			globalScripts[globalName].start;
			this.changed(\start, globalName);
		}.defer; // Defer needed for GUI actions from OSC
	}
	*rStop { | globalName |
		{
			globalScripts[globalName].stop;
			this.changed(\stop, globalName);
		}.defer; // Defer needed for GUI actions from OSC
	}
	*rStopAll { | globalName |
		globalScripts.do { |s| s.stop };
		this.changed(\stopAll);
	}
	*rSet { | globalName, parameter, value |
		globalScripts[globalName].set(parameter, value);
		this.changed(\set, globalName, parameter, value);
	}
	*rAddInput { | globalName, sourceName, input = \in, output = \out |
		globalScripts[globalName].addInput(globalScripts[sourceName], input, output);
		this.changed(\addInput, globalName, sourceName, input, output);
	}
	// not yet implemented!
	*rRemoveInput { | globalName, sourceName, input = \in, output = \out |
		globalScripts[globalName].removeInput(globalScripts[sourceName], input, output);
		this.changed(\removeInput, globalName, globalScripts, input, output);
	}
	add { | scriptName |
		// Access of global scripts by Script[<scriptname>]
		^globalScripts[scriptName];
	}
	*at { | folderName, scriptName |
		// Access of ScriptBrowsers scripts by Script.at(<foldername>, <scriptname>)
		^ScriptBrowser.at(folderName, scriptName);
	}
	*putGlobal { | ... keyValuePairs |
		// "macro" for adding objects to globalParentEnvir
		globalParentEnvir.putPairs(keyValuePairs);
		^keyValuePairs; // for further use
	}
	*atGlobal { | key |
		// "macro" for getting objects from globalParentEnvir
		^globalParentEnvir[key];
	}
	// ================= Snapshots  ================= 
	takeSnapshot {
		// save full current state in snapshots array for saving to disk
		snapshots.addSelect(
			Snapshot[snapshots.size.asString, parameters.collect(_.getPreset)]
		).selectLast;
		thisMethod.report(this, "snapshot stored");
	}
	loadSelectedSnapshot {
		if (snapshots.selection.notNil) {
			this.loadSnapshot(snapshots.selection)
		}
	}
	loadSnapshot { | snapshot | // snapshot is an Array of parameter values
		snapshot do: { | v, i | parameters[i].setPreset(v) };
	}
	removeSnapshot { | index | snapshots.removeAt(index) }
	showSnapshots { | snapshotListView |
		if (gui.notNil) {
			gui.bounds = gui.bounds
				.width_(this.defaultGuiBounds.width + snapshotViewWidth);
			snapshotListView.focus(true);
			if (snapshots.selection.isNil and: { snapshots.list.size > 0}) {
				snapshots.selectAt(0)
			}
		}
	}
	hideSnapshots { | snapshotListView |
		if (gui.notNil) {
			gui.bounds = gui.bounds.width_(this.defaultGuiBounds.width);
			snapshotListView.focus(false);
		}
	}
	// experimental 070606f midi handling of snapshots ?
	makeFuncCodeForSnapshot { | snapshot | // snapshot is an Array of parameter values
		// this code is meant to be human-readable for modification and use 
		// in custom MIDIresponder functions. 
		var code;
		if (snapshot.size == 0) { ^"No snapshot given to make code".postln };
		code = String streamContents: { | s |
			s << "{ | script, src, chan, num, val |";
			parameters do: { | p | s << "\n\t\script.set("
				<<< p.name << ", " <<< p.getPreset << ");";
			};
			s << "\n}\n";
		};
		code.postln;
	}
	snapshotNamed { | argName |
		// get snapshot by name. argName should be a String
		^snapshots.list detect: { | s | s.first == argName }
	}
	// ================= Saving in Sessions  ================= 
	writeArrayHeader { | file |
		file putAll: [
			"[",
		 	scriptGroup.name.asCompileString, ", ", name.asCompileString,
			","
		];
	}
	writeArrayFooter { | file |
		file putString: "\n],\n";
	}
	folder_names_bounds {
		// return items for storing and reloading in Session scripts
		var latestBounds;
		latestBounds = if (gui.isNil) { guiBounds } { gui.bounds };
		^[folder, originalName, name, latestBounds.left, latestBounds.top, gui.notNil];
	}
	saveLinks { | file |
		if (output.notNil) { output.saveLinks(file) }
	}
	saveSnapshots { | file |
		if (snapshots.list.size == 0) { ^this };
		this.writeArrayHeader(file);
		snapshots.list do: _.writeSnapshot(file);
		this.writeArrayFooter(file);
	}
/*	write1MIDI { | file |
		var resp;
		if ((resp = this.getMIDIbinding).notNil) {
			file putAll: [
				"\n\t[",
				name.asCompileString, ", ", resp.asScriptString,
				"\n\t],"
			];
		}
	}
*/
	saveData { | file |
		var action;
		action = envir[\saveData];
		if (action.notNil) {
			this writeArrayHeader: file;
			action.(envir, file);
			this writeArrayFooter: file;
		}
	}
	// ---------------------------- PRINTING ----------------------------
	storeArgs { ^[name] }
	printOn { | stream | this.storeOn(stream); }
}
