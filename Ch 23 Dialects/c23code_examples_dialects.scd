
// Chapter 23. Dialects, Constraints, and Systems within Systems

// 1 Dialects

// not a Figure
// a quine in SC3
(_ + '.(*' +  quote(_)  + '! 2)').(*"(_ + '.(*' +  quote(_)  + '! 2)')" ! 2)


// not a Figure
// ensure VagueList.sc in in the enxtensions folder 
// VagueList has a vague 'at' method 
a = VagueList[0, 1, 2, 3];  
a.at(2); 	// may return 1, 2, or 3 
a.at(2); 
a.at(2); 


// not a Figure
// Eniac Cycling Unit with adjustable clock speed
(
{	var clockspeed = MouseX.kr(1, 300, 1);
	var clocksignal = LFSaw.ar(clockspeed).range(0, 80);
 	var timingPulses = CU_PulseLookUpTables.ar(clocksignal); // 10 channels.
 	timingPulses * 0.2
}.scope;
)

// Figure 23.1 The ENIAC Cycling Unit (graphic image).


// not a Figure
// two ways of coupling parameters

{ var freq = MouseX.kr(20, 2000, 1); Pulse.ar(freq, freq.explin(20, 2000, 0.95, 0.05)) }.play;


// more intricate dependency
{ var freq = MouseX.kr(20, 2000, 1);  Pulse.ar(freq,  freq.explin(20, 2000, 0, 5pi).sin * 0.45 + 0.5) }.play;


// Figure 23.2: Two ways of constraining parameters
(
{	var f = { | a, b | [a.min(1 - b), b.min(1 - a)] };
	var freq = f.value(MouseX.kr, MouseY.kr) * 400 + 500;
	SinOsc.ar(freq) * 0.1
}.play;
)


(
a = { |freq=100, width=0.5|
	var df, dw;
	df = freq - LastValue.kr(freq);
	dw = width - LastValue.kr(width);
	freq = freq + (dw * 100);
	width = width + (df / 100);
	Pulse.ar(freq, width.clip(0.01, 0.99).poll) * 0.1
}.play;
)

a.set(\freq, exprand(200.0, 600.0));
a.set(\width, 1.0.rand);


//  Figure 23.3 – Pokey registers (graphic image)



// Figure 23.4:  Modulating Pokey inputs
// modulating the frequency input to a Pokey UGen results in great variance
(
{ 
	var rate = MouseX.kr(0, 255);
	var mod = LFPulse.kr(1);
	var amp = 2r1100; // 12 of 16
	Pokey.ar(rate + mod, audc1: 2r01000000 + amp);
}.play
);

// modulating the pure tone bit
(
{ 
	var rate = MouseX.kr(0, 255);
	var mod = LFPulse.kr(1);
	var amp = 2r1100; // 12 of 16
	Pokey.ar(rate, audc1: 2r00100000 + (mod * 2r00100000) + amp);
}.play
);




// Figure 23.5: Setting up rd_clt and mapping Ctls to a Synth's controls. 
// You may need to install the rd_clt Quark. See the Quarks help file.
(
	SynthDef("FreqMod", { |car=440, carFine=1, mod=100, modFine=1, index=4, amp=0.3, pan=0| 
		Out.ar(0, Pan2.ar(PMOsc.ar(car * carFine, mod * modFine, index), pan, amp)); 
	}).add; 		
	
	c = Controller.new(s, 256);
	c.makeInterface(2, 3, 0, "Freq Mod controller");
	
	c[0].setup("carFreq", [50, 4800, \exp].asSpec, 440);
	c[1].setup("carFreqFine", [0.95, 1.05, \exp].asSpec, 1);
	c[2].setup("modFreq", [10, 4800, \exp].asSpec, 10);
	c[3].setup("modFreqFine", [0.95, 1.05, \exp].asSpec, 1);
	c[4].setup("mIndex", [0, 24, \lin].asSpec, 1);
	c[5].setup("amp", [0, 1, \lin].asSpec, 0.2);
);


a = Synth("FreqMod") // start the synth
6.do({|i| a.map(i, c[i].index)}); // map the synth control to the controller.

a.free


//  3 Scheduling constraints: HierSch


// Figure 23.6 HierSch scheduling constraints and priority levels (graphic image)


// Figure 23.7: Priority-based HierSch scheduling.
// You may need to install the HierSch Quark. See the Quarks help file. 

(
SynthDef(\ping, {	arg out = 0, mfreq = 69, pan = 0, gain = 0.2, dur = 0.25;	Out.ar(out, Pan2.ar(	SinOsc.ar(mfreq.midicps, 0, 	EnvGen.kr(envelope: (Env.perc(0.01, dur)), doneAction: 2)),	pan, gain));}).add;// function to play a synthm = {|f, d=0.3, g=0.2, p=0| Synth(\ping, [\mfreq, f + 45, \pan, p, \gain, g, \dur, d])};// function to make a chordc = {|a, b, c| [a, b, c].do{|i| m.value(i, 1.2, 0.075, rrand(-1.0, 1.0))}};t = TempoClock.default.tempo_(116 / 60); // assign clock to tb = HierSch.new(t); // start new HierSch, pass in clock)(// HierSch schedulesb.schedAbs(t.beats.ceil + 48, 1, { var offset = [0, 5, 7, 12]; c.value(*[12, 16, 19]+ offset.choose)}, Prand(#[1.5, 3], 30)); // enters last, priority highestb.schedAbs(t.beats.ceil + 14, 2, {|b, p, d, c| m.value([0, 0, 7, 5, 4].at(c % 5) + [12, 24].choose, 0.4, 0.15, rrand(-1.0, 1.0))}, Pseq(#[2, 2, 2, 1], 15)); // enters middle, priority middleb.schedAbs(t.beats.ceil, 3, { |b, p, d, c| m.value(VagueList[0, 12, 4, 7, 10, 10, 9, 9, 7].at(c % 9))}, Pseq(#[0.5, 0.5, 0.5, 0.5, 0.25, 0.75, 1, 0.5, 0.5], 17)); // enters first, priority lowest)

// 	4 Object Systems: Redirections and Constraints


// 	4.1 Redirecting Assignment: Maybe and LazyEnvir

// not a Figure

x = 1;	// assigment

~x = 1;	// environment put
\x.envirPut(1);
currentEnvironment.put(\x, 1); 




// Figure 23.8: Maybe yes.

p = LazyEnvir.push;
~a = ~b * ~c;
~a.value; 	// => nil
~b = Pseq([1, 2, 3]).asStream;
~c = 10;
~a.value; 	// => 10
~a.value; 	// => 20
~b = [1, 2, 3];
~a.value; 	// => [10, 20, 30];
~a.postcs;	// => Maybe((Maybe([ 1, 2, 3 ]) * Maybe(10)))
p.pop


//	4.2 Declaring Constraints: List Comprehensions

// not a Figure
_ + 1 ! 7  // => [ 1, 2, 3, 4, 5, 6, 7 ]

Array.fill(7, { |i| i + 1 })  // => [ 1, 2, 3, 4, 5, 6, 7 ]


// not a Figure
// list comprehensions example 
	
f = {:[x, y], x <- (2..10), y <- (x..10), gcd(x, y) == 1  }; 

f.next; // [2, 3] 
f.next; 

f.all;

f = {:[x, y], x<-(2..10), y<-(x + 1..10), gcd(x, y) == 1; x.isPrime.not and: y.isPrime.not  }; 
f.next; // [2, 3] 
f.next;  


// Figure 23.9: Coprimes as frequency and trigger rates
(
var x;
x = { |rates=#[1, 1]| Ringz.ar(Impulse.ar(rates) * 0.1, rates * 80, 1 / rates) }.play;
fork {
	var str = {:[x, y], 
		x<-(40..2), 
		y<-(x + 1..40),
		gcd(x, y) == 1,
		x.isPrime.not and: y.isPrime.not
	};
	0.5.wait;
	str.do { |primes|
		x.setn(\rates, primes.postln);
		(primes.product / primes.sum / 20).wait;
	}
};
)

// 5 Text Systems


// not a Figure
a = Pseq("aggaca").collect(_.ascii)
a.asStream.all // => [ 97, 103, 103, 97, 99 ]


// Figure 23.10: A very simple notation translater

(
var dict, maxLength = 0;
dict = (
	ab: { (note: [4, 0, 7], legato: 0.1, dur: 1) },
	ba: { (note: [4, 9, 8], legato: 0.3, dur: 0.3) },
	aaa: { (note: 5, legato:1.5) },
	bbb: { (note: 0, legato:2.5, dur: 0.25) }
);

dict.keys.do { |key| maxLength = max(maxLength, key.asString.size) };

f = { |str|
	var i = 0, n = 0, substr, event;
	while { i < str.size } {
		substr = str[i..i + n];
		event = dict[substr.asSymbol].value;
		if(event.notNil) {
			substr.postln;
			i = i + n + 1;
			n = 0;
			event.postln.play;
			event.delta.wait;
		} {
			if(n + 1 < maxLength) { n = n + 1 } { n = n - 1; i = i + 1 }
		};
	};
};
)

// play some sequences
fork { f.value("abbbbaab"); }
fork { f.value("aaabbbabbaaaabbabaaaaba"); };



// not a Figure
	
x = "1 + 2";
x.interpret / 3; // 1
x = x.replace("+", "-"); 
x.interpret / 3; // -0.33333


this.preProcessor = { |str| str.replace("+", "-") } 
1 + 2 // -1
this.preProcessor = nil 
1 + 2 // 3

