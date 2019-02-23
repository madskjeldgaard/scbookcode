/* IZ 051207
============== BROWSER CONTROL WINDOW ==============
Use in "Current startup" Script to obtain a small window
with buttons for opening and closing browsers for the following items:
Scripts, Sessions, Samples, SynthDefs, MIDIListener.
*/

var browserControlWindow; // window with buttons for opening / closing browsers
var scripts, sessions, samples, synthdefs, midi;
var controller;

browserControlWindow = SCWindow("Browsers", Rect(330, 10, 150, 128));
browserControlWindow.view.decorator = FlowLayout(browserControlWindow.view.bounds);
scripts = SCButton(browserControlWindow, Rect(0,0,144,20))
	.states_([["Scripts", Color.red], ["Scripts", Color.black]])
	.action_({ | me |
		ScriptBrowser.perform([\makeGui, \closeGui][me.value]);
	})
	.value_(ScriptBrowser.window.isNil.binaryValue);

sessions = SCButton(browserControlWindow, Rect(0,0,144,20))
	.states_([["Sessions", Color.red], ["Sessions", Color.black]])
	.action_({ | me |
		Session.perform([\makeGui, \closeGui][me.value]);
	})
	.value_(if (Session.all.notNil) {
			Session.all.window.isNil.binaryValue;
	} {
		1
	});

//browserControlWindow.front;

samples = SCButton(browserControlWindow, Rect(0,0,144,20))
	.states_([["Samples", Color.red], ["Samples", Color.black]])
	.action_({ | me |
		Samples.perform([\makeGui, \closeGui][me.value]);
	})
	.value_(Samples.new.window.isNil.binaryValue);
synthdefs = SCButton(browserControlWindow, Rect(0,0,144,20))
	.states_([["Synthdefs", Color.red], ["Synthdefs", Color.black]])
	.action_({ | me |
		SynthDefs.perform([\makeGui, \closeGui][me.value]);
	})
	.value_(ScriptBrowser.window.isNil.binaryValue);
midi = SCButton(browserControlWindow, Rect(0,0,144,20))
	.states_([["MIDI", Color.red], ["MIDI", Color.black]])
	.action_({ | me |
		MIDIListener.perform([\makeGui, \closeGui][me.value]);
	})
	.value_(MIDIListener.window.isNil.binaryValue);

controller = { | who, how |
	switch (who,
		ScriptBrowser, {
			switch (how,
				\windowOpened, { scripts.value = 0 },
				\windowClosed, { scripts.value = 1 }
			)
		},
		Session, {
			switch (how,
				\windowOpened, {
					sessions.value = 0;
				},
				\windowClosed, { sessions.value = 1 }
			)
		},
		Samples, {
			switch (how,
				\windowOpened, { samples.value = 0 },
				\windowClosed, { samples.value = 1 }
			)
		},
		SynthDefs, {
			switch (how,
				\windowOpened, { synthdefs.value = 0 },
				\windowClosed, { synthdefs.value = 1 }
			)
		},
		MIDIListener, {
			switch (how,
				\windowOpened, { midi.value = 0 },
				\windowClosed, { midi.value = 1 }
			)
		}
	);
};

ScriptBrowser.addDependant(controller);
Session.addDependant(controller);
Samples.addDependant(controller);
SynthDefs.addDependant(controller);
MIDIListener.addDependant(controller);

browserControlWindow.front;
browserControlWindow.onClose = {
	ScriptBrowser.removeDependant(controller);
	Session.removeDependant(controller);
	Samples.removeDependant(controller);
	SynthDefs.removeDependant(controller);
	MIDIListener.removeDependant(controller);
};

