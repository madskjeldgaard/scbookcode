/*
 *	JSCWindow
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
 *		- 27-Jul-08	using new java Frame class (de.sciss.common.AppWindow)
 */

/**
 *	A replacement for (Cocoa) SCWindow.
 *
 *	Different behaviour
 *	- bounds are automatically restored when quitting
 *	  minimization or full screen (cocoa windows don't do this)
 *
 *	Added features
 *	- method id returns the node ID
 *
 *	@author		Hanns Holger Rutz
 *	@version		0.64, 28-Jan-10
 */
JSCWindow : Object
{
	classvar <>nativeDecoration = true;
	classvar <>internalFrames	= false;
//	classvar <>floatingPalettes	= false;
	
	classvar <>verbose = false;
	classvar <>allWindows;
	classvar <>initAction;
	
	var dataptr, <name, <>onClose, <view, <userCanClose = true;
	var <alwaysOnTop = false;
	var <drawHook;
	var <acceptsMouseOver = false;	var <acceptsClickThrough = true;
	var <>toFrontAction, <>endFrontAction;
	
	var <server, <id;
	var bounds;
	var acResp;	// OSCpathResponder for window listening
	var penID		= nil;
	var <visible	= false;
	var <resizable;
	var <border;
	
// NOTE: pendingAlpha is not needed any more since Frame now calls pack and
// hence makes itself 'displayable'!
//	var pendingAlpha;	// alpha can only be set when window was made visible, so this is a lazy storage
	var pendingDraw = false;
	var wasOpened = false;
	var updServer;
	
	*initClass {
		UI.registerForShutdown({ this.closeAll });
	}
	
	// ----------------- constructor -----------------

	*new { arg name = "panel", bounds, resizable = true, border = true, server, scroll = false;
 		server = server ?? { SwingOSC.default };
 		if( server.serverRunning.not, {
			MethodError( "SwingOSC server not running", thisMethod ).throw;
		});
		^super.new.initSCWindow( name, bounds, resizable, border, scroll, server );
	}
	
	// ----------------- public class methods -----------------

	*closeAll {
		var list;
		list = allWindows.copy;
		allWindows = Array.new( 8 );
		list.do( _.close );
	}
		
	*screenBounds { arg server;
		^this.prGetScreenBounds( Rect.new, server );
	}
	
	*viewPalette {
		var w, v, f, c, h, scrB;
		
		c = [JSCSlider, JSCRangeSlider, JSC2DSlider, JSCPopUpMenu, JSCButton, 
			JSCNumberBox, JSCMultiSliderView,
			JSCStaticText, JSCDragSource, JSCDragSink, JSCDragBoth,
			JSCEnvelopeView, JSCUserView, JSCCheckBox, JSCScrollBar
		];

//		c = JSCView.allSubclasses.select({ arg cl; cl.class.findMethod( \paletteExample ).notNil })
//			.sort({ arg a, b; a.name <= b.name });
		
		scrB	= this.screenBounds;
		h	= c.size * 28 + 12;
		w	= JSCWindow( "View Palette", Rect( (scrB.width - 300) / 2, (scrB.height - h) / 2, 300, h ),
				resizable: false );
		w.view.decorator = f = FlowLayout( w.view.bounds );

		c.do({ arg item;
			var n;

			n = JSCDragSource( w, Rect( 0, 0, 140, 24 ));
			n.object = item;
		
			item.paletteExample( w, Rect( 0, 0, 140, 24 ));
		});
		
		^w.front;
	}

	// ----------------- public instance methods -----------------

	drawHook_ { arg func;
		if( drawHook.isNil, {
			if( func.notNil, {
				penID	= server.nextNodeID;
				server.sendBundle( nil,
					[ '/local', penID, '[', '/new', 'de.sciss.swingosc.Pen', '[', '/method', this.id, \getWindow, ']', ']' ],
					[ '/set', this.view.id, \icon, '[', '/ref', penID, ']' ]
				);
			}, {
				^this;
			});
		}, {
			if( func.isNil, {
				server.sendBundle( nil,
					[ '/set', this.view.id, \icon, '[', '/ref', \null, ']' ],
					[ '/method', penID, \dispose ],
					[ '/free', penID ]
				);
				penID = nil;
				drawHook = nil;
				pendingDraw = false;
				^this;
			});
		});
		drawHook = func;
		if( visible, {
			pendingDraw = false;
			JPen.protRefresh( drawHook, this, server, penID, this.id );
		}, {
			pendingDraw = true;
		});
	}

	asView { ^view }
	
	addFlowLayout { |margin, gap| 
		view.relativeOrigin.if
			{view.decorator_( FlowLayout( view.bounds.moveTo(0,0), margin, gap ) )}
			{view.decorator_( FlowLayout( view.bounds, margin, gap ) )};
		^this.view.decorator;
		 }
		 
	close { this.prClose }
	
	isClosed { ^dataptr.isNil }

	addToOnClose { arg function; onClose = onClose.addFunc( function )}
	removeFromOnClose { arg function; onClose = onClose.removeFunc( function )}
	
	visible_ { arg bool;
		var pre, post;
		if( bool && wasOpened.not, {
			^this.front;
		});
		if( visible != bool, {
			visible = bool;
			if( pendingDraw, {
				pendingDraw = false;
				JPen.protRefresh( drawHook, this, server, penID, this.id );
			});
			pre	= List.new;
			post	= List.new;
			view.prInvalidateAllVisible;
			view.prVisibilityChange( pre, post );
			pre.add([ '/set', this.id, \visible, visible ]);
			pre.addAll( post );
			server.listSendBundle( nil, pre.asArray );
		});
	}	

	fullScreen {
		server.sendMsg( '/set', this.id, 'graphicsConfiguration.device.fullScreenWindow', '[', '/ref', this.id, ']' );
	}
	
	endFullScreen {
		server.sendMsg( '/set', this.id, 'graphicsConfiguration.device.fullScreenWindow', '[', '/ref', \null, ']' );
	}
	
	userCanClose_ { arg bool;
		if( userCanClose != bool, {
			userCanClose = bool;
												// HIDE_ON_CLOSE, DO_NOTHING_ON_CLOSE
			server.sendMsg( '/set', this.id, \defaultCloseOperation, if( userCanClose, 1, 0 ) );
		});
	}
	
	acceptsMouseOver_ { arg bool;
		if( acceptsMouseOver != bool, {			acceptsMouseOver = bool;				server.sendMsg( '/method', this.id, \setAcceptMouseOver, bool );
		});
	}	

	pack {
		server.sendMsg( '/method', this.id, \pack );
	}

	front {
		var bndl;
		wasOpened = true;
		bndl = Array( 3 );
		if( visible.not, {
			visible = true;	// must be set to true before calling view.protDraw!
			if( pendingDraw, {
				pendingDraw = false;
				JPen.protRefresh( drawHook, this, server, penID, this.id );
			});
			view.prInvalidateAllVisible;
			view.prVisibilityChange;
			bndl.add([ '/set', this.id, \visible, true ]);
		});
//		if( pendingAlpha.notNil, {
//			bndl.add([ '/set', this.id, \alpha, pendingAlpha ]);
//			pendingAlpha = nil;
//		});
		bndl.add([ '/method', this.id, \toFront ]);
		server.listSendBundle( nil, bndl );
	}
	
	alwaysOnTop_ { arg bool = true;
		if( alwaysOnTop != bool, {
			alwaysOnTop = bool;
			server.sendMsg( '/set', this.id, \alwaysOnTop, alwaysOnTop );
		});
	}
		
	acceptsClickThrough_ { arg bool = true;
		if( acceptsClickThrough != bool, {
			acceptsClickThrough = bool;
			if( verbose, { "JSCWindow.acceptsClickThrough_ : has no effect".warn });
		});
	}
	
	resizable_ { arg bool;
		if( resizable != bool, {
			resizable = bool;
			if( border, {
				server.sendMsg( '/set', this.id, \resizable, resizable );
			});
		});
	}
	
	refresh {
		pendingDraw = false;
		if( drawHook.isNil, {
			server.sendMsg( '/method', this.id, \repaint );
		}, {
			JPen.protRefresh( drawHook, this, server, penID, this.id );
		});
		view.protDraw;
	}
	
	minimize {
		// java.awt.Frame.ICONIFIED
//		server.sendMsg( '/set', this.id, \extendedState, 1 );
		server.sendMsg( '/method', this.id, \minimize );
	}

	unminimize {
//		server.sendMsg( '/set', this.id, \extendedState, 0 );
		server.sendMsg( '/method', this.id, \unminimize );
	}

	alpha_ { arg alpha;
		// this would be perfect :
		// '/field', 'java.awt.SystemColor', \window
		// ... but : it's a texture which returns 0xFFFFFFFF thru getRGB ...
		// so we use grey ...
	
//		server.sendMsg( '/set', this.id, \background, *(Color( 1, 1, 1, alpha ).asSwingArg) );
//		server.sendMsg( '/set', this.id, \background, *(Color( 0.8, 0.8, 0.8, alpha ).asSwingArg) );

//		if( visible, {
//			pendingAlpha = nil;
			server.sendMsg( '/set', this.id, \alpha, alpha );
//		}, {
//			pendingAlpha = alpha;
//		});
	}
	
	name_ { arg argName;
		if( name != argName, {
			name = argName;
			server.listSendMsg([ '/set', this.id, \title ] ++ name.asSwingArg );
		});
	}
	
	bounds_ { arg argBounds;
		this.prSetBounds( argBounds );
	}
	
	setTopLeftBounds { arg rect, menuSpacer = 45;
		rect = rect.copy;
		// 45 is the height of the mac os menu
		// if you are in full screen mode you would want to pass in 0
		rect.top = JSCWindow.screenBounds.height - rect.height - rect.top - menuSpacer;
		this.bounds = rect;
	}
	
	setInnerExtent { arg w, h; // resize window keeping top left corner fixed
		var b;
		b = this.bounds;
		w = w ? b.width;
		h = h ? b.height;
		this.bounds = Rect.new( b.left, b.top + b.height - h, w, h );
	}
	
	bounds {
		^this.prGetBounds( Rect.new );
	}
	
	play { arg function;
		SwingOSC.clock.play({ 
			if( dataptr.notNil, {
				function.value;
			});
		});
	}
	
	findByID { arg id;
		^view.findByID( id );
	}

//	callDrawHook {
//		this.refresh;
//	}
	
//	id { ^id }

	// ----------------- quasi-interface methods : crucial-lib support -----------------

	asPageLayout { arg title, bounds;
		^MultiPageLayout.on( this.asView, bounds );
	}
	
	asFlowView { arg bounds;
		^FlowView( this, bounds );
	}

	flow { arg func, bounds;
		var f, comp;
		f = FlowView( this, bounds ?? { this.bounds });
		func.value( f );
		f.resizeToFit;
		^f;
	}

	// ----------------- private class methods -----------------
	
	*prGetScreenBounds { arg argBounds, server;
		server = server ?? { SwingOSC.default };
		^argBounds.set( 0, 0, server.screenWidth, server.screenHeight );
	}

	// ----------------- private instance methods -----------------

//	protDraw {
//		if( drawHook.notNil and: { this.visible }, {
//			JPen.protRefresh( drawHook, this, server, penID, this.id );
//		});
//	}

	add { arg aView; view.add( aView )}

	closed {
		dataptr = nil;
		view.prClose;
		onClose.value; // call user function
		allWindows.remove( this );
	}
	
	/*
	 *	@param	argName		(String or Symbol) is mapped to property 'title'
	 *	@param	argBounds 	(Rect) is translated to java's coordinate system
	 *	@param	argResizable	(Boolean) is mapped to property 'resizable'
	 *	@param	argBorder		(Boolean) is mapped to property 'undecorated'
	 */
	initSCWindow { arg argName, argBounds, argResizable, argBorder, scroll, argServer;
		name			= argName.asString;
		border		= argBorder;
		resizable		= argResizable;
		argBounds		= argBounds ?? { Rect.new( 128, 64, 400, 400 )};
		server		= argServer;
		allWindows	= allWindows.add( this );
		id			= server.nextNodeID;
		dataptr		= this.id;
								// parent, bounds
//		view			= JSCTopView( nil, argBounds.moveTo( 0, 0 ), server );
//		id			= view.id;
		this.prInit( name, argBounds, resizable && border, border, scroll ); // , view );
		initAction.value( this );
	}

//	prBoundsToJava { arg cocoa;
//		var screenBounds;
//
//		screenBounds 	= JSCWindow.screenBounds( server );
//
//		^if( border, {
//			// + 20 for window bar XXX this is only true on aqua lnf ...
//			Rect.new( cocoa.left, screenBounds.height - cocoa.top - cocoa.height - 22,
//					 cocoa.width, cocoa.height + 22 );
//		}, {
//			Rect.new( cocoa.left, screenBounds.height - cocoa.top - cocoa.height,
//					 cocoa.width, cocoa.height );
//		});
//	}
		
//	prBoundsFromJava { arg java;
//		var screenBounds, cocoaHeight;
//
//		screenBounds 	= JSCWindow.screenBounds( server );
//		cocoaHeight	= java.height - 22;
//
//		^Rect.new( java.left, screenBounds.height - java.top - 22 - cocoaHeight, java.width, cocoaHeight );
//	}
		
	prInit { arg argName, argBounds, resizable, border, scroll; // , view;
		var viewID, bndl;

		bounds 	= argBounds;
		// tricky, we have to allocate the TopView's id here
		// to be able to assign our content pane to it, so
		// that JSCView can add key and dnd listeners
		viewID	= server.nextNodeID;

		acResp = OSCpathResponder( server.addr, [ '/window', this.id ], { arg time, resp, msg;
			var state;
		
			state = msg[2].asSymbol;
			switch( state,
			\resized, {
//				bounds = this.prBoundsFromJava( Rect( msg[3], msg[4], msg[5], msg[6] ));
				bounds = Rect( msg[3], msg[4], msg[5], msg[6] );
//				if( drawHook.notNil, { this.refresh });
			},
			\moved, {
//				bounds = this.prBoundsFromJava( Rect( msg[3], msg[4], msg[5], msg[6] ));
				bounds = Rect( msg[3], msg[4], msg[5], msg[6] );
			},
			\activated, {
				if( toFrontAction.notNil, {
					{ toFrontAction.value( this )}.defer;
				});
			},
			\deactivated, {
				if( endFrontAction.notNil, {
					{ endFrontAction.value( this )}.defer;
				});
			},
			\closing, {
				if( userCanClose, {
					{ this.prClose }.defer;
				});
			});
		}).add;

//		server.sendBundle( nil,
//			[ '/set', '[', '/local', this.id, '[', '/new', "de.sciss.swingosc.Frame" ] ++ argName.asSwingArg ++ [ scroll, ']', ']',
//				\bounds ] ++ this.prBoundsToJava( argBounds ).asSwingArg ++ if( resizable.not, [ \resizable, 0 ]) ++
//				if( border.not, [ \undecorated, 1 ]),
//			[ '/local', "ac" ++ this.id,
//				'[', '/new', "de.sciss.swingosc.WindowResponder", this.id, ']',
//				viewID, '[', '/method', this.id, "getContentPane", ']' ]
//		);
		bndl = Array( 3 );
		server.protEnsureApplication;
		bndl.add([ '/local', this.id, '[', '/new', "de.sciss.swingosc.Frame" ] ++ argName.asSwingArg ++ argBounds.asSwingArg ++ [ border.not.binaryValue | (scroll.binaryValue << 1) | (resizable.not.binaryValue << 2), ']', ]);
//		if( resizable.not, { bndl.add([ '/set', this.id, \resizable, 0 ])});
		bndl.add([ '/local', "ac" ++ this.id,
				'[', '/new', "de.sciss.swingosc.WindowResponder", this.id, ']',
				viewID, '[', '/method', this.id, "getContentPane", ']' ]);
		server.listSendBundle( nil, bndl );

		view = if( scroll, {
			JSCScrollTopView( this, argBounds.moveTo( 0, 0 ), viewID );
		}, {
			JSCTopView( this, argBounds.moveTo( 0, 0 ), viewID );
		});
		
		updServer = UpdateListener.newFor( server, { arg upd, s;
			if( s.serverRunning.not, { this.close });
		}, \serverRunning );
	}
	
	prClose {
		if( dataptr.notNil, {
			acResp.remove;
			updServer.remove;
			this.drawHook_( nil );
			server.sendBundle( nil,
				[ '/method', "ac" ++ this.id, \remove ],
				[ '/method', this.id, \dispose ],
				[ "/free", "ac" ++ this.id, this.id ]);
			this.closed;
		},{
			"JSCWindow-remove : this view was already removed.".debug( this );
		});
	}

	prGetBounds { arg argBounds;
		^argBounds.set( bounds.left, bounds.top, bounds.width, bounds.height );
	}

	prSetBounds { arg argBounds;
		bounds		= argBounds;
//		argBounds		= this.prBoundsToJava( argBounds );
//		server.listSendMsg([ '/set', this.id, \bounds ] ++ argBounds.asSwingArg );
		server.listSendMsg([ '/set', this.id, \cocoaBounds ] ++ argBounds.asSwingArg );
	}
}