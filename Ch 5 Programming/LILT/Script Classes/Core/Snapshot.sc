/* IZ 2007-01-26 { SC3
Stores the states of all parameters of a Script in a List as a "snapshot". It adds the capability of binding a MIDI command to load a Snapshot instance to its script. 
It is made as a subclass of List for simplicity in use with ListModel and Script. 
The structure of the Array inside a Snapshots array variable (inherited from List) is: 
	[<name of parameter>, [v1, v2, ... vn]] 
where v1, v2 ... vn are the values of the parameters taken by the snapshot. 
To implement the MIDI binding capability, the following 2 variables are added: 

midiResponder: the MIDIresponder which can be activated to load this snapshot via MIDI command.
script: the Script that will load this snapshot when the MIDI command is received. 

MIDI responder save / load implementation is under construction.

} */

Snapshot : List {
	var <>script;		 // Script that will load this snapshot when the MIDI command is received. 
	var <>midiResponder; // MIDIresponder which can be activated to load this snapshot via MIDI command

	*fromScriptData { | script, name, paramValues, responderSpec |
		^this.newUsing([name, paramValues]).init(script, responderSpec);
	}
	init { | argScript, argResponderSpec |
		script = argScript;
		if (argResponderSpec.notNil) { this.makeResponder(argResponderSpec) };
	}
	makeResponder { | argResponderSpec |

	}
	writeSnapshot { | file |
		file putAll: ["\t", this.asScriptData.asCompileString, ",\n"];
	}
	asScriptData {
		if (midiResponder.isNil) {
			^array;
		}{
			^array add: midiResponder.asScriptString;
		}
	}
	// TODO: Rewrite using MIDIHandler
	activateMIDI {
		if (midiResponder.notNil) { midiResponder.remove }
	}
	deactivateMIDI {
		if (midiResponder.notNil) { midiResponder.add }
	}
}
