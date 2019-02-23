/* Figure 2.4 */

(
SynthDef(\UGen_ex4a, {arg id, limit = 1;
	var src, pitch, hasPitch, keynum, outOfTune;
	// read input
	src = SoundIn.ar(0);
	// analyze the frequency of the input
	#pitch, hasPitch = Pitch.kr(src);
	// convert to a midi keynum, but don't round! This value will be used later.
	pitch = pitch.cpsmidi;
	// if you are within an eighth tone of an equal tempered pitch, send a trigger
	outOfTune = (pitch - pitch.round).abs < 0.25;
	// if outOfTune is true, send a trigger. Limit to 1 trigger every 'limit' seconds  
	SendTrig.kr(Trig.kr(outOfTune, limit), id, pitch.round);
}).add;
	
SynthDef(\UGen_ex4b, {arg id1, id2, limit = 1, thresh = 0.5;
	var src, amp, amptrig, timer;
	src = SoundIn.ar(0);
	// analyze the amplitude input, cause a trigger if the output is over the thresh
	amp = Amplitude.kr(src);
	amptrig = Trig.kr(amp > thresh, limit);
	// use amptrig to see how long it is between triggers.
	timer = Timer.kr(amptrig);
	// send the values back with two different ids
	SendTrig.kr(amptrig, id1, amp);
	SendTrig.kr(amptrig, id2, timer);
}).add;

// plays a SinOsc of the pitch you were closest to
SynthDef(\UGen_ex4c, {arg freq;
	Out.ar(1, SinOsc.ar(freq, 0, XLine.kr(0.1, 0.00001, 0.5, doneAction: 2)))
}).add;

// modulated noise to respond to amp spikes
SynthDef(\UGen_ex4d, {arg freq;
	Out.ar(1, LFNoise1.ar(200) * SinOsc.ar(freq, 0, 
		XLine.kr(0.1, 0.00001, 0.5, doneAction: 2)));
}).add;

// allocate three unique ids for the trigger ids
a = UniqueID.next;
b = UniqueID.next;
c = UniqueID.next;	

// an envelope to poll for amp values later
e = Env([440, 880], [1], \exp);

// add the responder
o = OSCresponderNode(s.addr, '/tr', {arg time, responder, msg;
	// the msg is an array with 4 values... post them
	msg.postln;
	// the id sent back from the SendTrig is msg[2]... use it to decide what to do
	case 
		// pitch trigger
		{msg[2] == a}
		// msg[3] is the rounded keynum
		{Synth(\UGen_ex4c, [\freq, msg[3].midicps])}
		// amp trigger
		{msg[2] == b}
		// play a noise burst, higher the amp value, higher the freq (polls the 
		// Env 'e')
		{Synth(\UGen_ex4d, [\freq, e[msg[3]]])}
		// use the Timer value to play a delayed noise burst at 2000 Hz
		{msg[2] == c}
		{SystemClock.sched(msg[3], {
			Synth(\UGen_ex4d, [\freq, 2000]);
			})}
}).add;

// schedule the start our listening synths...
// then sing or tap away on the input.
SystemClock.sched(1.0, {
	Synth(\UGen_ex4a, [\id, a, \limit, 1]);
	Synth(\UGen_ex4b, [\id1, b, \id2, c, \limit, 0.2, \thresh, 0.25]);
});
	
// add a command period function to stop the synths and remove the responder
CmdPeriod.doOnce({
	o.remove; "Removed the responder".postln;
})
)

