/* (IZ 2005-09-03)

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
As of 2007 05 10 this will become obsolete, being replaced by Script:allocBuffer
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

Utility for synth processes that need empty audio buffers, such as BufIO UGens, DelayWr, FFT ugens (PV_ UGens), etc.

Pre-allocate an array of audio buffers of equal size. Make the next unused buffer available to any process that requires it. A process that needs a buffer gets it from that array and then frees it when it finishes. BufferArray takes care of keeping track of free and used buffers.

// allocate 50 buffers of 2048 samples and single channel each:
a = BufArray(Server.local, 50, 2048, 1);
// Create new synth, using the next available buffer number from a:
s = Synth("myPVocoder", [\bufnum, b = a.alloc]); 
// make the synth free the buffer as soon as it ends;
s.onEnd({ a.free(b) });

The code for getting and freeing the buffer can be shortened even more with the method makeSynth:

a = BufArray.new;
a.makeSynth("myPVocoder", nil, [\sensitivity, 0.23]);

// freeing the synth will automatically also free the buffer numbers:
a.free;

*/

BufArray {
	var <server, <numBufs = 50, <numFrames = 2048, <numChannels = 1;
	var <all, free, <allocated;
	*new { | server, numBufs = 50, numFrames = 2048, numChans = 1 |
		^super.newCopyArgs.init;
	}

	init {
		server = server ?? { Server.local};
		this.onBoot({
			all = ({ Buffer.alloc(server, numFrames, numChannels) } ! numBufs) collect: _.bufnum;
			free = all.copy;
			allocated = [];
		}, server)
	}
	alloc {
		var bufnum;
		bufnum = free.pop;
		allocated = allocated.add(bufnum);
		^bufnum;
	}

	free { | bufnum |
		allocated.remove(bufnum);
		free = free.add(bufnum);
	}
	makeSynth { | defname, bufnumArgs, args, target, addAction = \addToHead |
		var synth, bufnums;
		bufnumArgs = (bufnumArgs ? \bufnum).asArray;
		bufnums = bufnumArgs.collect { |b| this.alloc };
		synth = Synth(defname, [bufnumArgs, bufnums].flop.flat ++ args, target, addAction);
		synth.onEnd {
			bufnums.do { |b| this.free(b) };
		};
		^synth;
	}
}