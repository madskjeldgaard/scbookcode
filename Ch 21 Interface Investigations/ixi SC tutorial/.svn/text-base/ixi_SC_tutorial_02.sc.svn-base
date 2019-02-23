

// =====================================================================
// - SuperCollider Basics -
// =====================================================================

// Tutorial 02 - Server Basics

// =====================================================================
// - ixi audio tutorial - www.ixi-audio.net
// =====================================================================


/*		
		---------------------------------------------------------------
 		Copyright (c) 2005-2008, ixi audio.
 		This work is licensed under a Creative Commons 
		Attribution-NonCommercial-ShareAlike 2.0 England & Wales License.
 		http://creativecommons.org/licenses/by-nc-sa/2.0/uk/
		---------------------------------------------------------------
*/



// ========== Contents of this tutorial ==========

// 	1) Booting the server
// 	2) The play and scope functions
// 	3) SynthDefs
//	4) Getting values back to the language 




// 1) ========= Booting the server ==========

/*

SuperCollider has an architecture of a sound server and a client (the language).

The server is an independent program that can be communicated with using
the OSC (Open Sound Control) protocol. All you need to know is its IP address
(such as "127.0.0.1" or check www.whatsmyip.org) and its port (default 57110).

All sound generation happens on the server and most of the control
happens in the SC language (sclang) or any other programming language 
or interface that sends OSC to the server.

On OS X, by default two servers windows appear, localhost and internal. 
The internal is part of the language and runs in the same memory space. 
The localhost server is external to the language, so if it crashes, the 
servers keeps working. Other supercollider clients (sc-lang) and other 
programming languages (on the network, for example) could communicate to 
the localhost server.

The localhost server is the default server and it is stored in the
global variable "s" (by convention).

*/

// s is the server:
// ( if you open the class file of Main.sc, you will see how the "s" variable is set)

s.postln;
s.addr
s.name // the local host server is the default server (see Main.sc file)
s.serverRunning // is it running?
s.avgCPU // how much CPU is it using right now?

// OK, we start the server:
s.boot; // you'll see it starts the localhost server by default

// if we want the internal server to be the default server:
s.quit; // first we quit the localhost server (which was the default one)

Server.default = s = Server.internal;
// or in more understandable code:
s = Server.internal;
Server.default = s;
s.boot;

// in some cases we might want create our own server:
// (if you set up a server to be controlled over the network)

n = NetAddr("127.0.0.1", 57200); // IP (get it from whatsmyip.org) and port
p = Server.new("thor", n);
p.makeWindow; // make the gui window
p.boot; // boot it

// try the server:
{SinOsc.ar(444)}.play(p);
// stop it
p.quit;


// but let us just use the default (local) server:
Server.default = s = Server.local;
s.boot;



// 2) ========= The play and scope functions ==========


// Function (see tutorial 1) can play Unit Generators (UGens).
// we try some unit generators (UGens) 

{SinOsc.ar(333)}.play // sine wave

{Saw.ar(333)}.play // saw wave

{LFTri.ar(333)}.play // triangle wave

// if you want to scope the wave, use the internal server (see last tutorial)

Server.default = s = Server.internal;
s.boot;

{LFTri.ar(333)}.scope // triangle wave

{Saw.ar(333)}.scope // saw wave


// TIP: (OS X) Run UGen.browse to get a browser with all the UGens and their methods

UGen.browse;



// 3) ========= SynthDefs ==========

/*
Synth definitions are UGen graphs written and compiled in SC language and sent
to the server. That's what happens behind the scenes of Function-play
(just highlight (Function:play) and open class def to look at the source code).

What this means is that the synth def code is evaluated when the synth def is 
COMPILED, not when a synth is instantiated. This means that there are things that
cannot be passed as arguments from the synth, such as if statements, number of
UGens and other things.
*/

// a synth definition of the above:

(
SynthDef(\sine, {arg freq=333, amp=0.4;
	Out.ar(0, SinOsc.ar(freq, 0, amp));
}).load(s) // we load the synthdef into the server
)

Synth(\sine); // try it!
// now hit Apple (or Ctrl) + dot to stop the synthesis


a = Synth(\sine, [\freq, 777]); // we assign the synth to a variable
a.set(\freq, 444) // set the frequency from outside
a.set(\freq, 333)
a.set(\amp, 0.8)

a.free; // free the synth

// check out the helpfiles for SynthDef and Synth

// NOTE: We need the Out Ugen in a synth definition to tell the server
// which audiobus the sound should go out of. (0 is left, 1 is right)

// which leads up to the question: how to make a stereo signal:

(
SynthDef(\stereosine, {arg freq=333, amp=0.4, pan=0.0; // we add a new argument
	var signal;
	signal = SinOsc.ar(freq, 0, amp);
	signal = Pan2.ar(signal, pan);
	Out.ar(0, signal);
}).load(s) // we load the synthdef into the server
)

Synth(\stereosine); // try it!

// kill the above with Apple+dot and then we run it again
a = Synth(\stereosine, [\freq, 777]); // we assign the synth to a variable
a.set(\freq, 444) // set the frequency from outside
a.set(\amp, 0.8)

// trying the panning
a.set(\pan, -1)
a.set(\pan, 1)
a.set(\pan, 0)

a.free; // free the synth



// try to run this line a few times and hear the sound build up and 
// so do the synths (and CPU) on the server window

Synth(\stereosine, [\freq, rrand(333,545)]);

// the solution is to have an Envelope in the synth definition
// check the EnvGen and Env helpfiles

(
SynthDef(\stereosineWenv, {arg freq=333, amp=0.4, pan=0.0; // we add a new argument
	var signal, env;
	env = EnvGen.ar(Env.perc, doneAction:2); // doneAction gets rid of the synth
	signal = SinOsc.ar(freq, 0, amp) * env;
	signal = Pan2.ar(signal, pan);
	Out.ar(0, signal);
}).load(s) // we load the synthdef into the server
)

Synth(\stereosineWenv); // try it, and try some other Envelopes in stead of .perc

// or with random freq:
Synth(\stereosineWenv, [\freq, rrand(333,545)]);



// 4)  ========= Getting values back to the language  ========= 

/*
It can be necessary in some circumstances to get values from the sc synth
back to the sc language. In order to do so we have two possibilities:
- using the Poll (for testing purposes - values displayed in the Post window)
- using SendTrig (for getting values into variables and functions)
*/

// ----- Using Poll ---- Check the Poll helpfile
// Here we use poll to see the value of the XLine UGen
{XLine.kr(44, 100000, 6).poll(Impulse.kr(20), "value")}.play

// the poll needs an Impulse to know when to send back to the language
{SinOsc.ar(LFSaw.ar(0.75, 0, 100, 500).poll(Impulse.ar(10), "freq"), 0, 0.5) }.play

// or simply
{MouseY.kr.poll}.play


// ----- Using SendTrig ----- Check the SendTrig helpfile

// here we need to create an OSCresponder in the language to listen to the server
// (as you remember the server and the language communicate through OSC)

// create the responder (language listening to OSC messages from the server)
(
a = OSCresponderNode(s.addr, '/tr', { arg time, responder, msg;
	[time, responder, msg].postln;
	~freq = msg[3];
}).add;
)

// create the OSC sender in the server (SendTrig sends OSC messages when it's triggered)
(
{
	var freq;
	freq = LFSaw.ar(0.75, 0, 100, 500);
	SendTrig.kr(Impulse.kr(10), 0, freq);
	SinOsc.ar(freq, 0, 0.5)
}.play
)

~freq // and we can see that the freq is now stored in a variable on the language

a.remove; // remove the responder



// --------- Using a Control Bus ---------------

b = Bus.control(s,1); // we create a control bus (not an audio bus) - Check the Bus helpfile

{Out.kr(b, MouseX.kr(20,22000))}.play // and we run some UGen on the bus

b.get({|val| val.postln;}); // then from the language we can poll (ask for) the value

/*
The language sends OSC message to the server and gets returned the value).
Check the source of Bus and find the .get method. You will see that the Bus class
is using an OSCresponder underneith. It is therefore "asynchronous", meaning that
it will not happen in the linear order of your code. (language asking server for the value,
server sending back to language. This takes time)
*/

// here is a program that shows how b.get is asynchronous. The {}.play from above has to be running.
// note how the numbered lines of code appear in the post window "in the wrong order"
// it takes between 2 and 15 milliseconds to get the value on a 1000 MHz PPC computer
(
x = 0; y= 0;
t = Task({
	inf.do({
		"1 - before b.get : ".post; x = Main.elapsedTime.postln;
		b.get({|val| 	
			"2 - ".post; val.postln; 
			y = Main.elapsedTime.postln;
			"the asynchronious process took : ".post; (y-x).post; "seconds".postln;
		}); //  this value is returned AFTER the next line
		"3 - after b.get : ".post;  Main.elapsedTime.postln;	
		0.5.wait;
	})
}).play;
)

t.stop;

