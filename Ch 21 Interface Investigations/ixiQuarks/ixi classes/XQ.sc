
// XQ is the storage for all environmental variables for ixiQuarks
// it used to be kept in envir vars (~) but because of ProxySpace I put it in a storage class

/*
Testing:
XQ.globalBufferDict
XQ.globalWidgetList
XQ.bufferPoolNum
XQ.pref
XQ.pref.midi
*/

XQ {
	classvar <>globalWidgetList, <>globalBufferDict, <>bufferPoolNum;
	classvar <>pref;
	
	*new{
		^super.new.initXQ;
	}
	
	initXQ{
		var soundsfolderpath, preferencesfolderpath;
		globalWidgetList = List.new; // keep track of active widgets
		// (contains [List [buffers], [selstart, sellength]])
		globalBufferDict = (); 
		bufferPoolNum = -1;
		// check if there is a sounds folder
		soundsfolderpath = String.scDir++"/sounds/ixiquarks";
		if(soundsfolderpath.pathMatch==[], {
			("mkdir -p" + soundsfolderpath.quote).unixCmd;
			"ixi-NOTE: an ixiquarks soundfolder was not found, it was created in sounds".postln;
		});
		preferencesfolderpath = String.scDir++"/preferences";
		if(preferencesfolderpath.pathMatch==[], {
			("mkdir -p" + preferencesfolderpath.quote).unixCmd;
			"ixi-NOTE: an ixiquarks preferences folder was not found, it was created".postln;
		});

	}

	*preferences {
		var prefFile, preferences;
		try{
			prefFile = File("preferences/preferences.ixi", "r");
			preferences = prefFile.readAllString;
			preferences.interpret;
		} {

		"ixi-NOTE: you don't have the preferences.ixi file installed! Check ixiQuarks help".postln;
			this.pref = ()
				.emailSent_(true) // change to true when you have sent ixi an email
				.numberOfChannels_(52) // number of audio channels used
				.polyMachineTracks_(6) // how many tracks in polymachine (4 is default)
				.bufferPlayerTracks_(16) // tracks in BufferPlayer (8, 16, 24 and 32 being ideal)
				.midi_(false) // using midi or not?
				.midiControllerNumbers_( [73, 72, 91, 93, 74, 71, 5, 84, 7] ) // evolution mk-449c
				.midiControllerNumbers_( [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14] ) // doepfer pocket 
				.midiRotateWindowChannel_(15) // the controller number to switch between windows
				.midiInPorts_( 2 ) // how many inports you are using
				.midiOutPorts_( 2 ); // how many outports - not used really yet
		}
	}

}