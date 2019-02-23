Figure 20.4
// let's put a pan global control on the voicer

v = Voicer(10, \default);

// v.mapGlobal(name, bus, value, spec)
// \bipolar is a ControlSpec for the range -1..+1
v.mapGlobal(\pan, nil, 0, \bipolar);

v.gui;

// Pbind works with voicer, using 'voicerNote' event type
p = Pbind(
	\type, \voicerNote,
	\voicer, v,
	\degree, Pn(Pseries(0, 1, 8), inf),
	\amp, Pwhite(0.05, 0.15, inf),
	\dur, 0.125,
	\legato, 0.8
).play;

p.stop;

v.free;


