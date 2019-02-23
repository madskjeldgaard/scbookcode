
	// figure 16.16 - Pulsar basics - make a set of waveform and control tables
(
q = ();
q.curr = (); 	// make a dict for the set of tables
q.curr.tab = ();
				// random tables for pulsaret  and envelope waveforms:
q.curr.tab.env = Env.perc.discretize; 
q.curr.tab.pulsaret = Signal.sineFill(1024, { 1.0.rand }.dup(7));

			// random tables for the control parameters:
q.curr.tab.fund = 200 ** Env({1.0.rand}!8, {1.0.rand}!7, \sin).discretize.as(Array);
q.curr.tab.form = 500 ** ( 0.5 + Env({rrand(0.0, 1.0)}!8, {1.0.rand}!7, \sin).discretize.as(Array));
q.curr.tab.amp = 0.2.dup(1024);
q.curr.tab.pan = Signal.sineFill(1024, { 1.0.rand }.dup(7));

			// make buffers from all of them:
q.bufs = q.curr.tab.collect({ |val, key| Buffer.sendCollection(s, val, 1) });
)
		// plot one of them
q.bufs.pulsaret.plot("a pulsaret");




	// figure 16.17 - Pulsars as nodeproxies using GrainBuf
(
p = ProxySpace.push; 

		// fund, form, amp, pan
~controls = [ 16, 100, 0.5, 0]; 
~pulsar1.set(\wavebuf, q.bufs.pulsaret.bufnum);
~pulsar1.set(\envbuf, q.bufs.env.bufnum);

~pulsar1 = { |wavebuf, envbuf = -1| 
	var ctls = ~controls.kr;
	var trig = Impulse.ar(ctls[0]);
	var grdur = ctls[1].reciprocal;
	var rate = ctls[1] * BufDur.kr(wavebuf);
		
	GrainBuf.ar(2, trig, grdur, wavebuf, rate, 0, 4, ctls[3], envbuf);
};
~pulsar1.play;
)

	// crossfade between control settings
~controls.fadeTime = 3; 
~controls = [ 16, 500, 0.5, 0]; 	// change formfreq
~controls = [ 50, 500, 0.5, 0]; 	// change fundfreq
~controls = [ 16, 100, 0.5, 0]; 	// change both
~controls = [ rrand(12, 100), rrand(100, 1000)]; 

(	// control parameters from looping tables
~controls = { |looptime = 10| 
	var rate = BufDur.kr(q.bufs.pulsaret.bufnum) / looptime; 
	A2K.kr(PlayBuf.ar(1, [\fund, \form, \amp, \pan].collect(q.bufs[_]), 
		rate: rate, loop: 1));
};
)


	// figure 16.18 - make new tables and send them to buffers

	// make new pulsaret tables and send them to the buffer:
q.bufs.pulsaret.sendCollection(Array.linrand(1024, -1.0, 1.0)); // noise burst
q.bufs.pulsaret.read("sounds/a11wlk01.wav", 44100 * 1.5);       // sample
q.bufs.pulsaret.sendCollection(Pbrown(-1.0, 1.0, 0.2).asStream.nextN(1024));

	// make a new random fundfreq table, and send it
q.curr.tab.fund = 200 ** Env({1.0.rand}!8, {1.0.rand}!7, \sin).discretize.as(Array);
q.bufs.fund.sendCollection(q.curr.tab.fund);

	// and a new random formfreq table
q.curr.tab.form = 500 ** ( 0.5 + Env({rrand(0.0, 1.0)}!8, {1.0.rand}!7, \sin).discretize.as(Array));
q.bufs.form.sendCollection(q.curr.tab.form);







	// Pulsar synthesis with client-side control
	
		// a pulsaret and an envelope
a = Signal.sineFill(1024, 1/(1..10).scramble).putLast(0);
b = Env.perc.discretize(1024).putLast(0);
		// as buffers
x = Buffer.sendCollection(s, a, 1);
y = Buffer.sendCollection(s, b, 1);

		// a pulsar synthdef
(
SynthDef(\pulsar1, {|out, wavebuf, envbuf, form=200, amp=0.2, pan| 
	var grDur =  1/form;
	var pulsaret = Osc1.ar(wavebuf, grDur);
	var env = Osc1.ar(envbuf, grDur, doneAction: 2);
	
	OffsetOut.ar(out, Pan2.ar(pulsaret * env, pan, amp));
}, \ir ! 6).memStore;
)
Synth(\pulsar1, [\wavebuf, x, \envbuf, y]);

	// a simple pattern
(
Pbindef(\pulsar1, 
		\instrument, \pulsar1, 
		\wavebuf, x, \envbuf, y, 
		\form, Pn(Penv([20, 1200], [4], \exp)).loop, 
		\amp, 0.2, \pan, 0,
		\fund, 12, 
		\dur, Pfunc({ |ev| ev.fund.reciprocal })
).play;
)



	// Control from patterns and tables

Pbindef(\pulsar1, \form, Pn(Penv([20, 1200], [4], \exp)).loop);
Pbindef(\pulsar1, \fund, Pn(Penv([5, 50], [5])).loop);
Pbindef(\pulsar1, \amp, Pn(Penv([0, 0.2, 0.1], [7])).loop);
Pbindef(\pulsar1, \pan, Pbrown(-1.0, 1.0, 0.2));
Pbindef(\pulsar1, \amp, 0.2);

	// use Pseg for a control table reader
f = Env({ 100.0.rand }.dup(10), {1.0.rand}.dup(9).normalizeSum, \sine).asSignal;
f.plot;

Pbindef(\pulsar1, \fund, Pseg(f, 0.01, \lin, inf)).play;


