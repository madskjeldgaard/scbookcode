/* IZ 051230 
Example of Script using more than one buffer.
- A Group is used to group 2 synths together in the Scripts process, 
  When that Group is free'd, the Synths it contains are also free'd. 
- the bufnum controlSpec's name creates a BufParameter by default
- For the second buffer, the 'BufParameter' Class is explicitly declared
  as first argument of the specification array in ~controlSpecs
- Per default, the specs create Guis that set parameters of the Group, 
  thereby controlling all Synths in the Group at once. To address individual
  synths separately by the GUI's elements, custom action functions are 
  provided as last arguments in a controlSpec element array. Here this is used
  to control individually each of the 2 bufnum parameters of the 2 Synths.
*/

var group, synth1, synth2;

~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
// first buffer
[ 'bufnum', 0, 1023, 'linear', 1, 0, { | val |
	~bufnum = val;	// custom action required to set the bufnum parameter
	if (~process.notNil) { // of synth 1.
		synth1.set(\bufnum, val);
	};
}],
[ 'rate', 0.125, 8, 'exp', 0, 1 ],
[ 'amp', 0, 1, 'amp', 0, 0.5 ],
[ 'stop', 0, 1, 'linear', 0, 0 ],
[ 'trigAt', 0, 1, 'linear', 0, 1 ],
[ 'trigID', 0, 1, 'linear', 0, 0 ],
// second buffer: Class 'BufParameter' is declared explicitly by first
// element of the specification array. 
[ 'BufParameter', 'buffer2', 0, 1, 'linear', 0, 0, { | val |
	~buffer2 = val; // custom action sets bufnum of second synth.
	if (~process.notNil) {
		synth2.set(\bufnum, val);
	};
}]
];

~start = {
	// create Group to put the Synths in
	group = Group(~target, #[\h, \t, \addBefore, \addAfter,
		\addReplace][~addAction]);
	synth1 = Synth("playbuf_looping", ['out', ~out, 'bufnum', ~bufnum,
		'rate', ~rate, 'amp', ~amp, 'loop', 1], group);
	synth2 = Synth("playbuf_looping", ['out', ~out, 'bufnum', ~buffer2,
		'rate', ~rate, 'amp', ~amp, 'loop', 1], group);
	group;	// Group is stored in ~process for further control by Script
};
