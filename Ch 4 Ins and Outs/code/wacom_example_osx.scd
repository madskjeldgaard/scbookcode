// =========================================
// Wacom tablet example
// =========================================

(
// Unfortunately, OS X does allow to use a graphics-tablet with HID.
// So, here you find the HID example adapted to use a tablet-enabled GUI element (TabletSlider2D)

// The frequency and modulation are modulated with x and y, the pressure controls the amplitude.
// The room and damp of the reverb are only changed when pen is tilted to a positive amount towards  x resp. y-axises. You may want to adapt this behavior according to your specific tablet.

w = Window.new;
t = TabletSlider2D(w,Rect(40,40,300,300));
t.background = Color.white;
w.front;

// we use q as our holder for synths
q = ();
q.active = false;

t.mouseDownAction = { arg view,x,y,pressure,tiltx,tilty,deviceID, buttonNumber,clickCount,absoluteZ,rotation;
	if ( q.active.not, {
		q.active = true;
		q.synthi = Synth.new( \wacotheremin ); 

	});
	view.action.value(view,x,y,pressure,tiltx,tilty,deviceID, buttonNumber,clickCount,absoluteZ,rotation)
};

t.action = { arg view,x,y,pressure,tiltx,tilty,deviceID, buttonNumber,clickCount,absoluteZ,rotation;
	q.synthi.set( \freq, y);
	q.synthi.set( \mod, x );
	q.synthi.set( \amp, pressure );
	q.synthi.set( \room, x );
	q.synthi.set( \damp, y );
	q.synthi.set( \roomset, (tiltx > 0.1).binaryValue );
	q.synthi.set( \dampset, (tilty > 0.1).binaryValue );
};

t.mouseUpAction = { arg view,x,y,pressure,tiltx,tilty,deviceID, buttonNumber,clickCount,absoluteZ,rotation;
	if (q.active, {
		q.synthi.free;
		q.active = false;
	});
};
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
