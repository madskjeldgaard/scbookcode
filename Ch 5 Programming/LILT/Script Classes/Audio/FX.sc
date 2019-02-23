/* IZ 050626 under construction

Serves to group multiple fx synths that read from a bus and multiple 
send synths that send to that bus. That is, you can create many fx synths that
inside an fx group and send to them the outputs of many 
send synths.  Sending synths and fx synths can be created and freed in any 
order independently of each other. 
When the fx is created, it allocates the shared bus and when 
it is freed, it frees that bus. 

To be used with Script to create drag-and-drop interfaces for adding multiple
synths that write to one bus and multiple fx synths that read from that bus. 
makeGui method must still be written for this. 


Synths added to an FX instance with Synth.fx(fxinstance, synthdefname, args) 
Will get their in argument set to the input bus number and their out argument
set to the out number given to the FX instance. 

Synths added to an FX instance with Synth.send(fxinstance, synthdefname, args) 
Will get their out argument set to the input bus number. 

To chain another FX group as sender to an existing FX group, create the sending
group with the message 'fx'. 

(
var sendingFXgroup, receivingFXgroup;
receivingFXgroup = FX.new;
sendingFXgroup = FX.fx(receivingFXgroup);
)

============= Examples:

SynthDef("resonz", { | out = 0, in = 0, freq = 440, bwr = 1.0, mul = 1.0, add = 0.0 |
	Out.ar(out, 
		Resonz.ar(In.ar(in), freq, bwr, mul, add)
	)
}).load(Server.local);

SynthDef("pink", { | out = 0, mul = 1, add = 0 |
	Out.ar(out, PinkNoise.ar(mul, add));
}).load(Server.local);

SynthDef("ringz", { | out = 0, in = 0, freq = 2440, decaytime = 0.01, mul = 1.0, add = 0.0 |
	Out.ar(out, 
		Ringz.ar(In.ar(in), freq, decaytime, mul, add)
	)
}).load(Server.local);


f = FX.new;
a = Synth.fx(f, "resonz", \freq, 500, \bwr, 15.0);
b = Synth.send(f, "pink");
a.set(\freq, 1000);
a.set(\bwr, 28);
// you can also send set messages to the group "globally":
f.set(\bwr, 10, \freq, 300, \mul, 0.2); 
// you can add more fx or sources here, and you can free these individually

// ...

f.free; // stops the contained synths, both fx and sources.

*/

FX : Group {
	var <>in, <>out = 0, <>rate = \audio, numChannels = 1;

	*new { | out = 0, rate = \audio, numChannels = 1, target, addAction=\addToHead |
		^super.new(target, addAction).init(out, rate, numChannels);
	}

	init { | argOut, argRate, argNumChannels |
		isRunning = true;
		out = argOut;
		rate = argRate;
		numChannels = argNumChannels;
		in = Bus.alloc(rate, server, numChannels);
		this.onEnd {
			isRunning = false;
			in.free;
		};
	}

	// create a new instance that sends its output to an existing FX instance
	*fx { | fx, rate = \audio, numChannels = 1 |
		^this.new(fx.in.index, rate, numChannels, fx, \addToHead)
	}
}
