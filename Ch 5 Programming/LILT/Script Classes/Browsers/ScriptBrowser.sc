/* IZ 050723 {

Loads Scripts from folders. Optionally descends recursively into the subfolders
of a folder.  Provides gui for doing the following:

1 Loading a single script from file
2 Loading a folder of script files, including any files in subfolders of this folder
3 Selecting a script folder from a list of script folders and a script from a second list showing all
  scripts under the selected folder.
4 Editing the selected script on a SCTextView
5 Saving a script onto file and reloading it - all copies of this script in
  script windows should be updated
6 Saving a script onto another file name and updating the folder and script lists
7 Dragging the selected script from a drag source view to a drag sink on a script item
  in a script-patch window to load the selected script on the receiving script item
  (see class ScriptPatch).
8 Creating a new ScriptPatch on a new window
9 Loading a ScriptPatch from file and opening a window for it
10 reverting a scripts text to the version most recently saved on file
11 (Key command "e" on script list view): Opening a script in a document window for editing
	(must be resaved as Pasteboard type - not rtf - for text view of script to work

Design draft - gui will have following items:

--- Menu for actions 1, 2, 6, 8, 9 above 
Draft:
Load...		// load one or more selected scripts
Load Folder 	// load all scripts in selected folder and its subfolders
Save as...	// save script under new path and update list views

--- 2 list views for action 3 above (selecting folder and script)
--- 1 text view for action 4 above (editing script)

--- Button for (5) saving the edited script

--- Drag Source View: for (7) dragging current script onto an item on a script window

Arrangement of views on window:

-------------------------------------------------------------
menu  		|  drag view 	| save button 	| text edit view	|
folder list	| script list 	|			text edit view				|
-------------------------------------------------------------

ScriptBrowser.makeGui;
keyboard commands on script list:
	enter: add script to current script group of current Session

TODO: Debug subfolder loading mechanism to always keep up to date.
Rewrite the whole thing using ListModel for streamlined consistent 
list management and updating. 

} */

ScriptBrowser : Model {
// { Variables: 
	classvar <all;	// 2-level dict of script by folder name / script name
						// holds the paths of the scripts as values
	classvar <selectedFolder, <scriptPath;	// current selected  folder and script paths
	// gui:
	classvar <window;		// window of GUI for accessing all scripts
	classvar <scriptview; 	// text view holding the script for editing
	classvar <savebutton;	// save the edited script on file
	classvar <scriptlist;	// list of current scripts of current folder
	classvar <serverview;	// button for server boot/quit. Notified by me when server is running
	classvar <lastLoadedFolders;
	classvar <folderPaths;	// dictionary with full path for each folder name.
	classvar <currentFolderName; // this + folderPaths needed for adding scripts to empty folders.
// }
	*initClass {
		all = ();
		folderPaths = ();
		Post << "\t================ Lilt Installed ================
		To open a browser window list, evaluate this expression:\
			ScriptBrowser.startup;\n\t================================================\n";
	}
	*startup { | scriptFolder |
		var startupFolder, startupScript;
		this.loadFolder(scriptFolder); // load the default script folder
		// also load the folder scripts under ~/scwork/ if it exists: 
		if ("~/scwork/scripts/".pathMatch.size > 0) {
			this.loadFolder("~/scwork/scripts/");
		};
		startupFolder = all[\Startup];
		if (startupFolder.notNil) {
			startupScript = startupFolder['Current startup'];
			if (startupScript.notNil) { startupScript.loadPaths; };
		};
		this.makeGui;
	}
	*getScriptPath { | folder, file |
		// get the full path for script found in default scripts folder under folder, name
		^all[folder][file];
	}
	*reloadFolders {
		all = ();	// initialize all. Loose any other folders (!)
		this.loadFolders(lastLoadedFolders ?? { [this.getPath] });
	}
	*loadFolders { | paths |
		paths.do { |p| this.loadFolder(p.dirname ++ "/*") };
		this.changed;
	}
	*addFolder  { | path, loadSubfolders = true |
		// kludge: load + update - because built in update
		// presented some problems. 
		// This is the top level method for users to add their folders
		// TODO: Check behavior for loading SUBFOLDERS recursively
		// TODO: sort out the update mechanism to clear bugs and always
		// keep the lists up to date at any load mechanism.
		this.loadFolder(path, loadSubfolders);
		this.changed;
	}
	*loadFolder { | path, loadSubfolders = true |
		var selectedFolderEnvir, currentFolder;
		path = this.getPath(path);
		currentFolder = path.dirname.basename.asSymbol;
		// save full path of folder for reference, needed to access
		// when adding scripts to an empty folder. 
		folderPaths[currentFolder] = path;
		selectedFolderEnvir = ();
		path.pathMatch do: { | match |
			if (match.last != $/) {
				this.addScriptPath(match, selectedFolderEnvir);
			}{
				if (loadSubfolders) { this.loadFolder(match ++ "*") }
			}
		};
		all[currentFolder] = selectedFolderEnvir;
//		this.changed;
	}
	*getPath { | path |
		^path ?? {
			filenameSymbol.asString.dirname.dirname.dirname ++ "/Scripts/*";
		};
	}
	*getFolderEnvir { | folderSymbol |
		var envir;
		if ((envir = all[folderSymbol]).isNil) {
			all[folderSymbol] = envir = ();
		};
		^envir;
	}
	*addScriptPath { | path, folderEnvir, update = false |
		// Do not load the script itself, just store the path.
		var name;
		folderEnvir = folderEnvir ?? {
			this.getFolderEnvir(this.getFolderFromPath(path));
		};
		name = this.getNameFromPath(path);
		folderEnvir[name] = path;
		if (update) { this.changed };
	}
	// call these for consistency also in other Classes if needed
	*getNameFromPath { | path |
		^path.basename.splitext.first.asSymbol
	}
	*getFolderFromPath { | path |
		^path.dirname.basename.asSymbol
	}
	*makeGui {
		var menu, dragview, folderlist; // , sinkview;
		var adapter, script;
		if (window.notNil) {
			if (Server.default.serverRunning) {
				serverview.value = 2
			}{
				serverview.value = 0;
			}
			^window.front;
		};
		window = SCWindow("Script Browser-Editor", Rect(0, 0, 900, 400).fromTop);
		this.changed(\windowOpened);
		window.onClose = {
			this.changed(\windowClosed);
			window = nil;
		};
		menu = SCPopUpMenu(window, Rect(3, 3, 45, 20));
		menu.items = ["-Menu", "Load Scripts Folder ...", /* "Load Scripts ...", ,*/ "Save Script as ...",
			"Revert Script to saved version",
			"New Session ...", "New Script Group ... ", "(-", "Reload top folder",
			"Open SynthDefs Browser", "Open Buffers Browser", "Open Globals Browser",
			"Open Session Browser"];
		menu.action = { | me |
			[nil, { this.openFolderDialog }, /* { this.openScriptsDialog },*/
				{ this.saveScriptDialog; }, { this.revert },
				{ Session.newFromUser },
				{ Session.addScriptGroupFromUser }, {},
				{ { this.reloadFolders }.defer(0.1) }, { SynthDefs.makeGui },
				{ Samples.makeGui }, { Script.makeGlobalsGui }, { Session.makeGui }
			][me.value].value;
			me.value = 0;
		};
		menu.font_(Font("Helvetica-Bold", 10));
		serverview = SCButton(window, Rect(50, 3, 73, 20));
		serverview.states = [
			["Start Server", Color.blue, Color.white],
			["BOOTING", Color.gray, Color.yellow],
			["Stop Server", Color.red, Color.black]
		];
		serverview.action = { | me |
			[	{Server.default.quit},
				{Server.default.boot},
				{ "Server is booting".postln; me.value = 1 }
			][me.value].value;
//			if (me.value == 2) { Server.default.boot } { Server.default.quit }
		};
		serverview.onBoot({ {serverview.value = 2 }.defer; });
		serverview.onQuit({ { serverview.value = 0 }.defer; });
		serverview.onClose = {
			ServerWatcher.remove(serverview)
		};
		serverview.font = Font("Helvetica-Bold", 10);
		dragview = SCDragSource(window, Rect(126, 3, 128, 20))
			.background_(Color(0.9, 0.2, 0.6)).font_(Font("Helvetica", 10));
//		sinkview
		savebutton = SCButton(window, Rect(257, 3, 40, 20))
			.states_([["Save", Color.grey(0.4), Color.white],
				["Save", Color.red, Color.white]
				]).font_(Font("Helvetica-Bold", 10))
			.action_({ this.saveScript })
			.enabled_(false);
		folderlist = SCListView(window, Rect(3, 25, 145, 370))
			.resize_(4).font_(Font("Helvetica", 10));
		scriptlist = SCListView(window, Rect(151, 25, 145, 370))
			.resize_(4).font_(Font("Helvetica", 10));
		scriptview = SCTextView(window, Rect(298, 3, 600, 394))
			.resize_(5)
			.autohidesScrollers_(false)		// any other setting will result in
			.hasVerticalScroller_(true)		// glitch: when scroller disappears,
			.hasHorizontalScroller_(true)		// left column of text will be hidden
			.keyDownAction_({ |view, char, modifiers, unicode, keycode|
				switch (unicode,
					3, { this.saveScript; },
					{
						this.scriptChanged;
						view.defaultKeyDownAction(char,modifiers,unicode,keycode);
					}
				);
			});
////////////////////// now add the actions:
		adapter = { | who, how |
			if (how.isNil) {
				folderlist.items = all.keys.asArray.sort;
				folderlist.doAction;
			};
		};
		this.addDependant(adapter);
		folderlist.onClose = { this.removeDependant(adapter); };
		folderlist.action = {
			currentFolderName = folderlist.items[folderlist.value].asSymbol;
			selectedFolder = all[currentFolderName];
			scriptlist.items = selectedFolder.keys.asArray.sort;
			scriptlist.value =
				if (scriptPath.isNil) { 0
				}{
					scriptlist.items.indexOf(scriptPath.basename.splitext.first.asSymbol) ? 0;
				};
			scriptlist.doAction;
			scriptlist.focus(true);
		};
		scriptlist.action = {
			var name;
			scriptPath = selectedFolder[(name = scriptlist.items[scriptlist.value]).asSymbol];
			dragview.object = scriptPath;
			if (scriptPath.notNil) { dragview.string = name };
			this.setScriptViewContents;
		};
		scriptlist.keyDownAction = { | me, char, modifiers, unicode, keycode |
			switch (char,
				$e, { Script.edit(scriptPath); },
				$t, { Script.load(scriptPath).test },
				$T, { selectedFolder[scriptlist.items[scriptlist.value].asSymbol].load; },
				$g, {
					script = Script.load(scriptPath);
					Script.addGlobalScript(script.name.asSymbol, script);
				},
				$R, { "reloading Folders".postln; this.reloadFolders },
				3.asAscii, { // enter: add script to current scriptgroup
					if (unicode == 3) {
						Script.load(scriptPath).makeGui;
						window.front; /*me.focus(true);*/
						}
				},
				{ me.defaultKeyDownAction(char,modifiers,unicode,keycode); }
			);
		};

		window.front;
		this.changed;
	}
	*closeGui {
		if (window.notNil) { window.close }
	}
	*scriptChanged {
		// Script text and can be saved. Mark save button red and enable it
		savebutton.value_(1).enabled = true;
	}
	*setScriptViewContents { | string |
		scriptview.setString("", 0, 50000);
		scriptview.string = string ?? { this.getScript(scriptPath) };
		// forcing update of scrollers to prevent scrol bar wrong size glitch:
		scriptview
			.autohidesScrollers_(false)		// any other setting will result in
			.hasVerticalScroller_(false)		// glitch: when scroller disappears,
			.hasHorizontalScroller_(false);	// left column of text will be hidden
		scriptview.refresh;
		savebutton.value_(0).enabled_(false);
		{	scriptview
				.autohidesScrollers_(false)		// any other setting will result in
				.hasVerticalScroller_(true)		// glitch: when scroller disappears,
				.hasHorizontalScroller_(true);	// left column of text will be hidden
		}.defer(0.1);
	}
	*getScript { | path |
		var file, result;
		if (path.isNil) { ^" ( no script selected ) " };
		if (File.exists(path).not) {
			^"/* (no script file) */";
		};
		file = File(path, "r");
		result = file.readAllStringRTF;
		file.close;
		^result;
	}
	*openFolderDialog {
		CocoaDialog.getPaths({ | paths |
			lastLoadedFolders = paths;
			this.loadFolders(paths);
		}, {
			"load cancelled".postln;
		});
	}
	*openScriptsDialog {
		CocoaDialog.getPaths({ | paths |
			paths.do { |p| this.addScriptPath(p) };
			this.changed;
		}, {
			"load cancelled".postln;
		});
	}
	*open { | path, folderEnvir, update = false |
		var file, name, script;
		path = path ? scriptPath;
		folderEnvir = folderEnvir ?? {
			this.getFolderEnvir(path.dirname.basename.asSymbol);
		};
		Post << "Loading script: " << (name = path.basename.splitext.first) << "\n";
		file = File(path, "r");
		script = file.readAllString;
		file.close;
//		script.postln;
//		if (scriptview.notNil) { scriptview.string = script; };
		folderEnvir[name.asSymbol] = path;
		if (update) { this.changed }
	}
	*revert {
		// revert to script as saved on disc, from current selected script path
		this.open;
		scriptlist.value = scriptlist.items.indexOf(
			scriptPath.basename.splitext.first.asSymbol;
		);
		scriptlist.doAction;
	}
	*saveScriptDialog {
		CocoaDialog.savePanel({ | path |
			scriptPath = path;
			this.saveScript;
			this.open(path, update: true);
		}, {
			"save cancelled".postln;
		});
	}
	*saveScript {
		var file;
		if (scriptPath.isNil) { ^"No script path to save".warn };
		postln("Saving: " ++ scriptPath);
		file = File(scriptPath, "w");
		file.putAll(scriptview.string);
		file.close;
		// needed in case we are saving a script generated from SynthDefs:
		this.addScriptPath(scriptPath);
		scriptlist.doAction; // update dragview object
		savebutton.value_(0).enabled = false;
		scriptlist.focus(true);
	}
	*openGroupDialog {

	}
	*addScript { | script, name |
		/* create new script from text generated by SynthDefs from a SynthDesc/SynthDef
			construct path from currently chosen folder */
		if (scriptPath.isNil) {
//			^thisMethod.report(folderPaths[currentFolderName])
			scriptPath = folderPaths[currentFolderName];
		};
		scriptPath = scriptPath.dirname ++ "/" ++ name.asString;
		selectedFolder[name.asSymbol] = scriptPath;
		this.changed;
		this.setScriptViewContents(script);
		this.scriptChanged;
	}
	*at { | foldername, scriptname |
		var folder, script;
		folder = all[foldername.asSymbol];
		if (folder.notNil) {
			script = folder[scriptname.asSymbol];
		};
		if (script.notNil ) {
			^Script.load(script);
		}{
			Post << "Script " << foldername << " " << scriptname << " not found\n";
			^nil
		}
	}
	*loadPath { | foldername, scriptname |
		// Load the code of <scriptname> at folder <foldername> as sc code (not as script)
			all[foldername.asSymbol][scriptname.asSymbol].asString.loadPath;
	}

}
