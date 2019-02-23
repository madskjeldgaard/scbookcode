Figure 15.4
// Prepare the buffer
b = Buffer.alloc(s, 512);

(
x = {
	var sig, chain, onsets, pips, trigger;
	
	sig = SoundIn.ar(0); 
	
	chain = FFT(b, sig);
	
	// - move the mouse left/right to change the threshold:
	onsets = Onsets.kr(chain, MouseX.kr(0,1), \complex);
	
	trigger= SendTrig.kr(onsets);
	
	pips = SinOsc.ar(880, 0, EnvGen.kr(Env.perc(0.001, 0.1, 0.2), onsets));
	
	Out.ar(0, ((sig * 0.1) + pips).dup);
}.play;
)

(
// register to receive message
a= OSCresponder(s.addr,'/tr',{ arg time,responder,msg;
	[time,responder,msg].postln;
}).add;
)

a.remove; //Free the OSCresponder
x.free; // Free the synth
b.free; // Free the buffer




