/*
 *	SwingGUI
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
 *	This is the Java/Swing(OSC) framework GUI implementation.
 *	It can be accessed from the GUI
 *	class using GUI.swing, GUI.fromID( \swing ) or GUI.get( \swing ).
 *
 *	@author		Hanns Holger Rutz
 *	@version		0.64, 30-Mar-10
 */
SwingGUI {
	classvar extraClasses;

	*initClass {
		Class.initClassTree( Event );
		extraClasses = Event.new;
		Class.initClassTree( GUI );
		GUI.add( this );
	}

	// ----------------- public class methods -----------------

	*id { ^\swing }
	
	*put { arg key, object;
		extraClasses.put( key, object );
	}

	///////////////// Common -> GUI /////////////////

	*freqScope { ^JFreqScope }
	*freqScopeView { ^JSCFreqScope }
	*scopeView { ^JSCScope }
	*stethoscope { ^JStethoscope }

	///////////////// Common -> GUI -> Base /////////////////

	*view { ^JSCView }
	*window { ^JSCWindow }
	*compositeView { ^JSCCompositeView }
	*hLayoutView { ^JSCHLayoutView }
	*vLayoutView { ^JSCVLayoutView }
	*slider { ^JSCSlider }
	*rangeSlider { ^JSCRangeSlider }
	*slider2D { ^JSC2DSlider }
//	*tabletSlider2D { ^JSC2DTabletSlider }
	*button { ^JSCButton }
	*popUpMenu { ^JSCPopUpMenu }
	*staticText { ^JSCStaticText }
	*listView { ^JSCListView }
	*dragSource { ^JSCDragSource }
	*dragSink { ^JSCDragSink }
	*dragBoth { ^JSCDragBoth }
	*numberBox { ^JSCNumberBox }
	*textField { ^JSCTextField }
	*userView { ^JSCUserView }
	*multiSliderView { ^JSCMultiSliderView }
	*envelopeView { ^JSCEnvelopeView }
	*tabletView { ^JSCTabletView }
	*soundFileView { ^JSCSoundFileView }
	*movieView { ^JSCMovieView }
	*textView { ^JSCTextView }
//	*quartzComposerView { ^JSCQuartzComposerView }
	*scrollView { ^JSCScrollView }
	*ezScroller { ^EZScroller }
	*ezSlider { ^EZSlider }
	*ezListView { ^EZListView }
	*ezPopUpMenu { ^EZPopUpMenu}
	*ezNumber { ^EZNumber}
	*ezRanger { ^EZRanger }	
	
	*knob { ^JKnob }		
	*font { ^JFont }
	*pen { ^JPen }
	*image { ^JSCImage }
	
	///////////////// Common -> Audio /////////////////

	*mouseX { ^JMouseX }
	*mouseY { ^JMouseY }
	*mouseButton { ^JMouseButton }
	*keyState { ^JKeyState }
			
	///////////////// Common -> OSX /////////////////

	*dialog { ^SwingDialog }
	*speech { ^JSpeech }

	///////////////// extras /////////////////
			
//	*panel { ^JSCPanel }
	*checkBox { ^JSCCheckBox }
	*tabbedPane { ^JSCTabbedPane }
	*scrollBar { ^JSCScrollBar }
	*peakMeter { ^JSCPeakMeter }

	///////////////// crucial /////////////////
//	*startRow { ^JStartRow }

	// ----------------- private class methods -----------------

	*doesNotUnderstand { arg selector ... args;
		^extraClasses.perform( selector, *args );
	}

	/**
	 *	Returns a Rect object describing the logical bounds of
	 *	a given (String) string as rendered at standard scale using a
	 *	given (JFont) font.
	 *
	 *	This method must be called from within a Routine, otherwise
	 *	the asynchronous SwingOSC call cannot be made and only a coarse
	 *	estimation (based on the Helvetica typeface) will be made.
	 *
	 *	Note that on Mac OS X, the calculated width is exactly the same
	 *	as if CocoaGUI was used, however there are variations in the
	 *	returned height, depending on the typeface.
	 *
	 *	It is suggested that due to the asynchronous nature of this
	 *	call, measurements be cached by classes that need to use
	 *	string bounds.
	 */
	*stringBounds { arg string, font, server;
		var msg, id;
		if( thisThread.isKindOf( Routine ), {
			if( server.isNil, { server = SwingOSC.default });
			id		= server.nextNodeID;
//			msg		= server.sendMsgSync([ '/get', '[', '/local', id,
//				'[', '/methodr' ] ++ font.asSwingArg ++ [ \getStringBounds ] ++ string.asSwingArg ++ [
//					'[', '/new', "java.awt.font.FontRenderContext", '[', '/ref', \null, ']', true, true, ']',
//				']', ']', \x, \y, \width, \height ], [ '/set', id ]);
			msg = [ '/get', '[', '/local', id, '[', '/methodr', '[', '/new', "java.awt.font.TextLayout" ] ++ string.asSwingArg ++ font.asSwingArg ++ [
				    '[', '/new', "java.awt.font.FontRenderContext", '[', '/ref', \null, ']', true, true, ']', ']', \getBounds, ']', ']',
				        /* \x, \y,*/ \width, \height ];
			msg		= server.sendMsgSync( msg, [ '/set', id ]);

//	// NOTE: getPixelBounds is java 1.6 only!
//			msg = [ '/get', '[', '/local', id, '[', '/methodr', '[', '/new', "java.awt.font.TextLayout" ] ++ string.asSwingArg ++ font.asSwingArg ++ [
//				    '[', '/new', "java.awt.font.FontRenderContext", '[', '/ref', \null, ']', true, true, ']', ']', \getPixelBounds, '[', '/ref', \null, ']', 0.0, 0.0, ']', ']',
//				        \x, \y, \width, \height ];
//			msg		= server.sendMsgSync( msg, [ '/set', id ]);
			server.sendMsg( '/free', id );
			if( msg.notNil, {
//				^Rect( msg[ 3 ], msg[ 5 ], msg[ 7 ], msg[ 9 ]);
				^Rect( 0, 0,  msg[ 3 ].ceil.asInteger + 1, msg[ 5 ].ceil.asInteger + 1 );
			});
			
			"Meta_SwingGUI:stringBounds : server timeout".warn;
		}, {
			"Meta_SwingGUI:stringBounds : should be called inside a Routine".warn;
		});
		"Using coarse approximation".postln;
		// width in Helvetica approx = string size * font size * 0.52146
		// 0.52146 is average of all 32-127 ascii characters widths
		// this is a bad hack...
		^Rect( 0, 0, string.size * font.size * 0.52146, font.size * 1.25 );
	}

	*defer { arg func; func.value }
}
