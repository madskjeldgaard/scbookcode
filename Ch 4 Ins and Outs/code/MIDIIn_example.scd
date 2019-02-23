Ins and Outs - MIDI

MidiIn

// connects one input, to the first available output from a device or program
MIDIIn.connect;
MIDIIn.connect(0,3);
// assigns a function to incoming noteOn messages
MIDIIn.noteOn = { |port, chan, note, vel|	[port, chan, note, vel].postln};

// first unassign the noteOn method:
MIDIIn.noteOn = nil;
// create a NoteOnResponder and assign it to the variable n
n = NoteOnResponder( { |src, chan, num, vel|	[src, chan, num, vel].postln},nil,nil,(0..127), (0..127) );

// create a NoteOnResponder and assign it to the variable m
m = NoteOnResponder( { |src, chan, num, vel| "responder m ".post;[src, chan, num, vel].postln},nil,nil,[64], (0..127) );

// remove the NoteOnResponder n:
n.remove;

// Hit note 64 again, and you'll get only one post.

// remove the NoteOnResponder m:
m.remove;

// Now you've removed all the responders.

NoteOnResponder.removeAll;

// now a Sound example:

s.boot;
// create a NoteOnResponder that makes a sound:
m = NoteOnResponder( { |src, chan, num, vel| 
	var x = Synth.new(\default,[\freq, num.midicps,\out,0,\amp,0.2,\gate,1,\dur,1,\pan,0]);
	Task({ 1.0.wait; x.free }).play;
},nil,nil,(0..127), (0..127) );


m.remove;
