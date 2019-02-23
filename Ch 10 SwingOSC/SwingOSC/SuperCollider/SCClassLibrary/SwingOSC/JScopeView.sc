/*
 *	JScopeView
 *	(SwingOSC classes for SuperCollider)
 *
 *	Copyright (c) 2005-2010 Hanns Holger Rutz, Marije Baalman. All rights reserved.
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
 *	Replacement for the (Cocoa) SCScope class.
 *
 *	@author		Hanns Holger Rutz, Marije Baalman
 *	@version		0.61, 11-Aug-08
 */
JScopeOut : UGen {
	// ----------------- quasi-constructors -----------------

	*ar { arg inputArray, bufnum = 0, run=1;
		^RecordBuf.ar( inputArray, bufnum, run: run );
	}
	
	*kr { arg inputArray, bufnum = 0, run=1;
		^RecordBuf.kr( inputArray, bufnum, run: run );
	}
}

JSCScope : JSCView {

	var audioServer;

	// ----------------- public instance methods -----------------

	bufnum {
		^this.getProperty( \bufnum );
	}
	
	bufnum_ { arg num;
		this.setProperty( \bufnum, num );
	}
	
	x {
		^this.getProperty( \x );
	}
	
	x_ { arg val;
		this.setProperty( \x, val );
	}
	
	y {
		^this.getProperty( \y );
	}
	
	y_ { arg val;
		this.setProperty( \y, val );
	}
	
	xZoom {
		^this.getProperty( \xZoom );
	}
	
	xZoom_ { arg val;
		this.setProperty( \xZoom, val );
	}
	
	yZoom {
		^this.getProperty( \yZoom );
	}
	
	yZoom_ { arg val;
		this.setProperty( \yZoom, val );
	}

	gridColor {
		^this.getProperty( \gridColor );
	}
	
	gridColor_ { arg color;
		this.setProperty( \gridColor, color );
	}

	waveColors {
		^this.getProperty( \waveColors );
	}
	
	waveColors_ { arg arrayOfColors;
		this.setProperty( \waveColors, arrayOfColors );
	}
	
	style_ { arg val;
		this.setProperty( \style, val );
		// 0 = vertically spaced
		// 1 = overlapped
		// 2 = x/y
	}
	
	// ----------------- private instance methods -----------------

	properties {
//		^super.properties ++ #[\bufnum, \x, \y, \xZoom, \yZoom, \gridColor, \waveColors, \style, \antiAliasing ]
		^super.properties ++ #[\bufnum, \x, \y, \xZoom, \yZoom, \gridColor, \waveColors, \style ]
	}

//	// JJJ begin
//	antiAliasing {
//		^this.getProperty( \antiAliasing );
//	}
//	antiAliasing_ { arg onOff;
//		this.setProperty( \antiAliasing, onOff );
//	}
//	// JJJ end

	prInitView {
		var addr;
		
		audioServer	= Server.default;
		addr 		= audioServer.addr;
	
		properties.put( \bufnum, 0 );
		properties.put( \x, 0.0 );
		properties.put( \y, 0.0 );
		properties.put( \xZoom, 1.0 );
		properties.put( \yZoom, 1.0 );
		properties.put( \style, 0 );
//		properties.put( \antiAliasing, true );
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.ScopeView", ']' ],
			[ '/method', this.id, \setServer, addr.hostname, addr.port, audioServer.options.protocol ],
			[ '/method', this.id, \startListening ]
		]);
	}

	prClose { arg preMsg, postMsg;
		^super.prClose( preMsg ++ [[ '/method', this.id, \stopListening ]], postMsg );
	}

	prSendProperty { arg key, value;
//		var bndl, bufNum, numFrames, numChannels, sampleRate, resp, cond, timeOut;

		key	= key.asSymbol;

		switch( key,
			\bufnum, {
				key		= \bufNum;	// let the java class handle the /b_query stuff
//				cond		= Condition.new;
//				bufNum	= value.asInteger;
//				resp		= OSCpatResponder( audioServer.addr, [ '/b_info', bufnum ], { arg time, resp, msg;
//					// [ "/b_info", <bufNum>, <numFrames>, <numChannels>, <sampleRate> ]
//						timeOut.stop;
//						resp.remove;
//						numFrames		= msg[ 2 ].asInteger;
//						numChannels	= msg[ 3 ].asInteger;
//						sampleRate	= msg[ 4 ].asFloat;
//						cond.test		= true;
//						cond.signal;
//				});
//				timeOut	= Routine({
//					4.0.wait;
//					resp.remove;
//					cond.unhang;
//				}).play;
//				Routine({
//					cond.wait;
//					if( cond.test, {
//						server.sendMsg( "/method", this.id, \setBuffer, bufNum, numFrames, numChannels, sampleRate );
//					}, {
//						"JScopeView : timeout while changing buffer".error;
//					});
//				}).play;
//				resp.add;
//				audioServer.sendMsg( "/b_query", bufNum );
//				^nil;
			},
			\waveColors, {
				key = \objWaveColors;
			}
		);
		^super.prSendProperty( key, value );
	}
}
