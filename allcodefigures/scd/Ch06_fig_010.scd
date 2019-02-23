Figure 6.10. Using Prout to define and play patterns on the fly
	Prout({| ev |
		var pat, refPat;

		refPat = Pbind(*[dur: 0.2, note: Pseq([0,0, 0, 7,0, 7])]);

		loop {
			ev = refPat.embedInStream(ev);

			pat = Pbind(*[
				dur: [0.2, 0.4].choose,
				note: Pseq(Array.fill(5, { 10.rand }), 3.rand )
			]);	
ev = pat.embedInStream(ev);
		}
	
}).play


