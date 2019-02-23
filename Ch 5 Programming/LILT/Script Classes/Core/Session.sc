/* (IZ 2005-10-16) {
	Holds the configuration of a working session. 
	Saves and loads onto file this configuration. 
	The configuration data saved are: 
	- Session name
	- Session's server's name and network address
	- The Scripts of the session (grouped in Script Groups), saved by folder name and script name
	- Paths of buffers loaded in Samples
	- Paths of SynthDefs loaded in SynthDefs
	- MIDI bindings of the scripts
	- Parameter snapshots of the scripts
TODO: ??? - further configuration data like 
		wavetables, 
		audio files on disk
		audio i/o channels

As gui, Session opens one window with:
-	a menu of all currently available session instances, 
-	a list of the script groups of the currently selected session
-	a menu with commands for opening and saving the currently selected session

}*/

Session {
	// {
	classvar <all;	// a ListWithGui for multiple sessions (under construction);
	classvar <midiInited = false; // sessions init midi input when loaded if needed
	// INSTANCE VARIABLES:
	var <name;		// for displaying the session
	var <server;	// Each session runs on one server. (Multiple sessions possible)
	var <numGroups;	// number of groups: recreated on server reboot!
	var <scriptGroups;	// all script groups, stored in a ListModel
					// each scriptgroup is a ListWithGui, containing a list of scripts
	var <>path;		// path for saving / loading this Session
	var <midiConfig;// (OBSOLETE?) current MIDI configuration, for setting MIDI actions of scripts
	var <samples;	// ListModel instance for saving/reloading audio samples of this session.
					// Holds list of sample names only. The samples are loaded by Samples
	var <groups;	// node groups for keeping synths of scripts in order-of-execution
	var <>script;	// the script that created this session.
	var <synthdefs;	// synthdefs that this session sends to the server whenever it boots or if it is already 
					// booted. Any Script can add its own required synthdefs by: ~session.loadSynthDefs(...);
	// }
	*new { | name = 'Session', server, numGroups = 1, scriptGroups,
		path, midiConfig, samples |
		this.initMIDI;
		^this.newCopyArgs(
			name,
			server = server ?? { Server.default },
			numGroups,
			/* { Store list of script groups in a ListModel. ListModel handles
				display and selection of groups in the list view of the Session gui.
				The listview is attached to the gui from the Session gui whenever a new
				Session is selected.
			} */
			ListModel(
				list: scriptGroups ? [],
				makeNames: { | l | l.list collect: { | s | [s.name, s] }}
			),
			path,
			midiConfig,
			ListModel(samples, makeNames: { | l | l.list collect: { | s | [s.path, s] }})
		).init;
	}
	*initMIDI {
		if (midiInited.not) {
			MIDIClient.init(16, 16);
			16 do: { arg i; MIDIIn.connect(i, MIDIClient.sources.at(i)) };
			midiInited = true;
		}
	}
	init {
		var groupTarget = server.asTarget;
		groups = { NodeTier.new } ! numGroups;
		this.onReset({
			groups do: { | n | n.group = Group(groupTarget, \addToTail) };
		}, server, doNowIfRunning: true);
		this scriptsDo: { | s | s.envir.target = groups[s.groupIndex] };
		CmdPeriod.add(this);
		this.add;
	}
	name_ { | argName |
		name = argName.asSymbol;
		all.list = all.list;	// update name list and notify dependents
	}
	cmdPeriod { // CmdPeriod stops groups. So remake them
		this.makeGroups;
	}
	makeGroups { // create the groups that this session needs to run
		var groupTarget;
		// TODO: now that the Sessions's script's environment is parent environment
		// for all scripts of that Session, access to the groups can be done via indexing an
		// array ~targetGroups stored in the Session's environment. 
		// that way no updates will be needed any more - thereby simplifying the group mechanism
		groupTarget = server.asTarget;
		if (groups.isNil) {
			groups = { NodeTier.new } ! numGroups;
			this scriptsDo: { | s | s.envir.target = groups[s.groupIndex] }
		};
		if (server.serverRunning) {
		}
	}
	getGroup { | groupIndex = 0 |
		var isRunning = server.serverRunning;
		var target = server.asTarget;
		while { groups.size <= groupIndex } {
			groups = groups add: NodeTier.new(
				if (isRunning) { Group(target, \addToTail ) } { nil }
			);
		};
		numGroups = groups.size;
		^groups[groupIndex];
	}
	getScript { | groupName, scriptName |
	// get script from this session by the name of its group and its name
		var sGroup, script, msg;
		sGroup = this.scriptGroupNamed(groupName.asSymbol);
		if (sGroup.isNil) {
			msg = format("% could not find a script group named '%'\n", this, groupName);
			msg.postln;
			Warn(msg);
			^nil;
		};
		script = sGroup.list
			.detect({|s| s.name.asSymbol == scriptName });
		if (script.isNil) {
			msg = format("% could not find a script named '%' in group '%'\n",
				this, scriptName, groupName);
			Warn(msg);
		};
		^script;
	}
	add { this.class.add(this) }
	*add { | session |
		// add and select
		this.getAll.add(session);
		all.selectItem(session);
	}
	*getAll {
		if (all.isNil) { this.initAll };
		^all;
	}
	*select { | sessionName, groupName |
		// make sessionName, groupName the currently selected sesison/group
		var session, group;
		session = this.named(sessionName);
		if (session.isNil) { ^Warn("Could not find session named: " ++ sessionName.asString) };
		if (groupName.isNil) {
			group = session.scriptGroups.selectAt(0);
		}{
			group = session.scriptGroupNamed(groupName);
		};
		if (group.isNil) { ^Warn("Could not find script group named: " ++ groupName.asString ) };
		all.selectItem(session);
		session.scriptGroups.selectItem(group)
	}
	*named { | name | ^all.list detect: { | s | s.name == name } }
	scriptGroupNamed { | groupName |
		^scriptGroups.list detect: { | g | g.name == groupName }
	}
	*scriptGroupNamed { | sessionName, groupName |
		^all.list.detect.({ | s | s.name == sessionName })
			.scriptGroupNamed(groupName);
	}
	*makeGui { ^this.getAll.makeGui }
	makeGui { ^this.getAll.makeGui }
	*closeGui { if (all.notNil) { all.closeGui } }
	*current { ^this.getAll.selection }
	*currentScriptGroup { ^this.current.currentScriptGroup }
	currentScriptGroup { ^scriptGroups.selection }
	*addScript { | script |
		// Add script to current group of current session. See instance methods.
		^all.selection.addScript(script);
	}
	*initAll {
		// create ListWithGui to put sessions in. This is evaluated lazily, because doing it
		// at initClass involves too many dependencies on other classes being initialized.
		// *initAll is called by *makeGui.
		// TODO: Try to make this work as *initClass by resolving the initClass dependencies.
		all = ListWithGui(
			name: "Sessions",
			/* { Create window with menu for selecting a session and list view showing
				the script groups of the currently selected session.
				Keyboard action on the script group list: 
					return = show gui of selected script group 
			} */
			// sessions is the ListWithGui in 'all', holding all sessions
			makeGui: { | sessions |
				var session, scriptGroupList, newSession;
				var window, menu, scriptGroupView, scriptView, decorator, adapter;
				window = SCWindow("Sessions", Rect(0, 0, 210, 300).fromTop.fromRight);
				this.changed(\windowOpened);
				window.view.decorator = decorator = FlowLayout(window.view.bounds, 3@3, 2@2);
				// menu with all sesssions
				menu = SCPopUpMenu(window, Rect(0, 0, decorator.rest[0], 20))
					.background_(Color.grey(0.6))
					.stringColor_(Color.red)
					.font_(Font('Helvetica-Bold', 12));
				menu.keyDownAction = { | view, char, mod, uni, key |
					switch (uni,
						3, {      // enter: add new session
							this.newSessionDialog;
						},
						13, {      // return: add new script group
							this.addScriptGroupFromUser;
						},
						127, {    // backspace: close + delete current session
							this.confirmClose(all.selection);
						},
						114, { 	// r: revert 
							this.current.revertDialog;
						},
						{ view.defaultKeyDownAction(char, mod, uni, key) }
					)
				};
				sessions.addListView(menu);
				decorator.nextLine;
				menu = SCPopUpMenu(window, Rect(0, 0, 60, 20))
					.background_(Color(0.7, 0.3, 0.3, 0.4));
				menu.items = ["(Session", "New...", "Open...", "Save", "Save as...", "Remove Entirely",
				"Revert...", "Rename Session ...", "Rename Group ...",
				"New Script Group ...", "Delete Script Group ...",
				"Take Snapshot", "Show Load String"
				];
				menu.action = { | me |
					[{ }, { this.newSessionDialog }, { this.openDialog }, { this.save },
						{ this.saveAs }, { this.confirmRemove(this.current) },
						{ this.current.revertDialog },
						{ this.current.nameSessionDialog },
						{ this.current.nameScriptGroupDialog },
						{ this.addScriptGroupFromUser },
						{ this.current.removeScriptGroup(this.currentScriptGroup) },
						{ this.current scriptsDo: _.takeSnapshot },
						{ this.showLoadString(this.current) }
					][me.value].value;
					me.value = 0;	// reselect first item
				};
				SCButton(window, Rect(0, 0, 35, 20))
					.states_([["Show", nil, Color(0.7, 0.3, 0.3, 0.4)]])
					.action_({ this.current scriptsDo: _.makeGui });
				SCButton(window, Rect(0, 0, 33, 20))
					.states_([["Hide", nil, Color(0.7, 0.3, 0.3, 0.4)]])
					.action_({ this.current scriptsDo: _.closeGui });
				SCButton(window, Rect(0, 0, 34, 20))
						.states_([["Start", nil, Color(0.7, 0.3, 0.3, 0.4)]])
						.action_({ this.current scriptsDo: _.start });
				SCButton(window, Rect(0, 0, 33, 20))
						.states_([["Stop", nil, Color(0.7, 0.3, 0.3, 0.4)]])
						.action_({ this.current scriptsDo: _.stop });
				decorator.nextLine;
				// list the script groups of the selected session:
				menu = SCPopUpMenu(window, Rect(0, 0, 60, 20))
					.background_(Color(0.1, 0.7, 0.1, 0.2));
				menu.items = ["(Group", "New...", "Delete ...", "Rename ..."];
				menu.action = { | me |
					[{ }, { this.addScriptGroupFromUser },
					{ this.removeScriptGroupDialog },
					{ this.current.nameScriptGroupDialog }
					][me.value].value;
					me.value = 0;	// reselect first item
				};
				SCButton(window, Rect(0, 0, 35, 20))
					.states_([["Show", nil, Color(0.1, 0.7, 0.1, 0.2)]])
					.action_({ this.currentScriptGroup.list do: _.makeGui });
				SCButton(window, Rect(0, 0, 33, 20))
					.states_([["Hide", nil, Color(0.1, 0.7, 0.1, 0.2)]])
					.action_({ this.currentScriptGroup.list do: _.closeGui });
				SCButton(window, Rect(0, 0, 34, 20))
					.states_([["Start", nil, Color(0.1, 0.7, 0.1, 0.2)]])
					.action_({ this.currentScriptGroup.list do: _.start });
				SCButton(window, Rect(0, 0, 33, 20))
					.states_([["Stop", nil, Color(0.1, 0.7, 0.1, 0.2)]])
					.action_({ this.currentScriptGroup.list do: _.stop });
				decorator.nextLine;
				scriptGroupView = SCListView(window, Rect(0, 0, decorator.rest[0], 62));
				scriptGroupView.background_(Color(0.1, 0.7, 0.1, 0.2));
				scriptGroupView.keyDownAction = { | view, char, mod, uni, key |
					// coding in separate method to return if nil and for clarity
					this.performScriptGroupActions(view, char, mod, uni, key)
				};
				decorator.nextLine;
				menu = SCPopUpMenu(window, Rect(0, 0, 60, 20))
					.background_(Color(0.2, 0.3, 0.7, 0.2));
				menu.items = ["(Script", "Delete Script", "Rename Script",
					"Toggle MIDI", "Learn MIDI", "Take Snapshot"];
				menu.action = { | me |
					[{ }, { this.currentScriptGroup.selection.close },
					{ this.currentScriptGroup.selection.renameDialog },
					{ this.currentScriptGroup.selection.toggleMIDI },
					{ this.currentScriptGroup.selection.learnMIDI },
					{ this.currentScriptGroup.selection.takeSnapshot }
					][me.value].value;
					me.value = 0;	// reselect first item
				};
				SCButton(window, Rect(0, 0, 35, 20))
					.states_([["Show", nil, Color(0.2, 0.3, 0.7, 0.2)]])
					.action_({ this.currentScriptGroup.selection.makeGui });
				SCButton(window, Rect(0, 0, 33, 20))
					.states_([["Hide", nil, Color(0.2, 0.3, 0.7, 0.2)]])
					.action_({ this.currentScriptGroup.selection.closeGui });
				SCButton(window, Rect(0, 0, 34, 20))
					.states_([["Start", nil, Color(0.2, 0.3, 0.7, 0.2)]])
					.action_({ this.currentScriptGroup.selection.start });
				SCButton(window, Rect(0, 0, 33, 20))
					.states_([["Stop", nil, Color(0.2, 0.3, 0.7, 0.2)]])
					.action_({ this.currentScriptGroup.selection.stop });
				decorator.nextLine;
				scriptView = SCListView(window, Rect(0, 0, *decorator.rest));
 				scriptView.background = Color(0.2, 0.3, 0.7, 0.2);
				sessions.onSelect(menu, { | session |
					// get rid of your attachment to the previous session's script list:
					scriptGroupView.onClose.value;
					scriptGroupView.items_([]).value_(0);
					scriptView.onClose.value;
					scriptView.items_([]).value_(0);
					// attach to the script groups of the newly selected session:
					if (session.notNil) {
						session.scriptGroups.addListView(scriptGroupView);
						session.scriptGroups.onSelect(scriptGroupView, { | sGroup |
							scriptView.onClose.value;
							scriptView.items_([]).value_(0);
							if (sGroup.notNil) {
								sessions.selection.currentScriptGroup.addListView(scriptView);
							};
						});
						// update the views index to reflect currently selected script group
						scriptGroupView.value =
							session.scriptGroups.indexOf(session.scriptGroups.selection) ? 0;
						session.scriptGroups.selectAt(0);
					} {
						"no session selected".postln;
					}
				});
				// add capability of receiving scripts dragged from ScriptBrowser?
//				scriptView.canReceiveDragHandler = { true };
				scriptView.keyDownAction = { | view, char, mod, uni, key |
					var listModel, script;
					listModel = Session.currentScriptGroup;
					script = listModel.selection;
					if (script.notNil) {
						switch (uni,
							127, { // backspace (delete): stop script and remove it
								script.close;
							},
							13, {	// return
								case ( // see note on hack above for determining modifier key
									// plain return: start script
									{ mod <= 256 }, { script.start },
									// shift return: show script gui
									{ mod < 200000 }, { script.makeGui },
									// control return: activate MIDI bindings
									{ true }, { script.activateMidi }
								);
							},
							27, { // escape
								case (
									// plain escape: stop script
									{ mod <= 256 }, { script.stop },
									// shift escape: close script gui
									{ mod < 200000 }, { script.closeGui },
									// control escape: deactivate MIDI bindings
									{ true }, { script.deactivateMidi }
								);
							},
							32, { // space
								case (
									// plain space: toggle script on/off
									{ mod <= 256 }, { script.toggle },
									// shift space: toggle script gui open/close
									{ mod < 200000 }, { script.toggleGui },
									// control space: toggle activate/deactivate MIDI bindings
									{ true }, { script.toggleMidi }
								);
							},
							3, { // enter: make copy of this script to the same script group
								// TODO: IMPLEMENT copy script to script group on enter key!
							},
							{ view.defaultKeyDownAction(char, mod, uni, key) }
						)
					};
				};
				menu.focus(true);
				window;
			},
			list: [],
			makeNames: { |l| l.list collect: {|s| [s.name, s] }}
		);
		this.new('Untitled Session').addScriptGroup;
		this.new('Session Makers').addScriptGroup('Session Making Scripts');
		// make Session class update its dependants like its list with model does:.
		// Used by BrowserControlWindow script controller, and can be used elsewhere. 
		all.addDependant { | who ... whats |
			this.changed(*whats);
		};
		all.selectAt(0);
	}
	*doIfSelected { | action |
		var session;
		session = all.selection;
		if (session.isNil) { ^nil };
		action.(session);
	}
	*confirmRemove { | argSession |
		// close guis and remove current session if user confirms it on dialogue
		if (argSession.isNil) { ^Post << thisMethod.name << " nil session - stopped" << "\n"};
		Confirm(
			"Do you really want to remove the session named:\n\t"
				++ argSession.name ++
				"\nAND it's creating script from the current working space?",
			{ |ok| if (ok) { argSession.remove.removeParentScript } }
		)
	}
	*performScriptGroupActions { | view, char, mod, uni, key |
		/* { perform keyboard action on the selected script group, based on 
			keyboard command entered on the listview of the script groups of the session
			in the main session window. enter: add new script group,
			space: toggle (open/close) group gui  
			return: start group, escape: stop group
			backspace: remove group. 
			Note: I am using a "hack" to determine if shift or control is pressed 
			independently of mac model (g4 pb or g5 desktop):
			if modifier value <= 256, then no modifier key is pressed.
			if 200000 > modifier > 256 then shift key is pressed
			if modifier > 200000 then the control key is pressed
		} */
		var session, groupList, group;
		if ((session = all.selection).isNil) { "no session selected".postln; ^nil };
		groupList = session.scriptGroups;
		group = groupList.selection;
		if (uni == 3) {	// enter: create new script group
			this.addScriptGroupFromUser;
		};
		if (group.isNil) { ^view.defaultKeyDownAction(char, mod, uni, key) };
		switch (uni,
			127, {	// backspace: close and remove script group
				session.removeScriptGroup(group);
			},
			13, {	// return: start/open GUI/activate MIDI for all
				case (
					// plain return: start scripts of script group
					{ mod <= 256 }, { group.list do: _.start },
					// shift return: show guis of all scripts in group
					{ mod < 200000 }, { group.list do: _.makeGui },
					// control return: activate MIDI bindings of script group
					{ true }, { group.list do: _.activateMidi }
				);
			},
			27, { // escape: stop/close GUI/deactivate MIDI for all
			//	thisMethod.report("escape!, mod is:", mod);
				case (
					// plain escape: stop scripts
					{ mod <= 256 }, { group.list do: _.stop },
					// shift return: close guis of all scripts in group
					{ mod < 200000 }, { group.list do: _.closeGui },
					// control escape: deactivate MIDI bindings
					{ true }, { group.list do:  _.deactivateMidi }
				);
			},
			32, { // space:
				case (
					// plain escape: stop scripts
					{ mod <= 256 }, { group.list do: _.toggle },
					// shift return: close guis of all scripts in group
					{ mod < 200000 }, { group.list do: _.closeGui },
					// control escape: deactivate MIDI bindings
					{ true }, { group.list do:  _.deactivateMidi }
				);
				if (mod <= 256) { // space: toggle run all scripts in group
					group.list do: _.toggle
				}{  // shift space: toggle scriptGroup gui
					group.toggleGui;
				};
				Session.makeGui; // bring focus back to session gui
			},
/*			[ 0, 262401 ] // control space
			[ 160, 524576 ] // option space
*/			160, { // option space: toggle guis of all scripts in group
				if (group.list.first.gui.isNil) {
					group.list do: _.makeGui
				}{
					group.list do: _.closeGui
				};
				Session.makeGui; // bring focus back to session gui
			},
			114, { 	// r: revert entire Session
				this.current.revertDialog;
			},
			{ ^view.defaultKeyDownAction(char, mod, uni, key) }
		);
		view.focus(true);
	}
	removeScriptGroupDialog { | group |
		Confirm(format("Do you really want to remove the Script group::\n\t\"%\"", name),
			_.if({ this.removeScriptGroup(group) })
		);
	}
	removeScriptGroup { | group |
		group.list.do { | s | s.closeGui; s.stop };
		group.closeGui;
		scriptGroups.removeItem(group);
	}
	*addScriptGroupFromUser {
		var session;
		this.doIfSelected { | session |
			TextDialog("Edit name of new Script Group:", "Scripts",
				{ | ok, argGroupName |
					if (ok) {
						session.addScriptGroup(argGroupName);
						// session.scriptGroups.selection.makeGui;
						Session.makeGui; // bring focus back to session gui
					}
				}
			);
		};
	}
	addScriptGroup { | argName = 'Scripts', scripts |
		scripts = scripts ? [];
		scriptGroups.addSelect(ListWithGui(
			name: scriptGroups.names.makeUniqueName(argName),
			list: scripts,
			makeNames: { |me| me.list.collect {|i| [i.name, i] }},
			// gui for script groups:
			makeGui: { | listModel |
				var window, decorator, sessionlabel, listview;
				window = SCWindow(listModel.name,
					(listModel.bounds ?? {Rect(200,0,200,300).fromTop.fromRight})
				);
				window.view.decorator = decorator = FlowLayout(window.view.bounds, 3@3, 2@2);
				SCStaticText(window, Rect(0, 0, decorator.rest[0], 23))
					.align_(\center)
					.string = "(" ++ this.name.asString ++ ")";
				decorator.nextLine;
				listview = SCListView(window, Rect(0,0,*decorator.rest));
				listModel.addListView(listview);
				listview.keyDownAction = { | view, char, mod, uni, key |
					var script;
					script = listModel.selection;
					if (script.notNil) {
						switch (uni,
							127, { // backspace: stop script, close guis, remove remove it and its links 
								script.close;
							},
							13, {	// return
								case ( // see note on hack above for determining modifier key
									// plain return: start script
									{ mod <= 256 }, { script.start },
									// shift return: show script gui
									{ mod < 200000 }, { script.makeGui },
									// control return: activate MIDI bindings
									{ true }, { script.activateMidi }
								);
							},
							27, { // escape
								case (
									// plain escape: stop script
									{ mod <= 256 }, { script.stop },
									// shift escape: close script gui
									{ mod < 200000 }, { script.closeGui },
									// control escape: deactivate MIDI bindings
									{ true }, { script.deactivateMidi }
								);
							},
							32, { // space
								case (
									// plain space: toggle script on/off
									{ mod <= 256 }, { script.toggle },
									// shift space: toggle script gui open/close
									{ mod < 200000 }, { script.toggleGui },
									// control space: toggle activate/deactivate MIDI bindings
									{ true }, { script.toggleMidi }
								);
							},
							3, { // enter: make copy of this script to the same script group
								// TODO: IMPLEMENT copy script to script group on enter key!
							},
							{ view.defaultKeyDownAction(char, mod, uni, key) }
						)
					};
				};
				window;
			}
		).addDependant(this.makeGuiPlacerAction));
	}
	makeGuiPlacerAction { | x = 0, y = 420 |
		// { create dependent function that uses its external scope variables to 
		//	create non-overlapping gui bounds for each new script of each group }
		var minimumX, minimumY;
		minimumX = x;
		minimumY = y;
		^{ | who, what, script |
			var bounds, width, height;
			if (what == \newScript) {
				bounds = script.guiBounds;
				width = bounds.width;
				height = bounds.height;
				// if window bounds exceed right edge of screen...
				if ((x + width) > SCWindow.screenBounds.width) {
					y = y + 20; // ...then move down one row
					// ... but never lower than 100 from bottom of screen
					if ((y + 100) > SCWindow.screenBounds.height) { y = minimumY; };
					// ... also indent next row (!), but never beyond 500 
					x = minimumX = minimumX + 20 % 500;
				};
				// set the bounds of this script's gui window
				script.guiBounds = bounds.moveTo(x, y).fromTop;
				// move right of this window for next window;
				x = (x+width);
			}
		}
	}
	addScriptAt { | folderName, scriptName, scriptGroup |
		// Add script found under folderName, scriptName in the ScriptBrowser 
		// to the currently selected scriptGroup of the currenltly selected Session.
		^this.addScript(Script.at(folderName, scriptName), scriptGroup);
	}
	addScript { | script, scriptGroup |
		// { Add script to the current script group and select it
		//	If script with same name exists, append counter to new script name
		// Place the scripts guiBounds so that guis of the same groups scripts
		// do not overlap. }
		scriptGroup = scriptGroup ?? { scriptGroups.selection };
		script.name = scriptGroup.names.makeUniqueName(script.name.asSymbol);
		script.session = this;	// need to know my session to get its groups
		script.scriptGroup = scriptGroup;
		script.envir.target = this.getGroup(script.groupIndex);
		scriptGroup.addSelect(script);
		scriptGroup.changed(\newScript, script);
		^script;
	}
	midiConfig_ { | argMidiConfig |
		if (midiConfig.notNil) { midiConfig.stopLearn.close.removeDependant(this); };
		midiConfig = argMidiConfig;
		midiConfig.addDependant(this);
	}
	*configureMidi {
		if(all.selection.notNil) { all.selection.configureMidi };
	}
	configureMidi {
		midiConfig.makeGui;
	}
	update { | who, what ... args |
		switch (what,
			\midiConfig, { this.midiConfig = args[0]; }
		)
	}
	close { // close all script group guis and all scripts
		scriptGroups.list.do { |sg|
			sg.closeGui;				// close guis of all script groups
			sg.list.copy do: _.close;	// close+remove scripts of all script groups
		};
		groups.do { | g | g.free };	// free all Groups from your server
		groups = [];
		this.removeServer(server);	// stop listening to updates from ServerWatcher
		this.changed(\closed);
		CmdPeriod.remove(this);
		this.release;
	}
	remove { // close this session and remove it from all
		all.removeItem(this);
		this.close;
	}
	removeParentScript {
		// currently Scripts that make Sessions are set by the Session making
		// Script to remove themselves from the Session-makers Session
		// when their gui closes (:>)
		if (script.notNil) {
			script.closeGui;
			// but I'll re-remove it anyway: 
			script.removeFromSession;
		};
	}
	activateMidi {
		// activate midi bindings in all your scripts
		this.allScripts.do { |s| s.activateMidi };
	}
	deactivateMidi {
		// deactivate midi bindings in all your scripts
		this.allScripts.do { |s| s.deactivateMidi };
	}
	allScripts {
		^scriptGroups.list.collect({|sg| sg.list }).flat;
	}
	// Loading Synthdefs: 
	loadSynthDefs { | ... argDefs |
		// synthdefs that this session sends to the server whenever it boots or if it is already 
		// booted. Any Script can add its own required synthdefs by: ~session.loadSynthDefs(...);
		if (synthdefs.isNil) { synthdefs = () };
		argDefs do: { | def |
			if (synthdefs[def.name.asSymbol].isNil) {
				synthdefs[def.name.asSymbol] = def;
				{ def.send(server) }.onBoot(server, true);
			}
		}
	}
	// =================== Creating, Saving and Loading ===================
	*newSessionDialog {
		this.new('Untitled Session')
			.addScriptGroup
			.nameSessionDialog("Edit name of new Session:");
		Session.makeGui; // bring focus back to session gui
	}
	nameSessionDialog { | message, action |
		TextDialog(message, "Session", { | ok, argSessionName |
			if (ok) {
				this.name = argSessionName;
				action.(this);
			}
		})
	}
	nameScriptGroupDialog {
		var sg;
		sg = this.currentScriptGroup;
		if (sg.notNil) {
			TextDialog("Edit name of ScriptGroup:", sg.name.asString,
				{ | ok, argGroupName |
					if (ok) {
						sg.name = argGroupName.asSymbol;
						scriptGroups.list = scriptGroups.list;
					}
				}
			);
		}
	}
	revertDialog {
		if (script.isNil) {
			^Warn(format("Session % does not have a script. Cannot revert.", name))
		};
		Confirm(format(
			"Do you really want to revert session:\n\t\"%\"\nto the last version saved on disk?",
			name),
			{ | ok | if (ok) { this.revert } }
		);
	}
	revert {
		// must reload script from file to get the old data!
		var path;
		this.remove;
		Script.load(script.path);
	}
	*openDialog {
		CocoaDialog.getPaths({ | paths |
			Script.load(paths.first/*, nil, Session.named(\Sessions)*/).makeGui.start;
		});
	}
	*save {
		this.doIfSelected { | session | session.save; }
	}
	save {
		var file;
		if (path.isNil) {
			this.saveAs;
		}{
			file = File(path, "w");
			Post << "Starting to save session: '" << name << "' ... \n";
			this.saveAsScript2File(file);
			file.close;
			Post << "Saved " << name << " under: " << path;
		}
	}
	*saveAs {
		this.doIfSelected { | session | session.saveAs; }
	}
	saveAs {
 		if (name.asSymbol == 'Untitled Session') {
			^this.nameSessionDialog(
				"Name this session before saving:",
				{ this.saveSessionDialog }
			);
		};
		this.saveSessionDialog;
	}
	saveSessionDialog {
		CocoaDialog.savePanel({ | argPath |
			path = argPath;
			this.save;
		}, {
			"Save session cancelled".postln;
		})
	}
	saveAsScript2File { | file |
		// save session data as script to file
		var check;
		file.putAll([
			"// (SC3) Script for session '", name.asSymbol,
			"'\n// Generated on ", Date.getDate.asString,
			"\n\n~sessionName = \"",
			name, "\";\n~server = Server.named['",
			server.name, "'] ? { Server.default };\n",
			"~serverAddressDetails = ", [server.addr.hostname, server.addr.port].asCompileString,
			";	// Servers address saved for reference\n\n",
			"//List of paths of samples to be loaded for this session:\n~samplePaths = [\n"
		] ++
		Samples(server).buffers.values.asArray.select({ | b | b.path.notNil })
			.collect({ | buf | "\t\"" ++ buf.path ++ "\",\n"  })
		++ ["];\n\n"]
		);
		this.saveScriptGroups(file);
		this.saveLinks(file);
		this.saveMIDI(file);
		this.saveSnapshots(file);
		this.saveScriptSpecificData(file);
		file.putString(
			"\n// the common part of the script for starting and stopping is included as template:");
		file.putString("\n~script.include('Includes', 'SessionLoad');\n");
		file.close;
	}
	saveScriptGroups { | file |
		file.putAll([
			"// Specs for creating scripts: names of Groups, Folders, Files, Scripts\n",
			"~scriptGroups = [\n"
		]);
		scriptGroups.list do: { | group |
			file.putAll(["\t", $[, $", group.name, "\",\n"]);
			group.list do: { | script |
				file.putAll(["\t\t", script.folder_names_bounds.asCompileString, ",\n"]);
			};
			file.putString("\t");
			file.putChar($]);	// bug in SuperCollider client language parser?
			file.putString(",\n");
		};
		file.putChar($]);	// bug in SuperCollider client language parser?
		file.putString(";\n");
	}
	saveLinks { | file |
		file.putAll(["\n// Specs for creating links between scripts:\n",
			"~linkSpecs = [\n"
		]);
		this scriptsDo: { | script | script.saveLinks(file) };
		file putString: "];\n";
	}
	saveMIDI { | file |
		file.putAll([
			"\n// Specs to create MIDIResponders for each Script:\n",
			"~midiBindings = [\n"
		]);
		this scriptsDo: { | script | script.saveMIDIbindingsIfPresent(file) };
		file putString: "];\n";
	}
	saveSnapshots { | file |
		file.putAll(["\n// Specs for the parameter snapshots of the scripts:\n",
			"~snapshots = [\n"
		]);
		this scriptsDo: { | script | script.saveSnapshots(file) };
		file.putChar($]);	// bug in SuperCollider client language parser?
		file.putString(";\n");
	}
	saveScriptSpecificData { | file |
		file putString: "\n// -------------- SCRIPT-SPECIFIC DATA: --------------\n";
		file putString: "~scriptData = [";
		this scriptsDo: _.saveData(file);
		file putString: "\n];";
		file putString: "\n// -------------- END SCRIPT-SPECIFIC DATA --------------\n";
	}
	scriptsDo { | func |
		scriptGroups.list do: { | sgroup, i |
			sgroup.list do: { | s, j |
				func.(s, i, j, sgroup)
			}
		}
	}
	printOn { | stream |
		super.printOn(stream);
		stream << ": " << name;
	}
	*showLoadString { | session |
		session = session ? { Session.current };
		Warn(format("Script.loadFromBrowser('%', '%'en)",
			session.script.folder, session.script.originalName),
			"To load this session, evaluate this:");
	}
}
