/*
 *	JSCTextField
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
 *	Replacement for the (Cocoa) SCTextField.
 *
 *	@author		Hanns Holger Rutz
 *	@version		0.61, 11-Aug-08
 */
JSCTextField : JSCTextEditBase {   // not a child class of JSCNumberBox

// JJJ
//	*viewClass { ^SCNumberBox }
	
	var acResp;	// OSCpathResponder for action listening
	var txResp;
	var serverString = "";	// necessary coz we immediately store client-side on string_ !
	
	// ----------------- public instance methods -----------------

	defaultKeyDownAction { arg key, modifiers, unicode; ^this } // swallow all

	string_ { arg s; super.string = s.as( String )}

// doesn't work yet
	defaultGetDrag { ^this.string }
	defaultCanReceiveDrag { ^currentDrag.isString }
	defaultReceiveDrag { this.valueAction = currentDrag }

	// ----------------- private instance methods -----------------

	prClose { arg preMsg, postMsg;
		acResp.remove;
		txResp.remove;
		^super.prClose( preMsg ++
			[[ '/method', "ac" ++ this.id, \remove ],
			 [ '/method', "tx" ++ this.id, \remove ],
			 [ '/free', "ac" ++ this.id, "tx" ++ this.id ]], postMsg );
	}

	prInitView {
		acResp = OSCpathResponder( server.addr, [ '/action', this.id ], { arg time, resp, msg;
			// don't call valueAction coz we'd create a loop
			object = msg[4].asString;
			string = object;
			properties.put( \string, object );
			{ this.doAction }.defer;
		}).add;
		txResp = OSCpathResponder( server.addr, [ '/doc', this.id ], { arg time, resp, msg;
			var state, str;
			
			state = msg[2];
	
			case
			{ state === \insert }
			{
				str = msg[5].asString;
if( msg[4] != str.size, { ("Yukk. len is "++msg[4]++"; but string got "++str.size).postln });
				string = object = serverString = serverString.insert( msg[3], str );
			}
			{ state === \remove }
			{
				string = object = serverString = serverString.keep( msg[3] ) ++ serverString.drop( msg[3] + msg[4] );
			};
		}).add;
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.TextField", ']',
				"ac" ++ this.id,
				'[', '/new', "de.sciss.swingosc.ActionResponder", this.id, \text, ']',
				"tx" ++ this.id,
				'[', '/new', "de.sciss.swingosc.DocumentResponder", this.id, ']' ]
		]);
	}
}