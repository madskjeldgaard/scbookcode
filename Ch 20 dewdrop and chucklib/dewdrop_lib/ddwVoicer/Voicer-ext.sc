
+ Ref {
	draggedIntoVoicerGUI { |dest| value.draggedIntoVoicerGUI(dest) }
}

+ Object {
	draggedIntoVoicerGCGUI { |gui|
		gui.model.spec = this.asSpec;
	}
}

	// needed because Cocoa GUI no longer interprets strings for you
+ String {
	draggedIntoVoicerGUI { |gui|
		^this.interpret.draggedIntoVoicerGUI(gui)
	}
	
	draggedIntoVoicerGCGUI { |gui|
		^this.interpret.draggedIntoVoicerGCGUI(gui)
	}
}
