/* (IZ 2005-10-29) { 
	this way of playing buffers allows one to set the output bus number so that they can be used
	as input for testing effects synths etc.
	Requires playbuf synthdef from Lilt
	~controlSpecs = [
	[ 'out', 0, 4095, 'linear', 1, 0 ],
	[ 'bufnum', 0, 1023, 'linear', 1, 0 ],
	[ 'rate', 0.125, 8, 'exp', 0, 1 ],
	[ 'trigger', 0, 1, 'linear', 1, 1 ],
	[ 'startPos', 0, 1, 'linear', 0, 0 ],
	[ 'loop', 0, 1, 'linear', 1, 0 ],
	[ 'amp', 0, 1, 'amp', 0, 1 ]
	];
	
} */

+ Buffer {
	xplay { | out = 0, loop = 1 |
		^Synth('playbuf_looping', [\out, out, \bufnum, bufnum, \loop, loop]);
	}
}
