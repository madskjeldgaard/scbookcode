/* (IZ 2005-08-15)

To install Lilt, place the Lilt folder inside the SCClassLibrary folder and recompile SuperCollider (start the SuperCollider application or, if it is already running, type Command-K). When SuperCollider compiles with the Lilt folder installed, it will post the following message to the Untitled window: 

	================ Lilt Installed ================
		To open a browser window list, evaluate this expression:
			ScriptBrowser.startup;
	================================================

Place the cursor on the above line (ScriptBrowser.startup;) and press the [Enter] key. This will open the Script browser. 

The Lilt/Script library also per default loads to the local server SynthDefs that are used by examples in this documentation. The SynthDefs are loaded by the *initClass method of  the SynthDefs class, from the Lilt/SynthDefs folder. If you have installed the library according to the instructions, all examples should work. 

IMPORTANT: Lilt/Script's MIDI binding mechanism uses MIDIResponder classes which are in crucial library. The crucial library is part of the standard release of SC3. If you remove the crucial library, then you must add the MIDIResponder.sc file back to SCClassLibrary to include these classes for MIDI binding to work. 

Lilt does not modify methods defined by the standard SCClassLibrary except for the following 1 method, 
	EventStreamPlayer:next
It is therefore compatible with the standard release of SC3. 

EventStreamPlayer-next has an additional statement notifying all dependants
when the stream has ended:
			if (stream.notNil) {
				streamHasEnded = true;
				// notify dependant objects that stream has ended. 
				this.changed(\stopped);
			};
This is needed to notify the script when a Pbind or Ppar pattern have ended, so as 
to update the state of a Script and its GUI when the top-level pattern played ends.

Some methods have been added to existing classes. 

*/
