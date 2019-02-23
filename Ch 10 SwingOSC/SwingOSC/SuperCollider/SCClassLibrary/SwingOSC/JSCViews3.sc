/*
 *	JSCViews collection 3
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
 *	@version		0.61, 11-Aug-08
 *	@author		Hanns Holger Rutz
 */
JSCCheckBox : JSCControlView {
	var acResp;	// OSCpathResponder for action listening

	// ----------------- public class methods -----------------

	*paletteExample { arg parent, bounds;
		^this.new( parent, bounds ).value_( true );
	}
	
	// ----------------- public instance methods -----------------

	value_ { arg val;
		this.setProperty( \value, val );
	}
	
	value { ^this.getProperty( \value ); }
	
	valueAction_ { arg val;
		this.setPropertyWithAction( \value, val );
	}	

	font_ { arg font;
		this.setProperty( \font, font );
	}
	
	font { ^this.getProperty( \font ); }

	string_ { arg string;
		this.setProperty( \string, string );
	}

	string { ^this.getProperty( \string ); }
	
	defaultGetDrag { 
		^this.value;
	}
	
	defaultCanReceiveDrag {
		^currentDrag.isNumber or: { currentDrag.isKindOf( Function ) or: { currentDrag.isKindOf( Boolean )}};
	}
	
	defaultReceiveDrag {
		case
		{ currentDrag.isNumber }
		{
			this.valueAction = currentDrag != 0;
		}
		{ currentDrag.isKindOf( Boolean )}
		{
			this.valueAction = currentDrag;
		}
		{ currentDrag.isKindOf( Function )}
		{
			this.action = currentDrag;
		};
	}

	// ----------------- private instance methods -----------------

	properties {
		^super.properties ++ #[ \value, \font, \string ];
	}
	
	prNeedsTransferHandler {
		^true;
	}

	prClose { arg preMsg, postMsg;
		acResp.remove;
		^super.prClose( preMsg ++
			[[ '/method', "ac" ++ this.id, \remove ],
			 [ '/free', "ac" ++ this.id ]], postMsg );
	}

	prInitView {
		properties.put( \value, false );
		acResp = OSCpathResponder( server.addr, [ '/action', this.id ], { arg time, resp, msg;
			// don't call valueAction coz we'd create a loop
			properties.put( \value, msg[4] != 0 );
			{ this.doAction; }.defer;
		}).add;
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.CheckBox", ']',
				"ac" ++ this.id,
				'[', '/new', "de.sciss.swingosc.ActionResponder", this.id, \selected, ']' ]
		]);
	}

	prSendProperty { arg key, value;
		key	= key.asSymbol;

		// fix keys
		case { key === \value }
		{
			key = \selected;
		}
		{ key === \string }
		{
			key = \text;
		};
		^super.prSendProperty( key, value );
	}
}

JSCTabbedPane : JSCContainerView {
	var tabs;		// List -> (IdentityDictionary properties)
	var acResp;	// OSCpathResponder for change listening
	
	// ------------- public general methods -------------

	font_ { arg font;
		this.setProperty( \font, font );
	}
	
	font { ^this.getProperty( \font ); }
	
	tabPlacement_ { arg type;
		this.setProperty( \placement, type );
	}
	
	tabPlacement { ^this.getProperty( \placement ); }
	
	numTabs { ^tabs.size }
	
	value_ { arg index;
		this.setProperty( \value, index );
	}

	valueAction_ { arg index;
		this.setPropertyWithAction( \value, index );
	}	
	
	value { ^this.getProperty( \value ); }

	// ------------- public per tab methods -------------

	setTitleAt { arg index, title;
		this.prSetTabProperty( index, title, \title, \setTitleAt );
	}
	
	getTitleAt { arg index;
		^this.prGetTabProperty( index, \title );
	}

	setEnabledAt { arg index, enabled;
		this.prSetTabProperty( index, enabled, \enabled, \setEnabledAt );
	}

	getEnabledAt { arg index;
		^this.prGetTabProperty( index, \enabled );
	}

	setBackgroundAt { arg index, color;
		this.prSetTabProperty( index, color, \background, \setBackgroundAt );
	}

	getBackgroundAt { arg index;
		^this.prGetTabProperty( index, \background );
	}

	setForegroundAt { arg index, color;
		this.prSetTabProperty( index, color, \foreground, \setForegroundAt );
	}

	getForegroundAt { arg index;
		^this.prGetTabProperty( index, \foreground );
	}

	setToolTipAt { arg index, text;
		this.prSetTabProperty( index, text, \tooltip, \setToolTipTextAt );
	}

	getToolTipAt { arg index;
		^this.prGetTabProperty( index, \tooltip );
	}

	// ------------- private methods -------------
	
	add { arg child;
		var tab;

		tab = IdentityDictionary.new;
		tab.put( \enabled, true );
		tab.put( \component, child );
		tabs.add( tab );
		if( this.value.isNil, { properties.put( \value, 0 )});
		^super.add( child );
	}

	prSetTabProperty { arg index, value, key, javaSelector;
		var tab;
		if( index == -1, {
			tabs.size.do({ arg index; this.prSetTabProperty( index, value, key, javaSelector )});
			^this;
		});
		tab = tabs[ index ];
		if( tab.notNil, {
			tab.put( key, value );
			server.listSendMsg([ '/method', this.id, javaSelector, index ] ++ value.asSwingArg );
		}, {
			this.prMethodError( thisMethod, "Illegal tab index " ++ index ++ " (" ++ key ++ ")" );
		});
	}
	
	prGetTabProperty { arg index, key;
		var tab;	
		tab = tabs[ index ];
		if( tab.notNil, {
			^tab[ key ];
		}, {
			this.prMethodError( thisMethod, "Illegal tab index " ++ index ++ " (" ++ key ++ ")" );
			^nil;
		});
	}

	prRemoveChild { arg child;
		block { arg break;
			tabs.do({ arg tab, index;
				if( tab[ \component ] === child, {
					tabs.removeAt( index );
					break.value;
				});
			});
			this.prMethodError( thisMethod, "Child was not a registered tab : " ++ child );
		};
		^super.prRemoveChild( child );
	}

	prInitView {
		tabs = List.new;
		properties.put( \opaque, false );
		acResp = OSCpathResponder( server.addr, [ '/action', this.id ], { arg time, resp, msg;
			// don't call valueAction coz we'd create a loop
			properties.put( \value, msg[ 4 ]);
			{ this.doAction; }.defer;
		}).add;
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.TabbedPane", ']',
				"ac" ++ this.id,
				'[', '/new', "de.sciss.swingosc.ActionResponder", this.id, \selectedIndex, ']' ]
		]);
	}

	prSendProperty { arg key, value;
		key	= key.asSymbol;

		// fix keys
		case
		{ key === \value }
		{
			key 		= \selectedIndexNoAction;
		}
		{ key === \placement }
		{
			key 		= \tabPlacement;
			value	= [ \top, \left, \bottom, \right ].indexOf( value ) + 1;
		};
		^super.prSendProperty( key, value );
	}

	prMethodError { arg methodName, message;
		(this.class.name ++ "." ++ methodName ++ " failed : " ++ message).error;
	}

	prClose { arg preMsg, postMsg;
		acResp.remove;
		^super.prClose( preMsg ++
			[[ '/method', "ac" ++ this.id, \remove ],
			 [ '/free', "ac" ++ this.id ]], postMsg );
	}

//	public void setMnemonicAt(int tabIndex, int mnemonic)
}

//// !!! DEPRECATED !!!
// JSCScrollPane : JSCContainerView {
//	horizontalScrollBarShown_ { arg type;
//		this.setProperty( \hPolicy, type );
//	}
//
//	horizontalScrollBarShown { ^this.getProperty( \hPolicy ); }
//
//	verticalScrollBarShown_ { arg type;
//		this.setProperty( \vPolicy, type );
//	}
//
//	verticalScrollBarShown { ^this.getProperty( \vPolicy ); }
//	
//	add { arg child;
//		var bndl;
//
//		if( children.size > 0, {
//			MethodError( "Cannot add more than one child", this ).throw;
//		});
//
//		children = children.add( child );
//		bndl = List.new;
//		bndl.add([ '/method', this.id, \setViewportView,
//				'[', '/ref', child.prIsInsideContainer.if({ "cn" ++ child.id }, child.id ), ']' ]);
////		if( this.prGetWindow.visible, {
////			bndl.add([ '/method', this.id, \revalidate ]);
////		});
//		server.listSendBundle( nil, bndl );
//	}
//
//	prSendProperty { arg key, value;
//		key	= key.asSymbol;
//
//		// fix keys
//		case
//		{ key === \hPolicy }
//		{
//			key 		= \horizontalScrollBarPolicy;
//			value	= [ \auto, \never, \always ].indexOf( value ) + 30;
//		}
//		{ key === \vPolicy }
//		{
//			key 		= \verticalScrollBarPolicy;
//			value	= [ \auto, \never, \always ].indexOf( value ) + 20;
//		};
//		^super.prSendProperty( key, value );
//	}
//	
//	prRemoveChild { arg child;
//		children.remove( child );
//		server.sendMsg( '/method', this.id, \setViewportView, '[', '/ref', \null, ']' );
//	}
//
//	prSCViewNew {
//		^super.prSCViewNew([
//			[ '/local', this.id, '[', '/new', "javax.swing.JScrollPane", ']' ]
//		]);
//	}
// }

JSCScrollBar : JSCControlView {
	var acResp;	// OSCpathResponder for action listening
	var clpse;
	var orientation;	// 0 for horiz, 1 for vert
	var maxExtent;
	var <isAdjusting = false;

	// ----------------- public class methods -----------------

	*paletteExample { arg parent, bounds;
		^this.new( parent, bounds ).extent_( 0.1 );
	}
	
	// ----------------- public instance methods -----------------

	value_ { arg val;
		val = val.clip( 0.0, 1.0 - this.extent );
		this.setProperty( \value, val );
	}
	
	value { ^this.getProperty( \value )}
	
	valueAction_ { arg val;
		val = val.clip( 0.0, 1.0 - this.extent );
		this.setPropertyWithAction( \value, val );
	}
	
	extent_ { arg val;
		var val2;
		val = val.clip( 0.0, maxExtent );
		if( (this.value + val) <= 1.0, {
			this.setProperty( \extent, val );
		}, {
			val2 = 1.0 - val;
			properties.put( \value, val2 );
			properties.put( \extent, val );
			server.sendMsg( '/method', this.id, \setValuesNoAction,
				(val2 * 0x40000000).asInteger, (val * 0x40000000).asInteger, 0, 0x40000000 );
		});
	}

	extent { ^this.getProperty( \extent )}
	
	setSpan { arg lo, hi;
		var value, extent;
		value	= min( lo, hi );
		extent	= max( lo, hi ) - value;
		extent	= extent.clip( 0.0, maxExtent );
		value	= value.clip( 0.0, 1.0 - extent );
		properties.put( \value, value );
		properties.put( \extent, extent );
		value	= value * 0x40000000;
		extent	= extent * 0x40000000;
		server.sendBundle( nil,	[ '/method', this.id, \setValuesNoAction, value.asInteger, extent.asInteger, 0, 0x40000000 ],
							[ '/set', this.id,
								\unitIncrement, (this.unitIncrement * extent).asInteger.clip( 1, 0x40000000 ),
								\blockIncrement, (this.blockIncrement * extent).asInteger.clip( 1, 0x40000000 )]);
	}
	
	setSpanActive { arg lo, hi;
		this.setSpan( lo, hi );
		this.doAction;
	}

	unitIncrement_ { arg val;
		val = val.clip( 0.0, 1.0 / this.extent );
		this.setProperty( \unitIncrement, val );
	}
	
	unitIncrement { ^this.getProperty( \unitIncrement )}

	blockIncrement_ { arg val;
		val = val.clip( 0.0, 1.0 / this.extent );
		this.setProperty( \blockIncrement, val );
	}

	blockIncrement { ^this.getProperty( \blockIncrement )}

	defaultGetDrag { ^Point( this.value, this.value + this.extent )}	
	defaultCanReceiveDrag {
		^currentDrag.isNumber or: { currentDrag.isKindOf( Function ) or: { currentDrag.isKindOf( Point )}};
	}
	
	defaultReceiveDrag {
		case
		{ currentDrag.isNumber }
		{
			this.valueAction = currentDrag;
		}
		{ currentDrag.isKindOf( Function )}
		{
			this.action = currentDrag;
		}
		{ currentDrag.isKindOf( Point )}
		{
			this.setSpanActive( min( currentDrag.x, currentDrag.y ), abs( currentDrag.x - currentDrag.y ));
		};
	}

	bounds_ { arg rect;
		var result;
		result = super.bounds_( rect );
		if( if( rect.width > rect.height, 0, 1 ) != orientation, {
			orientation = 1 - orientation;
			server.sendMsg( '/set', this.id, \orientation, orientation );
		});
		^result;
	}

	// ----------------- private instance methods -----------------

	properties {
		^super.properties ++ #[ \value ];
	}
	
	prNeedsTransferHandler { ^true }

	prClose { arg preMsg, postMsg;
		acResp.remove;
		clpse.cancel;
		^super.prClose( preMsg ++
			[[ '/method', "ac" ++ this.id, \remove ],
			 [ '/free', "ac" ++ this.id ]], postMsg );
	}

	prInitView {
		var b;
		maxExtent		= 0x3FFFFFFF / 0x40000000;
		properties.put( \value, 0.0 );
		properties.put( \extent, 1.0 );
		properties.put( \blockIncrement, 1.0 );
		properties.put( \unitIncrement, 0.1 );
		b			= this.prBoundsReadOnly;
		orientation	= if( b.width > b.height, 0, 1 );
		clpse		= Collapse({ this.doAction });
		acResp		= OSCpathResponder( server.addr, [ '/action', this.id ], { arg time, resp, msg;
			var newVal, newAdjust;
			newVal		= msg[4].asInteger / 0x40000000;
			newAdjust		= msg[6] != 0;
//			[ msg[6], isAdjusting ].postln;
			if( (newVal != this.value) or: { newAdjust != isAdjusting }, {
				isAdjusting = newAdjust;
				// don't call valueAction coz we'd create a loop
				properties.put( \value, newVal );
				clpse.instantaneous;
			});
		}).add;
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.ScrollBar", orientation, 0, 0x40000000, 0, 0x40000000, ']',
				"ac" ++ this.id,
				'[', '/new', "de.sciss.swingosc.ActionResponder", this.id, '[', '/array', \value, \valueIsAdjusting, ']', ']' ],
			[ '/set', this.id, \unitIncrement, 0x06666666, \blockIncrement, 0x40000000 ]
		]);
	}

	prSendProperty { arg key, value;
		key	= key.asSymbol;

		// fix keys
		case { key === \value }
		{
			key		= \valueNoAction;
			value	= (value * 0x40000000).asInteger;
		}
		{ key === \extent }
		{
			value = value * 0x40000000;
			server.sendMsg( '/set', this.id, \visibleAmount, value.asInteger,
				\unitIncrement, (this.unitIncrement * value).asInteger.clip( 1, 0x40000000 ),
				\blockIncrement, (this.blockIncrement * value).asInteger.clip( 1, 0x40000000 ));
			^nil;
		}
		{ key === \unitIncrement }
		{
			value = (value * this.extent * 0x40000000).asInteger.clip( 1, 0x40000000 );
		}
		{ key === \blockIncrement }
		{
			value = (value * this.extent * 0x40000000).asInteger.clip( 1, 0x40000000 );
		};
		^super.prSendProperty( key, value );
	}
}