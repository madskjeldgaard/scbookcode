/* (IZ 2005-09-03) {
Utilities for popping up dialog windows:
Warn: present a message in a pop up window instead of posting it on the Untitled window.

DialogWindow.new
Warn("THIS IS NOT right");
Confirm("Is this right?", {| ok | (if (ok) { "yes"}{"no"}).postln});
TextDialog("Enter some text", "My default text is customizeable\nand more ... ...",
	{| ok, text | if (ok) 
		{ 
			"You entered the text between the ==== lines:\n====================".postln;
			text.postln;
			"====================".postln;
		}{
			"Enter text was canceled ...".postln;
		}
	}
);
} */


DialogWindow {
	var <>title, <>text = "", <>onClose, <>numButtons = 1;
	var <>editable = false, <>ok = true;
	var window, textView, yesButton, noButton;
	*new { | title, text = "", onClose, numButtons = 1, editable = false, ok = true |
			^this.newCopyArgs(title, text, onClose, numButtons, editable, ok).init;
	}
	init {
		window = SCWindow(title ? "Warning:", Rect(200, 200, 400, 220).fromTop)
		.onClose_({ |me|
			onClose.(ok, textView.string, me);
			window = nil;
		});
		textView = SCTextView(window, Rect(0,0,400,195))
			.font_(this.textFont)
			.stringColor_(this.textColor)
			.string_(text ? "-")
			.editable_(editable);
		yesButton = SCButton(window, Rect(3, 197, 392/numButtons, 20))
			.states_([["OK"]])
			.action_({ ok = true; window.close });
		yesButton.focus(true); // if only view, then focus it anyway
		if (numButtons == 2) {
			noButton = SCButton(window, Rect(200, 197, 194, 20))
				.states_([["CANCEL"]])
				.action_({ ok = false; window.close });
			noButton.focus(ok.not); // if present, focus if not ok
		};
		if (editable) { textView.focus(true);};
		window.front;
	}
	textColor { ^Color.red }
	textFont { ^Font("Helvetica-Bold", 12) }
	autoCloseAfter { | seconds = 7 |
		{ if (window.notNil) { window.close } }.defer(seconds);
	}
}

Warn : DialogWindow {
	*new { | message, title, onClose |
		^super.new(title ? "WARNING:", message, onClose);
	}
}

Confirm : DialogWindow {
	*new { | message, onClose, ok = false |
		^super.new("Confirm:", message, onClose, 2, false, ok);
	}
}

TextDialog : DialogWindow {
	*new { | title, message, onClose, ok = true |
		^super.new(title ? "Edit text:", message, onClose, 2, true, ok);
	}
	textColor { ^Color.black }
	textFont { ^Font("Monaco", 12) }
}