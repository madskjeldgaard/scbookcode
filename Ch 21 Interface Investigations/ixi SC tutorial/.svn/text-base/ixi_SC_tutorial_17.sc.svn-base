
// =====================================================================
// - SuperCollider Basics -
// =====================================================================

// Tutorial 17 - Graphical User Interfaces

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

// 	1) SuperCollider and GUIs
// 	2) A Sine Controlling GUI
//	3) ControlSpec - Scaling/mapping values
// 	4) Other Views
//	5) Further exploration



// 1) ========= SuperCollider and GUIs ==========


/*
SuperCollider is two things: the server and the language. The server receives OSC messages from the language. Any OSC sending language (which are most programming languages these days) can be used to create an interface and use the SC server as the sound engine. 

If you want to use the SC language itself (which is recommended as it's beautiful) you have some choices of creating graphical user interfaces. On the Mac, there is the default Cocoa (Apple) window system. You can also use SwingOSC:

http://www.sciss.de/swingOSC/

SwingOSC is cross-platform as it uses Java. Check the GUI class (and its helpfile) for further info.

TIP: If you are on a Mac and you don't care about cross-platform compatability of your code, you can use the Cocoa GUI lib, otherwise use the SwingOSC. 

Also, on Linux there is the SCUM GUI system, which I've never used. Sorry!

At the moment the GUI is being developed in SuperCollider, but all GUI items are the same in Cocoa and SwingOSC, with the difference that you put a J in front of the GUI items on SwingOSC.
*/

// so let's start...

// we create a window
w = SCWindow("SC Window", Rect(128, 64, 340, 360)).front;

// we can change it's location and shape
w.bounds_(Rect(400, 64, 600, 360));

// change the colour:
w.view.background_(Color.white)
w.view.background_(Color.clear)

// let's put a button on the window

(
b = SCButton(w, Rect(20,20, 100, 30));
b.states_([["on",Color.black,Color.clear],["off",Color.white,Color.black]]);
)

// this button doesn't do anything yet. We need to tell it what to do:
b.action_({ arg button; button.value.postln;});

// let's use this synth
(
SynthDef(\GUIsine, {arg freq=440, amp=0.2;
	Out.ar(0, SinOsc.ar(freq, 0, amp)!2);
}).load(s)
)

// usually we need to do something like starting or stopping a process:
(
b.action_({ arg button; 
	if(button.value == 1, {
		a = Synth(\GUIsine); // if button value is 1 we start a synth
	}, {
		a.free; // else we free the synth
	});
});
)

// how about having slider for pitch and amplitude?
(
// pitch
SCSlider(w, Rect(20, 100, 100, 20))
	.action_({arg sl;
		a.set(\freq, sl.value*1000); // simple mapping (check ControlSpec)
	});
	
// amplitude
SCSlider(w, Rect(20, 130, 100, 20))
	.action_({arg sl;
		a.set(\amp, sl.value);
	}); 
)




// 2) ========= A Sine Controlling GUI ==========

// let's do the above with all the code in one program:

/*
Note that we are creating N number of synths (defined in the variable 
"nrSynths") and putting them all into one List. That way we can access and
control them individually from the GUI. Look at how the sliders and buttons
of the GUI are controlling directly their respective synth by accessing
synthList[i] (where "i" is the index of the synth in the list)
*/


// TIP: change the nrSynths variable to some other number (10, 16, etc) and see what happens.

(
var synthList, nrSynths;
nrSynths = 6;

synthList = Array.fill(nrSynths, {0});

w = SCWindow("SC Window", Rect(400, 64, 650, 360)).front;

nrSynths.do({arg i;

	// we create the buttons
	SCButton(w, Rect(10+(i*(w.bounds.width/nrSynths)), 20, (w.bounds.width/nrSynths)-10, 20))
		.states_([["on",Color.black,Color.clear],["off",Color.white,Color.black]])
		.action_({arg butt;
			if(butt.value == 1, {
				synthList.put(i, Synth(\GUIsine));
				synthList.postln;
			}, {
				synthList[i].free;
			})
		});
		
	// frequency slider
	SCSlider(w, Rect(10+(i*(w.bounds.width/nrSynths)), 60, (w.bounds.width/nrSynths)-10, 20))
		.action_({arg sl;
				synthList[i].set(\freq, sl.value*1000); // simple mapping (check ControlSpec)
		});
		
	// amplitude slider
	SCSlider(w, Rect(10+(i*(w.bounds.width/nrSynths)), 100, (w.bounds.width/nrSynths)-10, 20))
		.action_({arg sl;
				synthList[i].set(\amp, sl.value);
		});
});
)




// 3) ========= ControlSpec - Scaling/mapping values ==========


/*
In the examples above we have used a very crude mapping of a slider onto a frequency argument in a synth. A slider in SuperCollider GUI gives a value from 0 to 1.0 in resolution defined by yourself and the size of the slider (the longer the slider, the higher resolution). So above we are using parts of the slider to control frequency values from 0 to 20 Hz that we are most likely not interested in. And we might want an exponential mapping or negative.

The ControlSpec is the equivalent to [scale] in Pd or Max. Check the helpfile.
*/

// The ControlSpec takes the following arguments: minval, maxval, warp, step, default,units

// So let's try it>

a = ControlSpec.new(20, 22050, \exponential, 1, 440);
a.warp
a.default

// so any value we pass to the ControlSpec is mapped to our specification above
a.map(0.1)
a.map(0.99)

// we could constrain the mapping
a.constrain(16000)
a.map(1.66) // clips at max frequency (22050)

// we can also unmap values
a.unmap(11025) // we get a high value as pitch is exponetial

// let's see what this maps to on a linear scale (yes you guessed right)
a = ControlSpec.new(20, 22050, \lin, 1, 440);
a.unmap(11025).round(0.1)


// TIP: An array can be cast into a ControlSpec with the method .asSpec
[300, 3000, \exponential, 100].asSpec

// TIP2: Take a look at the source file for ControlSpec (Apple+y)
// You will see lots of different warps, like db, pan, midi, rq, etc.

(
var w, c, d, warparray, stringarray;
w = SCWindow("control", Rect(128, 64, 340, 960)).front;
warparray = [\unipolar, \bipolar, \freq, \lofreq, \phase, \midi, \db, \amp, \pan, \delay, \beats];
stringarray = [];

warparray.do({arg warpmode, i;
	a = warpmode.asSpec;
	SCStaticText(w, Rect(10, 30+(i*50), 300, 20)).string_(warparray[i].asString);
	stringarray = stringarray.add(SCStaticText(w, Rect(80, 30+(i*50), 300, 20)));
	SCSlider(w, Rect(10, 10+(i*50), 300, 20))
		.action_({arg sl;
			stringarray[i].string = "unmapped value"
			 + sl.value.round(0.01) 
			 + "......" 
			 + "mapped to:" 
		 + warpmode.asSpec.map(sl.value).round(0.01)
		})
});
)


	
// Now we finish this by taking the example above and map the slider to pitch
// try to explore different warp modes for the pitch. And create an amplitude slider
(
var spec, synth;
w = SCWindow("SC Window", Rect(128, 64, 340, 360)).front;
spec = [100, 1000, \exponential].asSpec;

SCButton(w, Rect(20,20, 100, 30))
	.states_([["on",Color.black, Color.clear],["off",Color.black, Color.green(alpha:0.2)]])
	.action_({ arg button; if(button.value == 1, { synth = Synth(\GUIsine)}, {synth.free }) });
	
SCSlider(w, Rect(20, 100, 200, 20))
	// HERE WE USE THE SPEC !!! - we map the spec to the value of the slider (0 to 1.0)
	.action_({arg sl; synth.set(\freq, spec.map(sl.value)) }); 
)




// 4) ========= Other Views (but not all) ==========

(

w = SCWindow("SC Window", Rect(400, 64, 650, 360)).front;
a = SCButton(w, Rect(20,20, 60, 20))
	.states_([["on",Color.black,Color.clear],["off",Color.black,Color.clear]])
	.action_({arg butt; butt.value.postln;});

b = SCSlider(w, Rect(20, 50, 60, 20))
	.action_({arg sl;
		sl.value.postln;
	});

e = SCSlider(w, Rect(90, 20, 20, 60))
	.action_({arg sl;
		sl.value.postln;
	});
	
c = SC2DSlider(w, Rect(20, 80, 60, 60))
	.action_({arg sl;
		[\x, sl.x.value, \y, sl.y.value].postln;
	});

d = SCRangeSlider(w, Rect(20, 150, 60, 20))
	.action_({arg sl;
		[\lo, sl.lo.value, \hi, sl.hi.value].postln;
	});

f = SCNumberBox(w, Rect(130, 20, 100, 20))
	.action_({
		arg numb; numb.value.postln;	
	});

g = SCStaticText(w, Rect(130, 50, 100, 20))
	.string_("some text");
	
h = SCListView(w,Rect(130,80,80,50))
	.items_(["aaa","bbb", "ccc", "ddd", "eee", "fff"])
	.action_({ arg sbs;
		[sbs.value, sbs.item].postln;	// .value returns the integer
	});

i = SCMultiSliderView(w, Rect(130, 150, 100, 50))
	.action_({arg xb; ("index: " ++ xb.index ++" value: " ++ xb.currentvalue).postln});

j = SCPopUpMenu(w, Rect(20, 178, 100, 20))
	.items_(["one", "two", "three", "four", "five"])
	.action_({ arg sbs;
		sbs.value.postln;	// .value returns the integer
	});

k = SCEnvelopeView(w, Rect(20, 220, 200, 80))
	.drawLines_(true)
	.selectionColor_(Color.red)
	.drawRects_(true)
	.resize_(5)
	.action_({arg b; [b.index,b.value].postln})
	.thumbSize_(5)
	.value_([[0.0, 0.1, 0.5, 1.0],[0.1,1.0,0.8,0.0]]);

)




// 5) ========= For further exploration ==========


// Check also the helpfiles of the following GUI classes :

SCSoundFileView
SCTabletView
SCMovieView
Pen
SCQuartzComposerView // quite recent addition (~Feb 2007)








