Figure 5.3
// Boot the default server first:
Server.default.boot;
// Then select all lines between the outermost parentheses and run: 
( 	
{
	Resonz.ar(GrayNoise.ar,
		XLine.kr(100, 1000, 10, doneAction: 2), 
		XLine.kr(0.5, 0.01, [4, 7], doneAction: 0)
	)
}.play
)
// further examples:
{ WhiteNoise.ar(EnvGen.kr(Env.perc, timeScale: 3, doneAction: 2)) }.play;
{ WhiteNoise.ar(EnvGen.kr(Env.perc, timeScale: 0.3, doneAction: 2))}.play;


