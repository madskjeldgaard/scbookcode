
~script routine: {
	CocoaDialog.getPaths({ | path |
			path = path.first.dirname;
			Session.current.addScriptGroup(path.basename.asSymbol);
			(path ++ "/*").pathMatch do: { | f |
				if (f.last != $/) { Script.load(f) }
			};
		},
		{ "Session loading from folder cancelled".postln }
	);
	1.wait; // go back to stopped after 1 second
};
