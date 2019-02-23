
MixerControl : GlobalControlBase {
	var <>mixerGui, controlKey;

	update { |bus, msg|
		value = msg[0];
		(mixerGui.notNil).if({
			mixerGui.updateView(controlKey, value)
		});
	}
	
	watch { |key, gui|
		mixerGui = gui;
		controlKey = key;
		super.watch;
	}
	
	stopWatching {
		mixerGui = controlKey = nil;
		super.stopWatching;
	}
	
	makeGUI {}	// MixerChannelGUI class does this
}