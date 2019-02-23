Figure 6.18.  Implementation of the event type \bus.
	{ |server|
		var lag, array;
		lag = ~lag + server.latency;
		array = ~array.asArray;
		server.sendBundle(lag, 
			[\c_setn, ~out.asUGenInput, array.size] ++ array);
	}
