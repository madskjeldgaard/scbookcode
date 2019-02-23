
///////////////////////////////////////////////////////////////
// Clock examples

SystemClock.sched(2, {"foo".postln; }); 

// "foo" repeats every second
SystemClock.sched(0, {"foo".postln; 1.0 }); 

// "bar" repeats at a random delay
SystemClock.sched(0, {"bar".postln; 1.0.rand }); 

// clear all scheduled events 
SystemClock.clear; 

// causes an "operation cannot be called from this Process" error
SystemClock.sched(1, { SCWindow.new.front });

// defer reschedules GUI code on the AppClock, so this works
SystemClock.sched(1, { { SCWindow.new.front }.defer });

//TempoClock
(
t = TempoClock.new; // make a new TempoClock
t.sched(0, {"Hello!".postln; 1});
)
t.tempo = 2; // twice as fast
t.clear;

// Routine
(
r = Routine({
"foo".yield;
"bar".yield;
});
)
r.value; // foo
r.value; // bar
r.value; // we've reached the end, so it returns nil

// Routine in a Clock
(
r = Routine({
	"foo".postln;
	1.yield; // reschedule after 1 second
	"bar".postln;
	1.yield;
	"foobar".postln;
});
SystemClock.sched(0, r);
) 

///////////////////////////////////////////////////////////////
// Figure 3.1 A simple routine illustrating a musical use of yield
// Fermata
s.boot;
(
r = Routine({	
	x = Synth(\default, [freq: 76.midicps]);
	1.wait;
	
	x.release(0.1);
	y = Synth(\default, [freq: 73.midicps]);
	"Waiting...".postln;
	nil.yield;// fermata
	
	y.release(0.1);
	z = Synth(\default, [freq: 69.midicps]);
	2.wait;
	z.release;
});
)
// do this then wait for the fermata
r.play;
// feel the sweet tonic... 
r.play; 







/////////////////////////////////////////////////////////////// 
// Figure 3.2 Using Task so you can pause the sequence
(
t = Task({	
	loop({	 // loop the whole thing
		3.do({	 // do this 3 times
			x.release(0.1);
			x = Synth(\default, [freq: 76.midicps]);
			0.5.wait;
			x.release(0.1);
			x = Synth(\default, [freq: 73.midicps]);
			0.5.wait;
		});
		"I'm waiting for you to press resume".postln;
		nil.yield;// fermata
		x.release(0.1);
		x = Synth(\default, [freq: 69.midicps]);
		1.wait;
		x.release;
	});
});

w = Window.new("Task Example", Rect(400, 400, 200, 30)).front;
w.view.decorator = FlowLayout(w.view.bounds);
Button.new(w, Rect(0, 0, 100, 20)).states_([["Play/Resume", Color.black, Color.clear]])
	.action_({ t.resume(0);});
Button.new(w, Rect(0, 0, 40, 20)).states_([["Pause", Color.black, Color.clear]])
	.action_({ t.pause;});
Button.new(w, Rect(0, 0, 40, 20)).states_([["Finish", Color.black, Color.clear]])
	.action_({ 
		t.stop; 
		x.release(0.1);
		w.close;
	});
)








///////////////////////////////////////////////////////////////
// Figure 3.3 Nesting tasks inside routines
(
r = Routine({	
	c = TempoClock.new; // make a TempoClock
	// start a 'wobbly' loop
	t = Task({	
		loop({	
			x.release(0.1);
			x = Synth(\default, [freq: 61.midicps, amp: 0.2]);
			0.2.wait;
			x.release(0.1);
			x = Synth(\default, [freq: 67.midicps, amp: 0.2]);
			rrand(0.075, 0.25).wait; // random wait from 0.1 to 0.25 seconds
		});
	}, c); // use the TempoClock to play this Task
	t.start;
	nil.yield;
	
	// now add some notes
	y = Synth(\default, [freq: 73.midicps, amp: 0.3]);
	nil.yield;
	y.release(0.1);
	y = Synth(\default, [freq: 79.midicps, amp: 0.3]);
	c.tempo = 2; // double time
	nil.yield;
	t.stop; y.release(1); x.release(0.1); // stop the Task and Synths
});
)

r.next; // start loop
r.next; // first note
r.next; // second note; loop goes 'double time'
r.next; // stop loop and fade








///////////////////////////////////////////////////////////////
// Figure 3.4 Using patterns within a task

(// random notes from lydian b7 scale
p = Pxrand([64, 66, 68, 70, 71, 73, 74, 76], inf).asStream; 
// ordered sequence of durations
q = Pseq([1, 2, 0.5], inf).asStream; 
t = Task({	
	loop({	
		x.release(2);
		x = Synth(\default, [freq: p.value.midicps]);
		q.value.wait;
	});
});
t.start;
)
t.stop; x.release(2);






///////////////////////////////////////////////////////////////
// Figure 3.5 Thanks to polymorphism we can substitute objects that understand the same message
(
p = 64; // a constant note
q = Pseq([1, 2, 0.5], inf).asStream; // ordered sequence of durations
t = Task({	
	loop({	
		x.release(2);
		x = Synth(\default, [freq: p.value.midicps]);
		q.value.wait;
	});
});
t.start;
)
// now change p
p = Pseq([64, 66, 68], inf).asStream; // to a Pattern: do re mi
p = { rrand(64, 76) }; // to a Function: random notes from a chromatic octave
t.stop; x.release(2);






///////////////////////////////////////////////////////////////
// Figure 3.6 Using 'messaging style': Score

(
SynthDef("ScoreSine",{ arg freq = 440;
Out.ar(0,
	SinOsc.ar(freq, 0, 0.2) * Line.kr(1, 0, 0.5, doneAction: 2)
)
}).add;
x = [
// args for s_new are synthdef, nodeID, addAction, targetID, synth args ...
[0.0, [ \s_new, \ScoreSine, 1000, 0, 0,  \freq, 1413 ]],
[0.5, [ \s_new, \ScoreSine, 1001, 0, 0,  \freq, 712 ]],
[1.0, [ \s_new, \ScoreSine, 1002, 0, 0,  \freq, 417 ]],
[2.0, [\c_set, 0, 0]] // dummy command to mark end of NRT synthesis time
];
z = Score(x);
)
z.play;





///////////////////////////////////////////////////////////////
// Figure 3.7 Executing one line at a time

(
// here's a synthdef that allows us to play from a buffer, with a fadeout
SynthDef("playbuf", { arg out = 0, buf, gate = 1;
	Out.ar(out,
		PlayBuf.ar(1, buf, BufRateScale.kr(buf), loop: 1.0) 
			* Linen.kr(gate, doneAction: 2); // release synth when fade done
	)
}).add;
// load all the paths in the sounds/ folder into buffers
~someSounds = "sounds/*".pathMatch.collect{ |path |  Buffer.read(s, path)}; 
)
// now here's the score, so to speak
// execute these one line at a time
~nowPlaying = Synth("playbuf", [buf: ~someSounds[0]]);
~nowPlaying.release; ~nowPlaying = Synth("playbuf", [buf: ~someSounds[1]]);
~nowPlaying.release; ~nowPlaying = Synth("playbuf", [buf: ~someSounds[2]]);
~nowPlaying.release;
// free the buffer memory
~someSoundsBuffered.do(_.free);
 
 
 
 
 
 
 
///////////////////////////////////////////////////////////////
// Figure 3.8 Play cues with a simple GUI

(
SynthDef("playbuf", { arg out = 0, buf, gate = 1;
	Out.ar(out,
		PlayBuf.ar(1, buf, BufRateScale.kr(buf), loop: 1.0) 
		* Linen.kr(gate, doneAction: 2) * 0.6; 
		// with 'doneAction: 2' we release synth when fade is done	
) }).add;
~someSounds = "sounds/*".pathMatch.collect{ |path |  Buffer.read(s, path)}; 
n = 0; // a counter
// here's our GUI code
w = Window.new("Simple CuePlayer", Rect(400, 400, 200, 30)).front;
w.view.decorator = FlowLayout(w.view.bounds);
//this will play each cue in turn
Button.new(w, Rect(0, 0, 80, 20)).states_([["Play Cue", Color.black, Color.clear]]).action_({ 
	if(n < ~someSounds.size, {
		if(n != 0, {~nowPlaying.release;}); 
		~nowPlaying = Synth("playbuf", [buf: ~someSounds[n]]); n=n+1;
	});
}); 
//this sets the counter to the first cue
Button.new(w, Rect(0, 0, 80, 20)).states_([["Stop / Reset", Color.black, Color.clear]]).action_({ n=0; ~nowPlaying.release; }); 
// free the buffers when the window is closed
w.onClose = { ~someSounds.do(_.free); };
)










///////////////////////////////////////////////////////////////
// Figure 3.9 Gathering up files for multichannel cues 

// gather all your folder paths
//this will path match each folder in the collection, i.e. we will have a collection of collections of paths

~groupOfindivCueFolders = "sounds/*".pathMatch.collect{ | item |  (item.asSymbol++"*").pathMatch };

Post << ~groupOfindivCueFolders;  //see them all !

//check how many cues you will have in the end
~groupOfindivCueFolders.size; 

//automate the buffering process for all cues:
~bufferedCues = ~groupOfindivCueFolders.collect{|item, i| item.collect{| path |  Buffer.read(s,  path)}}; //now all our cue files are sitting in their buffers ! 

~bufferedCues[0];  //here is cue 1

// see it in the post window:
Post << ~bufferedCues[0];

// play them all in a Group, using our previous synthdef
// we use bind here to ensure they start simultaneously
(
s.bind({	
	~nowPlaying = Group.new(s); // a group to put all the channel synths in
	~bufferedCues[0].do({|cue| Synth("playbuf", [buf: cue], ~nowPlaying)})
});
)
// fade them out together by sending a release message to the group
~nowPlaying.release;








///////////////////////////////////////////////////////////////
// Figure 3.10 Recording the results of making sounds with supercollider

s.boot; // make sure the server is running
(  	// first evaluate this section
b = Buffer.read(s, "sounds/a11wlk01.wav"); // a source
s.prepareForRecord; // prepare the server to record (you must do this first)
)
(	// simultaneously start the processing and recording
s.bind({
	// here's our funky effect
	x = { var columbia, amp; 
		columbia = PlayBuf.ar(1, b, loop: 1);
		amp = Amplitude.ar(columbia, 0.5, 0.5, 4000, 250); // 'sticky' amp follower
		Out.ar(0, Resonz.ar(columbia, amp, 0.02, 3)) // filter; freq follows amp
		}.play;
s.record;
});
)
s.pauseRecording; // pause
s.record // start again
s.stopRecording; // stop recording and close the resulting sound file









///////////////////////////////////////////////////////////////

// Thinking in the Abstract examples
(
{
Resonz.ar(Dust2.ar(5), 300, 0.001, 100)  +
Resonz.ar(Dust2.ar(5), 600, 0.001, 100)  +
Resonz.ar(Dust2.ar(5), 900, 0.001, 100) * 3.reciprocal; // scale to ensure no clipping
}.play
)

(
f = 300;
n = 3;
{
Mix.fill(n, {|i| Resonz.ar(Dust2.ar(5), f * (i + 1), 0.001, 100)})
* n.reciprocal; // scale to ensure no clipping
}.play
)

(
f = 40;
n = 50;
{
Mix.fill(n, {|i| Resonz.ar(Dust2.ar(5), f * (i + 1), 0.001, 300)})
* n.reciprocal; // scale to ensure no clipping
}.play
)






///////////////////////////////////////////////////////////////
// Figure 3.12 A variable number of resonators with an automatically created GUI       
(
f = 300;
n = 30; // number of resonators
t = Array.fill(n, { |i|
{
Resonz.ar(Dust2.ar(5), f * (i + 1), 0.001, 300)
* n.reciprocal; // scale to ensure no clipping
}.play;
});

// now make a GUI
// a scrolling window so we don't run out of space
w = Window.new("Buttons", Rect(50, 100, 290, 250), scroll:true);
w.view.decorator = FlowLayout.new(w.view.bounds); // auto layout the widgets
n.do({|i|
Button.new(w, Rect(0, 0, 130, 30)).states_([
["Freq" + (f * (i + 1)) + "On", Color.black, Color.white],
["Freq" + (f * (i + 1)) + "Off", Color.white, Color.black]
])
.action_({ arg butt;
t[i].run(butt.value == 0);
});
});
w.front;
)
  



///////////////////////////////////////////////////////////////
// Empty Gestures

( //you will be able to add multiple soundfiles, just shift click when selecting !
var file, soundPath;
~buffers = List[];
Dialog.getPaths({ arg paths;
paths.do({|soundPath|
//post the path to verify that is the one you expect!
    soundPath.postln; 
//adds the recently selected buffer to your list 
    ~buffers.add(Buffer.read(s, soundPath);); })
});
)

~buffers.size;
~buffers[0];
~buffers[0].play;


(
// buffer player with done action and control of envelope and panning
SynthDef(\samplePlayer, { arg out=0, buf = 0,
rate = 1, at =0.01, rel= 0.1, pos =0, pSpeed = 0, lev = 0.5;
var sample, panT,  amp, aux;
sample = PlayBuf.ar(1, buf, rate*BufRateScale.kr(buf), 1, 0, 0);
panT= FSinOsc.kr(pSpeed);
amp = EnvGen.ar(Env.perc(at, rel, lev), doneAction: 2);
Out.ar(out, Pan2.ar(sample, panT, amp));
}).memStore;
)

Synth(\samplePlayer,[\out, 0, \bufnum, ~buffers[0], \rel, 0.25]);
Synth(\samplePlayer,[\out, 0, \bufnum, ~buffers[~buffers.size.rand], \rel, 0.25]); 






///////////////////////////////////////////////////////////////
// Figure 3.13 Making a stuttering gesture using a geometric pattern

(/* a routine for creating a ritardando stutter with panning, you must have
run the code in fig 3.9 so that this routine may find some sounds already loaded into buffers, you can change the index of ~bufferedCues to test the routine on different sounds */


~stut = Routine( { var dur, pos;
~stutPatt = Pseq([Pgeom(0.01, 1.1707, 18), Pn(0.1, 1),Pgeom(0.1, 0.94, 200) ]);
~str= ~stutPatt.asStream;
100.do{
    dur =  ~str.next;
    dur.postln; 	//so we can check values on the post window
    ~sample = Synth("samplePlayer",[\out, 0, \buf,  ~bufferedCues[0], \at, 0.1, \rel, 0.05,\pSpeed, 0.5]);
    dur.wait;
}
});
)

//now play it
~stut.play;
// reset before you play again!
~stut.reset; 
 


///////////////////////////////////////////////////////////////
// More Empty Gestures

// randomly selected frequency, duration 0.1 seconds
Pbind(\freq, Prand([300, 500, 231.2, 399.2], 30), \dur, 0.1).play;

~gest1 = Pbind(\instrument, \samplePlayer, \dur, 2, \rel, 1.9);
~player = ~gest1.play; //make it play
~player.stream = Pbind(\instrument, \samplePlayer, \dur, 1/8, \rate, Pxrand([1/2,1,2/3,4],inf), \rel, 0.9).asStream; //substitute the stream
~player.stop;


~gest1 = Pbind(\instrument, \samplePlayer, \dur, Pgeom(0.01, 1.1707, 20), \rel, 1.9);
~gest1.play;

Pbind(\instrument, \samplePlayer, \dur, Pseq([Pgeom(0.01, 1.1707, 20), Pgeom(0.01, 0.93, 20)], 1), \rel, 1.9, \pSpeed, 0.5).play;

Pbind(\instrument, \samplePlayer, \dur, Pseq([Pgeom(0.01, 1.1707, 20), Pgeom(0.01, 0.93, 20)], 1),\rate, Pxrand([1/2,1,2/3,4],inf), \rel, 1.9, \pSpeed, 0.5).play;

~rhythm1 = Pseq([1/4, 1/4, 1/8, 1/12, 1/24, nil]); //the nil is so it will stop !

~gest1 = Pdef(\a, Pbind(\instrument, \samplePlayer, \dur, ~rhythm1, \rel, 1.9, \pSpeed, 0.5) );
~gest1.play;

~rhythm1 = Pseq([1/64,1/64,1/64, 1/32, 1/32, 1/32, 1/32, 1/24, 1/16, 1/12, nil]);
~gest1 = Pdef(\a, Pbind(\instrument, \samplePlayer, \dur, ~rhythm1, \rel, 1.9, \pSpeed, 0.5) );

~gest1 = Pdef( \a , Pbind(\instrument, \samplePlayer, \att, 0.5, \rel, 3, \lev, {rrand(0.1,0.2)}, \dur, 0.05, \rate, Pseq([ Pbrown(0.8, 1.01, 0.01, 20) ]) ) );


