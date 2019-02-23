/*
 *	JSCNumberBox
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
 *		keydown behaviour 12/4/9 Alberto de Campo
 */

/**
 *	Replacement for the (Cocoa) SCNumberBox.
 *
 *	@author		Hanns Holger Rutz
 *	@version		0.61, 21-Apr-09
 */
JSCNumberBox : JSCTextEditBase {

	var <>step = 1;
	var <>scroll_step = 1; // a dummy for SC compatibility
	var <>scroll = true; // a dummy for SC compatibility

	var acResp;	// OSCpathResponder for action listening
	var txResp;
	var serverString = "";	// necessary coz we immediately store client-side on string_ !

	var <>shift_scale = 100.0, <>ctrl_scale = 10.0, <>alt_scale = 0.1;
	var <>clipLo = -inf, <>clipHi = inf;

	// mouse scrolling
	var scrollHit, scrollInc, scrollDir;
	
	// ----------------- public class methods -----------------

	*paletteExample { arg parent, bounds;
		^this.new( parent, bounds ).value_( 123.456 ); // .maxDecimals_( 4 );
	}

	// ----------------- public instance methods -----------------

	getScale { arg modifiers;
		^case
		{ (modifiers & 0x020000) != 0 } { shift_scale }
//		{ (modifiers & 0x040000) != 0 } { ctrl_scale }
		{ (modifiers & 0x100000) != 0 } { ctrl_scale } // cmd-key since ctrl is used for dnd
		{ (modifiers & 0x080000) != 0 } { alt_scale }
		{ 1 };
	}

	value_ { arg val; ^super.value_( val.clip( clipLo, clipHi ))}
	
	increment { arg zoom = 1; this.valueAction = this.value + (step * zoom) }
	decrement { arg zoom = 1; this.valueAction = this.value - (step * zoom) }
	
	defaultKeyDownAction { arg char, modifiers, unicode;
		if( unicode == 0xF700, { this.increment( this.getScale( modifiers )); ^this });
		if( unicode == 0xF703, { ^this });
		if( unicode == 0xF701, { this.decrement( this.getScale( modifiers )); ^this });
		if( unicode == 0xF702, { ^this });
		if( (char == $\r) || (char == $\n), { ^this }); // enter key
		if( char.isDecDigit or: { "+-.eE".includes( char )}, { ^this });
		^nil;	// bubble if it's an invalid key
	}

	defaultGetDrag { ^object.asFloat }
	defaultCanReceiveDrag { ^currentDrag.isNumber }

	defaultReceiveDrag {
		this.valueAction = currentDrag;	
	}

	maxDecimals {
		^this.getProperty( \maxDecimals, 8 );
	}
	
	maxDecimals_ { arg val;
		val = max( 0, val.asInteger );
		if( val < this.minDecimals, {
			this.minDecimals_( val );
		});
		this.setProperty( \maxDecimals, val );
	}
	
	minDecimals {
		^this.getProperty( \minDecimals, 0 );
	}
	
	minDecimals_ { arg val;
		val = max( 0, val.asInteger );
		if( val > this.maxDecimals, {
			this.maxDecimals_( val );
		});
		this.setProperty( \minDecimals, val );
	}
	
	// ----------------- private instance methods -----------------

// this is very ugly, leave away client side scrolling atm
//	prNeedsMouseHandler { ^true }
//
//	mouseDown { arg x, y, modifiers, buttonNumber, clickCount;
//		scrollHit = Point( x, y );
//		if( scroll, { scrollInc = this.getScale( modifiers )});
//		^super.mouseDown( x, y, modifiers, buttonNumber, clickCount );
//	}
//
//	mouseMove { arg x, y, modifiers;
//		var direction;
//		if( scroll, {
//			if( scrollDir.isNil, {
//				abs( x - scrollHit.x ).postln;
//				if( abs( y - scrollHit.y ) > 1, {
//					scrollDir = \v;
//					scrollHit.y = y + sign( scrollHit.y - y );
//					server.sendMsg( '/set', this.id, \cursor, '[', '/ref', \null, ']');
//				}, { if( abs( x - scrollHit.x ) > 1, {
//					scrollDir = \h;
//					scrollHit.x  = x + sign( scrollHit.x - x );
//					server.sendMsg( '/set', this.id, \cursor, '[', '/ref', \null, ']');
//				})});
//			});
//			if( scrollDir.notNil, {
//				if( scrollDir == \v, {
//					this.valueAction = this.value + ((scrollHit.y - y) * this.scroll_step * scrollInc);
//				}, {
//					this.valueAction = this.value + ((x - scrollHit.x) * this.scroll_step * scrollInc);
//				});
//				scrollHit = Point( x, y );
//			});
//		});
//		^super.mouseMove( x, y, modifiers );
//	}
//	
//	mouseUp { arg x, y, modifiers;
//		if( scrollDir.notNil, {
//			server.sendMsg( '/set', this.id, \cursor, '[', '/ref', \null, ']');
//		});
//		^super.mouseUp( x, y, modifiers );
//	}
	
	properties {
		^super.properties ++ #[ \minDecimals, \maxDecimals ];
	}

	prClose { arg preMsg, postMsg;
		acResp.remove;
		txResp.remove;
		^super.prClose( preMsg ++
			[[ '/method', "ac" ++ this.id, \remove ],
			 [ '/method', "tx" ++ this.id, \remove ],
			 [ '/free', "ac" ++ this.id, "tx" ++ this.id ]], postMsg );
	}

	prInitView {
		properties.put( \minDecimals, 0 );
		properties.put( \maxDecimals, 8 );
		acResp = OSCpathResponder( server.addr, [ '/number', this.id ], { arg time, resp, msg;
			// don't call valueAction coz we'd create a loop
			object = msg[4];
			properties.put( \string, msg[4].asString );
			{ this.doAction; }.defer;
		}).add;
		txResp = OSCpathResponder( server.addr, [ '/doc', this.id ], { arg time, resp, msg;
			var state, str;

			state = msg[2];
	
			case
			{ state === \insert }
			{
				str = msg[5].asString;
// doesn't work for UTF-8 chars, therefore don't print the warning at the moment ...
//if( msg[4] != str.size, { ("JSCNumberBox. len is "++msg[4]++"; but string got "++str.size).postln });
				serverString = serverString.insert( msg[3], str );
				object = serverString.asFloat;
			}
			{ state === \remove }
			{
				serverString = serverString.keep( msg[3] ) ++ serverString.drop( msg[3] + msg[4] );
				object = serverString.asFloat;
			};
		}).add;
		^this.prSCViewNew([
			[ '/set', '[', '/local', this.id,
				'[', '/new', "de.sciss.swingosc.NumberField", ']', ']',
				\space, '[', '/new', "de.sciss.util.NumberSpace", inf, -inf, 0.0, 0, 8, ']' ],
			[ '/method', parent.id, \add, '[', "/ref", this.id, ']' ],
			[ '/local', "ac" ++ this.id,
				'[', '/new', "de.sciss.swingosc.NumberResponder", this.id, \number, ']',
				"tx" ++ this.id,
				'[', '/new', "de.sciss.swingosc.DocumentResponder", this.id, ']' ]
		]);
	}

	prSendProperty { arg key, value;
		key	= key.asSymbol;

		// fix keys
		case { key === \string }
		{
			key 		= \number;
			value	= object; // .asFloat;
		}
		{ key === \minDecimals }
		{
			// send directly here because the array would
			// be distorted in super.prSendProperty by calling asSwingArg !!
			server.sendMsg( "/set", this.id, \space,
				'[', "/new", "de.sciss.util.NumberSpace", inf, -inf, 0.0, value, this.maxDecimals, ']'
			);
			^nil;
		}
		{ key === \maxDecimals }
		{
			// send directly here because the array would
			// be distorted in super.prSendProperty by calling asSwingArg !!
			server.sendMsg( "/set", this.id, \space,
				'[', "/new", "de.sciss.util.NumberSpace", inf, -inf, 0.0, this.minDecimals, value, ']'
			);
			^nil;
		};
		^super.prSendProperty( key, value );
	}
}