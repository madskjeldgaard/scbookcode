Figure 5.38
(
~sound_adapter =  { | counter, what, count |
	switch (what, 
		\reset, { Synth("wham"); },
		\max_reached, { counter.reset },
		\count, { Synth("ping", 
			[\freq, count.postln * 10 + counter.max_count * 20]
			)
		}
	)
};
)
 

