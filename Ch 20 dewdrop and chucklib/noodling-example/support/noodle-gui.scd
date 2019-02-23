
// set up guis etc. for composition development

var	masterLayout, chuckKeyCtl, composeDocKeyResp,
	originalDocInit;		// must maintain independent of environment

var	path;

~toolbarFlow.isNil.if({

Library.at(\mixer, \smallskin).isNil.if({
	Library.put(\mixer, \smallskin, MixerSkin.new.maxAcross_(1));
});

AbstractChuckArray.loadWindowBounds;

MIDIPort.init;
MIDIPort.resetAll;
MIDIPort.autoFreeSockets = false;	// protect midi controllers from being destroyed accidentally

s.boot;

~statusW = GUI.window.new("Building GUI, please wait...",
	Rect(GUI.window.screenBounds.width - 250 div: 2,
	GUI.window.screenBounds.height - 40 div: 2,
	250, 40));
~statusSl = GUI.rangeSlider.new(~statusW, Rect(10, 15, 230, 10))
	.lo_(0).hi_(0);
~numUpdates = (~voicerRows * ~voicerColumns) + 6;
~currentUpdate = 0;
~advanceStatus = {
	~currentUpdate = ~currentUpdate + 1;
	~statusSl.hi_(~currentUpdate / ~numUpdates);
};
~statusW.front;

~masterLayout = ResizeFlowWindow("control panel");
masterLayout = ~masterLayout;

~toolbarFlow = FlowView(~masterLayout.view, Rect(0, 0, 290, GUI.window.screenBounds.height-5));
(~useScroll ? false).if({
	~voicerScroll = GUI.scrollView.new(~masterLayout.view, Rect(0, 0, 450 * ~voicerColumns,
		GUI.window.screenBounds.height-50)).hasHorizontalScroller_(false);
}, {
	~voicerScroll = ~masterLayout.view;
});
~voicerFlow = FlowView(~voicerScroll, Rect(0, 0, 450 * ~voicerColumns, 20000));

GUI.staticText.new(~toolbarFlow, Rect(0, 0, 280, 20)).string_("Toolbar").align_(\center);

// voicerproxies -- creating this gui is time sensitive
// thus the remainder of the gui building is wrapped in a routine that is run on AppClock
Routine({
	var	count = 0;
	(~voicerRows * ~voicerColumns).do({ |i|
		VoicerProxy.new => VP.prNew(i);
			// first proxy has 4 controls, the rest have 3
		(3 + (count < ~voicersWith4Controls).binaryValue).do({
			VP(i).v.addControlProxy(nil, true)
		});
		count = count + 1;
		VP(i).v.maxControlProxies = VP(i).v.controlProxies.size;
		VP(i).v.smallGui(~voicerFlow, nil, nil, nil, nil, false);
		0.2.wait;
		~advanceStatus.();
		(i+1 % ~voicerColumns == 0).if({ ~voicerFlow.startRow; });
	});

~voicerFlow.recursiveResize;


// MIDI bufs

MIDIBufManager(nil, 0) => MBM.prNew(0);
MBM(0).v.gui(~toolbarFlow, Rect(0, 0, 290, 300));

// midi trigger
~toolbarFlow.startRow;
MT(1).gui(~toolbarFlow);

0.2.wait;
~advanceStatus.();

// make draggable modwheel and pitchbend controls
~midiControlFlow = FlowView.new(~toolbarFlow, Rect(0, 0, 80, 250));
VoicerMIDIController.defaultDest = VoicerGCDummy;
#[mw, pb].do({ |type|
	var mc;
	mc = VoicerMIDIController(\omni, type);
	GUI.dragSource.new(~midiControlFlow, Rect(0, 0, 30, 20))
		.align_(\center)
		.object_(mc).string_(mc.ccnum.shortName)
		.beginDragAction_({ |drag| drag.object });
//		.action_({ |drag|
//			try { drag.string_(mc.ccnum.shortName) }
//		});
	mc => CC.prNew(mc.ccnum.shortName.asSymbol);
});

~midiControlFlow.startRow;

GUI.dragSink.new(~midiControlFlow, 80@20).string_("reset").align_(\center)
	.background_(Color.new255(190, 198, 225)).stringColor_(Color.new255(191, 0, 0))
	.action_({ |drag|
		try { drag.object.reset };
		drag.string = "reset";
	});

GUI.dragSink.new(~midiControlFlow, 80@20).string_("free").align_(\center)
	.background_(Color.new255(190, 198, 225)).stringColor_(Color.new255(191, 0, 0))
	.action_({ |drag|
		var	vc, vp;
		(drag.object.class == VoicerProxy).if({
			vp = drag.object;
			vc = VC.collection.detect({ |vc| vc.v === vp.voicer });
			try { vc.free }
		}, {
			try { drag.object.free };
		});
		drag.string = "free";
	});

GUI.button.new(~midiControlFlow, 80@20)
	.states_([
		["use VProxy", Color.new255(191, 0, 0), Color.new255(190, 198, 225)],
		["use Voicer", Color.new255(191, 0, 0), Color.new255(190, 198, 225)],
	])
	.value_(1)
	.action_({ |view|
		BP.useVoicerProxy = (view.value == 0);
	});
BP.useVoicerProxy = false;

0.2.wait;
~advanceStatus.();

~toolbarFlow.startRow;
s.gui(~toolbarFlow);

~toolbarFlow.startRow;

~chuckBrowser = ChuckableBrowser(~toolbarFlow);
~chuckKeyCtl = ChuckBrowserKeyController(~chuckBrowser);
chuckKeyCtl = ~chuckKeyCtl;

	// ctrl-` from gui switches focus to current document
	// any other key, if not caught by a view, switches focus back to the chuck key controller
	// and passes key thru
if(thisProcess.platform.name == \osx) {
	~masterLayout.view.keyDownAction_({ |view, char, mods, unicode, keycode|
	//view.dumpBackTrace;
	//[char, mods, unicode, keycode].debug("bubbled up to masterlayout");
		(unicode == 30 and: { (mods bitAnd: 0x40000) > 0 }).if({
			Document.current.front;
		}, {
			chuckKeyCtl.focus.doKey(view, char, mods, unicode, keycode);
		});
	});
};

0.2.wait;
~advanceStatus.();

// fix views with nil keyDownActions so that bubbling will be right
// defaultKeyDownAction returns nil if it should bubble, so func should just pass the return thru
f = { |view|
		// MIDIBufManager gui uses a toggletextfield - this change to key down action breaks it
	(view.class.name != 'ToggleTextField').if({
		(view.respondsTo(\keyDownAction) and: { view.keyDownAction.isNil }).if({
			view.keyDownAction = { |view, char, modifiers, unicode, keycode|
				view.defaultKeyDownAction(char, modifiers, unicode, keycode)
			};
		});
			// nil-do is its own nil check
		view.tryPerform(\children).do(f.value(_));
	});
};

f.value(~masterLayout.view);
f = nil;

~toolbarFlow.recursiveResize;

0.2.wait;
~advanceStatus.();

//~masterLayout.recursiveResize.front;

// mixer gui slots
~board = MixingBoard("mixer control pool", Library.at(\mixer, \smallskin));
8.do({ |i| MixerChannelGUI(nil, ~board) => MCG.prNew(i);
	~board.add(MCG(i).v);
});
1.0.wait;
~advanceStatus.();
~board.refresh;
1.0.wait;
~advanceStatus.();

~masterLayout.window.bounds = ~controlpanelBounds;
	// test scrollability
~voicerScroll.respondsTo(\visibleOrigin).if({
	~voicerScroll.bounds = ~voicerScroll.bounds.width_(305)
});
~board.w.bounds = ~mixerBounds;
if(thisProcess.platform.name == \osx) {
	~codedoc = Document.new("type code");
	~codedoc.bounds_(~codedocBounds);
	Document.listener.bounds = ~postwindowBounds;
};

Library.put(\codeBounds, ~codedocBounds);

~statusW.close;
~statusW = ~statusSl = ~numUpdates = ~currentUpdate = ~advanceStatus = nil;
~masterLayout.front.refresh;

}).play(AppClock);

}, {
	"gui already loaded".warn;
});
