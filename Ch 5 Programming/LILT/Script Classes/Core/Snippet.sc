/* IZ 2007-06-19 { SC3
Holds a function that is applied to a Script. Offers features for editing, gui, saving in a session, MIDI bindings.
} */

Snippet {
	var <script, <name, <actionCode, <action, <midiResponder;
	*new { | script, name = "snippet", actionCode = " " |
		^this.newCopyArgs(script, name, actionCode).init;
	}
	init {
		action = actionCode.interpret;
		// create unique name for snippet and store it in scripts envir var snippets
		this.name = name;
	}
	name_ { | argName = "snippet"|
		// create unique name if needed, store this under its name in 
		// script.envir[snippets] event. 
		var snippets;
		snippets = script.envir[\snippets];
		if (snippets.isNil) { script.envir[\snippets] = snippets = () };
		snippets.removeAt(name);
		snippets[name = snippets.keys.asArray.makeUniqueName(argName)] = this;
	}
	run { | ... args |
		action.(script, *args);
	}
//	makeGui {}
}