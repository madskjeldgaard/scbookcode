/* Testing midi reception for LIVID UNION and MAX
Max works OK with the ctlin help file, even when the script is not running, receiving MIDI from the ~controlSpecs action. 
Livid Union receives only from the Routine that runs inside the ~start function.
*/

var destinations, midiOuts;
MIDIClient.init(8, 8);

destinations = MIDIClient.destinations;
midiOuts = destinations collect: { | d, i | MIDIOut(i, destinations[i].uid) };

~controlSpecs = [
[\ctlout1, 0, 127, \linear, 1, 64, { | value |
	~ctlout1 = value;
	midiOuts[0].control(0, 7, value);
}]
];

~start = {
	Routine({
		loop {
			0.5.wait;
			midiOuts do: { | o, i | o.control(i, 7, ~ctlout1) }
		}
	}).play;
};

~stop = { ~process.stop };

