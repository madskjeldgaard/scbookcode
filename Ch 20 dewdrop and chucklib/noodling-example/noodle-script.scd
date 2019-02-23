//////// Algorithmic Noodling - H. James Harkins
//////// Performance script

//////// INITIALIZATION

// where am I on the hard disk?
// Windows users: See noodle-readme.html
~root = PathName(thisProcess.nowExecutingPath).pathOnly;

// open gui if desired
// this has some MIDI dependencies so if MIDI doesn't work in your system, omit this
(~root ++ "support/noodle-gui.scd").loadPath;

// loading the gui takes a few seconds
// after gui is finished loading, run this to move this window to a better location
BP.openCodeDoc(~root ++ "noodle-script.scd");

// if you are NOT using the gui, make sure to start the server
s.boot;

// run initialization
(~root ++ "noodle-init.scd").loadPath;

~noodle.push;

~hasGui = VP.exists(0);

// only if you have the GUI - you can put the things in the mixingboard
~hasGui.if({ ~master => MCG(7) });

// load drums
(
Fact(\kik) => BP(\kik);
Fact(\stick) => BP(\stick);

~hasGui.if({
	BP(\kik) => MT(1);
	BP(\stick) => MT(1);
	BP(\kik) => MCG(3);
	BP(\stick) => MCG(4);
});
)

// play both at once
BP(#[stick, kik]).play;


// load bass voicer and process
(
Fact(\bass_v) => VC(\bass);
Fact(\roots) => BP(\roots);
Fact(\basslink) => BP(\basslink);
Fact(\bass) => BP(\bass);
BP(\bass) => VC(\bass);

~hasGui.if({
	VC(\bass) => VP(0);
	BP(\roots) => MT(1);
	VC(\bass) => MCG(0);
});
)

BP(\roots).play;


// load pad
(
Fact(\pad_v) => VC(\pad);
Fact(\pad) => BP(\pad);
BP(\pad) => VC(\pad);

~hasGui.if({
	VC(\pad) => VP(1);
	BP(\pad) => MT(1);
	VC(\pad) => MCG(1);
});
)

BP(\pad).play;

// load jumpy
(
Fact(\jumpy_v) => VC(\jumpy);
Fact(\jumpy) => BP(\jumpy);
BP(\jumpy) => VC(\jumpy);

~hasGui.if({
	VC(\jumpy) => VP(2);
	BP(\jumpy) => MT(1);
	VC(\jumpy) => MCG(2);
});
)

// play with fadein
VC(\jumpy).env.target.level = 0;
BP(\jumpy).play;

VC(\jumpy).env.target.levelTo(-14.dbamp, 20);


/***
Here, you can play with starting and stopping processes.
With the GUI, you have more options for realtime tweaking.
	- You can change the mix.
	- A couple of synth parameters are exposed in the Voicer GUIs at right.
Or just sit back and listen.
***/


// fade out jumpy
VC(\jumpy).env.target.levelTo(0, 20);

// when done, stop it
BP(\jumpy).stop;

// fade out bass, but keep it playing to drive the chord changes
VC(\bass).env.target.levelTo(0, 20);

// (to save cpu, after bass is silent, you can suppress its synths by setting the process voicer to nil)
BP(\bass).event.voicer = nil;


// fade it all out with master
~master.levelTo(0, 40);

BP.all.stop;


// wipe everything for this track only out of the workspace
// if you're using the GUI, lots of things will disappear!
AbstractChuckArray.freeTypeAll(\noodle);

// reverb still exists because it didn't belong to a chucklib object
Event.pop;
~noodle.cleanup;

// only ~master MixerChannel is left
~master.free;
