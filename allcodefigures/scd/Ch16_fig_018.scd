Figure 16.18: Making new tables and sending them to buffers

q.bufs.pulsaret.sendCollection(Array.linrand(1024, -1.0, 1.0)); // noise burst
q.bufs.pulsaret.read("sounds/a11wlk01.wav", 44100 * 1.5);       // sample
q.bufs.pulsaret.sendCollection(Pbrown(-1.0, 1.0, 0.2).asStream.nextN(1024));

	// make a new random fundfreq table, and send it
q.curr.tab.fund = 200 ** Env({1.0.rand}!8, {1.0.rand}!7, \sin).discretize.as(Array);
q.bufs.fund.sendCollection(q.curr.tab.fund);

	// and a new random formfreq table
q.curr.tab.form = 500 ** ( 0.5 + Env({rrand(0.0, 1.0)}!8, {1.0.rand}!7, \sin).discretize.as(Array));
q.bufs.form.sendCollection(q.curr.tab.form);


