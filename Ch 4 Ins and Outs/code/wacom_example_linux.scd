// =========================================
// Wacom tablet example
// =========================================

// Note: this example only works on Linux. The OSX Wacom drivers unfortunately do not give you access to the HID layer of the Wacom device. Look at the osx example for how to do the same thing with the GUI utilities.)

// find the Wacom tablet:
GeneralHID.buildDeviceList;
d = GeneralHID.deviceList;
GeneralHID.postDevices;
// suppose this is the tablet, so open it (mine is a Wacom PenPartner2, so I look for that name in the device list. The first entry in the array that is posted on each line is the index into the deviceList)
w = GeneralHID.open( d[6] );
// check whether it was the right one:
w.info;
// get which args we could use to automatically find the tablet in future sessions:
w.info.findArgs;
// usually the first two are sufficient to get the device (the others are version numbers, or are related to which USB port the device is connected to)
// So in future sessions we can open the device with:
w = GeneralHID.open( GeneralHID.findBy( 1386, 100 ) );

// grab it, so we don't fuck up our mouse (linux only):
w.grab;
// create a GUI, so we can see whether it works:
GeneralHIDDeviceGUI.new( w );
// or:
w.makeGui;

// we can also use:
w.debug = true;
w.debug = false;

// my PenPartner2 seems to have several things which are defined, but do not seem to do anything. Probably the manufacturer uses the same firmware for several devices, but doesn't hook up the same hardware functions to all of them.

// create names for different functions (look at the gui to see which action causes which buttons or sliders to move)
(
w.add( \x, [3,0]);
w.add( \y, [3,1]);
w.add( \pressure, [3,24]);
w.add( \topbutton, [1,331]);
w.add( \botbutton, [1,332]);
w.add( \inrange, [1,320]);
w.add( \touch, [1,330] );
);

// view the spec:
w.spec.map

// save the spec:
w.spec.save( "WacomExample");

// now we can find it:
w.findSpec;

// so in future session we can reload it with:
w.setSpec( w.findSpec.first );
w.spec.map

s = Server.local.boot;

// we use q as our holder for synths
q = ();

// create buses for the defined slots:
(
w.createAllBuses( s );
)

// create actions for two of the "buttons"
(
q.active = false;
// when the pen hits the tablet, spawn a synth and map the buses:
w[\touch].action_( { |v| 
	if ( v.value == 1 and: q.active.not, {
		q.active = true;
		q.synthi = Synth.new( \wacotheremin ); 
		q.synthi.map( \freq, w[\y].bus );
		q.synthi.map( \mod, w[\x].bus );
		q.synthi.map( \amp, w[\pressure].bus );
		q.synthi.map( \room, w[\x].bus );
		q.synthi.map( \damp, w[\y].bus );
		q.synthi.map( \roomset, w[\topbutton].bus );
		q.synthi.map( \dampset, w[\botbutton].bus );

	});
});
// when the pen leaves the sensitive range, free the synth:
w[\inrange].action_( { |v| 
	if ( v.value == 0 and: q.active, {
		q.synthi.free;
		q.active = false;
	});
});
)

/// and here's the synth:

( // freely derived from the Theremin example:
SynthDef(\wacotheremin, { arg freq=0, amp=1, mod = 7, detune = 0, roomset=0, dampset=0, room=0.5, damp=0.2;
	var f, a, z;
	f = [4000, 200, 'exponential'].asSpec.map( freq.lag(0.1) )*0.8 + detune;
	a = SinOsc.ar(f + (f * SinOsc.ar( [2,20].asSpec.map(mod.lag(0.1)),0,0.02)), mul: amp.lag(0.1));
	z = FreeVerb.ar( Mix.ar(a), 0.5, Gate.kr( room.lag(0.1), roomset ), Gate.kr( damp.lag(0.1), dampset ) );
	Out.ar(0, z) + Out.ar(1, z)
}).add;
)

// the frequency and modulation are modulated with x and y, the pressure controls the amplitude
// the room and damp of the reverb are only changed when the buttons are pushed.


// cleanup

w.freeAllBuses;
w.close;
