Figure 20.3
// Voicer(voices, things, args, bus, target, addAction)
// target may be a Group, Server, or MixerChannel
v = Voicer(10, \default);

v.trigger(440, 1);  // v.trigger(freq, gate, args, latency)
v.release(440);

// v.gate(freq, dur, gate, args, lat) -- lat = latency
v.gate(440, 2.0, 1, [pan: -0.5]);

r = fork {
	loop {
			// play over a C major scale
		[60, 62, 64, 65, 67, 69, 71, 72].do({ |midi|
			v.gate(midi.midicps, 0.1, 1, [amp: rrand(0.05, 0.15)],
				lat: 0.1);
			0.125.wait;
		});
	}
};

r.stop;

v.free;

