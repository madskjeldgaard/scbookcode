/*
 *	JSCViews collection 1
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
 *	@version		0.64, 28-Jan-10
 */
JSCContainerView : JSCView { // abstract class
	var <children, <decorator;
	var pendingValidation = false;
			
	// ----------------- public instance methods -----------------

	removeAll {
		children.copy.do({ arg child; child.remove });
	}
	
	relativeOrigin_ { arg bool;
		relativeOrigin = bool;
//		this.setProperty(\relativeOrigin, bool);
		this.prInvalidateBounds;
	}
	
		// this is a TEMPORARY method
		// will be removed when relativeOrigin variable is permanently banished
	prRelativeOrigin { ^relativeOrigin }
	
	addFlowLayout { arg margin, gap;
		this.relativeOrigin.if
			{this.decorator_( FlowLayout( this.bounds.moveTo(0,0), margin, gap ) )}
			{this.decorator_( FlowLayout( this.bounds, margin, gap ) )};
		^this.decorator;
	}

	decorator_ { arg decor;
		if( relativeOrigin, {
			decor.bounds = decor.bounds.moveTo( 0, 0 );
			decor.reset;
		});
		decorator = decor;
	}

	// ----------------- quasi-interface methods : crucial-lib support -----------------

	asPageLayout { arg title, bounds;
		// though it won't go multi page
		// FlowView better ?
		^MultiPageLayout.on( this, bounds );
	}


	flow { arg func, bounds;
		var f, comp;
		f = FlowView( this, bounds /*?? { this.bounds }*/ );
		func.value( f );
		f.resizeToFit;
		^f;
	}
	
	horz { arg func, bounds;
		var comp;
		comp = JSCHLayoutView.new( this, bounds ?? { this.bounds });
		func.value( comp );
		^comp;
	}
	
	vert { arg func, bounds;
		var comp;
		comp = JSCVLayoutView.new( this, bounds ?? { this.bounds });
		func.value( comp );
		^comp;
	}
	
	comp { arg func, bounds;
		var comp;
		comp = JSCCompositeView.new( this, bounds ?? { this.bounds });
		func.value( comp );
		^comp;
	}		

	// ----------------- private instance methods -----------------

	prViewPortID { ^id }	// actually this refers to a viewport-view!! we should fuse that with the containerID concept XXX
	prChildOrder { arg child; ^-1 }

	prGetRefTopLeft {
// more efficient but too difficult to maintain
//		var refTopLeft;
//		var pTopLeft;
//		if( scBounds.isNil, {
//			// need to revalidate bounds
//			pTopLeft		= parent.prGetRefTopLeft;
//			refTopLeft	= jBounds.moveBy( pTopLeft.x, pTopLeft.y );
//			scBounds		= jinsets.addTo( refTopLeft );
//			^refTopLeft;
//		}, {
//			^(scBounds.leftTop - jinsets.leftTop);
//		});
//		^(this.bounds.leftTop - jinsets.leftTop);
		^if( relativeOrigin, {
			Point( jinsets.left.neg, jinsets.top.neg )
		}, {
			this.prBoundsReadOnly.leftTop - jinsets.leftTop
		});
	}

	prAddAllTopLeft { arg rect;
		^parent.prAddAllTopLeft( rect.moveBy( jBounds.left, jBounds.top ));
	}

	add { arg child;
		var bndl, vpID;
		
		children = children.add( child );
		if( decorator.notNil, { decorator.place( child )});

		if( child.id.notNil, {
			vpID = this.prViewPortID;
			bndl = Array( 4 );
			bndl.add([ '/method', vpID, \add, '[', '/ref', child.prContainerID, ']', this.prChildOrder( child )]);
			if( this.prAllVisible, {
				if( this.id != vpID, {
					bndl.add([ '/method', vpID, \validate ]);
				});
				bndl.add([ '/method', this.id, \revalidate ]);
				bndl.add([ '/method', child.id, \repaint ]);
				pendingValidation = false;
			}, {
				pendingValidation = true;
			});
			server.listSendBundle( nil, bndl );
		});
	}
	
	prInvalidateBounds {
		scBounds = nil;
		children.do({ arg child;
//			child.prSetScBounds( nil );
			child.prInvalidateBounds;
		});
	}

	prInvalidateAllVisible {
		allVisible = nil;
		children.do({ arg child;
			child.prInvalidateAllVisible;
		});
	}

	prVisibilityChange { arg pre, post;
		var vpID;
		if( pendingValidation, {
			if( this.prAllVisible, {
				vpID = this.prViewPortID;
				if( this.id != vpID, {
					post.add([ '/method', vpID, \validate ]);
				});
				post.add([ '/method', this.id, \revalidate ]);
				post.add([ '/method', this.id, \repaint ]);
				pendingValidation = false;
			});
		});
		children.do({ arg child;
			child.prVisibilityChange( pre, post );
		});
	}

	prRemoveChild { arg child;
		var bndl, vpID;
		
		children.remove( child );
		bndl = Array( 4 );
		vpID = this.prViewPortID;
		bndl.add([ '/method', vpID, \remove, '[', '/ref', child.prContainerID, ']' ]);
		if( this.prAllVisible, {
			if( this.id != vpID, {
				bndl.add([ '/method', vpID, \validate ]);
			});
			bndl.add([ '/method', this.id, \revalidate ]);
			bndl.add([ '/method', this.id, \repaint ]);
			pendingValidation = false;
		}, {
			pendingValidation = true;
		});
		server.listSendBundle( nil, bndl );
		// ... decorator replace all
	}
	//bounds_  ... replace all

	prMoveChild { arg bndl, child;
		var vpID;
		if( child.prAllVisible, {
			vpID = this.prViewPortID;
			if( this.id != vpID, {
				bndl.add([ '/method', vpID, \validate ]);
			});
			bndl.add([ '/method', this.id, \revalidate ]);
			bndl.add([ '/method', child.id, \repaint ]);
			pendingValidation = false;
		}, {
			pendingValidation = true;
		});
	}

	prVisibleChild {}

	prClose { arg preMsg, postMsg;
		super.prClose( preMsg, postMsg );
		children.do({ arg item; item.prClose });
	}

	prSCViewNew { arg preMsg, postMsg;
		properties.put( \canFocus, false );
		^super.prSCViewNew( preMsg, postMsg );
	}
	
	protDraw {
		children.do({ arg child; child.protDraw });
	}

	prSendProperty { arg key, value;
		switch( key,
		\background, {	// overriden to redirect to viewport
			server.listSendMsg([ '/set', this.prViewPortID, key ] ++ value.asSwingArg );
			^nil;
		});
		^super.prSendProperty( key, value );
	}
}

JSCCompositeView : JSCContainerView {
	// ----------------- quasi-interface methods : crucial-lib support -----------------

	asFlowView { arg bounds;
		^FlowView( this,bounds ?? { this.bounds });
	}

	// ----------------- private instance methods -----------------

	prChildOrder { arg child; ^0 }

	prInitView {
		jinsets = Insets( 3, 3, 3, 3 );  // so focus borders of children are not clipped
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.Panel", '[', '/new', "de.sciss.swingosc.ColliderLayout", ']', ']' ]
		]);
	}
}

JSCTopView : JSCContainerView {	// NOT subclass of JSCCompositeView
	var window;

	// ----------------- public instance methods -----------------

	focus { arg flag = true;
		if( flag, {
			server.sendMsg( '/method', this.id, \requestFocus );
		}, {
			"JSCTopView.focus( false ) : not yet implemented".error;
		});
	}

	findWindow { ^this.prGetWindow }
	
	// only in construction mode, handled internally
	canReceiveDrag { ^currentDrag.isKindOf( Class )}

//	// bug: visible_( false ) doesn't properly refresh the view ...
//	visible_ { arg bool;
//		var bndl; //, cntID, tempID;
//		if( visible != bool, {
//			visible = bool;	// must be set before calling prVisiblityChange
//			this.prInvalidateAllVisible;
//			bndl = List.new;
//			this.prVisibilityChange( bndl );
////			cntID = if( this.prIsInsideContainer, { "cn" ++ this.id }, { this.id });
//			bndl.add([ '/set', this.id, \visible, visible ]);
////			if( visible, {
////				bndl.add([ '/methodr', '[', '/method', this.id, \getParent, ']', \repaint ]);
////			}, {
////				tempID = server.nextNodeID;
////				bndl.add([ '/local', tempID, '[', '/new', "javax.swing.JPanel", ']' ]);
////				bndl.add([ '/set', this.prGetWindow.id, \contentPane, '[', '/ref', tempID, ']' ]);
////				bndl.add([ '/method', tempID, \revalidate ]);
////				bndl.add([ '/set', this.prGetWindow.id, \contentPane, '[', '/ref', this.id, ']' ]);
////			});
//////			bndl.add([ '/methodr', '[', '/method', this.prGetWindow.id, \getRootPane, ']', \repaint ]);
////			bndl.add([ '/methodr', '[', '/method', cntID, \getParent, ']', \validate ]);
////			bndl.add([ '/methodr', '[', '/method', cntID, \getParent, ']', \repaint ]);
//			bndl.add([ '/method', this.id, \repaint ]);
//			server.listSendBundle( nil, bndl );
//		});
//	}

	// ----------------- private class methods -----------------

	*new { arg window, bounds, id;
		^super.new.prInitTopView( window, bounds, id );
	}
	
	// ----------------- public instance methods -----------------

	defaultReceiveDrag {
		var win, view;
		win = this.findWindow;
		view = currentDrag.paletteExample( win, Rect( 10, 10, 140, 24 ));
		view.keyDownAction_({ arg view, char, modifiers, unicode, keycode;
			if( keycode == 51, { view.remove });
		});
	}

	absoluteBounds { ^this.bounds }

	// ----------------- private instance methods -----------------

	prChildOrder { arg child; ^0 }
	
	init { }	// kind of overriden by prInitTopView

	prInitTopView { arg argWindow, argBounds, id;
//		parent		= argParent.asView;	// actual view
		window		= argWindow;
//		scBounds		= argBounds;
//		jBounds		= this.prBoundsToJava( scBounds );
//		jinsets		= Insets.new;
		this.prInit( nil, argBounds, this.class.viewClass, window.server, id );
//		argParent.add( this );		// maybe window or viewadapter
	}

	prSCViewNew { arg preMsg, postMsg;
		var bndl, argBounds;
		
		if( jinsets.isNil, { jinsets = Insets.new });
		
		bndl			= List.new;
		bndl.addAll( preMsg );
		jBounds		= this.prBoundsToJava( scBounds );
//		argBounds		= jBounds.asSwingArg;
//		bndl.add([ '/set', this.id, \bounds ] ++ argBounds ++ [ \font, '[', '/ref', \font, ']' ]);
//		if( this.prIsInsideContainer, {
//			bndl.add([ '/set', "cn" ++ this.id, \bounds ] ++ argBounds );
//		});
		if( this.prNeedsTransferHandler, {
			this.prCreateDnDResponder( bndl );
		});
		// NOTE: for global key actions to be working, every view
		// has to create a key responder, even if it's not using it personally ;-(
		this.prCreateKeyResponder( bndl );
		this.prCreateCompResponder( bndl );
		bndl.addAll( postMsg );
		server.listSendBundle( nil, bndl.asArray );
	}

	prInitView { ^this.prSCViewNew }

	prGetWindow { ^window }

	handleKeyDownBubbling { arg view, char, modifiers, unicode, keycode;
		keyDownAction.value( view, char, modifiers, unicode, keycode );
	}

	handleKeyUpBubbling { arg view, char, modifiers, unicode, keycode;
		keyUpAction.value( view, char, modifiers, unicode, keycode );
	}

	handleKeyModifiersChangedBubbling { arg view, modifiers;
		keyModifiersChangedAction.value( view, modifiers );
	}

	prBoundsToJava { arg rect; ^rect.copy }
	prBoundsFromJava { arg rect; ^rect.copy }

	prBoundsUpdated {
		if( window.drawHook.notNil, { window.refresh });
	}
	
	prAllVisible {
		^(visible and: { this.prGetWindow.visible });
	}

	prAddAllTopLeft { arg rect; ^rect }

	prBoundsReadOnly {
		if( scBounds.isNil, {
			// need to revalidate bounds
			scBounds	= jinsets.addTo( jBounds );
		});
		^scBounds;
	}

//	prGetParentRefTopLeft { ^Point( 0, 0 )}
}

JSCScrollTopView : JSCTopView {
	var <autohidesScrollers = true, <hasHorizontalScroller = true, <hasVerticalScroller = true;
	var <autoScrolls = true;
	var vpID, chResp;
	
	var viewX = 0, viewY = 0, viewW = 0, viewH = 0;
	
	// ----------------- public instance methods -----------------

	autohidesScrollers_ { arg bool;
		var hPolicy, vPolicy;
		autohidesScrollers = bool;
		hPolicy = JSCScrollView.protCalcPolicy( bool, hasHorizontalScroller ) + 30;
		vPolicy = JSCScrollView.protCalcPolicy( bool, hasVerticalScroller ) + 20;

		server.sendMsg( '/set', this.id, \horizontalScrollBarPolicy, hPolicy, \verticalScrollBarPolicy, vPolicy );
	}
	
	hasHorizontalScroller_ { arg bool;
		var policy;
		hasHorizontalScroller = bool;
		policy = JSCScrollView.protCalcPolicy( autohidesScrollers, bool ) + 30;
		server.sendMsg( '/set', this.id, \horizontalScrollBarPolicy, policy );
	}
	
	hasVerticalScroller_ { arg bool;
		var policy;
		hasVerticalScroller = bool;
		policy = JSCScrollView.protCalcPolicy( autohidesScrollers, bool ) + 20;
		server.sendMsg( '/set', this.id, \verticalScrollBarPolicy, policy );
	}
	
	visibleOrigin_ { arg point;
		viewX	= point.x;
		viewY	= point.y;
		server.sendMsg( '/method', this.id, \setViewPosition, point.x, point.y );
		this.doAction;
	}
	
	visibleOrigin {
		^Point( viewX, viewY );
	}
	
	autoScrolls_ { arg bool;
		"JSCScrollTopView.autoScrolls_ : not yet implemented".warn;
		autoScrolls = bool;
//		server.sendMsg( '/set', this.id, \autoScrolls, bool );
	}
	
	innerBounds {
		^Rect( 0, 0, viewW, viewH );
	}

	// ----------------- private instance methods -----------------

	prGetRefTopLeft {
		^Point( 0, 0 );
	}

	prVisibleChild { arg pre, post, child;
		var vpID;
		if( this.prAllVisible, {
			vpID = this.prViewPortID;
			if( this.id != vpID, {
				post.add([ '/method', vpID, \validate ]);
			});
			post.add([ '/method', this.id, \revalidate ]);
			pendingValidation = false;
		}, {
			pendingValidation = true;
		});
	}

	prClose { arg preMsg, postMsg;
		chResp.remove;
		^super.prClose( preMsg ++
			[[ '/method', "ch" ++ this.id, \remove ],
		      [ '/free', "ch" ++ this.id ]], postMsg );
	}
	
	prInitView {
		chResp	= OSCpathResponder( server.addr, [ '/change', this.id ], { arg time, resp, msg;
			var newVal;
		
			// [ /change, 1001, performed, viewX, ..., viewY, ..., innerWidth, ..., innerHeight, ... ]

			viewW = msg[8];
			viewH = msg[10];
			if( viewX != msg[4] or: { viewY != msg[6] }, {
				viewX = msg[4];
				viewY = msg[6];
				{ this.doAction }.defer;
			});
		}).add;
		vpID = "vp" ++ this.id;
		^this.prSCViewNew([[ '/local', vpID, '[', '/methodr', '[', '/method', this.id, \getViewport, ']', \getView, ']',
			"ch" ++ this.id,
			'[', '/new', "de.sciss.swingosc.ChangeResponder", this.id, '[', '/array', \viewX, \viewY, \innerWidth, \innerHeight, ']', ']' ]]);
	}

//	prInit { arg ... args;
//		var result;
//		result = super.prInit( *args );
//		vpID = "vp" ++ this.id;
//		server.sendMsg( '/local', vpID, '[', '/methodr', '[', '/method', this.id, \getViewport, ']', \getView, ']',
//			"ac" ++ this.id,
//			'[', '/new', "de.sciss.swingosc.ChangeResponder", this.id, '[', '/array', \viewX, \viewY, \innerWidth, \innerHeight ']', ']' );
//	}

	prViewPortID { ^vpID }
}

JSCScrollView : JSCContainerView {
	var <autohidesScrollers = true, <hasHorizontalScroller = true, <hasVerticalScroller = true;
	var <autoScrolls = true;
	var vpID, chResp;

	var viewX = 0, viewY = 0, viewW = 0, viewH = 0;
	
	var <hasBorder = false;

	// ----------------- public instance methods -----------------

	hasBorder_ { arg bool = true;
		if( hasBorder != bool, {
			hasBorder = bool;
			this.setProperty( \border, bool );
		});
	}

	autohidesScrollers_ { arg bool;
		var hPolicy, vPolicy;
		autohidesScrollers = bool;
		hPolicy = JSCScrollView.protCalcPolicy( bool, hasHorizontalScroller ) + 30;
		vPolicy = JSCScrollView.protCalcPolicy( bool, hasVerticalScroller ) + 20;

		server.sendMsg( '/set', this.id, \horizontalScrollBarPolicy, hPolicy, \verticalScrollBarPolicy, vPolicy );
	}
	
	hasHorizontalScroller_ { arg bool;
		var policy;
		hasHorizontalScroller = bool;
		policy = JSCScrollView.protCalcPolicy( autohidesScrollers, bool ) + 30;
		server.sendMsg( '/set', this.id, \horizontalScrollBarPolicy, policy );
	}
	
	hasVerticalScroller_ { arg bool;
		var policy;
		hasVerticalScroller = bool;
		policy = JSCScrollView.protCalcPolicy( autohidesScrollers, bool ) + 20;
		server.sendMsg( '/set', this.id, \verticalScrollBarPolicy, policy );
	}

	visibleOrigin_ { arg point;
		viewX	= point.x;
		viewY	= point.y;
		server.sendMsg( '/method', this.id, \setViewPosition, point.x, point.y );
		this.doAction;
	}
	
	visibleOrigin {
		^Point( viewX, viewY );
	}
	
	autoScrolls_ { arg bool;
		"JSCScrollView.autoScrolls_ : not yet implemented".warn;
		autoScrolls = bool;
//		server.sendMsg( '/set', this.id, \autoScrolls, bool );
	}
	
	innerBounds {
		^Rect( 0, 0, viewW, viewH );
	}

	// ----------------- private class methods -----------------

	*protCalcPolicy { arg auto, has;
//		autohidesScrollers			1	0	1	0
//		hasHorizontalScroller		1	1	0	0
//		--------------------------------------------
//		horizontalScrollBarPolicy	0	2	1	1	+ 30
		^(has.not.binaryValue | ((auto.not && has).binaryValue << 1));
	}
	
	// ----------------- private instance methods -----------------

	prGetRefTopLeft {
		^Point( 0, 0 );
	}

	prVisibleChild { arg pre, post, child;
		var vpID;
		if( this.prAllVisible, {
			vpID = this.prViewPortID;
			if( this.id != vpID, {
				post.add([ '/method', vpID, \validate ]);
			});
			post.add([ '/method', this.id, \revalidate ]);
			pendingValidation = false;
		}, {
			pendingValidation = true;
		});
	}

	prClose { arg preMsg, postMsg;
		chResp.remove;
		^super.prClose( preMsg ++
			[[ '/method', "ch" ++ this.id, \remove ],
		      [ '/free', "ch" ++ this.id ]], postMsg );
	}

	prInitView {
		chResp	= OSCpathResponder( server.addr, [ '/change', this.id ], { arg time, resp, msg;
			var newVal;
		
			// [ /change, 1001, performed, viewX, ..., viewY, ..., innerWidth, ..., innerHeight, ... ]

			viewW = msg[8];
			viewH = msg[10];
			if( viewX != msg[4] or: { viewY != msg[6] }, {
				viewX = msg[4];
				viewY = msg[6];
				{ this.doAction }.defer;
			});
		}).add;
		vpID = "vp" ++ this.id;
		^this.prSCViewNew([
			[ '/local', vpID, '[', '/new', "de.sciss.swingosc.ContentPane", 0, ']',
			  this.id, '[', '/new', "de.sciss.swingosc.ScrollPane", '[', '/ref', vpID, ']', ']',
 			  "ch" ++ this.id, '[', '/new', "de.sciss.swingosc.ChangeResponder", this.id,
 			  	'[', '/array', \viewX, \viewY, \innerWidth, \innerHeight, ']', ']' ]]);
	}

	prInit { arg ... args;
		var result;
		result = super.prInit( *args );
		vpID = "vp" ++ this.id;
		server.sendMsg( '/local', vpID, '[', '/methodr', '[', '/method', this.id, \getViewport, ']', \getView, ']' );
	}

	prViewPortID { ^vpID }
}

// abstract class!
JSCLayoutView : JSCContainerView {
	// ----------------- public instance methods -----------------

	spacing { ^this.getProperty( \spacing, 0 )}
	
	spacing_ { arg distance; this.setProperty( \spacing, distance )}

	// ----------------- quasi-interface methods : crucial-lib support -----------------

	asFlowView {}

	// ----------------- private instance methods -----------------

	properties { ^super.properties ++ #[ \spacing ]}

	prSendProperty { arg key, value;
		var bndl;

		key	= key.asSymbol;

		switch( key,
			\spacing, {
				server.sendBundle( nil, [ '/methodr', '[', '/method', this.id, \getLayout, ']', \setSpacing, value ],
									 [ '/method', this.id, \revalidate ]);
				^nil;
			}
		);
		^super.prSendProperty( key, value );
	}

	prSCViewNew { arg preMsg, postMsg;
		properties.put( \spacing, 4 );
		jinsets = Insets( 3, 3, 3, 3 );  // so focus borders of children are not clipped
		^super.prSCViewNew( preMsg, postMsg );
	}
}

JSCHLayoutView : JSCLayoutView {
	// ----------------- private instance methods -----------------

	prInitView {
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.Panel", '[', '/new', "de.sciss.swingosc.ColliderAxisLayout", 0, 4, ']', ']' ]
		]);
	}
}

JSCVLayoutView : JSCLayoutView {
	// ----------------- private instance methods -----------------

	prInitView {
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.Panel", '[', '/new', "de.sciss.swingosc.ColliderAxisLayout", 1, 4, ']', ']' ]
		]);
	}
}

JSCControlView : JSCView {} // abstract class

JSCSliderBase : JSCControlView {

	var <>shift_scale = 100.0, <>ctrl_scale = 10.0, <>alt_scale = 0.1;

	// ----------------- public instance methods -----------------

	getScale { arg modifiers;
		^case
		{ (modifiers & 0x020000) != 0 } { shift_scale }
		{ (modifiers & 0x040000) != 0 } { ctrl_scale }
		{ (modifiers & 0x080000) != 0 } { alt_scale }
		{ 1 };
	}

	knobColor {
		^this.getProperty(\knobColor, Color.new)
	}
	
	knobColor_ { arg color;
		this.setProperty(\knobColor, color)
	}
	
	step_ { arg stepSize;
		this.setPropertyWithAction(\step, stepSize);
	}
	step {
		^this.getProperty(\step)
	}
	
	// ----------------- private instance methods -----------------

	properties {
		^super.properties ++ #[ \knobColor, \step ];
	}

	prSnap { arg val;
		if( this.step <= 0.0, {
			^val.clip( 0.0, 1.0 );
		}, {
			^(val.clip( 0.0, 1.0 ) / this.step).round * this.step;
		});
	}
}

JSCSlider : JSCSliderBase
{
	var acResp;	// OSCpathResponder for action listening
	var orientation;	// 0 for horiz, 1 for vert
	var clpse;

	// ----------------- public instance methods -----------------

	value { ^this.getProperty( \value )}
	
	value_ { arg val;
		this.setProperty( \value, this.prSnap( val ));
	}
	
	valueAction_ { arg val;
		this.setPropertyWithAction( \value, this.prSnap( val ));
	}	
	
	increment { arg zoom = 1; ^this.valueAction = this.value + (max( this.step, this.pixelStep ) * zoom) }
	decrement { arg zoom = 1; ^this.valueAction = this.value - (max( this.step, this.pixelStep ) * zoom) }
	
	thumbSize { ^this.getProperty( \thumbSize, 12 )}
	
	thumbSize_ { arg size;
	//	"JSCSlider.thumbSize_ : not yet implemented".warn;
//		this.setProperty( \thumbSize, size );
	}

	pixelStep {
		var b = this.prBoundsReadOnly;
		// XXX thumbSize doesn't correspond to laf's thumbSize
		^(if( orientation == 0, b.width, b.height ) - this.thumbSize).max( 1 ).reciprocal;
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

	defaultKeyDownAction { arg char, modifiers, unicode, keycode;
		// standard keydown
		if (char == $r, { this.valueAction = 1.0.rand; ^this });
		if (char == $n, { this.valueAction = 0.0; ^this });
		if (char == $x, { this.valueAction = 1.0; ^this });
		if (char == $c, { this.valueAction = 0.5; ^this });
		if (char == $], { this.increment( this.getScale( modifiers )); ^this });
		if (char == $[, { this.decrement( this.getScale( modifiers )); ^this });
		if (unicode == 0xF700, { this.increment( this.getScale( modifiers )); ^this });
		if (unicode == 0xF703, { this.increment( this.getScale( modifiers )); ^this });
		if (unicode == 0xF701, { this.decrement( this.getScale( modifiers )); ^this });
		if (unicode == 0xF702, { this.decrement( this.getScale( modifiers )); ^this });
		^nil		// bubble if it's an invalid key
	}
	
	defaultCanReceiveDrag { ^currentDrag.isNumber }
	defaultGetDrag { ^this.value }
	defaultReceiveDrag { this.valueAction = currentDrag }

	// ----------------- private instance methods -----------------

	properties {
		^super.properties ++ #[ \thumbSize ];
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
//		jinsets = Insets( 3, 3, 3, 3 );
		properties.put( \value, 0.0 );
		properties.put( \step, 0.0 );
		if( scBounds.isNil, {
			orientation = 0;
		}, {
			b			= this.prBoundsReadOnly;
			orientation	= if( b.width > b.height, 0, 1 );
		});
		clpse	= Collapse({ this.doAction });
		acResp	= OSCpathResponder( server.addr, [ '/action', this.id ], { arg time, resp, msg;
			var newVal;
		
//			newVal = msg[4] / 0x40000000;
			newVal = this.prSnap( msg[4] / 0x40000000 );
			if( newVal != this.value, {
				// don't call valueAction coz we'd create a loop
				properties.put( \value, newVal );
				clpse.instantaneous;
			});
		}).add;
		^this.prSCViewNew([
			[ '/local', this.id,
				'[', '/new', "de.sciss.swingosc.Slider", orientation, 0, 0x40000000, 0, ']',
				"ac" ++ this.id,
				'[', '/new', "de.sciss.swingosc.ActionResponder", this.id, \value, ']' ]
		]);
	}
	
	prSendProperty { arg key, value;

		key	= key.asSymbol;

		// fix keys
		case { key === \value }
		{
			key		= \valueNoAction;
			value	= value * 0x40000000;
		}
		{ key === \step }
		{
			value = max( 1, value * 0x40000000 ).asInteger;
//			server.sendMsg( '/set', this.id, \snapToTicks, value != 0,
//							\minorTickSpacing, value, \extent, value );
			server.sendMsg( '/set', this.id, \snapToTicks, value != 0,
							\majorTickSpacing, value ); // stupidly, using extent won't let you move the slider to the max
			^nil;
		};
		^super.prSendProperty( key, value );
	}
}

JSCKnob : JSCSlider {}

JSCRangeSlider : JSCSliderBase {

	var acResp;	// OSCpathResponder for action listening
	var clpse;
	var orientation;	// 0 for horiz, 1 for vert

	// ----------------- public class methods -----------------

	*paletteExample { arg parent, bounds;
		var v;
		v = this.new( parent, bounds );
		v.setSpan( 0.2, 0.7 );
		^v;
	}

	// ----------------- public instance methods -----------------

	step_ { arg stepSize;
		super.step_( stepSize );
		this.setSpan( this.lo, this.hi );
	}
	
	lo { ^this.getProperty( \lo )}

	lo_ { arg val;
		this.setProperty( \lo, this.prSnap( val ));
	}
	
	activeLo_ { arg val;
		this.setPropertyWithAction( \lo, this.prSnap( val ));
	}
	
	hi { ^this.getProperty( \hi )}

	hi_ { arg val;
		this.setProperty( \hi, this.prSnap( val ));
	}
	
	activeHi_ { arg val;
		this.setPropertyWithAction( \hi, this.prSnap( val ));
	}
	
	range { ^(this.hi - this.lo).abs }

	range_ { arg val;
		this.hi_( this.prSnap( this.lo + val ));
	}
	
	activeRange_ { arg val;
		this.range_( val );
		this.doAction;
	}
	
	setSpan { arg lo, hi;
		lo = this.prSnap( lo );
		hi = this.prSnap( hi );
		properties.put( \lo, lo );
		properties.put( \hi, hi );
		server.sendMsg( '/set', this.id, \knobPos, min( lo, hi ), \knobExtent, abs( hi - lo ));
	}
	
	setSpanActive { arg lo, hi;
		this.setSpan( lo, hi );
		this.doAction;
	}

	setDeviation { arg deviation, average;
		var lo = (1 - deviation) * average;
		this.setSpan( lo, lo + deviation );
	}

	pixelStep { 
		var b = this.prBoundsReadOnly;
		^(if( orientation == 0, { b.width }, { b.height }) - 2).max( 1 ).reciprocal;
	}

	increment { arg zoom = 1;
		var inc, val; 
		inc = this.pixelStep * zoom;
		val = this.hi + inc;
		if( val > 1, {
			inc = 1 - this.hi;
			val = 1;
		});
		this.setSpanActive( this.lo + inc, val );
	}
	
	decrement { arg zoom = 1;
		var inc, val; 
		inc = this.pixelStep * zoom;
		val = this.lo - inc;
		if( val < 0, {
			inc = this.lo;
			val = 0;
		});
		this.setSpanActive( val, this.hi - inc );
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

	defaultKeyDownAction { arg char, modifiers, unicode;
		var a, b;
		// standard keydown
		if (char == $r, { 
			a = 1.0.rand;
			b = 1.0.rand;
			this.setSpanActive( min( a, b ), max( a, b ));
			^this;
		});
		if (char == $n, { this.setSpanActive( 0.0, 0.0 ); ^this });
		if (char == $x, { this.setSpanActive( 1.0, 1.0 ); ^this });
		if (char == $c, { this.setSpanActive( 0.5, 0.5 ); ^this });
		if (char == $a, { this.setSpanActive( 0.0, 1.0 ); ^this });
		if (unicode == 0xF700, { this.increment( this.getScale( modifiers )); ^this });
		if (unicode == 0xF703, { this.increment( this.getScale( modifiers )); ^this });
		if (unicode == 0xF701, { this.decrement( this.getScale( modifiers )); ^this });
		if (unicode == 0xF702, { this.decrement( this.getScale( modifiers )); ^this });
		^nil;		// bubble if it's an invalid key
	}

	defaultGetDrag { ^Point( this.lo, this.hi )}	
	defaultCanReceiveDrag {	 ^currentDrag.isKindOf( Point )}
	
	defaultReceiveDrag {
		// changed to x,y instead of lo, hi
		this.setSpanActive( currentDrag.x, currentDrag.y );
	}

	// ----------------- private instance methods -----------------

	properties {
		^super.properties ++ #[ \lo, \hi ];
	}
	
	prNeedsTransferHandler { ^true }

	prClose { arg preMsg, postMsg;
		acResp.remove;
		clpse.cancel;
		^super.prClose( preMsg ++
			[[ '/method', "ac" ++ this.id, \remove ],
			 [ '/free', "ac" ++ this.id ],
			 [ '/method', this.id, \dispose ]], postMsg );
	}

	prInitView {
		var b;
		properties.put( \lo, 0.0 );
		properties.put( \hi, 1.0 );
		properties.put( \step, 0.0 );
		jinsets		= Insets( 3, 3, 3, 3 );
		b			= this.prBoundsReadOnly;
		orientation	= if( b.width > b.height, 0, 1 );
		clpse		= Collapse({ this.doAction });
		acResp		= OSCpathResponder( server.addr, [ '/action', this.id ], { arg time, resp, msg;
			var newLo, newHi;
		
			newLo	= msg[4];
			newHi 	= newLo + msg[6];
			if( (newLo != this.lo) || (newHi != this.hi), {
				// don't call valueAction coz we'd create a loop
				properties.put( \lo, newLo );
				properties.put( \hi, newHi );
				clpse.instantaneous;
			});
		}).add;
		^this.prSCViewNew([
			[ '/set', '[', '/local', this.id, '[', '/new', "de.sciss.swingosc.RangeSlider", orientation, ']', ']',
				\knobColor ] ++ Color.blue.asSwingArg,
			[ '/local', "ac" ++ this.id,
				'[', '/new', "de.sciss.swingosc.ActionResponder", this.id, '[', '/array', \knobPos, \knobExtent, ']', ']' ]
		]);
	}

	prSendProperty { arg key, value;
		key	= key.asSymbol;

		// fix keys
		case { key === \lo }
		{
			server.sendMsg( '/set', this.id, \knobPos, min( value, this.hi ), \knobExtent, abs( value - this.hi ));
			^nil;		
		}
		{ key === \hi }
		{
			server.sendMsg( '/set', this.id, \knobPos, min( value, this.lo ), \knobExtent, abs( value - this.lo ));
			^nil;		
		}
		{ key === \step }
		{
			key = \stepSize;
		};
		^super.prSendProperty( key, value );
	}
}

JSC2DSlider : JSCSliderBase {

	var acResp;	// OSCpathResponder for action listening
	var clpse;

	// ----------------- public instance methods -----------------

	step_ { arg stepSize;
		super.step_( stepSize );
		this.x_( this.x );
		this.y_( this.y );
	}

	x { ^this.getProperty( \x )}

	x_ { arg val;
		this.setProperty( \x, this.prSnap( val ));
	}
	
	activex_ { arg val;
		this.setPropertyWithAction( \x, this.prSnap( val ));
	}
	
	y { ^this.getProperty( \y )}

	y_ { arg val;
		this.setProperty( \y, this.prSnap( val ));
	}
	
	activey_ { arg val;
		this.setPropertyWithAction( \y, this.prSnap( val ));
	}
	
	setXY { arg x, y;
		x = this.prSnap( x );
		y = this.prSnap( y );
		properties.put( \x, x );
		properties.put( \y, y );
		server.sendMsg( '/set', this.id, \knobX, x, \knobY, y );
	}
	
	setXYActive { arg x, y;
		this.setXY( x, y );
		this.doAction;
	}

	pixelStepX { ^(this.prBoundsReadOnly.width - 17).max( 1 ).reciprocal }
	pixelStepY { ^(this.prBoundsReadOnly.height - 17).max( 1 ).reciprocal }

	incrementY { arg zoom = 1; ^this.y = this.y + (this.pixelStepY * zoom) }
	decrementY { arg zoom = 1; ^this.y = this.y - (this.pixelStepY * zoom) }
	incrementX { arg zoom = 1; ^this.x = this.x + (this.pixelStepX * zoom) }
	decrementX { arg zoom = 1; ^this.x = this.x - (this.pixelStepX * zoom) }

	defaultKeyDownAction { arg char, modifiers, unicode,keycode;
		// standard keydown
		if (char == $r, { this.setXYActive( 1.0.rand, 1.0.rand ); ^this });
		if (char == $n, { this.setXYActive( 0.0, 0.0 ); ^this });
		if (char == $x, { this.setXYActive( 1.0, 1.0 ); ^this });
		if (char == $c, { this.setXYActive( 0.5, 0.5 ); ^this });
		if (unicode == 0xF700, { this.incrementY( this.getScale( modifiers )); this.doAction; ^this });
		if (unicode == 0xF703, { this.incrementX( this.getScale( modifiers )); this.doAction; ^this });
		if (unicode == 0xF701, { this.decrementY( this.getScale( modifiers )); this.doAction; ^this });
		if (unicode == 0xF702, { this.decrementX( this.getScale( modifiers )); this.doAction; ^this });
		^nil		// bubble if it's an invalid key
	}

	defaultGetDrag { ^Point( this.x, this.y )}
	defaultCanReceiveDrag { ^currentDrag.isKindOf( Point )}
	defaultReceiveDrag { this.setXYActive( currentDrag.x, currentDrag.y )}

	// ----------------- private instance methods -----------------

	properties {
		^super.properties ++ #[ \x, \y ];
	}
	
	prNeedsTransferHandler { ^true }

	prClose { arg preMsg, postMsg;
		acResp.remove;
		clpse.cancel;
		^super.prClose( preMsg ++
			[[ '/method', "ac" ++ this.id, \remove ],
			 [ '/free', "ac" ++ this.id ],
			 [ '/method', this.id, \dispose ]], postMsg );
	}

	prInitView {
		properties.put( \x, 0.0 );
		properties.put( \y, 0.0 );
		properties.put( \step, 0.0 );
		jinsets	= Insets( 3, 3, 3, 3 );
		clpse	= Collapse({ this.doAction });
		acResp	= OSCpathResponder( server.addr, [ '/action', this.id ], { arg time, resp, msg;
			var newX, newY;
		
			newX = msg[4];
			newY = msg[6];
			if( (newX != this.x) || (newY != this.y), {
				// don't call valueAction coz we'd create a loop
				properties.put( \x, newX );
				properties.put( \y, newY );
				clpse.instantaneous;
			});
		}).add;
		^this.prSCViewNew([
			[ '/local', this.id,
				'[', '/new', "de.sciss.swingosc.Slider2D", ']',
				"ac" ++ this.id,
				'[', '/new', "de.sciss.swingosc.ActionResponder", this.id, '[', '/array', \knobX, \knobY, ']', ']' ]
		]);
	}

	prSendProperty { arg key, value;

		key	= key.asSymbol;

		// fix keys
		case { key === \x }
		{
			key = \knobX;
		}
		{ key === \y }
		{
			key = \knobY;
		}
		{ key === \step }
		{
			key = \stepSize;
		};
		^super.prSendProperty( key, value );
	}
}

//// JJJ : not yet working
//JSC2DTabletSlider : JSC2DSlider {
//
////	var <>mouseDownAction,<>mouseUpAction;
//	
//	mouseDown { arg x,y,pressure,tiltx,tilty,deviceID,
//			 buttonNumber,clickCount,absoluteZ,rotation;
//		mouseDownAction.value(this,x,y,pressure,tiltx,tilty,deviceID, 
//			buttonNumber,clickCount,absoluteZ,rotation);
//	}
//	mouseUp { arg x,y,pressure,tiltx,tilty,deviceID, 
//			buttonNumber,clickCount,absoluteZ,rotation;
//		mouseUpAction.value(this,x,y,pressure,tiltx,tilty,deviceID, 
//			buttonNumber,clickCount,absoluteZ,rotation);
//	}
//	doAction { arg x,y,pressure,tiltx,tilty,deviceID, 
//			buttonNumber,clickCount,absoluteZ,rotation;
//		action.value(this,x,y,pressure,tiltx,tilty,deviceID, 
//			buttonNumber,clickCount,absoluteZ,rotation);
//	}
//}

JSCButton : JSCControlView {
	var <states;
	
	var acResp;	// OSCpathResponder for action listening

	// ----------------- public class methods -----------------

	*paletteExample { arg parent, bounds;
		var v;
		v = this.new( parent, bounds );
		v.states = [
			[ "Push", Color.black, Color.red ],
			[ "Pop", Color.white, Color.blue ]];
		^v;
	}
	
	// ----------------- public instance methods -----------------

	value { ^this.getProperty( \value )}
	
	value_ { arg val;
		this.setProperty( \value, this.prFixValue( val ));
	}
	
	valueAction_ { arg val;
		this.setPropertyWithAction( \value, this.prFixValue( val ));
	}	

	doAction { arg modifiers;
		action.value( this, modifiers );
	}
	
	font { ^this.getProperty( \font )}

	font_ { arg argFont;
//		font = argFont;
		this.setProperty( \font, argFont );
	}

	states_ { arg array;
		states = array.deepCopy;
		this.setProperty( \states, states );
	}
	
	defaultKeyDownAction { arg char, modifiers, unicode;
// JJJ handled automatically by javax.swing.AbstractButton
//		if (char == $ , { this.valueAction = this.value + 1; ^this });
		if (char == $\r, { this.valueAction = this.value + 1; ^this });
		if (char == $\n, { this.valueAction = this.value + 1; ^this });
		if (char == 3.asAscii, { this.valueAction = this.value + 1; ^this });
		^nil;		// bubble if it's an invalid key
	}

	defaultGetDrag { ^this.value }

	defaultCanReceiveDrag {
		^currentDrag.isNumber or: { currentDrag.isKindOf( Function )};
	}
	
	defaultReceiveDrag {
		if( currentDrag.isNumber, {
			this.valueAction = currentDrag;
		}, {
			this.action = currentDrag;
		});
	}

	// ----------------- private instance methods -----------------

	properties {
		^super.properties ++ #[ \value, \font, \states ];
	}
	
	prFixValue { arg val;
		val = val.asInteger;
		// clip() would be better but SCButton resets to zero always
		if( (val < 0) || (val >= states.size), {
			val = 0;
		});
		^val;
	}
	
	prNeedsTransferHandler { ^true }

	prClose { arg preMsg, postMsg;
		acResp.remove;
		^super.prClose( preMsg ++
			[[ '/method', "ac" ++ this.id, \remove ],
			 [ '/free', "ac" ++ this.id ]], postMsg );
	}

	prInitView {
		properties.put( \value, 0 );
		jinsets = Insets( 3, 3, 3, 3 );
		acResp = OSCpathResponder( server.addr, [ '/action', this.id ], { arg time, resp, msg;
			var value, modifiers;
			value	= msg[4];
			modifiers	= msg[6];
			// java->cocoa ; this translates shift (1), ctrl (2), cmd (4), alt (8)
//			modifiers		= ((modifiers & 3) << 17) |
//						  ((modifiers & 4) << 18) |
//						  ((modifiers & 8) << 16); // | plusMod;
			// don't call valueAction coz we'd create a loop
			properties.put( \value, msg[4] );
			{ this.doAction( modifiers )}.defer;
		}).add;
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.gui.MultiStateButton", ']',
				"ac" ++ this.id,
				'[', '/new', "de.sciss.swingosc.ActionResponder", this.id, '[', '/array', \selectedIndex, \lastModifiers, ']', ']' ]
//				'[', '/new', "de.sciss.swingosc.ActionResponder", this.id, '[', '/array', \selectedIndex, ']', ']' ]
		]);
	}

	prSendProperty { arg key, value;
		var bndl, msg;

		key	= key.asSymbol;

		// fix keys
		case { key === \value }
		{
			key = \selectedIndex;
		}
		{ key === \states }
		{
			bndl = [[ '/method', this.id, \removeAllItems ]];
			value.do({ arg state;
				bndl = bndl.add([ '/method', this.id, \addItem ] ++ state[0].asSwingArg ++ state[1].asSwingArg ++
					state[2].asSwingArg );
			});
			server.listSendBundle( nil, bndl );
			^nil;
		};
		^super.prSendProperty( key, value );
	}
}


JSCPopUpMenu : JSCControlView {
	var <items;
	
	var acResp;	// OSCpathResponder for action listening
	var <>allowsReselection = false;

	// ----------------- public class methods -----------------

	*paletteExample { arg parent, bounds;
		var v;
		v = this.new(parent, bounds);
		v.items = #[ "linear", "exponential", "sine", "welch", "squared", "cubed" ];
		^v;
	}
		
	// ----------------- public instance methods -----------------

	value { ^this.getProperty( \value )}
	
	value_ { arg val;
		this.setProperty( \value, this.prFixValue( val ));
	}
		
	valueAction_ { arg val;
		this.setPropertyWithAction( \value, this.prFixValue( val ));
	}
	
	font { ^this.getProperty( \font )}
	
	font_ { arg argFont;
//		font = argFont;
		this.setProperty( \font, argFont );
	}
	
	items_ { arg array;
		items = array.copy;
		this.setProperty( \items, items );
	}
	
	item { ^items[ this.value ]}

	stringColor {
		^this.getProperty( \stringColor, Color.new );
	}
	
	stringColor_ { arg color;
		this.setProperty( \stringColor, color );
	}
	
	defaultKeyDownAction { arg char, modifiers, unicode;
// JJJ used by lnf
//		if (char == $ , { this.valueAction = this.value + 1; ^this });
//		if (char == $\r, { this.valueAction = this.value + 1; ^this });
//		if (char == $\n, { this.valueAction = this.value + 1; ^this });
		if (char == 3.asAscii, { this.valueAction = this.value + 1; ^this });
//		if (unicode == 16rF700, { this.valueAction = this.value - 1; ^this });
		if (unicode == 16rF703, { this.valueAction = this.value + 1; ^this });
//		if (unicode == 16rF701, { this.valueAction = this.value + 1; ^this });
		if (unicode == 16rF702, { this.valueAction = this.value - 1; ^this });
		^nil		// bubble if it's an invalid key
	}
	
	defaultGetDrag { ^this.value }
	defaultCanReceiveDrag { ^currentDrag.isNumber }

	defaultReceiveDrag {
		this.valueAction = currentDrag;
	}

	// ----------------- private instance methods -----------------

	properties {
		^super.properties ++ #[ \value, \font, \items, \stringColor ];
	}

	prFixValue { arg val;
		^val.clip( 0, items.size - 1 );
	}

	prNeedsTransferHandler { ^true }

	prClose { arg preMsg, postMsg;
		acResp.remove;
		^super.prClose( preMsg ++
			[[ '/method', "ac" ++ this.id, \remove ],
			 [ '/free', "ac" ++ this.id ]], postMsg );
	}

	prInitView {
		properties.put( \value, 0 );
		acResp = OSCpathResponder( server.addr, [ '/action', this.id ], { arg time, resp, msg;
			var newVal;
			
			newVal = msg[4];
			if( allowsReselection or: { newVal != this.value }, {
				// don't call valueAction coz we'd create a loop
				properties.put( \value, newVal );
				{ this.doAction; }.defer;
			});
		}).add;
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.PopUpView", ']',
				"ac" ++ this.id,
				'[', '/new', "de.sciss.swingosc.ActionResponder", this.id, \selectedIndex, ']' ]
		]);
	}

	prSendProperty { arg key, value;

		key	= key.asSymbol;

		// fix keys
		case
//		{ key === \value }
//		{
//			key = \selectedIndex;
//		}
		{ key === \items }
		{
			this.prSetItems( value ); // .performUnaryOp( \asString );
			^nil;
		};
		^super.prSendProperty( key, value );
	}

// XXX at the moment...
//	prBoundsToJava { arg rect;
//		var pb;
//		
//		pb = parent.bounds;
//		if( rect.height < 26, {
//			rect			= Rect( rect.left - pb.left, rect.top - ((26 - rect.height) >> 1) - pb.top,
//							   rect.width, 26 );
//		}, {
//			rect	= rect.moveBy( pb.left.neg, pb.top.neg );
//		});
//		^rect;
//	}

// what shall we do ...
//	prBoundsFromJava { arg rect;
//		^rect;
//	}

	prSetItems { arg items;
		var sizes, dataSize, startIdx, itemArgs, bndl, selectedIdx;

		itemArgs	= items.collect({ arg it; it.asString.asSwingArg }); // necessary to escape plain bracket strings!
		selectedIdx = this.value;
		if( selectedIdx >= items.size, { selectedIdx = -1 });
		sizes	= itemArgs.collect({ arg itemArg; itemArg.sum( _.oscEncSize )});
		if( (sizes.sum + 55) <= server.options.oscBufSize, {
			server.listSendMsg([ '/method', this.id, \setListData, '[', '/array' ] ++ itemArgs.flatten ++ [ ']', selectedIdx ]);
		}, {	// need to split it up
			startIdx = 0;
			dataSize	= 147; // 45;
			bndl		= Array( 3 );
			bndl.add([ '/method', this.id, \beginDataUpdate ]);
			sizes.do({ arg size, idx;
				if( (dataSize + size) > server.options.oscBufSize, {
					bndl.add([ '/method', this.id, \addData, '[', '/array', ] ++
						itemArgs.copyRange( startIdx, idx - 1 ).flatten ++ [ ']' ]);
					server.listSendBundle( nil, bndl );
					dataSize	= 111 + size; // 45;
					startIdx	= idx;
					bndl		= Array( 2 );
				}, {
					dataSize = dataSize + size;
				});
			});
			bndl.add([ '/method', this.id, \addData, '[', '/array', ] ++
					itemArgs.copyRange( startIdx, items.size - 1 ).flatten ++ [ ']' ]);
			bndl.add([ '/method', this.id, \endDataUpdate, selectedIdx ]);
			server.listSendBundle( nil, bndl );
		});
	}
}

JSCStaticTextBase : JSCView {
	var <string, <object, <>setBoth = true;
	
	// ----------------- public instance methods -----------------

	font { ^this.getProperty( \font )}
	
	font_ { arg argFont;
//		font = argFont;
		this.setProperty( \font, argFont );
	}
	
	string_ { arg argString;
		string = argString.asString;
		this.setProperty(\string, string)
	}
	align_ { arg align;
		this.setProperty(\align, align)
	}
	
	stringColor {
		^this.getProperty(\stringColor, Color.new)
	}
	stringColor_ { arg color;
		this.setProperty(\stringColor, color)
	}

	object_ { arg obj;
		object = obj;
		if( setBoth, { this.string = object.asString( 80 )});
	}
	
	// ----------------- private instance methods -----------------

	properties {
		^super.properties ++ #[ \string, \font, \stringColor ];
	}

	prSendProperty { arg key, value;

		key	= key.asSymbol;

		// fix keys
		case { key === \stringColor }
		{
			key = \foreground;
		}
		{ key === \align }
		{
			key = \horizontalAlignment;
			case { value === \left }
			{
				value = 2;
			}
			{ value === \center }
			{
				value = 0;
			}
			{ value === \right }
			{
				value = 4;
			}
			// undocumented cocoa feature : -1 = left, 0 = center, 1 = right
			{ value.isKindOf( SimpleNumber )}
			{
				value = switch( value.sign, -1, 2, 0, 0, 1, 4 );
			};
		}
		{ key === \string }
		{
			key = \text;
//			value = value.asSwingArg;
// funktioniert nicht, weil BoundedRangeModel offensichtlich nochmal durch setText veraendert wird
//			server.sendBundle( nil,
//				[ '/set', this.id, \text, value ],	// make sure the text beginning is shown
//				[ "/methodr", [ '/method', this.id, \getHorizontalVisibility ], \setValue, 0 ]
//			);
//			^nil;
		};
		^super.prSendProperty( key, value );
	}
}

JSCStaticText : JSCStaticTextBase {

	// ----------------- public class methods -----------------
	
	*paletteExample { arg parent, bounds;
		var v;
		v = this.new(parent, bounds);
		v.string = "The lazy brown fox";
		^v
	}

	// ----------------- private instance methods -----------------

	prInitView {
		properties.put( \canFocus, false );
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.Label", ']' ]
		]);
	}
}

JSCListView : JSCControlView {
	var <items, <>enterKeyAction;
	var <allowsDeselection = false;
	
	var acResp;	// listens to list selection changes
	var cnID;
	
	// ----------------- public class methods -----------------

	*paletteExample { arg parent, bounds;
		var v;
		v = this.new(parent, bounds);
		v.items = #[ "linear", "exponential", "sine", "welch", "squared", "cubed" ];
		^v;
	}
	
	// ----------------- public instance methods -----------------

	item { ^items[ this.value ]}

	value { ^this.getProperty( \value )}

	value_ { arg val;
		this.setProperty( \value, this.prFixValue( val ));
	}
	
	valueAction_ { arg val;
		this.setPropertyWithAction( \value, this.prFixValue( val ));
	}
	
	allowsDeselection_ { arg bool;
		if( allowsDeselection != bool, {
			allowsDeselection = bool;
			if( allowsDeselection, {
				if( (this.value == 0) and: { items.size == 0 }, {
					this.valueAction_( nil );
				});
			}, {
				if( this.value.isNil, {
					this.valueAction_( 0 );
				});
			});
		});
	}

	font { ^this.getProperty( \font )}
	
	font_ { arg argFont;
//		font = argFont;
		this.setProperty( \font, argFont );
	}
	
	items_ { arg array;
		items = array.copy;
		this.setProperty( \items, items );
	}
	
	stringColor {
		^this.getProperty( \stringColor, Color.new );
	}
	
	stringColor_ { arg color;
		this.setProperty( \stringColor, color );
	}
	
	selectedStringColor {
		^this.getProperty( \selectedStringColor, Color.new );
	}
	
	selectedStringColor_ { arg color;
		this.setProperty( \selectedStringColor, color );
	}
	
	hiliteColor {
		^this.getProperty( \hiliteColor, Color.new );
	}
	
	hiliteColor_ { arg color;
		this.setProperty( \hiliteColor, color );
	}
	
	defaultKeyDownAction { arg char, modifiers, unicode;
		var index;
		if( this.value.notNil, {
			if( char == $ , { this.valueAction = this.value + 1; ^this });
			if( char == $\r, { this.enterKeyAction.value(this); ^this });
			if( char == $\n, { this.enterKeyAction.value(this); ^this });
			if( char == 3.asAscii, { this.enterKeyAction.value(this); ^this });
	// JJJ automatically handled by lnf
	//		if( unicode == 16rF700, { this.valueAction = this.value - 1; ^this });
			if( unicode == 16rF703, { this.valueAction = this.value + 1; ^this });
	//		if( unicode == 16rF701, { this.valueAction = this.value + 1; ^this });
			if( unicode == 16rF702, { this.valueAction = this.value - 1; ^this });
		});
		if (char.isAlpha, {
			char = char.toUpper;
			index = items.detectIndex({ arg item; item.asString.at(0).toUpper >= char });
			if( index.notNil, { this.valueAction = index });
			^this;
		});
		^nil;	// bubble if it's an invalid key
	}
	

	defaultGetDrag { ^this.value }
	defaultCanReceiveDrag { ^currentDrag.isNumber }

	defaultReceiveDrag {
		this.valueAction = currentDrag;
	}

	// ----------------- private instance methods -----------------

	properties {
		^super.properties ++ #[ \value, \font, \items, \stringColor ];
	}

	prFixValue { arg val;
		if( allowsDeselection and: { val.isNil }, { ^nil });
		val = (val ? 0).asInteger;
		if( (val < 0) || (val >= items.size), {
			val = 0;
		});
		^val;
	}

	prNeedsTransferHandler { ^true }

	prClose { arg preMsg, postMsg;
		acResp.remove;
		^super.prClose( preMsg ++
			[[ '/method', "ac" ++ this.id, \remove ],
			 [ '/free', "ac" ++ this.id ]], postMsg );
	}

	prContainerID { ^cnID }

	prInitView {
		cnID = "cn" ++this.id;
		properties.put( \value, 0 );
		
		acResp = OSCpathResponder( server.addr, [ '/list', this.id ], { arg time, resp, msg;
			var newVal;

			newVal = this.prFixValue( if( msg[4] >= 0, msg[4] ));
			if( newVal != this.value, {
				// don't call valueAction coz we'd create a loop
				properties.put( \value, newVal );
				{ this.doAction; }.defer;
			});
		}).add;
		^this.prSCViewNew([
			[ '/set', '[', '/local', this.id, '[', '/new', "de.sciss.swingosc.ListView", ']', ']',
				\selectionMode, 0 ],	// single selection only for compatibility
			[ '/local', "cn" ++ this.id, 	// bars : v=asNeeded, h=never
				'[', '/new', "javax.swing.JScrollPane", '[', '/ref', this.id, ']', 20, 31, ']',
				"ac" ++ this.id,
				'[', '/new', "de.sciss.swingosc.ListResponder", this.id,
					'[', '/array', \selectedIndex, ']', ']' ] // , \valueIsAdjusting
		]);
	}

	prSendProperty { arg key, value;

		key	= key.asSymbol;

		// fix keys
		case { key === \value }
		{
			value = value ? -1;
			if( value >= 0, {
				server.sendBundle( nil,
					[ '/set', this.id, \value, value ],
					[ '/method', this.id, \ensureIndexIsVisible, value ]
				);
			}, {
				server.sendMsg( '/set', this.id, \value, value );
			});
			^nil;
		}
		{ key === \stringColor }
		{
			key = \foreground;
		}
		{ key === \selectedStringColor }
		{
			key = \selectionForeground;
		}
		{ key === \hiliteColor }
		{
			key = \selectionBackground;
		}
		{ key === \items }
		{
			this.prSetItems( value ); // value.performUnaryOp( \asString );
			^nil;
//		}
//		{ key === \bounds }
//		{
//			server.listSendMsg([ '/set', "cn" ++ this.id, key ] ++ this.prBoundsToJava( value ).asSwingArg );
//			^nil;
		};
		^super.prSendProperty( key, value );
	}

	prSetItems { arg items;
		var sizes, dataSize, startIdx, itemArgs, bndl, selectedIdx;

		itemArgs	= items.collect({ arg it; it.asString.asSwingArg }); // necessary to escape plain bracket strings!
		selectedIdx = this.value ? -1;
		sizes	= itemArgs.collect({ arg itemArg; itemArg.sum( _.oscEncSize )});
		if( (sizes.sum + 55) <= server.options.oscBufSize, {
			server.listSendMsg([ '/method', this.id, \setListData, '[', '/array' ] ++ itemArgs.flatten ++ [ ']', selectedIdx ]);
		}, {	// need to split it up
			startIdx = 0;
			dataSize	= 147; // 45;
			bndl		= Array( 3 );
			bndl.add([ '/method', this.id, \beginDataUpdate ]);
			sizes.do({ arg size, idx;
				if( (dataSize + size) > server.options.oscBufSize, {
					bndl.add([ '/method', this.id, \addData, '[', '/array', ] ++
						itemArgs.copyRange( startIdx, idx - 1 ).flatten ++ [ ']' ]);
					server.listSendBundle( nil, bndl );
					dataSize	= 111 + size; // 45;
					startIdx	= idx;
					bndl		= Array( 2 );
				}, {
					dataSize = dataSize + size;
				});
			});
			bndl.add([ '/method', this.id, \addData, '[', '/array', ] ++
					itemArgs.copyRange( startIdx, items.size - 1 ).flatten ++ [ ']' ]);
			bndl.add([ '/method', this.id, \endDataUpdate, selectedIdx ]);
			server.listSendBundle( nil, bndl );
		});
	}
}

JSCDragView : JSCStaticTextBase {
	var <>interpretDroppedStrings = false;
	
	// ----------------- public class methods -----------------

	*paletteExample { arg parent, bounds;
		var v;
		v = this.new(parent, bounds);
		v.object = \something;
		^v
	}
	
	// ----------------- private instance methods -----------------

	defaultGetDrag { ^object }

	prNeedsTransferHandler { ^true }

	prImportDrag {
		if( interpretDroppedStrings, { JSCView.importDrag });
	}

	prSCViewNew { arg preMsg, postMsg;
		properties.put( \canFocus, false );
		jinsets = Insets( 3, 3, 3, 3 );
		^super.prSCViewNew( preMsg, postMsg );
	}
}

JSCDragSource : JSCDragView {
	// ----------------- private instance methods -----------------

	prInitView {
		^this.prSCViewNew([
//			[ '/set', '[', '/local', this.id, '[', '/new', "de.sciss.swingosc.Label", ']', ']',
//				\border, '[', '/method', "javax.swing.BorderFactory", \createRaisedBevelBorder, ']'
//			]
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.DragView", 1, ']',
			]
		]);
	}

	prGetDnDModifiers { ^0 } 	// no modifiers needed
}

JSCDragSink : JSCDragView {
	// ----------------- public instance methods -----------------

	defaultCanReceiveDrag { ^true }

	defaultReceiveDrag {
		this.object = currentDrag;
		this.doAction;
	}

	// ----------------- private instance methods -----------------

	prInitView {
		^this.prSCViewNew([
//			[ '/set', '[', '/local', this.id, '[', '/new', "de.sciss.swingosc.Label", ']', ']',
//				\border, '[', '/method', "javax.swing.BorderFactory", \createLoweredBevelBorder, ']'
//			]
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.DragView", 0, ']',
			]
		]);
	}

	prGetDnDModifiers { ^-1 }	// don't allow it to be drag source
}

JSCDragBoth : JSCDragView {		// in SwingOSC not subclass of JSCDragSink
	// ----------------- public instance methods -----------------

	defaultCanReceiveDrag { ^true }
	
	defaultReceiveDrag {
		this.object = currentDrag;
		this.doAction;
	}

	defaultGetDrag { ^object }

	// ----------------- private instance methods -----------------

	prInitView {
		^this.prSCViewNew([
//			[ '/set', '[', '/local', this.id, '[', '/new', "de.sciss.swingosc.Label", ']', ']',
//				\border, '[', '/method', "javax.swing.BorderFactory", \createCompoundBorder,
//					'[', '/method', "javax.swing.BorderFactory", \createRaisedBevelBorder, ']',
//					'[', '/method', "javax.swing.BorderFactory", \createLoweredBevelBorder, ']', ']'
//			]
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.DragView", 2, ']',
			]
		]);
	}

	prGetDnDModifiers { ^0 } 	// no modifiers needed
}

JSCAbstractUserView : JSCView {
	var <drawFunc;
	var <clearOnRefresh = true;
	var <>refreshOnFocus = true;
	var <>lazyRefresh   = true;

	var penID			= nil;
	var pendingDraw	= false;
	var routRefresh, condRefresh;

	// ----------------- public instance methods -----------------

	refresh {
		pendingDraw = false;
		if( drawFunc.notNil, { this.protRefresh });
	}
		
	clearDrawing {
		server.sendMsg( '/method', this.id, \clearDrawing );
	}
	
	clearOnRefresh_{ arg bool;
		clearOnRefresh = bool;
		this.setProperty( \clearOnRefresh, bool );
	}

	drawFunc_ { arg func;
		if( drawFunc.isNil, {
			if( func.notNil, {
				penID	= server.nextNodeID;
				server.sendBundle( nil,
					[ '/local', penID, '[', '/new', "de.sciss.swingosc.Pen", '[', '/ref', this.id, ']', relativeOrigin.not, ']' ],
					[ '/method', this.id, \setPen, '[', '/ref', penID, ']' ]
				);
			}, {
				^this;
			});
		}, {
			if( func.isNil, {
				server.sendBundle( nil,
					[ '/method', this.id, \setPen, '[', '/ref', \null, ']' ],
					[ '/method', penID, \dispose ],
					[ '/free', penID ]
				);
				penID = nil;
				drawFunc = nil;
				pendingDraw = false;
				^this;
			});
		});
		drawFunc = func;
		if( this.prAllVisible, {
			pendingDraw = false;
			this.protRefresh;
		}, {
			pendingDraw = true;
		});
	}
	
	focusVisible { ^this.getProperty( \focusVisible, true )}
	focusVisible_ { arg visible; this.setProperty( \focusVisible, visible )}
	
	focusColor_ { arg colr;
		if( (colr.alpha > 0) != this.focusVisible, {
			this.focusVisible = colr.alpha > 0;
		});
		^super.focusColor_( colr );
	}

	// ----------------- private instance methods -----------------

//	draw {
//		this.refresh;
//	}

	prSCViewNew { arg preMsg, postMsg;
		condRefresh = Condition.new;
		routRefresh = Routine({
			inf.do({
				condRefresh.wait;
				condRefresh.test = false;
				if( drawFunc.notNil, {
					try {
						JPen.protRefresh( drawFunc, this, server, penID, this.id )
					} { arg error;
						error.reportError;
					};
				});
				0.01.wait;
			});
		}).play( AppClock );
		^super.prSCViewNew( preMsg, postMsg );
	}

	prFocusChange {
		// the user may wish to paint differently according to the focus
		if( refreshOnFocus, { this.protDraw });
	}
	
	prVisibilityChange {
		if( pendingDraw, { this.protDraw });
	}

	prBoundsUpdated {
		this.protDraw;
	}

	prClose { arg preMsg, postMsg;
//		routRefresh.cancel;
		routRefresh.stop;
		this.drawFunc_( nil );
		^super.prClose( preMsg, postMsg );
	}

	protRefresh {
		if( lazyRefresh, {
//			routRefresh.instantaneous;
			condRefresh.test = true; condRefresh.signal;
		}, {
			JPen.protRefresh( drawFunc, this, server, penID, this.id );
		});
	}

	protDraw {
		if( drawFunc.notNil and: { this.prAllVisible }, {
//			// cmpID == nil --> don't repaint, because this
//			// will be done already by JSCWindow, and hence
//			// would slow down refresh unnecessarily (???)
//			JPen.protRefresh( drawFunc, this, server, penID, nil );
//			JPen.protRefresh( drawFunc, this, server, penID, this.id );
			this.protRefresh;
		});
	}
}

JSCUserView : JSCAbstractUserView {
	var lastMouseX = 0, lastMouseY = 0;

	// ----------------- public class methods -----------------

	*paletteExample { arg parent, bounds;
		^this.new( parent, bounds ).refreshOnFocus_( false ).drawFunc_({ arg view;
			var b = view.bounds, min = min( b.width, b.height ), max = max( b.width, b.height ),
			    num = (max / min).asInteger;
			JPen.addRect( b.moveTo( 0, 0 ));
			JPen.clip;
			JPen.scale( min, min );
			num.do({ 	arg i;
				var rel = i / num;
				JPen.fillColor = Color.hsv( rel, 0.4, 0.6 );
				JPen.addWedge( (0.5 + i) @ 0.5, 0.4, rel * pi + 0.2, 1.5pi );
				JPen.fill;
			});
		});
	}

	// ----------------- public instance methods -----------------

	mousePosition {
		var b;
		^if( relativeOrigin, {
			lastMouseX @ lastMouseY;
		}, {
			b = this.prBoundsReadOnly;
			(lastMouseX - b.left) @ (lastMouseY - b.top);
		});
	}

	relativeOrigin_ { arg bool;
		relativeOrigin = bool;
		this.setProperty( \relativeOrigin, bool );
	}

	// ----------------- private instance methods -----------------

	mouseDown { arg x, y ... rest;
		lastMouseX	= x;
		lastMouseY	= y;
		^super.mouseDown( x, y, *rest );
	}
	
	mouseUp { arg x, y ... rest;
		lastMouseX	= x;
		lastMouseY	= y;
		^super.mouseUp( x, y, *rest );
	}
	
	mouseMove { arg x, y ... rest;
		lastMouseX	= x;
		lastMouseY	= y;
		^super.mouseMove( x, y, *rest );
	}
	
	mouseOver { arg x, y ... rest;
		lastMouseX	= x;
		lastMouseY	= y;
		^super.mouseOver( x, y, *rest );
	}

	prInitView {
//		relativeOrigin	= false;
		jinsets			= Insets( 3, 3, 3, 3 );
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.UserView", ']' ]
		]);
	}

	prSendProperty { arg key, value;

		key	= key.asSymbol;

		// fix keys
		case { key === \relativeOrigin }
		{
			if( penID.notNil, { server.sendMsg( '/set', penID, \absCoords, value.not )});
			^nil;
		};
		^super.prSendProperty( key, value );
	}
}

JSCTextView : JSCView {
	classvar <>verbose = false;

	var <stringColor, <font, <editable = true;
	var <autohidesScrollers = false, <hasHorizontalScroller = false, <hasVerticalScroller = false;
	var <usesTabToFocusNextView = true, <enterInterpretsSelection = true;
//	var <textBounds;
	var <linkAction, <linkEnteredAction, <linkExitedAction;

	var txResp, hyResp;
	
	var <string = "";
	var selStart = 0, selStop = 0;
	
	var cnID;

//	mouseDown { arg clickPos;
////		this.focus(true);
//		mouseDownAction.value( this, clickPos );	
//	}	
	
//	string {
//		^this.getProperty( \string );
//	}

	// ----------------- public instance methods -----------------

	doLinkAction { arg url, description;
		linkAction.value( this, url, description );
	}
	
	string_ { arg str;
		^this.setString( str, -1 );
	}
		
	selectedString {
		^string.copyRange( selStart, selStop - 1 );  // stupid inclusive ending
	}
	
	selectedString_ { arg str;
		this.setString( str, selStart, selStop - selStart );
		this.select( selStart, str.size );
//		this.setProperty( \selectedString, str );
		// XXX
	}
	
	caret { ^selStart }
	selectionStart { ^selStart }
	selectionSize { ^(selStop - selStart) }
	
//	lineWrap_ { arg onOff;
//		server.sendMsg( '/set', this.id, \lineWrap, onOff );
//	}
	
	stringColor_ { arg color;
		stringColor = color;
		this.setStringColor( color, -1, 0 );
	}
	
	setStringColor { arg color, rangeStart = -1, rangeSize = 0;
		server.listSendMsg([ '/method', this.id, \setForeground, rangeStart, rangeSize ] ++ color.asSwingArg );
	}
	
	font_ { arg afont;
		font = afont;
		this.setFont( font, -1, 0 );
	}
	
	setFont { arg font, rangeStart = -1, rangeSize = 0;
		server.listSendMsg([ '/method', this.id, \setFont, rangeStart, rangeSize ] ++ font.asSwingArg );
	}
	
	tabs_ { arg tabs;
		this.setTabs( tabs, -1, 0 );
	}
	
	/**
	 *	@param	tabs		array of either positions (SimpleNumber) in pixels
	 *					or of two-element arrays [ position, align ]
	 *					where align is any of \left, \right, \center, \decimal, \bar
	 */
	setTabs { arg tabs, rangeStart = -1, rangeSize = 0;
		var pos, align, leader;
		tabs = tabs.collect({ arg t; #pos, align, leader = t.asArray; [ pos,
			(([ \left, \right, \center, nil, \decimal, \bar ].indexOf( align ) ? 0) << 8 ) |
			// note: leaders are currently not working!
			([ \none, \dots, \hyphens, \underline, \thickline, \equals ].indexOf( leader ) ? 0)]}).flatten;
		server.listSendMsg([ '/method', this.id, \setTabs, rangeStart, rangeSize ] ++ tabs.asSwingArg );
	}
	
 	leftIndent_ { arg indent;
		this.setLeftIndent( indent, -1, 0 );
	}
	
	/**
	 *	@param	indent	paragraph left indentation in pixels
	 */
 	setLeftIndent { arg indent, rangeStart = -1, rangeSize = 0;
		server.sendMsg( '/method', this.id, \setLeftIndent, rangeStart, rangeSize, indent );
	}
	
 	rightIndent_ { arg indent;
		this.setRightIndent( indent, -1, 0 );
	}
	
	/**
	 *	@param	indent	paragraph right indentation in pixels
	 */
 	setRightIndent { arg indent, rangeStart = -1, rangeSize = 0;
		server.sendMsg( '/method', this.id, \setRightIndent, rangeStart, rangeSize, indent );
	}
	
 	spaceAbove_ { arg space;
		this.setSpaceAbove( space, -1, 0 );
	}
	
	/**
	 *	@param	space	paragraph's top margin in pixels
	 */
 	setSpaceAbove { arg space, rangeStart = -1, rangeSize = 0;
		server.sendMsg( '/method', this.id, \setSpaceAbove, rangeStart, rangeSize, space );
	}
	
 	spaceBelow_ { arg space;
		this.setSpaceBelow( space, -1, 0 );
	}
	
	/**
	 *	@param	space	paragraph's bottom margin in pixels
	 */
 	setSpaceBelow { arg space, rangeStart = -1, rangeSize = 0;
		server.sendMsg( '/method', this.id, \setSpaceBelow, rangeStart, rangeSize, space );
	}
	
 	lineSpacing_ { arg spacing;
		this.setLineSpacing( spacing, -1, 0 );
	}
	
	/**
	 *	@param	spacing	paragraph's line spacing factor
	 */
 	setLineSpacing { arg spacing, rangeStart = -1, rangeSize = 0;
		server.sendMsg( '/method', this.id, \setLineSpacing, rangeStart, rangeSize, spacing );
	}
	
 	align_ { arg mode;
		this.setAlign( mode, -1, 0 );
	}
	
	/**
	 *	Sets paragraphs' alignment. The naming was chosen to
	 *	correspond with the align_ method in JSCTextField.
	 *
	 *	@param	mode		alignment mode (Symbol), one of \left, \center, \right, and \justified
	 */
 	setAlign { arg mode, rangeStart = -1, rangeSize = 0;
		server.sendMsg( '/method', this.id, \setAlignment, rangeStart, rangeSize,
			[ \left, \center, \right, \justified ].indexOf( mode ) ? 0 );
	}
	
	setString { arg string, rangeStart = 0, rangeSize = 0;
		var bndl, off, len, bndlSize;
	
//		string		= string.asString;
		
		// server.options.oscBufSize - sizeof([ '/method', 1234, \setString, 0, 1, "" ])
		if( string.size <= (server.options.oscBufSize - 44), {
			server.sendMsg( '/method', this.id, \setString, rangeStart, rangeSize, string );
		}, {
			bndl	= Array( 3 );
			off	= 0;
			// [ #bundle, [ '/method', 1234, \beginDataUpdate ],
			//            [ '/method', 1234, \addData, "GA" ],
			//            [ '/method', 1234, \endDataUpdate, 0, 1 ]
			bndlSize = 136;
			bndl.add([ '/method', this.id, \beginDataUpdate ]);
			while({ off < string.size }, {
				len = min( string.size - off, server.options.oscBufSize - bndlSize );
				bndl.add([ '/method', this.id, \addData, string.copyRange( off, off + len - 1 )]);
				off = off + len;
				if( off < string.size, {
					server.listSendBundle( nil, bndl );
					bndl = Array( 2 );
					bndlSize = 100; // wie oben, jedoch ohne \beginDataUpdate
				});
			});
			bndl.add([ '/method', this.id, \endDataUpdate, rangeStart, rangeSize ]);
			server.listSendBundle( nil, bndl );
		});
	}
	
	// e.g. "<HTML><BODY><H1>Heading</H1><P>Paragraph</P></BODY></HTML>"
	htmlString_ { arg content;
		var kitID, docID;
		kitID = server.nextNodeID;
		docID = server.nextNodeID;
		server.sendBundle( nil,
			[ '/local', kitID, '[', '/new', "javax.swing.text.html.HTMLEditorKit", ']',
			            docID, '[', '/method', kitID, \createDefaultDocument, ']' ],
//			[ '/method', this.id, \setContentType, "text/html", ']',
			[ '/set', this.id, \editorKit, '[', '/ref', kitID, ']',
			                   \document, '[', '/ref', docID, ']' ],
			[ '/method', kitID, \insertHTML, '[', '/ref', docID, ']', 0,
				content,
//				"<html><body><b>This is bold</b><i>this is italics</i></html></body>",
				0, 0,
				'[', '/field', "javax.swing.text.html.HTML$Tag", "BODY", ']' ]
		);
	}

// don't know how this is supposed to work (the parent tag etc.)
//	insertHTML { arg htmlString, pos = 0, tag = "BODY";
//		server.sendMsg( '/methodr', '[', '/method', this.id, \getEditorKit, ']',
//			\insertHTML, '[', '/method', this.id, \getDocument, ']', pos, htmlString, 0, 0,
//				'[', '/field', "javax.swing.text.html.HTML$Tag", tag, ']' );
//	}
	
	editable_ { arg bool;
		editable = bool;
		server.sendMsg( '/set', this.id, \editable, bool );
	}
	
	linkAction_ { arg func;
		if( func.notNil && hyResp.isNil, { this.prCreateLinkResponder });
		linkAction = func;
	}

	linkEnteredAction_ { arg func;
		if( func.notNil && hyResp.isNil, { this.prCreateLinkResponder });
		linkEnteredAction = func;
	}

	linkExitedAction_ { arg func;
		if( func.notNil && hyResp.isNil, { this.prCreateLinkResponder });
		linkExitedAction = func;
	}
	
	usesTabToFocusNextView_ { arg bool;
		usesTabToFocusNextView = bool;
		this.setProperty( \usesTabToFocusNextView, bool );
	}
	
	enterInterpretsSelection_ { arg bool;
		enterInterpretsSelection = bool;
//		this.setProperty( \enterExecutesSelection, bool );
	}
	
	autohidesScrollers_ { arg bool;
		autohidesScrollers = bool;
		this.prUpdateScrollers;
	}
	
	hasHorizontalScroller_{ arg bool;
		hasHorizontalScroller = bool;
		this.prUpdateScrollers;
	}
	
	hasVerticalScroller_{ arg bool;
		hasVerticalScroller = bool;
		this.prUpdateScrollers;
	}
	
// what's the point about this method??
//	textBounds_{ arg rect;
//		textBounds = rect;
//		this.setProperty(\textBounds, rect);
//	}

	caretColor { ^this.getProperty( \caretColor )}
	caretColor_ { arg color; this.setProperty( \caretColor, color )}

	openURL { arg url;
//		server.sendMsg( '/method', this.id, \setPage, '[', '/new', "java.net.URL", url, ']' );
//		server.sendMsg( '/set', this.id, \page, url );
		// XXX update client send string rep.
		server.sendMsg( '/method', this.id, \readURL, '[', '/new', "java.net.URL", url, ']' );
	}

	open { arg path;
		var file;
		if( path.beginsWith( "SC://" ), {
			path = Help.findHelpFile( path.copyToEnd( 5 ));
		}, {
			path = path.absolutePath;
		});
//		server.sendMsg( '/set', this.id, \page, '[', '/methodr', '[', '/new', "java.io.File", path, ']', 'toURL', ']' );
		server.sendMsg( '/method', this.id, \read, path );
	}
	
	select { arg start, len;
		server.sendMsg( '/method', this.id, \select, start, start + len );
	}
	
	selectAll {
		server.sendMsg( '/method', this.id, \selectAll );
	}

	caret_ { arg pos;
		server.sendMsg( '/set', this.id, \caretPosition, pos );
	}

	defaultKeyDownAction { arg key, modifiers, unicode;
		// check for 'ctrl+enter' = interprete
		if( (unicode == 0x0D) and: { ((modifiers & 0x40000) != 0) && enterInterpretsSelection }, {
			if( selStop > selStart, {	// text is selected
				this.selectedString.interpretPrint;
			}, {
				this.prCurrentLine.interpretPrint;
			});
			^this;
		});
		^nil;
	}

	// ----------------- private instance methods -----------------
	
	prCreateLinkResponder {
		if( hyResp.notNil, {
			"JSCTextView.prCreateLinkResponder : already created!".warn;
			^nil;
		});
		hyResp = OSCpathResponder( server.addr, [ '/hyperlink', this.id ], { arg time, resp, msg; var url, descr;
			{
				url   = msg[3].asString;
				descr = msg[4].asString;
				switch( msg[2],
					\ACTIVATED, { linkAction.value( this, url, descr )},
					\ENTERED,   { linkEnteredAction.value( this, url, descr )},
					\EXITED,    { linkExitedAction.value( this, url, descr )}
				);
			}.defer;
		}).add;
		server.sendMsg( '/local', "hy" ++ this.id,
			'[', '/new', "de.sciss.swingosc.HyperlinkResponder", this.id, ']' );
	}
	
	prContainerID { ^cnID }

	prInitView {
		cnID = "cn" ++this.id;
//		properties.put( \value, 0 );
		
		txResp = OSCpathResponder( server.addr, [ '/doc', this.id ], { arg time, resp, msg;
			var state, str;
			
			state = msg[2];
	
			case
			{ state === \insert }
			{
//				("insert at "++msg[3]++" len "++msg[4]++" text='"++msg[5]++"'").postln;
				str = msg[5].asString;
if( verbose and: { msg[ 4 ] != str.size }, { ("JSCTextView discrepancy. SwingOSC sees " ++ msg[ 4 ] ++ " characters, SuperCollider sees " ++ str.size ).postln });
				string = string.insert( msg[3], str );
				if( action.notNil, {{ action.value( this, state, msg[3], msg[4], str )}.defer });
			}
			{ state === \remove }
			{
//				("remove from "++msg[3]++" len "++msg[4]).postln;
				string = string.keep( msg[3] ) ++ string.drop( msg[3] + msg[4] );
				if( action.notNil, {{ action.value( this, state, msg[3], msg[4] )}.defer });
			}
			{ state === \caret }
			{
//				("caret now between "++msg[3]++" and "++msg[4]).postln;
				if( msg[3] < msg[4], {
					selStart	= msg[3];
					selStop	= msg[4];
				}, {
					selStart	= msg[4];
					selStop	= msg[3];
				});
				if( action.notNil, {{ action.value( this, state, msg[3], msg[4] )}.defer });
			};
		}).add;
		
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.TextView", ']',
				"cn" ++ this.id,				 	// bars : v=never, h=never
				'[', '/new', "javax.swing.JScrollPane", '[', '/ref', this.id, ']', 21, 31, ']',
				"tx" ++ this.id,
				'[', '/new', "de.sciss.swingosc.DocumentResponder", this.id, ']'
			]
		]);
	}

	prUpdateScrollers {
		server.sendMsg( '/set', "cn" ++ this.id,
			\horizontalScrollBarPolicy, hasHorizontalScroller.if( autohidesScrollers.if( 30, 32 ), 31 ),
			\verticalScrollBarPolicy, hasVerticalScroller.if( autohidesScrollers.if( 20, 22 ), 21 ));
	}

	prClose { arg preMsg, postMsg;
		txResp.remove;
		hyResp.remove; // nil.remove is allowed
		^super.prClose( preMsg ++ [
			[ '/method', "tx" ++ this.id, \remove ]] ++
			if( hyResp.notNil, {[[ '/method', "hy" ++ this.id, \remove ]]}), postMsg );
	}

	prCurrentLine {
		var startIdx, stopIdx;
		
		startIdx	= string.findBackwards( "\n", false, selStart - 1 ) ? 0;
		stopIdx	= string.find( "\n", false, selStart ) ?? { string.size };
		^string.copyRange( startIdx, stopIdx - 1 );
	}
}

JSCAbstractMultiSliderView : JSCView { 

	var <>metaAction;
	var <size = 0;
		
	// ----------------- public instance methods -----------------

	step_ { arg stepSize; this.setPropertyWithAction( \step, stepSize )}
	
	step { ^this.getProperty( \step )}
	
	selectionSize { ^this.getProperty( \selectionSize )}

	selectionSize_ { arg aval; this.setProperty( \selectionSize, aval )}

	currentvalue { // returns value of selected index
		^this.getProperty( \y );
	}
	
	strokeColor_ { arg acolor; this.setProperty( \strokeColor, acolor )}

	currentvalue_ { arg iny; this.setProperty( \y, iny )}
	
	drawLines { arg abool; this.setProperty( \drawLines, abool )}

	drawLines_ { arg abool; this.drawLines( abool )}
	
	drawRects_ { arg abool; this.setProperty( \drawRects, abool )}

	doMetaAction { // performed on ctrl click
		metaAction.value( this );
	} 

	// ----------------- private instance methods -----------------

	properties {
		^super.properties ++ #[ \value, \strokeColor, \x, \y, \drawLines, \drawRects, \selectionSize, \step ]; // JJJ not thumbSize, thumbWidth, not absoluteX
	}
	
	defaultCanReceiveDrag {	^true }
			
	prNeedsTransferHandler { ^true }
}

JSCMultiSliderView : JSCAbstractMultiSliderView { 

	var acResp;	// OSCpathResponder for action listening
	var vlResp;	// OSCpathResponder for value update listening
	var clpse;

	var <gap;
	var <editable = true;
	var <elasticMode = 0;
	var <steady = false, <precision = 0.05;
		
	// ----------------- public instance methods -----------------

	elasticMode_{ arg mode;
		elasticMode = mode;
		this.setProperty( \elasticResizeMode, mode );
	}

	value { // returns array
		^this.getProperty( \value, Array.newClear( this.size ));
	}
	
	value_ { arg val;
		size = val.size;
		this.setProperty( \value, val.copy );
	}

	valueAction_ { arg val;
		size = val.size;	
		this.setPropertyWithAction( \value, val.copy );
	}
	
	reference { // returns array
		^this.getProperty( \referenceValues, Array.newClear( this.size ));
	}
	
	reference_ { arg val;
		// this.size = val.size;
		this.setProperty( \referenceValues, val );
	}
	
	index { // returns selected index
		^this.getProperty( \x );
	}
	
	index_ { arg inx;
		this.setProperty( \x, inx );
	}
	
	fillColor_ { arg acolor; this.setProperty( \fillColor, acolor )}

	colors_ { arg strokec, fillc;
		this.strokeColor_( strokec );
		this.fillColor_( fillc );
	}
	
	isFilled_ { arg abool;
		this.setProperty( \isFilled, abool );
	}
	
	xOffset_ { arg aval;
		this.setProperty( \xOffset, aval );
	}
	
	gap_ { arg inx;
		gap = inx;
		this.setProperty( \xOffset, inx );
	}
	
	startIndex_ { arg val; this.setProperty( \startIndex, val )}
	
	showIndex_ { arg abool; this.setProperty( \showIndex, abool )}
	
	// = thumb width
	indexThumbSize_ { arg val; this.setProperty( \thumbWidth, val )}

	// = thumb height
	valueThumbSize_ { arg val; this.setProperty( \thumbHeight, val )}

	indexIsHorizontal_ { arg val; this.setProperty( \isHorizontal, val )}
	
	thumbSize_ { arg val;
		properties.put( \thumbWidth, val );
		properties.put( \thumbHeight, val );
		server.sendMsg( '/set', this.id, \thumbSize, val );
	}
	
	readOnly_ { arg val;
		editable = val.not;
		this.setProperty( \readOnly, val );
	}
	
	editable_ { arg val;
		editable = val;
		this.setProperty( \readOnly, editable.not );
	}
	
	defaultReceiveDrag {
		if( currentDrag[ 0 ].isSequenceableCollection, { 
			this.value_( currentDrag[ 0 ]);
			this.reference_( currentDrag[ 1 ]);
		}, {
			this.value_( currentDrag );
		});
	}
	
	defaultGetDrag {
		var setsize, vals, rvals, outval;
		rvals = this.reference;
		vals = this.value;
		if( this.selectionSize > 1, {
			vals = vals.copyRange( this.index, this.selectionSize + this.index );
		});
		if( rvals.isNil, { 
			^vals; 
		}, {
			if( this.selectionSize > 1, {
				rvals = rvals.copyRange( this.index, this.selectionSize + this.index );
			});
			outval = outval.add( vals );
			outval = outval.add( rvals );
		});
		^outval;
	}
		
	defaultKeyDownAction { arg key, modifiers, unicode;
		//modifiers.postln; 16rF702
		if (unicode == 16rF703, { this.index = this.index + 1; ^this });
		if (unicode == 16rF702, { this.index = this.index - 1; ^this });
		if (unicode == 16rF700, { this.gap = this.gap + 1; ^this });
		if (unicode == 16rF701, { this.gap = this.gap - 1; ^this });
		^nil		// bubble if it's an invalid key
	}

	steady_ { arg bool;
		if( steady != bool, {
			steady = bool;
			server.sendMsg( \set, this.id, \steady, bool );
		});
	}

	precision_ { arg factor;
		if( precision != factor, {
			precision = factor;
			server.sendMsg( \set, this.id, \precision, factor );
		});
	}
	
	// ----------------- private instance methods -----------------

	properties {
		^super.properties ++ #[ \elasticResizeMode, \fillColor, \thumbWidth, \thumbHeight, \xOffset, \showIndex, \startIndex, \referenceValues, \isFilled, \readOnly ]; // JJJ not \thumbSize, but \thumbHeight, added \readOnly
	}
		
	prClose { arg preMsg, postMsg;
		vlResp.remove;
		acResp.remove;
		clpse.cancel;
		^super.prClose( preMsg ++
			[[ '/method', "ac" ++ this.id, \remove ],
			 [ '/free', "ac" ++ this.id ]], postMsg );
	}

	prInitView {
		var initVal;
		initVal	= 0 ! 8;
		properties.put( \value, initVal );
		properties.put( \x, 0 );
		properties.put( \y, 0.0 );
		properties.put( \step, 0.0 );
		jinsets	= Insets( 3, 3, 3, 3 );
		clpse	= Collapse({ this.doAction });
		vlResp	= OSCpathResponder( server.addr, [ '/values', this.id ], { arg time, resp, msg;
			var dirtyIndex, dirtySize, vals, selectedIndex;

			vals			= properties[ \value ];
			dirtyIndex	= min( msg[ 2 ], vals.size );
			dirtySize		= min( msg[ 3 ], vals.size - dirtyIndex );
			
			dirtySize.do({ arg i;
				vals[ dirtyIndex + i ] = msg[ 4 + i ];
			});
			selectedIndex	= this.getProperty( \x, -1 );
//("selectedIndex = "++selectedIndex++"; vals = "++vals).inform;
			if( (selectedIndex >= dirtyIndex) and: { selectedIndex < (dirtyIndex + dirtySize) }, {
				properties.put( \y, vals[ selectedIndex ]);
			});
			if( dirtySize > 0, { clpse.instantaneous });
		}).add;
		acResp = OSCpathResponder( server.addr, [ '/action', this.id ], { arg time, resp, msg;
			var dirtyIndex, dirtySize, vals, selectedIndex;

			selectedIndex	= msg[ 4 ];
			properties.put( \x, selectedIndex );
			properties.put( \selectionSize, msg[ 6 ]);
			dirtyIndex	= msg[ 8 ];
			dirtySize		= msg[ 10 ];

			if( dirtySize == 0, {
				vals = properties[ \value ];
				properties.put( \y, vals[ selectedIndex ]);
				clpse.instantaneous;
			});
		}).add;
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.MultiSlider", ']',
				"ac" ++ this.id,
				'[', '/new', "de.sciss.swingosc.ActionMessenger",  // ActionResponder
					this.id, '[', '/array', \selectedIndex, \selectionSize, \dirtyIndex, \dirtySize, ']',
					\sendValuesAndClear, '[', '/array', this.id, ']', ']' ],
			[ '/set', this.id, \values ] ++ initVal.asSwingArg
		]);
	}

	prSendProperty { arg key, value;
		key	= key.asSymbol;

		// fix keys
		switch( key,
		\value, {
			key = \values;
			this.prFixValues;
		},
		\x, {
			key = \selectedIndex;
		},
		\isFilled, {
			key = \filled;
		},
		\isHorizontal, {
			key 		= \orientation;
			value	= if( value, 0, 1 );
		},
		\step, {
			key = \stepSize;
			this.prFixValues;
		});
		^super.prSendProperty( key, value );
	}
	
	prFixValues {
		var val, step;
		
		val	= properties[ \value ];
		step	= this.step;
		if( step > 0, {
			val.size.do({ arg i; val[ i ] = val[ i ].round( step ).clip( 0.0, 1.0 )});
		}, {
			val.size.do({ arg i; val[ i ] = val[ i ].clip( 0.0, 1.0 )});
		});
	}
}

JSCEnvelopeView : JSCAbstractMultiSliderView {
	var allConnections, selection;
	var items;
	var connectionsUsed = false;
	var idx = 0;	// the one that corresponds to select, x_ and y_
	
	var acResp;
	var vlResp;
	var clpse;

// rather useless behaviour in SCEnvelopeView (using shift+click you can ignore it),
// so keep it out for now
//	var <fixedSelection = false;
	
	// ----------------- public class methods -----------------

	*paletteExample { arg parent, bounds;
		^this.new( parent, bounds ).value_([ (0..4)/4, sqrt( (0..4)/4 )])
			.thumbSize_( 4 ).selectionColor_( Color.red );
	}

	// ----------------- public instance methods -----------------

	value_ { arg val;
		var oldSize, xvals, yvals, curves, valClip;
		
		oldSize	= size;
		xvals	= val[ 0 ];
		yvals	= val[ 1 ];
		curves	= val[ 2 ];
		if( xvals.size != yvals.size, {
			Error( "JSCEnvelopeView got mismatched times/levels arrays" ).throw;
		});
		size 	= xvals.size;
		case
		{ oldSize < size }
		{
			if( allConnections.notNil, {
				allConnections = allConnections.growClear( size );
			});
			if( items.notNil, {
				items = items.growClear( size );
			});
			selection = selection.growClear( size ).collect({ arg sel; if( sel.isNil, false, sel )});
		}
		{ oldSize > size }
		{
			if( allConnections.notNil, {
				allConnections = allConnections.copyFromStart( size - 1 );
			});
			if( items.notNil, {
				items = items.copyFromStart( size - 1 );
			});
			selection = selection.copyFromStart( size - 1 );
		};

		xvals =  xvals.collect(_.clip( 0.0, 1.0 ));
		yvals =  yvals.collect(_.clip( 0.0, 1.0 ));
		if( curves.isNil, {
			valClip = [ xvals, yvals ];
		}, {
			valClip = [ xvals, yvals, curves.asArray.clipExtend( size )];
		});
		this.setProperty( \value, valClip );
	}
	
	setString { arg index, astring;
		if( items.isNil, {
			items = Array.newClear( size );
		});
		if( index < 0, {
			items.fill( astring );
		}, { if( index < size, {
			items[ index ] = astring;
		})});
		// items = items.add( astring );
//		this.setProperty( \string, [ index, astring ]);
		server.listSendMsg([ '/method', this.id, \setLabel, index ] ++ astring.asSwingArg );
	}

	strings_ { arg astrings;
		astrings.do({ arg str,i;
//			this.string_( i, str );
			this.setString( i, str );
		});
	}
	
	strings {
		^items.copy;	// nil.copy allowed
	}
	
//	items_ { arg items; ^this.strings_( items )}
	
	value {
//		var ax, ay, axy;
//		ax = Array.newClear( this.size );
//		ay = Array.newClear( this.size );
//		axy = Array.with( ax, ay );
//		^this.getProperty( \value, axy );
		^properties[ \value ].deepCopy;
	}
	
	selection {
		^selection.copy;
	}
	
	connections {
		var result;
		if( allConnections.isNil, { ^nil });
		
		result = Array( allConnections.size );
		allConnections.do({ arg cons; result.add( cons.copy )});
		^result;
	}
	
	setThumbHeight { arg index, height;
//		this.setProperty( \thumbHeight, [ index, height ]);
		server.sendMsg( '/method', this.id, \setThumbHeight, index, height );
	}
	
	thumbHeight_ { arg height; this.setThumbHeight( -1, height )}
	
	setThumbWidth { arg index, width;
//		this.setProperty( \thumbWidth, [ index, width ]);
		server.sendMsg( '/method', this.id, \setThumbWidth, index, width );
	}

	thumbWidth_ { arg width; this.setThumbWidth( -1, width )}

	setThumbSize { arg index, size;
//		this.setProperty(\thumbSize, [index, size]);
		server.sendMsg( '/method', this.id, \setThumbSize, index, size );
	}
	
	thumbSize_ { arg size; this.setThumbSize( -1, size )}

	setFillColor { arg index, color;
//		this.setProperty(\fillColor, [index, color]);
		server.listSendMsg([ '/method', this.id, \setFillColor, index ] ++ color.asSwingArg );
	}

	fillColor_ { arg color; this.setFillColor( -1, color )}

	colors_ { arg strokec, fillc;
		this.strokeColor_( strokec );
		this.fillColor_( fillc );
	}

	curves_ { arg curves;
		var value;
		if( curves.isArray, {
			value = properties[ \value ];
//			curves.asArray.clipExtend( size ).postln;
			value = [ value[ 0 ], value[ 1 ], curves.asArray.clipExtend( size )];
			this.setProperty( \value, value );
		}, {
			this.setCurve( -1, curves );
		});
	}
	
	setEnv { arg env, minValue, maxValue, minTime, maxTime;
		var times, levels;
		var spec;

		times		= [ 0.0 ] ++ env.times.integrate;
		maxTime		= maxTime ? times.last;
		minTime		= minTime ? 0.0;
		levels		= env.levels;
		minValue		= minValue ? levels.minItem;
		maxValue		= maxValue ? levels.maxItem;
		
		levels		= levels.linlin( minValue, maxValue, 0, 1 );
		times		= times.linlin( minTime, maxTime, 0, 1 );
		
		this.value_([ times.asFloat, levels.asFloat, env.curves ]);
	}

	editEnv { arg env, minValue, maxValue, duration;
		var vals, levels, times, viewDur;
		vals		= this.value;
		times	= vals[ 0 ].differentiate.copyToEnd( 1 );
		viewDur	= vals[ 0 ].last - vals[ 0 ].first;
		if( viewDur > 0, {
			times = times / viewDur * (duration ? 1.0);
		});
		levels	= vals[ 1 ].linlin( 0, 1, minValue, maxValue );
		env.times_( times );
		env.levels_( levels );
		env.curves_( vals[ 2 ] ? \lin );
	}

	asEnv { arg minValue, maxValue, duration;
		var env;
		env = Env.new;
		this.editEnv( env, minValue, maxValue, duration );
		^env;
	}
	
	curve_ { arg curve = \lin; this.setCurve( -1, curve )}
	
	setCurve { arg index, curve = \lin;
		var shape, value, curves;
		value  = properties[ \value ];
		curves = value[ 2 ];
		if( index == -1, {
			if( curves.notNil, {
				curves.fill( curve );
			}, {
				properties[ \value ] = value ++ [(curve ! value[1].size)];
			});
		}, { if( index < size, {
			if( curves.notNil, {
				curves[ index ] = curve;
			}, {
				properties[ \value ] = value ++ [(\lin ! value[1].size).put( index, curve )];
			});
		})});
		if( curve.isNumber, {
			shape = 5;
			curve = curve.asFloat;
		}, {
			shape = Env.shapeNames[ curve ] ? 0;
			curve = 0.0;
		});
		server.sendMsg( '/method', this.id, \setShape, index, shape, curve );
	}
	
	lockBounds_ { arg val; this.setProperty( \lockBounds, val )}
	
	horizontalEditMode_ { arg val; this.setProperty( \horizontalEditMode, val )}
	
	connect { arg from, aconnections;
		var bndl, target, targetCons, fromCons;

		if( (from < 0) || (from >= size), { ^this });

		bndl			= Array( aconnections.size + 1 ); // max. number of messages needed
		fromCons		= Array( aconnections.size );

		if( connectionsUsed.not, {
			bndl.add([ '/set', this.id, \connectionsUsed, true ]);
			connectionsUsed	= true;
			allConnections	= Array.newClear( size );
		});

		aconnections.do({ arg target;
			target = target.asInteger;
			if( (target >= 0) && (target < size) && (target != from), {
				fromCons.add( target );
				targetCons = allConnections[ target ];
				if( targetCons.isNil or: { targetCons.includes( from ).not }, {
					targetCons = targetCons ++ [ from ];
					allConnections[ target ] = targetCons;
					// don't draw connections twice, so simply set only connections on the server whose target idx is greater than from idx
					bndl.add([ '/method', this.id, \setConnections, target ] ++ targetCons.reject({ arg idx; idx < target }).asSwingArg );
				});
			});
		});
		allConnections[ from ] = fromCons;
		bndl.add([ '/method', this.id, \setConnections, target ] ++ fromCons.reject({ arg idx; idx < from }).asSwingArg );
		server.listSendBundle( nil, bndl );
	}

	select { arg index; // this means no refresh;
		var vals;
//		this.setProperty(\setIndex, index);
		idx = index;
		if( (idx >= 0) && (idx < size), {
			vals = properties[ \value ];
			properties.put( \x, vals[ 0 ][ index ]);
			properties.put( \y, vals[ 1 ][ index ]);
		});
		server.sendMsg( '/set', this.id, \index, index );
	}
	
	selectIndex { arg index; // this means that the view will be refreshed
//		this.setProperty( \selectedIndex, index );
		properties.put( \selectedIndex, index );
		if( (idx >= 0) && (idx < size), {
			selection[ index ] = true;
		});
		server.sendMsg( '/method', this.id, \setSelected, index, true );
	}
	
	deselectIndex { arg index; // this means that the view will be refreshed
//		properties.put( \selectedIndex, index );
		if( (idx >= 0) && (idx < size), {
			selection[ index ] = false;
		});
		server.sendMsg( '/method', this.id, \setSelected, index, false );
	}
	
	x { ^this.getProperty( \x )}  // returns selected x
	y { ^this.getProperty( \y )}

	x_ { arg ax;
		ax = ax.round( this.step ).clip( 0.0, 1.0 );
		if( idx == -1, {
			properties[ \value ][ 0 ].fill( ax );
		}, { if( idx < size, {
			properties[ \value ][ 0 ][ idx ] = ax;
		})});
		this.setProperty( \x, ax );
	}

	y_ { arg ay;
		ay = ay.round( this.step ).clip( 0.0, 1.0 );
		if( idx == -1, {
			properties[ \value ][ 1 ].fill( ay );
		}, { if( idx < size, {
			properties[ \value ][ 1 ][ idx ] = ay;
		})});
		this.setProperty( \y, ay )
	}

	index { ^this.getProperty( \selectedIndex )}

//	lastIndex { ^this.getProperty( \lastIndex )}

	setEditable { arg index, boolean;
//		this.setProperty(\editable, [index,boolean]);
		server.sendMsg( '/method', this.id, \setReadOnly, index, boolean.not );
	}

	editable_{ arg boolean; this.setEditable( -1, boolean )}	
	selectionColor_ { arg acolor; this.setProperty( \selectionColor, acolor )}
	
// currently broken in cocoa
/*
	addValue { arg xval, yval;
		var arr, arrx, arry, aindx;
		// XXX could use custom server method!!
		aindx = this.lastIndex;
//		aindx.postln;
		if( xval.isNil && yval.isNil, {
			arr = this.value;
			arrx = arr @ 0;
			arry = arr @ 1;
			xval = arrx[ aindx ] + 0.05;
			yval = arry[ aindx ];
		});
		if( aindx < (arrx.size - 1), {
			arrx = arrx.insert( aindx + 1, xval );
			arry = arry.insert( aindx + 1, yval );
		}, {
			arrx = arrx.add( xval );
			arry = arry.add( yval );
		});		
		this.value_([ arrx, arry ]);
	}
*/

// see comment for <fixedSelection	
//	fixedSelection_ { arg bool;
//		fixedSelection =  bool;
//		this.setProperty(\setFixedSelection, bool);
//	}

	font { ^this.getProperty( \font )}

	font_ { arg argFont;
		this.setProperty( \font, argFont );
	}
	
	clipThumbs { ^this.getProperty( \clipThumbs )}

	clipThumbs_ { arg bool; this.setProperty( \clipThumbs, bool )}

	defaultGetDrag { ^this.value }

// currently broken in cocoa
/*
	defaultReceiveDrag {
		if( currentDrag.isString, {
			this.addValue;
//			items = items.insert( this.lastIndex + 1, currentDrag );
//			this.strings_( items );
			this.setString( this.lastIndex + 1, currentDrag );
		}, {
			this.value_( currentDrag );
		});
	}
*/
	defaultReceiveDrag { }
	
	defaultKeyDownAction { arg key, modifiers, unicode;
		var oldIdx, selIdx;

// gap is not working with envelope view!
//		if (unicode == 16rF700, { this.gap = this.gap + 1; ^this });
//		if (unicode == 16rF701, { this.gap = this.gap - 1; ^this });

		if( (unicode >= 16rF700) && (unicode <= 16rF703), {  // cursor
			selIdx	= this.index;
			oldIdx	= idx;
			if( (selIdx >= 0) and: { selIdx - 1 < this.size }, {
				case
				{ unicode == 16rF703 }	// cursor right
				{
					if( (modifiers & 524288) == 0, {	// test for alt
						this.select( selIdx );
						this.x = this.x + max( this.step, 0.015625 );
						this.select( oldIdx );
					}, { if( (selIdx + 1) < this.size, {
						this.deselectIndex( selIdx );
						this.selectIndex( selIdx + 1 );
					})});
				}
				{ unicode == 16rF702 }	// cursor left
				{
					if( (modifiers & 524288) == 0, {	// test for alt
						this.select( selIdx );
						this.x = this.x - max( this.step, 0.015625 );
						this.select( oldIdx );
					}, { if( selIdx > 0, {
						this.deselectIndex( selIdx );
						this.selectIndex( selIdx - 1 );
					})});
				}
				{ unicode == 16rF700 }	// cursor up
				{
					this.select( selIdx );
					this.y = this.y + max( this.step, 0.015625 );
					this.select( oldIdx );
				}
				{ unicode == 16rF701 }	// cursor down
				{
					this.select( selIdx );
					this.y = this.y - max( this.step, 0.015625 );
					this.select( oldIdx );
				};
			});
			^this;
		});
		^nil;		// bubble if it's an invalid key
	}

	// ----------------- private instance methods -----------------

	properties {
		^super.properties ++ #[ \font, \selectedIndex, \clipThumbs, \lockBounds, \horizontalEditMode ];  // \lastIndex
	}

	prClose { arg preMsg, postMsg;
		vlResp.remove;
		acResp.remove;
		clpse	= Collapse({ this.doAction });
		^super.prClose( preMsg ++
			[[ '/method', "ac" ++ this.id, \remove ],
			 [ '/free', "ac" ++ this.id ]], postMsg );
	}

	prInitView {
		var initVal;
//		initVal	= nil ! 8 ! 2;	// pretty stupid
		initVal	= [[],[]];
		properties.put( \value, initVal );
		properties.put( \index, -1 );
		properties.put( \x, 0.0 );
		properties.put( \y, 0.0 );
//		properties.put( \lastIndex, -1 );	// 0 in cocoa ...
		properties.put( \selectedIndex, -1 );
		properties.put( \step, 0.0 );
		properties.put( \clipThumbs, false );
		selection	= [];
		jinsets	= Insets( 3, 3, 3, 3 );
		clpse	= Collapse({ this.doAction });
//		items	= Array.new;
		vlResp	= OSCpathResponder( server.addr, [ '/values', this.id ], { arg time, resp, msg;
			var dirtySize, vals, xvals, yvals, action = false;

			vals			= properties[ \value ];
			xvals		= vals[ 0 ];
			yvals		= vals[ 1 ];
			dirtySize		= msg[ 2 ];
			msg.copyToEnd( 3 ).clump( 4 ).do({ arg entry; var idx, x, y, sel;
				#idx, x, y, sel = entry;
				if( idx < xvals.size, {
					xvals[ idx ]		= x;
					yvals[ idx ]		= y;
					selection[ idx ]	= sel != 0;
					action			= true;
				});
			});
			if( action, { clpse.instantaneous });
		}).add;
		acResp = OSCpathResponder( server.addr, [ '/action', this.id ], { arg time, resp, msg;
			var lastIndex, dirtySize;

			lastIndex	= msg[ 4 ];
			if( lastIndex >= 0, { properties.put( \selectedIndex, lastIndex )}); // \lastIndex
			dirtySize	= msg[ 5 ];

//			if( dirtySize == 0, {
//				vals = properties[ \value ];
//				properties.put( \y, vals[ selectedIndex ]);
//				clpse.instantaneous;
//			});
		}).add;
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.EnvelopeView", false, ']',
				"ac" ++ this.id,
				'[', '/new', "de.sciss.swingosc.ActionMessenger",  // ActionResponder
					this.id, '[', '/array', \lastIndex, \dirtySize, ']',
					\sendDirtyValuesAndClear, '[', '/array', this.id, ']', ']' ],
		]);
	}

	prSendProperty { arg key, value;
		var ival, shapes, curvesSC, curves;
		
		key	= key.asSymbol;

		// fix keys
		switch( key,
		\value, {
			this.prFixValues;
			if( value.size < 3, {
				// XXX should check against max bundle size
				server.listSendMsg([ '/method', this.id, \setValues ] ++ value[ 0 ].asSwingArg ++ value[ 1 ].asSwingArg );
			}, {
				curvesSC = value[ 2 ];
				if( curvesSC.isArray, {
					shapes = Array( curvesSC.size );
					curves = Array( curvesSC.size );
					curvesSC.do({ arg curve;
						if( curve.isNumber, {
							shapes.add( 5 );
							curves.add( curve.asFloat );
						}, {
							shapes.add( Env.shapeNames[ curve ] ? 0 );
							curves.add( 0.0 );
						});
					});
				}, {
					if( curvesSC.isNumber, {
						shapes = 5;
						curves = curvesSC.asFloat;
					}, {
						shapes = Env.shapeNames[ curvesSC ] ? 0;
						curves = 0.0;
					});
				});
				// XXX should check against max bundle size
				server.listSendMsg([ '/method', this.id, \setValues ] ++ value[ 0 ].asSwingArg ++ value[ 1 ].asSwingArg ++ shapes.asSwingArg ++ curves.asSwingArg );
			});
			^this;
		},
		\step, {
			key = \stepSize;
			this.prFixValues;
		},
		\horizontalEditMode, {
			ival = [ \free, \clamp, \relay ].indexOf( value );
			if( ival.isNil, { Error( "Illegal edit mode '" ++ value ++ "'" ).throw });
			value = ival;
		});
		^super.prSendProperty( key, value );
	}

	prFixValues {
		var val, step;
		
		val	= properties[ \value ];
		step	= this.step;
		if( step > 0, {
			2.do({ arg j; var xyvals = val[ j ]; xyvals.size.do({ arg i; xyvals[ i ] = xyvals[ i ].round( step ).clip( 0.0, 1.0 )})});
		}, {
			2.do({ arg j; var xyvals = val[ j ]; xyvals.size.do({ arg i; xyvals[ i ] = xyvals[ i ].clip( 0.0, 1.0 )})});
		});
	}

	prShapeNumber { arg name; ^Env.shapeNames.at( name ) ? 5 }
}

JSCTextEditBase : JSCStaticTextBase {

	var <>keyString;
	var <>typingColor, <>normalColor;
	var origBd;

	// ----------------- public instance methods -----------------

	caretColor { ^this.getProperty( \caretColor )}
	caretColor_ { arg color; this.setProperty( \caretColor, color )}

	value { ^object }
	
	value_ { arg val;
		keyString = nil;
//		this.stringColor = normalColor;
		object = val;
		this.string = object.asString;
	}
	
	valueAction_ { arg val;
		var prev;
		prev = object;
		this.value = val;
		if( object != prev, { this.doAction });
	}
	
	boxColor {
		^this.getProperty( \boxColor, Color.new );
	}
	
	boxColor_ { arg color;
		this.setProperty( \boxColor, color );
	}

	setNormalBorder {
		if( origBd.notNil, {
			server.sendBundle( nil, [ '/set', this.id, \border, '[', '/ref', origBd, ']' ], [ '/free', origBd ]);
			origBd = nil;
		});
	}

	setLineBorder { arg color = Color.black, thickness = 1;
		var msg;
		msg = [ '/set', this.id, \border, '[', '/method', 'javax.swing.BorderFactory', \createLineBorder ] ++
			color.asSwingArg ++ [ thickness, ']' ];
		if( origBd.isNil, {
			origBd = "bd" ++ this.id;
			server.sendBundle( nil, [ '/local', origBd, '[', '/method', this.id, \getBorder, ']' ], msg );
		}, {
			server.listSendMsg( msg );
		});
	}
	
	borderless_ { arg bool; ^if( bool, { this.setLineBorder( thickness: 0 )}, { this.setNormalBorder })}
	
	// ----------------- private instance methods -----------------

	properties {
		^super.properties ++ #[\boxColor]
	}

	init { arg argParent, argBounds, id;
		typingColor = Color.red;
		normalColor = Color.black;
		parent = argParent.asView; // actual view
// cocoa does parent.asView once more. too cryptic IMO ?
//		this.prInit( parent, argBounds.asRect, this.class.viewClass, parent.server, id );
		this.prInit( parent.asView, argBounds, this.class.viewClass, parent.server, id );
		argParent.add( this );//maybe window or viewadapter
	}

	prSendProperty { arg key, value;
		key	= key.asSymbol;

		// fix keys
		case { key === \boxColor }
		{
			key = \background;
			if( value == Color.clear, {
				value = nil;
			});
		};
		^super.prSendProperty( key, value );
	}

	prClose { arg preMsg, postMsg;
		if( origBd.notNil, {
			preMsg = preMsg.add([ '/free', origBd ]);
		});
		^super.prClose( preMsg, postMsg );
	}
	
	prNeedsTransferHandler { ^true }
}
