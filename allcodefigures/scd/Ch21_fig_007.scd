

// Figure 7. Irritia. A stochastic patch playing with the envelope view. The mouse can be used to interact with the patch
(
var nNodes, envView, startStop, myWait, timeSlider, mouseTracker;
var xLoc, yLoc, mousedown = false;
var randLoc = 0.12.rand;

SynthDef(\irritia, { arg out=0, gate=1, freq=440, pan=0.0; 
	Out.ar(out, Pan2.ar(LFSaw.ar(freq,0.4,0.05) * EnvGen.kr(Env.sine, gate, doneAction:2), pan))
}).add;

nNodes = 10;
myWait = 0.033;

w = Window("irritia", Rect(200 , 450, 400, 400)).front;

envView = EnvelopeView(w, Rect(20, 20, 355, 300))
			.thumbHeight_(6.0)
			.thumbWidth_(6.0)
			.fillColor_(Color.grey)
			.background_(Color.white)
			.drawLines_(true)
			.selectionColor_(Color.red)
			.drawRects_(true)
			.resize_(5) // can be resized and stretched
			.value_([{1.0.rand}!nNodes, {1.0.rand}!nNodes]);
			
// connect all the nodes in the envelope view to each other
nNodes.do({arg i; envView.connect(i, {|j|j}!nNodes); });

// create a little interaction where mouseactions affect the activity
UserView(w, Rect(20, 20, 355, 300))
	.mouseDownAction_({|view, x, y| mousedown = true; xLoc = x/355; yLoc = (-1+(y/300)).abs; })
	.mouseMoveAction_({|view, x, y| xLoc = x/355; yLoc = (-1+(y/300)).abs; })
	.mouseUpAction_({mousedown = false});

r = Routine({
	inf.do({ |i|
		envView.select(envView.size.rand);
		if(mousedown.not, {
			0.05.coin.if({
				0.5.coin.if({
					myWait = rrand(0.028, 0.042);
					xLoc = 1.0.rand; 
					yLoc = 1.0.rand;
				}); 
				randLoc = 0.12.rand2; 
			});
			xLoc = envView.x+rand2(randLoc); 
			yLoc = envView.y+rand2(randLoc);
		}, {
			xLoc = (xLoc + envView.x+rand2(0.1.rand))/2;
			yLoc = (yLoc + envView.y+rand2(0.1.rand))/2;
		});
		envView.x_(xLoc);
		envView.y_(yLoc);
		Synth(\irritia, [\freq, (yLoc*200)+50, \pan, (xLoc*2)-1]);
		myWait.wait;
	});
}).play(AppClock);

w.onClose_({ r.stop });

)

