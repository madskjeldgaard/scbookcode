// figure 13.15 -  Left-right Trigger
(
~distances = {|amp = 0.2795| 
	LFSaw.ar(min((~dist.kr * 2.5 * 90 + 20).midicps, 44100)) * amp
};
)


