~session = Session named: 'MIDI Test';
~script = ~session.getScript('Scripts', '05 group+synths');
~script.learnMIDI;
~mra = ~script.getMIDIbinding;
d =~mra.object_action_pairs;
e = d.first;
~buttonResponder = e.getMIDIbinding;
~knobResponder = d[2].getMIDIbinding;
~knobResponder.inspect;

~session = Session named: 'MIDI Test';
~script = ~session.getScript('Scripts', '05 group+synths');
r = ~script.getMIDIbinding;
r.object_action_pairs;




~buttonResponder2 = NoteOnOffResponder({ | ... args | ['note', args].postln; }, nil, 0, 0);
~buttonResponder2.install;
~knobResponder2 = CCResponder({ | ... args | ['knob', args].postln; }, nil, 0, 40);
~knobResponder2.install;
~sliderResponder2 = CCResponder({ | ... args | ['slider', args].postln; }, nil, 0, 32);
~sliderResponder2.install;

MIDIListener.initMIDI;

~data = [
~buttonResponder2 = NoteOnOffResponder({ | ... args | ['note', args].postln; }, nil, 0, 0, nil, false),
~knobResponder2 = CCResponder({ | ... args | ['knob', args].postln; }, nil, 0, 40, nil, false),
~sliderResponder2 = CCResponder({ | ... args | ['slider', args].postln; }, nil, 0, 32, nil, false)
];

~mra = MIDIResponderArray(1000, ~data.collect({ | r, i |
	MIDIHandler.register(r.postln, i.postln);
	[i, r.function]
}).flat);


MIDIHandler.register(~mra, 1000);
1000.getMIDIbinding.activate;

~mra.activate;
~mra.deactivate;

MIDIHandler.handler.bound.asCompileString;
MIDIHandler.handler.active.detect(true).getMIDIbinding.install;
MIDIHandler.handler.active.detect(true).getMIDIbinding.function = { "PROS THEOU EPITELOUS".postln };
k

/* =========================================================================================================

Given an array of responders and parameters to bind them to. 
Given the script, a responder for the script, the array of scripts parameters (names) 

['script', responder],
['freq', responder'],
['amp', responder],
... etc

1. Make a MIDIResponderArray 'mra'
2. The handle of mra will be the script
3. The script will be included as reference in the mra itself, and its responder will be
   the on off responder for turning the script on and off
4. Each of the parameters will be bound to a responder from the rest of mra
Now to install all this so that activation and deactivation works: 
5. *both* the handle of mra *and* all of the objects of mra must be registered with MIDIHandler

Then: 
To activate the whole set go: 
mra.activate;
To deactivate the whole set go: 
mra.deactivate.

*/

~register = { | handle, handleResponder ... objectResponderPairs |
	MIDIHandler.register(handleResponder, handle);
	objectResponderPairs pairsDo: { | object, responder |
		MIDIHandler.register(responder, object);
	}
};

~register.(\a, ~mra, \b, ~buttonResponder2, \c, ~knobResponder2, \d, ~sliderResponder2);


~mra.object_action_pairs;
~mra.;
MIDIHandler.handler.bound;
MIDIHandler.handler.active;



1000.getMIDIbinding;

1.getMIDIbinding;

MIDIHandler.register(~buttonResponder2, 1);
MIDIHandler.register(~knobResponder2, 2);
MIDIHandler.register(~sliderResponder2, 3);
MIDIHandler.activate;
MIDIHandler.deactivate;

MIDIHandler.handler.bound;
MIDIHandler.handler.active;
~mra.activate;

~buttonResponder2.install;
~knobResponder2.install;


~mra = MIDIResponderArray(~script, [~script, nil, 
// { | ... b | if (b[3] > 0) { ~script.start } { ~script.stop } },
]);
~mra.activate;
and
MIDIListener.initMIDI;

{ | ... args | args }.(1, *[2, 3]);




d[2];

==========
MIDIHandler.add(c, b);
c.install
c
d[2]
MIDIHandler.getMIDIbinding(d[2]);
MIDIHandler.add(d[2]);

MIDIHandler.handler.inspect;
MIDIHandler.handler.active;
MIDIHandler.handler.bound;
MIDIHandler.add(c, b);
b.activateMIDI
b.deactivate

b.readMIDIfromFileDialog
b.parameterEnvir.

b.getMIDIbinding

b.learnMIDI;
