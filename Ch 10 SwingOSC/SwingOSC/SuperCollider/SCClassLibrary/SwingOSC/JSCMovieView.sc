/*
 *	JSCMovieView
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
 *	Replacement for the (Cocoa) SCMovieView class by Jan Truetzschler.
 *
 *	@author		Hanns Holger Rutz
 *	@version		0.61, 11-Aug-08
 */
JSCMovieView : JSCView{
	var <rate, <loopMode, <muted, <path, <editable;
	var fixedAspectRatio = false;
	
	/*
	loop modes
	0 Playback runs forward and backward between both endpoints.
	1 Restarts playback at beginning when end is reached.
	2 Playback stops when end is reached.		
	*/
	
	// ----------------- public instance methods -----------------

	start {
		this.setProperty( \start );		
	}
	
	stop {
		this.setProperty( \stop );		
	}
	
	path_ { arg moviePath;
		path = moviePath;
		this.setProperty( \setMovie, moviePath );	
	}
	
	muted_ { arg bool;
		muted = bool;
		this.setProperty( \setMuted, bool );		
	}
	
	playSelectionOnly_ { arg bool;
		this.setProperty( \setPlaysSelectionOnly, bool );
	}
	
	rate_ { arg ratein;
		rate = ratein;
		this.setProperty( \setRate, ratein );
	}
	
	loopMode_ { arg mode;
		loopMode = mode;
		this.setProperty( \setLoopMode, mode );
	}	
	
	gotoEnd {
		this.setProperty( \gotoEnd );
	}
	
	stepForward {
		this.setProperty( \stepForward );
	}
	
	stepBack {
		this.setProperty( \stepBack );	
	}
	
	gotoBeginning {
		this.setProperty( \gotoBeginning );
	}	
	
	currentTime_ { arg time;
		this.setProperty( \setCurrentTime, time );
	}

	currentTime {
		"JSCMovieView-currentTime is not implemented".warn;
		^0;
//		^this.getProperty(\getCurrentTime);
	}	
		
	editable_ { arg bool;
		editable = bool;
		this.setProperty( \setEditable, bool );		
	}
	
	showControllerAndAdjustSize { arg show, adjust;
		this.setProperty( \showControllerAndAdjustSize, [ show, adjust ]);
	}
	
	resizeWithMagnification { arg size;
		this.setProperty( \resizeWithMagnification, size );
	}
	
	fixedAspectRatio_ { arg bool;
		fixedAspectRatio = bool;
		this.setProperty( \fixedAspectRatio, bool );
	}
	
	copy {
		this.setProperty( \copy );
	}
	
	clear {
		this.setProperty( \clear );
	}
	
	cut {
		this.setProperty( \cut );
	}
	
	paste {
		this.setProperty( \paste );
	}

	skipFrames { arg numFrames;
		server.sendMsg( '/method', this.id, \skip, numFrames );
	}
	
	frame_ { arg frameIdx;
		server.sendMsg( '/method', this.id, \setCurrentFrame, frameIdx );
	}

	// ----------------- private instance methods -----------------

	prInitView {
		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.MovieView", ']' ]		]);
	}

	prClose { arg preMsg, postMsg;
		^super.prClose( preMsg ++ [[ '/method', this.id, \dispose ]], postMsg );
	}

	prSendProperty { arg key, value;
		key	= key.asSymbol;

		switch( key,
			\start, {
				server.sendMsg( '/method', this.id, \start );
				^this;
			},
			\stop, {
				server.sendMsg( '/method', this.id, \stop );
				^this;
			},
			\setCurrentTime, {
				server.sendMsg( '/method', this.id, \setCurrentTime, value );
				^this;
			},
			\setMovie, {
				key = \movie;
			},
			\setMuted, {
				key = \muted;
			},
			\setRate, {
				key = \rate;
			},
			\setLoopMode, {
				key 		= \loopMode;
				value	= value == 1;
			},
			\gotoEnd, {
				server.sendMsg( '/method', this.id, \goToEnd );
				^this;
			},
			\gotoBeginning, {
				server.sendMsg( '/method', this.id, \goToBeginning );
				^this;
			},
			\stepForward, {
				server.sendMsg( '/method', this.id, \stepForward );
				^this;
			},
			\stepBack, {
				server.sendMsg( '/method', this.id, \stepBack );
				^this;
			},
			\resizeWithMagnification, {
				server.sendMsg( '/method', this.id, \setToPreferredSize, value );
				^this;
			},
			\showControllerAndAdjustSize, {
				server.sendMsg( '/method', this.id, \setControlPanelVisible, value.first );
				// ???
				^this;
			}
		);
		^super.prSendProperty( key, value );
	}
}