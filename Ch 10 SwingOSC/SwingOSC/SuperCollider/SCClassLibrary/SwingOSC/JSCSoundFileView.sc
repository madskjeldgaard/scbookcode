/*
 *	JSCSoundFileView
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
 *	- 30-Jan-07	setSelectionSpan removed, use setSelection now
 *				(was added to cocoa view)
 *	- 29-Jul-07	added cache support
 */

/**
 *	Replacement for / enhancement of the (Cocoa) SCSoundFileView class by Jan Truetzschler.
 *
 *	@author		Hanns Holger Rutz
 *	@version		0.64, 28-Jan-10
 */
JSCSoundFileView : JSCView { // in SwingOSC not a subclass of JSCScope
	classvar cacheServers;	// IdentitySet whose elements are SwingOSC instances

	classvar <cacheCapacity		= 50;
	classvar <cacheFolder;
	classvar <cacheActive		= false;

	var <soundfile;
	var <>metaAction, <elasticMode, <drawsWaveForm = true, <readProgress;
	var <startFrame;

	var <viewFrames = 0, <>block=64;
	
	var viewStart	= 0;
	var selections;
	
	var viewResp, asyncResp, asyncID;
	var <numFrames = 0, numChannels = 1, <sampleRate = 44100;
	
	// ----------------- public class methods -----------------
	
	*cacheCapacity_ { arg megaBytes;
		cacheCapacity = megaBytes;
		cacheServers.do(_.sendMsg( '/set', \cache, \capacity, cacheCapacity ));
	}
	
	*cacheFolder_ { arg path;
		cacheFolder = path;
		cacheServers.do(_.sendMsg( '/set', \cache, \folder, '[', '/new', "java.io.File", cacheFolder, ']' ));
	}
	
	*cacheActive_ { arg bool;
		cacheActive = bool;
		cacheServers.do(_.sendMsg( '/set', \cache, \active, cacheCapacity ));
	}
	
	// ----------------- public instance methods -----------------

	soundfile_ { arg snd;
		soundfile = snd;
		if( soundfile.isOpen ) { ^this };
		if( soundfile.openRead.not ) { ^this };
		soundfile.close;
	}

	read { arg startframe = 0, frames, block = 64, closeFile = true;
		if( soundfile.isOpen.not ) {
			if( soundfile.openRead.not ) {
				^this;
			};
		};

		startframe	= startframe.clip( 0, soundfile.numFrames );
		frames		= frames ?? { soundfile.numFrames - startframe };
		if( frames == -1, { frames = soundfile.numFrames - startframe });
		frames		= frames.clip( 0, soundfile.numFrames - startframe );

//		if( closeFile, { soundfile.close });	// _before_ calling readFile
		this.readFile( soundfile, startframe, frames, block );
	}

	readWithTask { arg startframe = 0, frames, block = 64, doneAction, showProgress = true;
		this.readFileWithTask( soundfile, startframe, frames, block, doneAction, showProgress );
	}
	
	makeProgressWindow {
		this.addDependant( JSoundFileViewProgressWindow( soundfile.path.split.last ));
	}
	
	// needs an open soundfile;
	readFileWithTask { arg soundfile, startframe = 0, frames, block = 64, doneAction, showProgress = true;

		startframe	= startframe.clip( 0, soundfile.numFrames );
		frames		= frames ?? { soundfile.numFrames - startframe };
		if( frames == -1, { frames = soundfile.numFrames - startframe });
		frames		= frames.clip( 0, soundfile.numFrames - startframe );
		soundfile.close;

		this.prSetSpecs( soundfile, startframe, frames, soundfile.numChannels, soundfile.sampleRate, block );

		if( showProgress, {		
			this.makeProgressWindow;
		});
		{
			this.prCallRead( soundfile, startFrame, frames, false );
			doneAction.value( this );
		}.fork( SwingOSC.clock );
	}

//	readFile { arg asoundfile, startframe = 0, frames = 0, block = 0, closefile = true; }
	readFile { arg asoundfile, startframe = 0, frames, block = 64, closefile = true;

// be carefull because the arg is 'asoundfile' not 'soundfile' like in readFileWithTask !!!
		startframe	= startframe.clip( 0, asoundfile.numFrames );
		frames		= frames ?? { asoundfile.numFrames - startframe };
		if( frames == -1, { frames = asoundfile.numFrames - startframe });
		frames		= frames.clip( 0, asoundfile.numFrames - startframe );
		if( closefile, { asoundfile.close });
		
		this.prSetSpecs( asoundfile, startframe, frames, asoundfile.numChannels, asoundfile.sampleRate, block );

		if( thisThread.isKindOf( Routine )) {
			^this.prCallRead( asoundfile, startframe, frames, false );
		} {
			"JSCSoundFileView.read : asynchronous call outside routine".warn;
			{
				this.prCallRead( asoundfile, startframe, frames, false );
			}.fork( SwingOSC.clock );
		};
	}
	
//	mouseEndTrack{|x,y|
////		mouseUpAction.value(this, x,y)
//	}
	
	// ??? what is this for actually ???
	doMetaAction {
		metaAction.value( this );
	}
	
	currentSelection_ { arg index;
		this.setProperty( \selectionIndex, index );
	}
	
	currentSelection {
		^this.getProperty( \selectionIndex );	
	}
	
	setSelectionStart { arg index, frame;
		var sel;
		sel = selections[ index ];
		if( sel.notNil ) {
			sel.put( \start, frame.max( 0 ));
			this.prSendSelection( index );
		} {
			this.prMethodError( thisMethod, "Illegal selection index "++index );
		};
	}

	setSelectionSize { arg index, frame;
		var sel;
		sel = selections[ index ];
		if( sel.notNil ) {
			sel.put( \size, frame.max( 0 ));
			this.prSendSelection( index );
		} {
			this.prMethodError( thisMethod, "Illegal selection index "++index );
		};
	}
	
//	// extra function in SwingOSC
//	setSelectionSpan { arg index, startFrame, numFrames;
//		var sel;
//		sel = selections[ index ];
//		if( sel.notNil ) {
//			sel.put( \start, startFrame.max( 0 ));
//			sel.put( \size, numFrames.max( 0 ));
//			this.prSendSelection( index );
//		} {
//			this.prMethodError( thisMethod, "Illegal selection index "++index );
//		};
//	}
	
	setEditableSelectionStart { arg index, bool;
		server.sendMsg( '/method', this.id, \setSelectionStartEditable, index, bool );
	}
	
	setEditableSelectionSize { arg index, bool;
		server.sendMsg( '/method', this.id, \setSelectionSizeEditable, index, bool );
	}
		
	setSelectionColor { arg index, color;
//		this.setProperty( \selectionColor, [ index, color ]);
		server.listSendMsg([ '/method', this.id, \setSelectionColor, index ] ++ color.asSwingArg );
	}
	
	selections {
		^selections.collect({ arg sel; [ sel[ \start ] ?? 0, sel[ \size ] ?? 0 ]});
	}
	
	selectionStart { arg index;
		var sel;
		sel = selections[ index ];
		if( sel.notNil ) {
			^sel[ \start ] ?? 0;
		} {
			this.prMethodError( thisMethod, "Illegal selection index "++index );
			^nil;
		};
	}

	selectionSize { arg index;
		var sel;
		sel = selections[ index ];
		if( sel.notNil ) {
			^sel[ \size ] ?? 0;
		} {
			this.prMethodError( thisMethod, "Illegal selection index "++index );
			^nil;
		};
	}

	/**
	 *	Queries a particular selection
	 *
	 *	@param	index	(Integer) the index of the selection to query
	 *	@return	(Array) the selection as [ <startFrame>, <numFrames> ]
	 */
	selection { arg index;
		var sel;
		sel = selections[ index ];
		if( sel.notNil ) {
			^[ sel[ \start ] ?? 0, sel[ \size ] ?? 0 ];
		} {
			this.prMethodError( thisMethod, "Illegal selection index "++index );
			^nil;
		};
	}
	
	setSelection { arg index, selection;
		var sel;
		sel = selections[ index ];
		if( sel.notNil ) {
			sel.put( \start, selection[ 0 ].max( 0 ));
			sel.put( \size, selection[ 1 ].max( 0 ));
			this.prSendSelection( index );
		} {
			this.prMethodError( thisMethod, "Illegal selection index "++index );
		};
	}
	
	selectionStartTime { arg index;
		var frame;
		frame = this.selectionStart( index );
		if( frame.notNil ) {
			^frame / (sampleRate ?? 44100);
		} {
			^nil;
		};
	}

	selectionDuration { arg index;
		var frames;
		frames = this.selectionSize( index );
		if( frames.notNil ) {
			^frames / (sampleRate ?? 44100);
		} {
			^nil;
		};
	}
	
	readSelection {
		this.read(
			this.selectionStart(this.currentSelection),
			this.selectionSize(this.currentSelection)
		);
	}
	
	readSelectionWithTask {
		this.readWithTask(
			this.selectionStart(this.currentSelection),
			this.selectionSize(this.currentSelection)
		);
	}
		
	gridOn_ { arg boolean;
		this.setProperty( \gridPainted, boolean );
	}
	
	gridResolution_ { arg resolution;
		this.setProperty( \gridResolution, resolution );
	}
	
	gridOn { ^this.getProperty( \gridPainted )}
	gridResolution { ^this.getProperty( \gridResolution )}
	gridColor { ^this.getProperty( \gridColor ) }	
	timeCursorEditable { ^this.getProperty( \timeCursorEditable )}
	timeCursorOn { ^this.getProperty( \timeCursorPainted )}
	timeCursorPosition { ^this.getProperty( \timeCursorPosition )}
	timeCursorColor { ^this.getProperty( \timeCursorColor )}

	dataFrames {
		^numFrames.div( block );
	}

	dataNumSamples {
		^(numFrames * numChannels).div( block );
	}
	
	data {
		var arr;
		arr = Array.newClear( this.dataNumSamples );
		^this.getProperty( \getViewData, arr );
	}
	
	data_ { arg arr;
		this.setData( arr, 64, 0, numChannels, sampleRate );
	}
	
	setData { arg arr, block = 64, startframe = 0, channels = 1, samplerate = 44100;
		var tmpPath, frames, tmpF, success = false;
		
//this.prMethodError( thisMethod, "Not yet implemented" );
//		this.block = block;
//		this.setProperty( \setViewData, [ arr, block, startframe, channels, samplerate ]);

		frames		= arr.size.div( channels );
		startframe	= startframe.clip( 0, frames );
		frames		= frames - startframe;

		// defaults to AIFF 32bit float mono
		tmpF = SoundFile.new.numChannels_( channels ).sampleRate_( samplerate );
		tmpPath = PathName.tmp ++ "data" ++ UniqueID.next.asString ++ ".aif";
		tmpF.openWrite( tmpPath );
		tmpF.path = tmpPath;	// sucky disko
		protect {		
			if( startframe > 0, { arr = arr.copyRange( startframe * channels, (startframe + frames) * channels - 1 )});
			tmpF.writeData( if( arr.isKindOf( FloatArray ), arr, { FloatArray.newFrom( arr )}));
			success = true;
		} {
			try { tmpF.close };
		};

		if( success, {
			this.prSetSpecs( nil, startframe, frames, channels, samplerate, block );
	
			if( thisThread.isKindOf( Routine )) {
				^this.prCallRead( tmpF, startframe, frames, true );
			} {
				"JSCSoundFileView.setData : asynchronous call outside routine".warn;
				{
					this.prCallRead( tmpF, startframe, frames, true );
				}.fork( SwingOSC.clock );
			};
		});
	}
		
	elasticMode_{arg mode;
		if( mode != 1, { "JSCSoundFileView.elasticMode_( 0 ) not yet supported!".warn });
		elasticMode = mode;
//		this.setProperty( \elasticResizeMode, mode );
	}	
	
	drawsWaveForm_ { arg bool;
		drawsWaveForm = bool;
		this.setProperty( \wavePainted, bool );
	}
	
	timeCursorPosition_ { arg frame;
		this.setProperty( \timeCursorPosition, frame );
	}
	
	timeCursorEditable_ { arg bool;
		this.setProperty( \timeCursorEditable, bool );
	}
	
	timeCursorOn_ { arg bool;
		this.setProperty( \timeCursorPainted, bool );
	}
	
	timeCursorColor_ { arg color;
		this.setProperty( \timeCursorColor, color );
	}

	zoom { arg factor;	// zoom factor n or 1/n.
		viewFrames	= (viewFrames * factor + 0.5).asInteger.clip( 0, numFrames );
		viewStart		= viewStart.min( numFrames - viewFrames );
		this.prUpdateViewSpan;
	}

	zoomToFrac { arg frac; // 0..1
		viewFrames 	= (numFrames * frac.clip( 0, 1 ) + 0.5).asInteger;
		viewStart		= viewStart.min( numFrames - viewFrames );
		this.prUpdateViewSpan;
	}

	zoomAllOut {
		viewStart		= 0;
		viewFrames	= numFrames;
		this.prUpdateViewSpan;
	}
	
	zoomSelection { arg index;	// selection index
		var sel;
		sel = selections[ index ];
		if( this.selectionSize( index ) > 0 ) {
			this.x_( this.selectionStart( index ));
			this.xZoom = this.selectionSize( index ) / (this.prBoundsReadOnly.width - 2) / block;
		};
	}
	
	scrollTo { arg position;		// absolute. from 0 to 1
		viewStart 	= ((numFrames - viewFrames) * position.clip( 0, 1 ) + 0.5).asInteger;
		this.prUpdateViewSpan;
	}
	
	scroll { arg amount;	// +/- range in viewFrames
		viewStart 	= (viewStart + (amount * viewFrames)).clip( 0, numFrames - viewFrames );
		this.prUpdateViewSpan;
	}
	
	scrollToStart {
		this.x_( 0 );
	}
	
	scrollToEnd {
		viewStart = numFrames  - viewFrames;
		this.prUpdateViewSpan;
	}
	
	selectAll { arg index;	// selection index
		this.setSelection( index, [ 0, numFrames ]);
	}
	
	selectNone { arg index;	 // selection index
		 // sends x to 0 when selection is not full width visible? ( thus update scrollPos )		this.setSelectionSize( index, 0 );
	}
	
	scrollPos {
		^viewStart / (numFrames - viewFrames).max( 1 );
	}
	
	gridOffset_{ arg offset;
		this.setProperty( \gridOffset, offset );
	}

	// (these are taken from JSCScope since we don't subclass that)
	x {
		^viewStart;
	}

	x_ { arg val;
		viewStart 	= val.min( numFrames - viewFrames );
		viewFrames	= viewFrames.min( numFrames - viewStart );
		this.prUpdateViewSpan;
	}
	
	viewFrames_ { arg val;
		viewFrames = val.min( numFrames );
		viewStart  = viewStart.min( numFrames - viewFrames );
		this.prUpdateViewSpan;
	}

//	y {
//		^this.getProperty(\y)
//	}
//	y_ { arg val;
//		this.setProperty(\y, val);
//	}	

	xZoom {
		^( viewFrames / ((this.prBoundsReadOnly.width - 2) * block) );
	}

	xZoom_ { arg val;
		viewFrames 	= (block * val * (this.prBoundsReadOnly.width - 2)).clip( 0, numFrames );
		viewStart 	= viewStart.min( numFrames - viewFrames );
		this.prUpdateViewSpan;
	}	

	yZoom {
		^this.getProperty( \yZoom );
	}

	yZoom_ { arg val;
		this.setProperty( \yZoom, val );
	}	

	gridColor_ { arg color;
		this.setProperty( \gridColor, color );
	}	

	waveColors {
		^this.getProperty( \objWaveColors );
	}
	
	waveColors_ { arg arrayOfColors;
		this.setProperty( \objWaveColors, arrayOfColors );
	}
	
	style_ { arg val;
if( val == 2, { "JSCSoundFileView.style_ : lissajou not yet implemented".error; ^this; }); // XXX
		this.setProperty( \style, val );
		// 0 = vertically spaced
		// 1 = overlapped
		// 2 = x/y
	}
	
//	properties {
//		^super.properties ++ #[ \yZoom, \gridColor, \waveColors, \style ]
//	}

	// ----------------- private instance methods -----------------

	init { arg argParent, argBounds;
		super.init( argParent, argBounds );
		soundfile = SoundFile.new;
	}
	
	prInitView {
		cacheServers = cacheServers ?? {
			UI.registerForShutdown({ cacheServers.do(_.sendMsg( '/free', \cache ))});
			IdentitySet.new;
		};
		if( cacheServers.includes( server ).not, {
			cacheServers.add( server );
			server.sendMsg( '/set', '[', '/local', \cache, '[', '/new', "de.sciss.io.CacheManager", ']', ']', \capacity, cacheCapacity, \folder, '[', '/new', "java.io.File", cacheFolder, ']', \active, cacheActive );
		});
//		properties.put( \bufnum, 0 );
//		properties.put( \x, 0.0 );
//		properties.put( \y, 0.0 );
//		properties.put( \xZoom, 1.0 );
		properties.put( \yZoom, 1.0 );
		properties.put( \style, 0 );
		properties.put( \selectionIndex, 0 );
		jinsets		= Insets( 3, 3, 3, 3 );
		selections	= Array.fill( 64, { IdentityDictionary.new });
		
		viewResp		= OSCpathResponder( server.addr, [ '/soundfile', this.id ], { arg time, resp, msg;
			var state, sel, idx, frame, size;
		
			state = msg[2].asSymbol;
			case
			{ state === \cursor }
			{
				frame = msg[3];
				properties.put( \timeCursorPosition, frame );
				{ action.value( this, \cursor, frame )}.defer;
			}
			{ state === \selection }
			{
				idx = msg[3];
				sel = selections[ idx ];
				if( sel.notNil ) {
					frame = msg[4];
					sel.put( \start, frame );
					size = msg[5] - frame;
					sel.put( \size, size );
					{ action.value( this, \selection, idx, frame, size )}.defer;
				} {
					"Yukk! Selection not found".error;
				};
			};
		});
		viewResp.add;

		^this.prSCViewNew([
			[ '/local', this.id, '[', '/new', "de.sciss.swingosc.SoundFileView", ']',
				"vw" ++ this.id,
				'[', '/new', "de.sciss.swingosc.SoundFileViewResponder", this.id, ']' ],
			[ '/set', this.id, \cacheManager, '[', '/ref', \cache, ']' ]
			]);
	}

	prClose { arg preMsg, postMsg;
		viewResp.remove;
		this.prFreeAsyncResp;
		^super.prClose( preMsg ++
			[[ '/method', "vw" ++ this.id, \remove ],
			 [ '/free', "vw" ++ this.id ],
			 [ '/method', this.id, \dispose ]], postMsg );
	}

	prSetSpecs { arg aSoundFile, fileStartFrame, fileNumFrames, fileNumChannels, sr, blockSize;
//("prSetSpecs( "++aSoundFile++", " ++ fileStartFrame++", "++fileNumFrames++", "++fileNumChannels++", "++sr++", "++blockSize++" )").postln;
		soundfile		= aSoundFile;
		startFrame	= fileStartFrame;
		numFrames		= fileNumFrames;
		numChannels	= fileNumChannels;
		sampleRate	= sr;
		block		= blockSize;
		viewStart		= 0;
		viewFrames	= numFrames;
	}

//	// JJJ begin
//	prSendProperty { arg key, value;
//		key	= key.asSymbol;
//
//		switch( key,
//			\readSndFile, {
//				if( thisThread.isKindOf( Routine ), {
//					^this.prCallRead( *value );
//				}, {
//					"JSCSoundFileView.read : asynchronous call outside routine".warn;
//					fork { this.prCallRead( *value )};
//				});
//				^this;
//			}
//		);
//		^super.prSendProperty( key, value );
//	}
//	// JJJ end

/* private methods*/

// JJJ begin
//	updateScroll {
//		scrollPos = this.x / (soundfile.numFrames - viewFrames)
//	}
// JJJ end
	
// JJJ begin
//	updateData {
//		scrollPos = 0;
//		dataFrames = this.dataNumSamples/this.soundfile.numChannels;
//		zoomOne = dataFrames / (this.bounds.width-2);
//		viewFrames = dataFrames * block * (this.xZoom / zoomOne);
//	}
// JJJ end

	prCallRead { arg sf, startFrame, numFrames, deleteWhenDisposed = false;
		var condition, bndl, cancel, timeout;

		condition = Condition.new;
		this.prFreeAsyncResp;

		asyncResp	= OSCpathResponder( server.addr, [ '/async', this.id ], { arg time, resp, msg;
			var what;

			if( cancel.notNil, { cancel.stop; });
			what = msg[ 2 ];
			case
			{ what === \finished }
			{
				this.prFreeAsyncResp;
//"JSCSoundFileView.read : done".inform;
				condition.test	= true;
				condition.signal;
				this.changed( \progressFinished );
			}
			{ what === \failed }
			{
				this.prFreeAsyncResp;
				"JSCSoundFileView.read : failed".error;
				condition.test	= true;
				condition.signal;
				this.changed( \progressFinished );
			}
			{ what === \cancelled }
			{
				this.prFreeAsyncResp;
				condition.test	= true;
				condition.signal;
				this.changed( \progressFinished );
			}
			{ what === \update }
			{
				readProgress = msg[ 4 ];
				this.changed( \progress );
			};
		});
		asyncResp.add;
		
		asyncID = "as" ++ server.nextNodeID;
		bndl = [[ '/method', this.id, \cancelAsyncRead ],
		        [ '/local', asyncID, '[', '/new', "de.sciss.swingosc.AsyncResponder", this.id, \readProgress, ']' ],
		        [ '/method', this.id, \readSndFile, sf.path, startFrame, numFrames, deleteWhenDisposed ]];

		condition.test = false;
		cancel = fork {
			8.0.wait;
			asyncResp.remove;
			"JSCSoundFileView.read : timeout".error;
			condition.test	= true;
			condition.signal;
		};
		readProgress = 0;
		server.listSendBundle( nil, bndl );
		this.changed( \progress );
		condition.wait;
	}
	
	prFreeAsyncResp {
		if( asyncResp.notNil, {
			asyncResp.remove;
			asyncResp = nil;
			server.sendBundle( nil, [ '/method', asyncID, \remove ], [ '/free', asyncID ]);
			asyncID = nil;
		});
	}	

	prUpdateViewSpan {
		server.sendMsg( '/method', this.id, \setViewSpan, viewStart, viewStart + viewFrames );
	}

	prMethodError { arg methodName, message;
		(this.class.name ++ "." ++ methodName ++ " failed : " ++ message).error;
	}
	
	prSendSelection { arg index;
		var sel, start, stop;
		sel 		= selections[ index ];
		start	= sel[ \start ] ?? 0;
		stop		= start + (sel[ \size ] ?? 0);
		server.sendMsg( '/method', this.id, \setSelectionSpan, index, start, stop );
	}
}

// note: this should be killed in favour of
// making SoundFileViewProgressWindow use the GUI class
JSoundFileViewProgressWindow{
	var win, slider;
	
	var popUpRout;
	
	// ----------------- constructor -----------------

	*new { arg name;
		^super.new.makeWindow( name );
	}
	
	// ----------------- public instance methods -----------------

	makeWindow { arg name;
		win = JSCWindow.new( "Reading: " ++ name,
				 Rect( 100, 100, 300, 40 ), false );
		win.view.decorator = FlowLayout( win.view.bounds );
		slider = JSCRangeSlider( win, Rect( 4, 4, 290, 10 ))
//			.editable_( false )
			.canFocus_( false );
		popUpRout = { 1.5.wait; win.front }.fork( SwingOSC.clock );
	}
	
	// ----------------- quasi-interface methods -----------------

	update { arg changed, changer;
		
		if( changer === \progress, {
			{ slider.lo_( 0 ).hi_( changed.readProgress )}.defer;
			^this;
		}, { if( changer === \progressFinished, {
			popUpRout.stop;
			{ win.close;
			  changed.removeDependant( this );
			}.defer;
		})});
	}
}