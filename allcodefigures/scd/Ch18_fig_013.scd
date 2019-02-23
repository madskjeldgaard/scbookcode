Figure 18.13
	initVSO {arg start = 0.0, dur = nil, freq = 622.254, ampPeakLevel = 0.707, 
		ampDecayLevel = 0.01, vibDepth = 0.21, vibRate = 3, addAction = 0, target = 1, 
			server;
		server = server ?? {Server.default};
		group = CtkGroup.new(start, dur, addAction: addAction, target: target, 
			server: server);
		freqCntl = VSO_Vib.new(start, dur, freq, vibDepth, vibRate, \head, group, server);
		ampCntl = VSO_ADR.new(start, dur, ampPeakLevel, ampDecayLevel, addAction: \head, 
			target: group, server: server);
		oscil = sinoscdef.new(start, dur, \tail, group, server)
			.freq_(freqCntl.control).amp_(ampCntl.control);
		score = CtkScore.new(group, oscil);
	}





