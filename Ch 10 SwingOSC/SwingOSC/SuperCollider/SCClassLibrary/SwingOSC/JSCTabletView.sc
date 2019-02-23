/*
 *	JSCTabletView
 *	(SwingOSC classes for SuperCollider)
 *
 *	Copyright (c) 2005-2008 Hanns Holger Rutz. All rights reserved.
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
 *	@version		0.62, 11-Aug-08
 *	@author		Hanns Holger Rutz
 */
JSCTabletView : JSCAbstractUserView {
	var <>proximityAction;

	var tabletResp, cocoaBorder;
	
	// ----------------- private instance methods -----------------

	// args:	x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
	//		absoluteX, absoluteY, buttonMask, tanPressure;
	doAction { arg ... args;
		action.value( this, *args );
		mouseMoveAction.value( this, *args );
	}

	// ----------------- private instance methods -----------------

	// args:	x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
	//		absoluteX, absoluteY, buttonMask, tanPressure;
	mouseDown { arg ... args;
		mouseDownAction.value( this, *args );
	}

	// args:	x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
	//		absoluteX, absoluteY, buttonMask, tanPressure;
	mouseUp { arg ... args;
		mouseUpAction.value( this, *args );
	}

	prClose { arg preMsg, postMsg;
		tabletResp.remove;
		^super.prClose( preMsg ++
			[[ '/method', "tab" ++ this.id, \remove ],
			 [ '/free', "tab" ++ this.id ]], postMsg );
	}

	prInitView {
		var bndl;
		
		relativeOrigin	= true;
		cocoaBorder		= if( parent.prGetWindow.border, 20, -2 );
		jinsets			= Insets( 3, 3, 3, 3 );
		bndl				= List.new;
		bndl.add([ '/local', this.id, '[', '/new', "de.sciss.swingosc.TabletView", ']' ]);
		this.prCreateTabletResponder( bndl );
		^this.prSCViewNew( bndl.asArray );
	}

	prCreateMouseResponder {} // overridden to not create one

	prCreateTabletResponder { arg bndl;
		var msg, win;
	
		if( tabletResp.notNil, {
			"JSCTabletView.prCreateTabletResponder : already created!".warn;
			^bndl;
		});
//		clpseMouseMove	= Collapse({ arg x, y, modifiers; this.mouseOver( x, y, modifiers )});
//		clpseMouseDrag	= Collapse({ arg x, y, modifiers; this.mouseMove( x, y, modifiers )});
	// [ "/tablet", <componentID>, <state>, <deviceID>, <localX>, <localY>, <pressure>,
	//   <tiltX>, <tiltY>, <rota>, <tanPressure>, <absX>, <absY>, <absZ>,
	//   <buttonMask>, <clickCount>
		tabletResp		= OSCpathResponder( server.addr, [ '/tablet', this.id ], { arg time, resp, msg;
			var state, deviceID, x, y, pressure, tiltx, tilty, rotation, tanPressure, absoluteX, absoluteY, absoluteZ,
			    buttonMask, clickCount, buttonNumber, bounds, entering, systemTabletID, tabletID, pointingDeviceType,
			    uniqueID, pointingDeviceID;
		
			state 		= msg[2];
			
			if( state === \proximity, {
				deviceID			= msg[3];
				entering			= msg[4] != 0;
				systemTabletID	= msg[5];
				tabletID			= msg[6];
				pointingDeviceType	= msg[7];
				uniqueID			= msg[8];
				pointingDeviceID	= msg[9];

				proximityAction.value( this, entering, deviceID, pointingDeviceType, systemTabletID, pointingDeviceID, tabletID, uniqueID );

			}, {	// from tabletEvent
				bounds		= this.prBoundsReadOnly;
				deviceID		= msg[3];
				x			= msg[4] - jinsets.left; // - bounds.left;
				y			= msg[5] - jinsets.top;  // bounds.bottom - msg[5] + cocoaBorder; // sucky cocoa
				pressure		= msg[6];
				tiltx		= msg[7];
				tilty		= msg[8];
				rotation		= msg[9];
				tanPressure	= msg[10];
				absoluteX		= msg[11];
				absoluteY		= msg[12];
				absoluteZ		= msg[13];
				buttonMask	= msg[14];
				clickCount	= msg[15];
				
				buttonNumber	= (buttonMask & 2) >> 1;  // hmmm...
	
				case { state === \pressed }
				{
					{ this.mouseDown( x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
			                           absoluteX, absoluteY, buttonMask, tanPressure )}.defer;
				}
				{ state === \released }
				{
					{ this.mouseUp( x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
			                         absoluteX, absoluteY, buttonMask, tanPressure )}.defer;
				}
				{ state === \moved }
				{
	//				{ this.mouseMoved( x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
	//		                            absoluteX, absoluteY, buttonMask, tanPressure )}.defer;
				}
				{ state === \dragged }
				{
					{ this.doAction( x, y, pressure, tiltx, tilty, deviceID, buttonNumber, clickCount, absoluteZ, rotation,
			                          absoluteX, absoluteY, buttonMask, tanPressure )}.defer;
				};
	// note: entered is followed by moved with equal coordinates
	// so we can just ignore it
	//			{ state === \entered }
	//			{
	//				{ this.mouseOver( x, y, modifiers )}.defer;
	//			};
			});
		});
		tabletResp.add;
		msg = [ '/local', "tab" ++ this.id, '[', '/new', "de.sciss.swingosc.TabletResponder", this.id, parent.prGetWindow.id, ']' ];
		if( bndl.notNil, {
			bndl = bndl.add( msg );
		}, {
			server.sendBundle( nil, msg );
		});
		^bndl;
	}
}