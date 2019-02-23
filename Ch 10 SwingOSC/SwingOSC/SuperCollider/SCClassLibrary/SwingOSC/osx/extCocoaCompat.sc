/*
 *	Cocoa-Compatibility Extensions
 *	(SwingOSC classes for SuperCollider)
 *
 *	Copyright (c) 2005-2010 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 *
 *
 *	Changelog:
 */

/**
 *	You can use this extension so that
 *	cocoa gui behaves exactly as swingOSC gui,
 *	at least has some more graceful fallbacks...
 *
 *	@version	0.62, 21-May-09
 *	@author	Hanns Holger Rutz
 */
+ SCDragView {
	interpretDroppedStrings { ^false }
	
	interpretDroppedStrings_ { arg bool;
		"SCDragView.interpretDroppedStrings_ : not yet working".warn;
	}
}

+ SCEnvelopeView {
	font { ^nil }
	font_ { arg argFont;
		"SCEnvelopeView.font_ : not yet working".error;
	}

	clipThumbs { ^false }
	clipThumbs_ { arg bool;
		"SCEnvelopeView.clipThumbs_ : not yet working".error;
	}
	
	strings {
		"SCEnvelopeView.strings : not yet working".error;
		^nil;
	}

	connections {
		"SCEnvelopeView.connections : not yet working".error;
		^nil;
	}

	selection {
		"SCEnvelopeView.selection : not yet working".error;
		^(nil ! this.value.first.size);
	}

	deselectIndex { arg index;
		"SCEnvelopeView.deselectIndex : not yet working".error;
	}

	curve_ { arg curve = \lin;
		"SCEnvelopeView.curve_ : not yet working".error;
	}

	lockBounds_ { arg val;
		"SCEnvelopeView.lockBounds_ : not yet working".error;
	}
	
	horizontalEditMode_ { arg val;
		"SCEnvelopeView.horizontalEditMode_ : not yet working".error;
	}
}

+ SCMovieView {
	skipFrames { arg numFrames;
		"SCMovieView.skipFrames : not yet working".error;
	}

	frame_ { arg frameIdx;
		"SCMovieView.frame_ : not yet working".error;
	}

	fixedAspectRatio_ { arg bool;
		"SCMovieView.fixedAspectRatio_ : not yet working".error;
	}
}

+ SCNumberBox {
	minDecimals { ^0 }
	maxDecimals { ^8 }
	
	minDecimals_ { arg val;
		"SCNumberBox.minDecimals_ : not yet working".error;
	}
	
	maxDecimals_ { arg val;
		"SCNumberBox.maxDecimals_ : not yet working".error;
	}
}

+ SCPopUpMenu {
	allowsReselection { ^false }
	
	allowsReselection_ { arg bool;
		"SCPopUpMenu.allowsReselection_ : not yet working".error;
	}
}

+ SCSoundFileView {
	*cacheFolder { ^nil }
	*cacheFolder_ { arg path;
		"SCSoundFileView.cacheFolder_ : not yet working".error;
	}
	
	*cacheCapacity { ^50 }
	*cacheCapacity_ { arg megaBytes;
		"SCSoundFileView.cacheCapacity_ : not yet working".error;
	}
	
	*cacheActive { ^false }
	*cacheActive_ { arg bool;
		"SCSoundFileView.cacheActive_ : not yet working".error;
	}
}

+ SCTextView {
	caretColor { ^nil }
	caretColor_ { arg color;
		"SCTextView.caretColor_ : not yet working".error;
	}

	openURL { arg url;
		if( url.beginsWith( "file:" ), {
			url = url.copyToEnd( 5 );
			if( url.beginsWith( "//" ), {
				url = url.copyToEnd( 2 );
			});
			^this.open( url );
		}, {
			"SCTextView.openURL : only working with file protocol".error;
		});
	}
}

+ SCUserView {
	focusVisible { ^this.focusColor.alpha > 0 }
	focusVisible_ { arg visible;
		this.focusColor = Color( 0.0, 0.0, 0.0, if( visible, 0.5, 0.0 ));
//		"SCUserView.focusVisible_ : not yet working".error;
	}

	refreshOnFocus { ^true }
	refreshOnFocus_ { arg bool;
		"SCUserView.focusVisible_ : not yet working".error;
	}
}

//+ SCWindow {
//	unminimize {
//		"SCWindow.unminimize : not yet working".error;
//	}
//
//	visible_ { arg boo;
//		"SCWindow.visible_ : not yet working".error;
//	}
//}
