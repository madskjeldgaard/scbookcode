/**
In this example the multichannel reverb changes according to where in the space a sound source is located.

*/

s.boot;

( // define the synthdefs:
SynthDef( \pulse, { |out=0,amp=0.3|
	Out.ar( out, amp*Decay.ar( Impulse.ar( 1 ), 0.25, LFCub.ar(1200,0,0.1) ) )
	}).send(s);

SynthDef( \reverb, { |in=0,out=0,mix=0.5,room=0.5,damp=0.5,amp=1|
	Out.ar( out, amp*FreeVerb.ar( In.ar( in, 1 ), mix, room, damp ) )
	}).send(s);
)

( // create infrastructure
b = Bus.audio( s, 1 );
)

( // create synths:
x = Synth.new( \pulse, [\out, b, \amp, 1] );
// create six reverbs, with slightly different settings:
y = 6.collect{ |it,i| Synth.new( \reverb, [\in, b, \out, it, \room, 0.2.rand + 0.4, \damp, 0.2.rand + 0.4], addAction: \addToTail ); };
)


(
// Assign positions to each speaker: (6 speaker setup in a rectangle)
~speakers = [ Point.new( 0, 0 ),  Point.new( 1, 0 ),  Point.new( 2, 0 ),  Point.new( 2, 1 ),  Point.new( 1, 1 ),  Point.new( 0, 1 ) ];

// Position for the source signal:
~src = Point.new( 0, 0 );

// distance spec:
~spec = [0,3].asSpec;

// function to update the location:
~updateLocation = { arg newpos;
	~src = newpos;
	y.do{ |it,i|
		// the mix gets more wet, the further the source is from the speaker:
		it.set( \mix,  ~spec.unmap( ~src.dist( ~speakers.at(i) ) ).postln );
	}
};
)

// corners:
~updateLocation.value( Point.new( 0,0 ) )
~updateLocation.value( Point.new( 1,0 ) )
~updateLocation.value( Point.new( 2,0 ) )
~updateLocation.value( Point.new( 0,1 ) )
~updateLocation.value( Point.new( 1,1 ) )
~updateLocation.value( Point.new( 2,1 ) )

// middle:
~updateLocation.value( Point.new( 0.5,0.5 ) )
~updateLocation.value( Point.new( 1,0.5 ) )
~updateLocation.value( Point.new( 1.5,0.5 ) )
~updateLocation.value( Point.new( 1.5,0 ) )


// clean up:
x.free; y.do{ |it| it.free; }
