Figure 16.16: Pulsar basics – a set of waveform and control tables

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
q.bufs.pulsaret.plot2("a pulsaret");


