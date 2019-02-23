Figure 6.14.  Yield versus EmbedInStream
	r = Routine{ 
		Pseq([1,2,3]).yield; 
		Pseq([1,2,3]).embedInStream;
		123445.embedInStream; 
		123445.embedInStream; 
	};

	[next(r), next(r), next(r), next(r), next(r), next(r)];
	// the result: [ a Pseq, 1, 2, 3, 123445, 123445, nil]


