/* IZ 050511

Load synthdefs from SC code to Local Server at startup.  Provide GUI tools for browsing, and editing of SynthDef code files and for automatic generation of Script code from a loaded SynthDef. 

SynthDefs.makeGui;

*/

SynthDefs { /* : Model */
	classvar <all;	// will replace old all variable when new version is done
	classvar <selectedFolder, <selectedDef;
	classvar <paths;
	classvar <foldersview, <listview, <dragview;
	classvar <currentScript; 	// cached script from synthdef - for making new Script
	// gui:
	classvar <window;	// window of GUI for accessing all synthdefs

	*initClass {
		// add some default to Spec
		Class.initClassTree(ControlSpec);
		Class.initClassTree(TrigSpec);	// don't let that change your gate and trig specs:
		ControlSpec.specs.addAll([
			\bufnum -> ControlSpec(0, 1023, \linear, 1, 0),
			\trigger -> ControlSpec(0, 1, \linear, 1, 1),
			\loop -> ControlSpec(0, 1, \linear, 1, 1),
			\trigAt -> ControlSpec(0, 1, \linear, 0, 1),
			\trigID -> ControlSpec(0, 1000, \linear, 1, 0),
			\in ->  ControlSpec(0, 127, \linear, 1, 0),
			\out -> ControlSpec(0, 4095, \linear, 1, 0),
			// overwriting \gate, \trig - sorry Crucial. Crucial does not care about
			// step anyway as far as I could gather so this would not bother his lib:
			\gate -> TrigSpec(0, 1, \linear, 1, 0), // only change: Step = 1, not 0
			\trig -> TrigSpec(0, 1, \linear, 1, 0), // only change: Step = 1, not 0
			\pulse -> ControlSpec(0.1, 30, \exponential, 0, 1),
			\attack -> ControlSpec(0.001, 5, \exponential, 0, 0.01),
			\decay -> ControlSpec(0.001, 5, \exponential, 0, 0.25),
			\vibfreq -> ControlSpec(0.00001, 30, \exponential, 0, 7),
			\vibamp -> ControlSpec(0.0, 1000, \linear, 0, 20),
			\pos -> ControlSpec(-1.0, 1.0, \linear, 0, 0)
		]);
		all = ();
		paths = ();
		Class.initClassTree(Server); 	// need to know default server
		Post << "Loading SynthDefs ...\n";
		this.loadFolder;				// load defs from ../SynthDefs/*
		"\n... finished loading.".postln;
	}

	*openFolderDialog {
		CocoaDialog.getPaths({ | paths |
			paths.do { |p| this.loadFolder(p.dirname ++ "/*") }
		}, {
			"load cancelled".postln;
		});
	}

	*loadFolder { | path, loadSubfolders = true |
		var match, selectedFolderEnvir;
		path = path ?? {
			filenameSymbol.asString.dirname.dirname.dirname ++ "/SynthDefs/*";
		};
//		Post << "\nLoading SynthDefs from: " << path << "\n";
		selectedFolder = path.dirname.basename.asSymbol;
//		Post << "Loading SynthDefs from folder: " << selectedFolder << " ... ";
		selectedFolderEnvir = this.getFolderEnvir(selectedFolder);
		match  = (path.dirname ++ "/*").pathMatch;
		match do: { |m| if (m.last != $/) { this.open(m); } };
		if (loadSubfolders) { this.loadSubfolders(path) };
//		"done loading SynthDefs.".postln;
		this.changed;
	}

	*getFolderEnvir { | folderSymbol |
		var envir;
		if ((envir = all[folderSymbol]).isNil) {
			all[folderSymbol] = envir = ();
		};
		^envir;
	}

	*loadSubfolders { | path |
		var match;
		path = path ?? {
			filenameSymbol.asString.dirname.dirname ++ "/SynthDefs/*";
		};
//		Post << "Loading subfolders\n";
		match  = (path.dirname ++ "/*").pathMatch;
		match do: { |m|
				if (m.last == $/) {
				this.loadFolder(m ++ "*");
			}
		};
		this.changed;
	}

	*open { | path, folderEnvir, update = false |
		var defs;
		Post << " " << path.basename.splitext.first << " ";
		if (folderEnvir.isNil) {
			folderEnvir = this.getFolderEnvir(path.dirname.basename.asSymbol);
		};
		defs = thisProcess.interpreter.executeFile(path);
		defs.asArray do: { | d |
//			Post << d.name << " * ";
			d.load(Server.local);
			SynthDesc.read((SynthDef.synthDefDir ++ d.name ++ ".scsyndef"),
				true, folderEnvir);
			paths[d.name.asSymbol] = path;
		};
		if (update) { this.changed }
	}

	*makeGui {
		var controlsView, editButton;
		if (window.notNil) { ^window.front };
		window = SCWindow("SynthDefs", Rect(280, 0, 465, 330).fromRight);
		window.view.decorator = FlowLayout(window.view.bounds, 3@3, 2@2);
		window.onClose = {
			this.changed(\windowClosed);
			window = nil; // this.release;
		};
		dragview = SCDragSource(window, Rect(0, 0, 120, 20))
			.background_(Color(0.19, 0.58, 0.85))
			.font_(Font("Helvetica", 10));
		SCStaticText(window, Rect(0, 0, 300, 20))
			.font_(Font("Helvetica", 11))
			.string_(
		"\"e\" = edit, \"r\" = reload \"t\" = test, \"s\" = add script from synthdef"
			).align_(\center)
			.stringColor_(Color.red);
		foldersview = SCListView(window, Rect(0,0,120, 300))
			.resize_(4)
			.font_(Font("Helvetica", 11));
		listview = SCListView(window, Rect(0,0,120, 300))
			.resize_(4)
			.font_(Font("Helvetica", 11));
		controlsView = SCTextView(window, Rect(0,0, 215, 300))
			.font_(Font("Helvetica", 11))
			.autohidesScrollers_(false)		// any other setting will result in
			.hasVerticalScroller_(true)		// glitch: when scroller disappears,
			.hasHorizontalScroller_(true)	// left column of text will be hidden
			.resize_(5);
		foldersview.action = {
			selectedFolder = all[foldersview.items[foldersview.value].asSymbol];
			listview.items = selectedFolder.keys.asArray.sort;
			listview.value = 0;
			listview.doAction;
			listview.focus(true);
		};
		listview.action = {
			/* to print the controls in order we have to use the controlnames var */
			selectedDef = selectedFolder[listview.items[listview.value].asSymbol];
			dragview.object = selectedDef;
			if (selectedDef.notNil) { dragview.string = selectedDef.name };
			controlsView.editable_(true);
			controlsView.setString("", 0, 10000);
			controlsView.string = this.defaultControlSpecs(selectedDef);
			controlsView.editable_(false);
		};
		listview.keyDownAction = { | me, char, modifiers, unicode, keycode |
			var doc;
			switch (char,
				$e, {
					doc = Document.open(paths[listview.items[listview.value].asSymbol]);
					if (doc.path.basename.splitext.last != "rtf") {
						doc.setFont(Font("Monaco", 9), -1).syntaxColorize;
					};
				},
				$r, { this.open(paths[listview.items[listview.value].asSymbol], nil, true) },
				$t, { Synth(listview.items[listview.value]) },
				$s, { ScriptBrowser.addScript(
					"// (SC3:) Generated from SynthDesc on: "
						++ Date.getDate ++ "\n\n" ++ currentScript,
					listview.items[listview.value])
				},
				{ me.defaultKeyDownAction(char,modifiers,unicode,keycode); }
			);
		};
		this.addDependant {
			foldersview.items = all.keys.asArray.sort;
			foldersview.doAction;
			foldersview.refresh;
		};
		this.changed(\windowOpened);
		window.front;
	}
	*closeGui { if (window.notNil) { window.close } }

	*defaultControlSpecs { | desc |
		var ctls, ctl, spec;
		if (desc.isNil) {
			currentScript = nil;
			^"(No synthdef selected)";
		}{
			^this.makeSpecs(desc);
		}
	}

	*makeSpecs { | desc |
		var ctls, specs, size;
		ctls = desc.controlNames.collect { | cn |
			desc.controls.detect({ | c | c.name == cn });
		};
		size = ctls.size - 1;
		specs = this.makeControlSpecs(desc, ctls, size);
		currentScript = specs ++ this.makeStartFunc(desc, ctls, size);
		^specs;

	}

	*makeControlSpecs { | desc, ctls, size |
		var ctl, spec;
		^String.streamContents { | stream |
			stream << "~controlSpecs = [";
			ctls.do { | ctl, i |
				spec = ctl.name.asSymbol.asSpec ?? {
					ControlSpec(0, (ctl.defaultValue * 2).max(1),
						\linear, 0, ctl.defaultValue);
				};
				spec.default = ctl.defaultValue;
				stream << "\n" <<< ([ctl.name.asSymbol] ++ spec.storeArgs[0..4].round(0.00001));
				if (i < size) { stream << ","; };
			};
			stream << "\n];\n";
		}
	}

	*makeStartFunc { | desc, ctls, size |
		^String.streamContents { | stream |
			stream << "\n~start = { Synth(" <<< desc.name;
			if (ctls.size > 0) {
				stream << ", [";
				ctls.do { |c, i|
					stream <<< c.name.asSymbol << ", ~" << c.name;
					if (i < size) { stream << ", " };
				};
				stream << "]";
			};
			stream <<
			",\n\t~target, #[\\h, \\t, \\addBefore, \\addAfter, \\addReplace][~addAction]) };\n"
		}
	}

	// enable access by SynthDefs[folderName, defName];
	add { | folderName, defName |
		var folder;
		folder = all[folderName];
		if (folder.isNil) { ^nil };
		if (defName.isNil) { ^folder };
		^folder[defName];
	}

}

