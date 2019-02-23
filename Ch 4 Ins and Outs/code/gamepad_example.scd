// Ins and Outs - Gamepad example
// This example was made with an Impact Gamepad, but can easily be adapted to work with gamepads of other vendors.


// General structure to access a device
// Look for the devices that are attached:
GeneralHID.buildDeviceList;
// Get the list of devices:
d = GeneralHID.deviceList;
// Check which devices have been found:
GeneralHID.postDevices;

// The impact has vendor number 1973, so look for it:
a = GeneralHID.findBy( 1973 );
// If you have a different gamepad, just look in the list that is posted. The first entry in the arrays that are posted is the index into the list, so if your device shows up as [ 5, device description ], you can do:
a = d[5];


// Pick the right device and open it and create an instance of it:
a = GeneralHID.open( a );
// Get info on the device (double check whether you have the right one):
a.info;
// if you did not know the vendorID and product yet, you can use
a.info.findArgs;
// to find out which args we could use to automatically find the gamepad in future sessions:


// Start eventloop:
GeneralHID.startEventLoop
// Get the capabilities of the device in a readable format:
a.caps;
// See if data is coming in:
a.debug_( true );
// Stop it:
a.debug_( false );

// find the joysticks

// on OSX:
a.slots[3][48].debug_(true); // right x
a.slots[3][48].debug_(false); // right x

a.slots[3][49].debug_(true); // right y
a.slots[3][49].debug_(false); // right y

a.slots[3][50].debug_(true); // left y
a.slots[3][50].debug_(false); // left y

a.slots[3][53].debug_(true); // left x
a.slots[3][53].debug_(false); // left x

// on linux:
a.slots[3][0].debug_(true); // left x
a.slots[3][0].debug_(false); // left x

a.slots[3][1].debug_(true); // left y
a.slots[3][1].debug_(false); // left y

a.slots[3][2].debug_(true); // right x
a.slots[3][2].debug_(false); // right x

a.slots[3][5].debug_(true); // right y
a.slots[3][5].debug_(false); // right y

/// We could also use a GUI:
a.makeGui;

// Note the Impact Gamepad has a button in the middle (above the LED), which changes the function of the arrow button on the left from being an alternative to the left joystick axes, or its own two axes.

// Now that we know which slot is which, we can name them:

( // using the OSX mapping
a.add( \lx, [3,48]);
a.add( \ly, [3,49]);
a.add( \rx, [3,50]);
a.add( \ry, [3,53]);
// buttons
(1..8).do{ |it,i| a.add( (i+1).asSymbol, [1,it]) }
)

( // using the linux mapping
a.add( \lx, [3,0]);
a.add( \ly, [3,1]);
a.add( \rx, [3,2]);
a.add( \ry, [3,5]);
a.add( \hatx, [3,16] );
a.add( \haty, [3,17] );
// buttons
(288..299).do{ |it,i| a.add( (i+1).asSymbol, [1,it]) }
)



// view the spec:
a.spec.map

// save the spec:
a.spec.save( "GamepadExample");

// now we can find it:
b = a.findSpec;

// so in future session we can reload it with:
a.setSpec( b[0] );

// Now that we have given the slots symbolic names, there is no difference anymore for using the HID device between OSX and Linux.

// The impact gamepad has two joysticks and a whole lot of buttons.
// In this example we will create a setup, where we can record movements of the joysticks, which are then used as wavetable for a synthesizer.

// on OSX, use the internal server:
s = Server.internal.boot;
// otherwise, use the local server (and SwingOSC)
s = Server.local.boot;

// GUI's
s.makeGui;
s.scope( 2 );

(
// synth to write the waveform to a buffer:
SynthDef( \wavewriteb2, { |input=0,buffer=0,dur=5,speed=1,offset=0.5,mul=1,gate=0|
	EnvGen.kr( Env.sine, gate, timeScale: dur ) *
	BufWr.kr( In.kr( input, 1 ) * mul - offset, buffer, Phasor.kr( gate, speed, 0, BufFrames.kr(buffer)), 0 );
}).send(s);
// synth to play back the waveform:
SynthDef( \waveplayb, { |buffer=0, speed=0.5, out=0, amp=1, vol = 0.5|
	Out.ar( out,
		vol * amp * BufRd.ar( 1, buffer, LFSaw.ar(BufDur.ir(buffer).reciprocal * speed * 2).range(0, BufFrames.ir(buffer)) )
		);
	}).add;
)

(
// create busses for each of the joystick axes:
[\rx,\ry,\ly,\lx].do{ |key| a.at( key ).createBus( s ); };
// create busses for eight of the buttons:
(1..8).do{ |id| a.at( id.asSymbol ).createBus( s ); };
)

(
// assign 4 Buffers for four wavetables:
~buffers = 4.collect{ Buffer.alloc( s, 4096, 1 ); };
)

( // create a window to view the buffer contents:
w = Window.new( "Gamepad Buffer play" );
~bufwins = 4.collect{ |i| CompositeView.new( w, Rect( 0, 100*i, w.bounds.width, 100 ) ) };
w.front;

Tdef( \updateplot, {
	loop {
		~bufwins.do{ |it| it.children.do{ |jt| jt.remove; } };
		~buffers.do{ |it,i| it.plot( "buffer "++i, parent: ~bufwins[i] ) };
		1.0.wait;
		}
	});
)


( // start the synths and window update routine:
Tdef( \updateplot ).play;
~writers = [ \lx, \ly, \lx, \ly ].collect{ |it,i|
	Synth.new( \wavewriteb2, [ \input, a.at(it).bus, \buffer, ~buffers[i], \offset, 1, \mul, 2 ], s ).busMap( \speed, a.at( (i+1).asSymbol).bus ).busMap( \gate, a.at( (i+1).asSymbol).bus );
	};
~players = [\rx, \rx, \ry, \ry ].collect{ |it,i|
	Synth.new( \waveplayb, [ \buffer, ~buffers[i], \out, (i/2).floor ], s).busMap( \amp, a.at( (i+5).asSymbol).bus ).busMap( \speed, a[it].bus );
	};
)


// Buttons 1 to 4 select to which buffer is recorded
// Buttons 5 to 8 (on the front), select which buffer is played back
// Left joystick determines recording
// Right joystick determines playback speed

// change volume:
~players.do{ |it| it.set( \vol, 0.5 ) };


// clean up:

(
~writers.do{ |it| it.free };
~players.do{ |it| it.free };

~buffers.do{ |it| it.free };

Tdef( \updateplot ).stop;

// free all the busses for each of the joystick axes:
a.freeAllBuses;

// Close the device after use:
a.close;
// stop the eventloop;
GeneralHID.stopEventLoop

)