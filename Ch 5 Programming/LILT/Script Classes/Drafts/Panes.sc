/* IZ 2007-06-29 { SC3
Needs redoing -- get rid of classvars, maybe model in nested manner ....
} */


Panes {
//	classvar <currentWindow, <currentPanes, <currentPane;
	var <window;
//	var <parent;
	var <panes;
	*new { | window |
		^this.newCopyArgs(window).init;
	}
	init {
//		currentWindow = window;
//		currentPanes = this;
		panes = (top: window.view);
	}
/*	*use { | paneName = \top |
		var pane;
		if (currentPanes.isNil) { ^Post << "No pane environment is active - cannot select pane\n" };
		pane = currentPanes.getPane(paneName);
		if (pane.isNil) { ^Post << "Cannot find pane named:" << paneName << "\n" };
		currentPane = pane;
	}
*/	getPane { | paneName |
		^panes[paneName];
	}
	split { | paneName = \top, orientation = \horizontal ... paneNamesDims |
		var pane;
		pane = this.getPane(paneName);
		if (pane.isNil) { ^Post << "cannot find pane named " << paneName << "\n" };
		if (orientation == \horizontal) {
			this.splitHorizontally(pane, paneNamesDims)
		}{
			this.splitVertically(pane, paneNamesDims)
		}
	}
	splitHorizontally { | pane, paneNamesDims |
		var maxWidth, height, panes, x, left = 0;
		maxWidth = pane.width;
		paneNamesDims pairsDo: { | pane, width |
			case
			{ x == 0 } { }
			{ 0 < x < 1 } {
				
			}{
				
			}
		}
	}
}
