/* IZ 2007-06-07 { SC3
First version of a method for saving snapshots of entire sessions.
Nothing actually done here yet.
} */

var w, l, d, m, r;
~start = {
	w = SCWindow("Session Snapshots", Rect(0, 0, 204, 304).fromTop);
	l = SCListView(w, Rect(2, 2, 200, 300));
	d = { ScriptBrowser.at('Examples', 'decaysin') } ! 10;
	m = ListModel(d);
	r = { | i |
		i.nset(
			\freq, 100 rrand: 1000,
			\pulse, 0.2 rrand: 10,
			\attack, 0.01 rrand: 0.5,
			\decay, 0.05 rrand: 2.5,
			\vibfreq, 0.1 rrand: 20,
			\vibamp, 10 rrand: 1000
		);
	};
	l.keyDownAction = { |me, char, mod, uni, key|
		switch (char,
			$a, { m.list do: _.start.postln },
			27.asAscii, { m.list do: _.stop },
			$\r, { if (m.selection.notNil) { m.selection.start; } },
			$r,  { if (m.selection.notNil) { r.(m.selection); } },
			$e, { if (m.selection.notNil) { m.selection.makeGui; } },
			$E, { m.list do: _.makeGui },
			$C, { m.list do: _.closeGui },
			$., { if (m.selection.notNil) {
				m.selection.set(\amp, postln(min(0.5, m.selection.get(\amp) + 0.01)));
			} },
			$,, {  if (m.selection.notNil) {
				m.selection.set(\amp, postln(max(0, m.selection.get(\amp) - 0.01)));
			} },
			127.asAscii, {  if (m.selection.notNil) { m.selection.stop; } },
			{ me.defaultKeyDownAction(char, mod, uni, key) }
		);
	};
	m.addListView(l);
	m.onSelect(l, { | i |
		Post << i << " status: " << [\stopped, \running][i.isRunning.binaryValue]
			<< " volume: " << i.get(\amp) << "\n";
	}, { "no item selected".postln; });
	w.front;
	l.focus(true);
};

~stop = { d do: _.close; w.close };
