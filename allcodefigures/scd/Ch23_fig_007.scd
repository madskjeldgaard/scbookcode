Figure 23.7 
(
SynthDef(\ping, {
	arg out = 0, mfreq = 69, pan = 0, gain = 0.2, dur = 0.25;
	Out.ar(out, Pan2.ar(
	SinOsc.ar(mfreq.midicps, 0, 
	EnvGen.kr(envelope: (Env.perc(0.01, dur)), doneAction: 2)),
	pan, gain));
}).add;

// function to play a synth
m = {|f, d=0.3, g=0.2, p=0| Synth(\ping, [\mfreq, f + 45, \pan, p, \gain, g, \dur, d])};

// function to make a chord
c = {|a, b, c| [a, b, c].do{|i| m.value(i, 1.2, 0.075, rrand(-1.0, 1.0))}};

t = TempoClock.default.tempo_(116 / 60); // assign clock to t
b = HierSch.new(t); // start new HierSch, pass in clock
)

(
// HierSch schedules
b.schedAbs(t.beats.ceil + 48, 1, { var offset = [0, 5, 7, 12]; c.value(*[12, 16, 19]+ offset.choose)}, Prand(#[1.5, 3], 30)); // enters last, priority highest

b.schedAbs(t.beats.ceil + 14, 2, {|b, p, d, c| m.value([0, 0, 7, 5, 4].at(c % 5) + [12, 24].choose, 0.4, 0.15, rrand(-1.0, 1.0))}, Pseq(#[2, 2, 2, 1], 15)); // enters middle, priority middle

b.schedAbs(t.beats.ceil, 3, { |b, p, d, c| m.value(VagueList[0, 12, 4, 7, 10, 10, 9, 9, 7].at(c % 9))}, Pseq(#[0.5, 0.5, 0.5, 0.5, 0.25, 0.75, 1, 0.5, 0.5], 17)); // enters first, priority lowest
)


