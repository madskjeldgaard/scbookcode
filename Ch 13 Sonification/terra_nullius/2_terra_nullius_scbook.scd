
// Terra Nullius

// requires worlddata to be loaded - run 1_WorldReadSimple.scd first.

(
q.numberOfChannels  = 8; // number of speakers in the ring
q.numParams = 24 * 2;	// number of data dimensions


q.all = [];

// collect data and keys
// q.all is an array of arrays of values

q.worlddata.keysValuesDo { |key, x| 
	q.all = q.all.add(x);
	q.allKeys = q.allKeys.add(key);
};

// keep a copy
q.allOrig = q.all.copy;

// a function to order along a given dimension
// if no value exists, leave in place (in this piece this doesn't happen, because longitude is used)
q.makeOrder = { |q, i=0|
	var lastval = inf;
	q.currentOrder = q.all.at(i).collect({ |x| x !? {Êlastval = x }; x ? lastval }).order;
	
	q.all = q.all.collect { |x| x[q.currentOrder] };
	q.allFlop = q.all.flop;
};

q.numberOfCountiesWithDist = {Ê|q, dist=5|
	q.allOrig.flop.count({ |x| x[q.allKeys.indexOf('Latitude')].absdif(50) < dist })
};

q.makeSelection = { |q, dist=5|
	var func =  { |country| country[q.allKeys.indexOf('Latitude')].absdif(50) < dist };
	
	q.allFlop = q.allOrig.flop;
	q.allFlop = q.allFlop.select(func);
	q.all = q.allFlop.flop;
	q.all = q.all.keep(q.numParams ? 24); // keep a number of data dimensions only
	q.makeOrder;

};

q.embedSlice = {|q, i=0, dt=1.0, bgamp=0.03, dtEnd|
	
	q.makeOrder(i);
	q.playCurrent(dt, bgamp, dtEnd).embedInStream;	

};

q.playCurrent = { |q, dt=0.05, bgamp=0, dtEnd|
	dtEnd = dtEnd ? dt;
	
	Routine {
		var n = q.allFlop.size;
		
		~out.set(\bgamp, bgamp); // set the background noise amp
		
		q.allFlop.do { |x, i|
			var t = (i+1).linexp(1, n+1, dt, dtEnd); // interpolate speed beteen rounds
			("  % ".format(t) ++ String.fill(x.size, {|i| if(x[i].isNil) { $* } { $ } })).postln;

			x = x.collect { |v| if(v.isNil) { 1 } {Ê0 } };
			
			~out.group.setn(\amp, x); // set the node proxy band amplitudes
			~out.group.set(\pan, (i / n * 2 - 1)); // set the node proxy pan pos
			
			t.wait;
		};	
	}
};
);


(
p = ProxySpace.push(s.boot);

(
~out = { |bgamp=0.2, pan=0, rq=0.1|
	var n = q.numParams;
	var amp = Control.names(\amp).kr(0.0 ! n); // amplitude controls
	var freqs = (0..n).linexp(0, n, 100, 12400);
	var u1, u2;
	
		thisThread.randSeed = 2006; // scramble the frequencies always the same way
		freqs = freqs.scramble;
	
	amp = amp.lag(0.02);
	
	// filtered noise bands with lagged filter coefficient
	u1 = {|i|
		var freq = freqs[i];
		BPF.ar(WhiteNoise.ar, freq, rq.lag(5)) * AmpComp.ir(freq, 100, 0.6) * amp[i].lag(0.02)
		* LFNoise1.kr(0.3, 0.2, 1)
	}.dup(n);
	
	u1 = u1.sum;
	
	// background noise
	u2 = HPF.ar(Crackle.ar({ 1.32 + 0.001.rand2 } ! n, bgamp.lag(10)), 500);
	
	// mix and pan
	PanAz.ar(q.numberOfChannels, u1, pan)
	+ 
	SplayZ.ar(q.numberOfChannels, u2)
}
);
)


(
Tdef(\x) {
	var steps, noise, count=0;
	
	// set level of background noise to 0, filter bandwidth to 0.5
	~out.set(\bgamp, 0.0, \rq, 0.5);

	// play the proxy to all output channels
	~out.play(0, q.numberOfChannels);
	
	3.wait; // pause before start
	
	steps = (1..11).linexp(1, 11, 3, 130).collect(_.asInteger);

	(steps.size - 2 * 2 + 1).do { |i|
			var dt, dt2, width, flopsize;
			
			// latitudinal width of selection
			width = steps.foldAt(i);
			width.postln;
			
			q.makeSelection(width);
			flopsize = q.allFlop.size;
			
			"q.allFlop.size: %\n".postf(flopsize);
			
			// calculate beginning and ending time steps of this round
			dt = flopsize.linexp(2.5, 100, 3, 0.01);
			dt2 = q.numberOfCountiesWithDist(steps.foldAt(i + 1)).linexp(2.5, 100, 3, 0.01);
			
			// set the filter bandwidth
			~out.set(\rq, i.linexp(1, steps.size * 2, 0.5, 0.01));
			
			// level of background noise
			noise = i.linexp(1, steps.size * 2, 0.002, 3.0);
			q.embedSlice(q.allKeys.indexOf('Longitude'), dt, noise, dt2);
			
	};
	
	~out.group.setn(\amp, 0.0 ! q.all.size); // set all to zero
	
	10.wait;
	
	~out.end(5);
	
	\ende.postln;
	
}.play;
);


