Figure 6.17. Definition of the key \play in the default event
{
	var tempo, server;
	
	~finish.value;						// user callback 
	server = ~server ?? { Server.default };	 
	tempo = ~tempo;					// assigning to a variable
									// saves repeated look ups
	if (tempo.notNil) {					// if not nil, change tempo of
		thisThread.clock.tempo = tempo;	// the clock playing the pattern
	};
	~eventTypes[~type].value(server);		// select play function from ~type
}


