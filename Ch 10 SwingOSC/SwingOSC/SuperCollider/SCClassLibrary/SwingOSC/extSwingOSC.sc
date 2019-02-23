/*
 *	Helper class extensions for SwingOSC communication
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
 *	@author	Hanns Holger Rutz
 *	@version	0.61, 11-Aug-08
 */
//+ Object {
//	asSwingArg {
//		^this;
//	}
//}

+ Integer {
	asSwingArg {
		^[ this ];
	}
	oscEncSize { arg server; ^5 }
}

+ Float {
	asSwingArg {
		^[ this ];
	}
	oscEncSize { arg server; ^if( server.useDoubles, 9, 5 )}
}

+ Boolean {
	asSwingArg {
		^[ this.binaryValue ];
	}
}

+ ArrayedCollection {
	asSwingArg {
		^([ '[', '/array' ] ++ this.performUnaryOp( \asSwingArg ).flatten ++ ']');
	}
}

+ List {
	asSwingArg {
		^([ '[', '/method', "java.util.Arrays", \asList ] ++ this.asArray.asSwingArg ++ [ ']' ]);
	}
}

+ Nil {
	asSwingArg {
		^([ '[', '/ref', \null, ']' ]);
	}
}

+ String {
	// String is a subclass of ArrayedCollection!!
	asSwingArg {
		case { this.size !== 1 }
		{
			^[ this ];
		}
		{ this == "[" }	// must be escaped
		{
			^([ '[', '/ref', "brko", ']' ]);
		}
		{ this == "]" }	// must be escaped
		{
			^([ '[', '/ref', "brkc", ']' ]);
		}
		{
			^[ this ];
		};
	}

	oscEncSize { arg server; ^(((this.size + 4) & -4) + 1) }
}

+ Symbol {
	asSwingArg {
		^this.asString.asSwingArg;
	}
	
	oscEncSize { arg server; ^(((this.asString.size + 4) & -4) + 1) }
}

+ Color {
	asSwingArg {
		^([ '[', '/new', "java.awt.Color", this.red.asFloat, this.green.asFloat, this.blue.asFloat, this.alpha.asFloat, ']' ]);
	}
}

+ Point {
	asSwingArg {
		^([ '[', '/new', "java.awt.Point", this.x, this.y, ']' ]);
	}
}

+ Rect {
	asSwingArg {
		^([ '[', '/new', "java.awt.Rectangle", this.left, this.top, this.width, this.height, ']' ]);
	}
}

// Note: Gradient Paining doesn't work
// , at least with Aqua lnf the panels are not painted properly
+ Gradient {
	asSwingArg {
		^([ '[', '/new', "java.awt.GradientPaint", 0.0, 0.0 ] ++ color1.asSwingArg ++ [
			if( direction == \h, 1.0, 0.0 ), if( direction == \h, 0.0, 1.0 )] ++ color2.asSwingArg ++ [ ']' ]);
	}
}

+ HiliteGradient {
	asSwingArg {
		^([ '[', '/new', "java.awt.GradientPaint", 0.0, 0.0 ] ++ color1.asSwingArg ++ [
			if( direction == \h, frac, 0.0 ), if( direction == \h, 0.0, frac )] ++ color2.asSwingArg ++ [ ']' ]);
	}
}

+ Server {
	/**
	 *	There is a bug in unixCmd when running on MacOS X 10.3.9
	 *	which blocks successive unixCmd calls. This seems to
	 *	fix it (ONLY ONCE THOUGH). so call this method once after
	 *	you launching a second server
	 */
	unblockPipe {
		this.sendMsg( '/n_trace', 0 );
	}
	
//	asSwingArg {
//		XXX
//	}
}

//+ Synth {
//	asSwingArg {
//		^([ '[', '/method', "de.sciss.jcollider.Synth", \basicNew, this.defName ] ++ this.server.asSwingArg ++ [ this.nodeID, ']' ]);
//	}
//}
//
//+ Group {
//	asSwingArg {
//		^([ '[', '/method', "de.sciss.jcollider.Group", \basicNew ] ++ this.server.asSwingArg ++ [ this.nodeID, ']' ]);
//	}
//}
//
//
//+ Bus {
//	asSwingArg {
//		^([ '[', '/new', "de.sciss.jcollider.Bus" ] ++ this.server.asSwingArg ++ [ this.rate, this.index, this.numChannels, ']' ]);
//	}
//}
//
//+ Buffer {
//	asSwingArg {
//		^([ '[', '/new', "de.sciss.jcollider.Buffer" ] ++ this.server.asSwingArg ++ [ this.numFrames, this.numChannels, this.bufNum, ']' ]);
//	}
//}

+ NetAddr {
	asSwingArg {
		^([ '[', '/new', "java.net.InetSocketAddress", this.hostname, this.port, ']' ]);
	}
}


// These are required for correct functioning of SCViewHolder.
// SCViewHolder IS A CROSS-PLATFORM CLASS
// Therefore its extensions must not be in an osx-specific folder.

// don't blame me for this hackery
+ SCViewHolder {
//	prIsInsideContainer { ^false }
//	prSetScBounds {}
	prInvalidateBounds {}
	prInvalidateAllVisible {}
	prVisibilityChange {}
	protDraw {}
	id { ^nil }	// this is detected by JSCContainerView!
} 
