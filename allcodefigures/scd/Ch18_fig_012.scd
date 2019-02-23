Figure 18.12
VSO_ADR {

	var <control, <attackDur, <releaseDur, <totalDur;

	*new {arg start = 0.0, dur =  nil, peak = 0.707, decay = 0.01, attackDur = 0.125, 
		releaseDur = 0.125, addAction = 0, target = 1, server;
		^super.new.initVSO_ADR(start, dur, peak, decay, attackDur, releaseDur, addAction,
			target, server);
	}
	
	initVSO_ADR {arg start = 0.0, dur =  nil, peak = 0.707, decay = 0.01, aDur = 0.125, 
		rDur = 0.125, addAction = 0, target = 1, server;
		server = server ?? {Server.default};
		attackDur = aDur;
		releaseDur = rDur;
		totalDur = dur;
		control = CtkControl.env(Env.new([0, peak, decay, 0], 
			[attackDur, this.decayDur, releaseDur], \sine), 
			start, addAction, target, server: server, doneAction: 0);
	}
	
	decayDur {
		^(totalDur - (attackDur + releaseDur));
	}

}


