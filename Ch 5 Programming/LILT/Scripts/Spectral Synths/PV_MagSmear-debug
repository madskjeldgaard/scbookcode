// This part added by hand for buffer allocation: 


/*
var script, buffer;
script = ~script;
script.onBoot({
//	script.session.server.postln;
//	script.session.server.serverRunning.postln;
	buffer = Buffer.alloc(script.session.server, 2048, 1).postln;
}, script.session.server, true);
*/

~script.allocBuffer;

// Generated from SynthDesc on: Fri Dec  9 09:34:00 2005

~controlSpecs = [
[ 'in', 0, 127, 'linear', 1, 0 ],
[ 'out', 0, 4095, 'linear', 1, 0 ],
//[ 'bufnum', 0, 1023, 'linear', 1, 0 ],
[ 'bins', 0, 100, 'linear', 0, 50 ]
];

~start = {

 Synth("PV_MagSmear",
      ['in', ~in, 'out', ~out, 'bufnum', ~buffer.bufnum, 'bins', ~bins],
	  ~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction])
};

/*
(
~buf = Buffer.alloc(Server.local, 2048, 1);

~synth =  Synth("PV_MagSmear",
      ['in', 24, 'out', 0, 'bufnum', ~buf.bufnum, 'bins', 50])

)

*/