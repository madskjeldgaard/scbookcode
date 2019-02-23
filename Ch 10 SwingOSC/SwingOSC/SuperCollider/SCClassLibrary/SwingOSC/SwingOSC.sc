/*
 *	SwingOSC
 *	(SwingOSC classes for SuperCollider)
 *
 *  Copyright (c) 2005-2010 Hanns Holger Rutz. All rights reserved.
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
 *	Changelog
 *		06-Mar-06		added fixes by AdC
 *		11-Jun-06		removed slowOSC stuff (fixed in SC)
 *		01-Oct-06		added SwingOptions and TCP mode
 *		18-Jan-08		added deathBounces to aliveThread
 *		28-Jan-08		bootServerApp adds -h option again
 *		27-Jun-10		does not subclass Model anymore
 */

/**
 *	The client side representation of a SwingOSC server
 *	and its options.
 *
 *	@author		Hanns Holger Rutz
 *	@version		0.65, 11-Jul-10
 */
SwingOptions
{
	classvar	<>default;

	var <>protocol 	= \tcp;
	var <>loopBack	= true;
	var <>initGUI		= true;
	var <>oscBufSize	= 65536;
	var <>javaOptions;	// option string that is passed to the java VM

	*initClass {
		default = this.new.javaOptions_(
			switch( thisProcess.platform.name,
			\osx, {
				"-Dapple.laf.useScreenMenuBar=true -Dapple.awt.graphics.UseQuartz=true -Xdock:icon=application.icns -Xdock:name=SwingOSC"; // "-Dswing.defaultlaf=apple.laf.AquaLookAndFeel"
			},
			\linux, {
				"-Dswing.defaultlaf=com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
			},
			\windows, {
				"-Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
			})
		);
	}
	
	// ----------------- constructor -----------------

	*new {
		default.notNil.if({
			^default.copy;
		}, {
			^super.new;
		});
	}

	// ----------------- public instance methods -----------------

	asOptionsString { arg port = 57111;
		var o;
		// XXX session-password
		o = if( protocol === \tcp, "-t ", "-u ");
		o = o ++ port;
		
		if( loopBack, { 
			o = o ++ " -L";
		});
		if( initGUI, { 
			o = o ++ " -i";
		});
		if( oscBufSize != 65536, {
			o = o ++ " -b " ++ oscBufSize;
		});
		^o;
	}
}

SwingOSC // : Model
{
	classvar <>local, <>default, <>named, <>set, <>java, <>program, <>clock;
	var <>gaga;

	// WARNING: this field might be removed in a future version
	var <>useDoubles = false;

	// note this is the SC class lib version, not necessarily the
	// server version (reflected by the instance variable serverVersion)
	classvar <version = 0.65;

	var <name, <addr, <clientID = 0;
	var <isLocal;
	var <serverRunning = false, <serverBooting = false, <serverVersion;
	var <dumpMode = 0, <dumpModeR = 0;
	var <tempRunning = false;
	var <numTraceLines = 3;

	var <>options; // , <>latency = 0.2, <notified=true;
	var <nodeAllocator;

	var <screenWidth, <screenHeight;
	
	var booting = false, aliveThread, statusWatcher;
		// number of times the server is allowed to fail to respond to /status
		// before the client assumes the server died
		// 8 is sufficient for most systems but Windows needs more
	var <>deathBounces = 8;

	var helloResp;
	var application = false;

	*initClass {
		Class.initClassTree( NetAddr );
		Class.initClassTree( SwingOptions );
		Class.initClassTree( OSCresponder );
		Class.initClassTree( AppClock );
		Class.initClassTree( JFont );	// because we read JFont.default in initTree !
		named		= IdentityDictionary.new;
		set			= Set.new;
		clock		= AppClock;
		default		= local = SwingOSC( \localhost, NetAddr( "127.0.0.1", 57111 ), SwingOptions.default );
		java			= "java";
		program 		= "SwingOSC.jar"; // -Dapple.awt.brushMetalLook=true if you like
		CmdPeriod.add( this );
	}

	// ----------------- constructor -----------------

	/*
	 *	@param	name			a unique name for the server
	 *	@param	addr			a NetAddr object specifying the SwingOSC
	 *						server address
	 *	@param	options		currently nil
	 *	@param	clientID		integer between 0 and 31
	 */
	*new { arg name, addr, options, clientID = 0;
		^super.new.init( name, addr, options, clientID );
	}
	
	// ----------------- public class methods -----------------

	*quitAll {
		set.do({ arg server; if( server.isLocal, { server.quit })});
	}

	*retrieveScreenBounds {
		this.default.retrieveScreenBounds;
	}
		
//	*resumeThreads {
//		"SwingOSC.resumeThreads...".postln;
//		set.do({ arg server;
//			server.stopAliveThread;
//			server.startAliveThread( 0.7 );
//		});
//	}
	
	// ----------------- public instance methods -----------------
	
	connect {
		if( options.protocol === \tcp, {
			addr.connect({ this.serverRunning = false });
		});
	}
	
	disconnect {
		if( options.protocol === \tcp, {
			addr.disconnect;
		});
	}
	
	initTree { arg onComplete, onFailure;
		var result;
		this.newAllocators;
		application = false;
		try {
			this.connect;
			{
				JFont.prMakeFontsAvailable( this );
				this.listSendMsg([ '/local', \font ] ++ JFont.default.asSwingArg );
				this.prRetrieveScreenBounds;
				this.dumpOSC( dumpMode, dumpModeR );
//				this.sendMsg( '/query', \status, '[', '/field', 'de.sciss.swingosc.SwingOSC', \VERSION, ']' );
				result = this.sendMsgSync([ '/query', \version, '[', '/field', 'de.sciss.swingosc.SwingOSC', \VERSION, ']' ],
					[ '/info', \version ]);
				if( result.notNil, {
					serverVersion = result[2];
					if( serverVersion != version, {
						("SwingOSC version mismatch: client is v" ++ version ++ ", server is v" ++ serverVersion ++ "!" ).warn;
					});
					onComplete.value;
				}, onFailure );
			}.fork( clock );
		}
		{ arg error;	// throws when TCP server not available
			onFailure.value;
		}
	}
	
	addClasses { arg ... urls;
		this.sendMsg( '/classes', \add, *urls );
	}
	
	removeClasses { arg ... urls;
		this.sendMsg( '/classes', \remove, *urls );
	}
	
	updateClasses { arg ... urls;
		this.sendMsg( '/classes', \update, *urls );
	}
	
	numTraceLines_ { arg lines;
		lines = lines.asInteger.max( 0 );
		this.sendMsg( '/set', \swing, \numTraceLines, lines );
		numTraceLines = lines;
	}
	
	sync { arg condition, bundles, latency, timeout = 4.0;
		var resp, cancel, id, queryMsg, result = true;

		if( condition.isNil, { condition = Condition.new };);

		id				= UniqueID.next;
		condition.test	= false;
		queryMsg			= [ '/query', id, 0 ];

		resp				= OSCpathResponder( addr, [ '/info', id ], { arg time, resp, msg;
			if( cancel.notNil, { cancel.stop; });
			resp.remove;
			condition.test = true;
			condition.signal;
		});
		resp.add;
		
		if( timeout > 0.0, {
			cancel = {
				timeout.wait;
				resp.remove;
				result			= false;
				condition.test	= true;
				condition.signal;
			
			}.fork( clock );
		});

		if( bundles.isNil, {
			// in SwingOSC, all messages are processed stricly after another, so a simple query
			// can be used as sync!
			addr.sendBundle( latency, queryMsg );
		}, {
			addr.sendBundle( latency, *(bundles ++ [ queryMsg ]));
		});

		condition.wait;
		^result;
	}
	
	retrieveScreenBounds {
		{
			this.prRetrieveScreenBounds;
		}.fork( clock );
	}
	
	newAllocators {
		nodeAllocator	= NodeIDAllocator( clientID );
	}

	nextNodeID {
		^nodeAllocator.alloc;
	}
	
	dispose {
		if( helloResp.notNil, {
			helloResp.remove;
			helloResp = nil;
		});
		addr = nil;

//		// slowOSC
//		dispatcher.stop;
//		dispatcher = nil;
	}
	
	sendMsg { arg ... msg;
//		if( slowOSC, {
//			dispPending.add( msg );
//			dispCond.test = true;
//			dispCond.signal;
//		}, {
			addr.sendMsg( *msg );
//		});
	}

	sendBundle { arg time ... msgs;
//		if( slowOSC, {
//			dispPending.addAll( msgs );	// no timetags for now
//			dispCond.test = true;
//			dispCond.signal;
//		}, {
			addr.sendBundle( time, *msgs )
//		});
	}
	
	/**
	 *	There is a bug in unixCmd when running on MacOS X 10.3.9
	 *	which blocks successive unixCmd calls. This seems to
	 *	fix it (ONLY ONCE THOUGH). so call this method once after
	 *	you started launching a second server
	 */
	unblockPipe {
		this.sendMsg( '/methodr', '[', '/field', 'java.lang.System', \out, ']', \println );
	}
	
	// needs to be called inside a routine ;(
	listSendMsgAndWait { arg msg, match;
		var cmdName, resp, condition, result;

		condition	= Condition.new;
		result	= nil;
		// XXX could better use an OSCpathResponder here
		resp = OSCresponderNode( addr, match[ 0 ], { arg time, resp, msg;
			if( match.every({ arg item, i; msg[ i ].asSymbol == item.asSymbol }), {
				resp.remove;
				condition.test = true;
				condition.signal;
				result	= msg;
			});
		}).add;
		condition.test = false;
		this.listSendMsg( msg );
		condition.wait;
		^result;
	}
	
	listSendMsg { arg msg;
//		if( slowOSC, {
//			dispPending.add( msg );
//			dispCond.test = true;
//			dispCond.signal;
//		}, {
			addr.sendMsg( *msg );
//		});
	}

 	listSendBundle { arg time, msgs;
//		if( slowOSC, {
//			dispPending.addAll( msgs );	// no timetags for now
//			dispCond.test = true;
//			dispCond.signal;
//		}, {
			addr.sendBundle( time, *msgs )
//		});
	}

	/**
	 *	@warning	asynchronous; needs to be called inside a Routine
	 */
	sendMsgSync { arg msg, successMsg, failedMsg, timeout = 4.0, condition;
		var respDone, respFailed, cancel, result;

		if( condition.isNil ) { condition = Condition.new; };

		successMsg = successMsg.asArray;
		respDone	= OSCresponderNode( addr, successMsg[ 0 ], { arg time, resp, msg;
			var ok = true;
		
			// string reply message args are always returned as symbols
			successMsg.do({ arg c, i;
				if( msg[ i ].isKindOf( Symbol ), {
					if( msg[ i ] !== c.asSymbol, { ok = false; });
				}, {
					if( msg[ i ] != c, { ok = false; });
				});
			});

			if( ok, {
				if( cancel.notNil, { cancel.stop; });
				resp.remove;
				result			= msg;
				condition.test	= true;
				condition.signal;
			});
		});
		respDone.add;
		if( failedMsg.notNil, {
			failedMsg = failedMsg.asArray;
			respFailed = OSCresponderNode( addr, failedMsg[ 0 ], { arg time, resp, msg;
				var ok = true;
			
				failedMsg.do({ arg c, i;
					if( msg[ i ] != c, { ok = false; });
				});
				
				if( ok, {
					if( cancel.notNil, { cancel.stop; });
					resp.remove;
					result			= msg;
					condition.test	= true;
					condition.signal;
				});
			});
			respFailed.add;
		});
		condition.test = false;
		if( timeout > 0.0, {
			cancel = {
				timeout.wait;
				respDone.remove;
				if( respFailed.notNil, { respFailed.remove; });
				result			= nil;
				condition.test	= true;
				condition.signal;
			
			}.fork( clock );
		});
		condition.test = false;
		this.listSendMsg( msg );
		condition.wait;
		^result;
	}

	wait { arg responseName;
		var resp, routine;

		routine	= thisThread;
		resp		= OSCresponderNode( addr, responseName, { 
			resp.remove; routine.resume( true ); 
		});
		resp.add;
	}
	
	waitForBoot { arg onComplete, timeout = 20.0;
		if( serverBooting, { ^this.doWhenBooted( onComplete, timeout )});
		if( serverRunning.not, { 
			this.boot;
			this.doWhenBooted( onComplete, timeout );
		}, onComplete );
	}

	doWhenBooted { arg onComplete, timeout = 20.0;
		var cancel, upd, exec;
		
		exec = Routine( onComplete );
		if( serverRunning.not, {
			upd = UpdateListener.newFor( this, {
				cancel.stop;
				upd.remove;
				exec.play( clock );
			}, \serverRunning );
			cancel = {
				timeout.wait;
				upd.remove;
				"SwingOSC server failed to start".error;
				serverBooting = false;
			}.fork( clock );
		}, { exec.play( clock )});
	}

	bootSync { arg condition;
		if( condition.isNil, { condition = Condition.new });
		condition.test = false;
		this.waitForBoot({
			// Setting func to true indicates that our condition has become true and we can go when signaled.
			condition.test = true;
			condition.signal
		});
		condition.wait;
	}

	startAliveThread { arg delay = 2.0, period = 0.7;
		var lives = deathBounces;
		^aliveThread ?? {
			statusWatcher = OSCpathResponder( addr, [ '/info', \status ], { arg time, resp, msg;
				lives = deathBounces;
//				alive = true;
//				"----- from /info".postln;
				this.serverRunning = true;
			}).add;	
			aliveThread = {
				// this thread polls the server to see if it is alive
				delay.wait;
				loop {
					if( serverBooting and: { (options.protocol === \tcp) and: { addr.isConnected.not }}, {
						try { this.connect };
					}, {
						this.status;
						lives = lives - 1;
					});
					period.wait;
					if( lives <= 0, {
//						"----- from lives <=0".postln;
						this.serverRunning = false;
					});
				};
			}.fork( clock );
			aliveThread;
		};
	}
	
	stopAliveThread {
		if( aliveThread.notNil, { 
			aliveThread.stop; 
			aliveThread = nil;
		});
		if( statusWatcher.notNil, { 
			statusWatcher.remove;
			statusWatcher = nil;
		});
	}
	
	boot { arg startAliveThread = true;
		var resp;
		
		{
			this.status;
			0.5.wait;
			block { arg break;
				if( serverRunning, { "SwingOSC server already running".inform; break.value; });
				if( serverBooting, { "SwingOSC server already booting".inform; break.value; });
				
				serverBooting = true;
				if( startAliveThread, { this.startAliveThread });
	//			this.newAllocators;		// will be done in initTree !
	//			this.resetBufferAutoInfo;	// not applicable to SwingOSC
				this.doWhenBooted({
	// there is no notification system at the moment
	//				if( notified, { 
	//					this.notify;
	//					"notification is on".inform;
	//				}, { 
	//					"notification is off".inform; 
	//				});
					serverBooting = false;
	//				serverRunning = true;
	//				this.initTree;
				});
				if( isLocal.not, { 
					"You will have to manually boot remote server.".inform;
				},{
					this.bootServerApp;
				});
			};
		}.fork( clock );
	}
	
	bootServerApp {
		var cmd, localAddr;
		// note : the -h option is used again because it significantly speeds
		// up the connection.
		localAddr = NetAddr.localAddr;
		cmd = java + (options.javaOptions ? "") + "-jar \"" ++ program ++ "\" " ++ options.asOptionsString( addr.port ) +
			("-h " ++ localAddr.ip ++ ":" ++ localAddr.port);
		("booting " ++ cmd).inform;
		unixCmd( cmd );
	}
	
	reboot { arg func; // func is evaluated when server is off
		if( isLocal.not, { "Can't reboot a remote server".inform; ^this });
		if( serverRunning, {
			Routine.run {
				this.quit;
				this.wait( \done );
				0.1.wait;
				func.value;
				this.boot;
			}
		}, {
			func.value;
			this.boot;
		});
	}
	
	status {
		this.sendMsg( '/query', \status, 0 );
	}
	
	dumpOSC { arg code = 1, reply;
		/*
			0 - turn dumping OFF.
			1 - print the parsed contents of the message.
			2 - print the contents in hexadecimal.
			3 - print both the parsed and hexadecimal representations of the contents.
		*/
		dumpMode	= code;
		if( reply.isNil, {
			this.sendMsg( '/dumpOSC', code );
		}, {
			dumpModeR	= reply;
			this.sendMsg( '/dumpOSC', code, reply );
		});
	}
	
	debugDumpLocals {
		this.sendMsg( '/methodr', '[', '/methodr', '[', '/method', "de.sciss.swingosc.SwingOSC", \getInstance, ']', \getCurrentClient, ']', \debugDumpLocals );
	}

	quit {
		this.sendMsg( '/quit' );
//		this.disconnect;	// try to prevent SC crash, this might be too late though ...
		"/quit sent\n".inform;
//		alive			= false;
		this.stopAliveThread;
		serverBooting 	= false;
		this.serverRunning	= false;
	}

	// ----------------- private class methods -----------------

	// stolen from jrh's extCollaboration
	*prMyIP {
		var i, k, line, pipe, indices, res, keySize, commandLine = "ifconfig", key = "inet", delimiter=$ ;
		try {
//			res = Pipe.findValuesForKey( "ifconfig", "inet" );
			key = key ++ delimiter;
			keySize = key.size;
			pipe = Pipe( commandLine, "r" );
//			Pipe.do( commandLine, { arg l; ... })
			protect {
				line	= pipe.getLine;
				i	= 0;
				while({ line.notNil }, {
					indices = line.findAll( key );
					indices !? {
						indices.do({ arg j;
							j = j + keySize;
							while({ line[ j ] == delimiter }, { j = j + 1 });
							k = line.find( delimiter.asString, offset: j ) ?? { line.size } - 1;
							res = res.add( line[ j..k ]);
						});
					};
					i = i + 1;
					line = pipe.getLine;
				});
			} {
				pipe.close;
			};
		};
		^res !? {
			res = res.reject( _ == "127.0.0.1" ); // remove loopback device ip
//			if(res.size > 1) { warn("the first of those devices were chosen: " ++ res) };
			res[ 0 ];
		};
	}

	*cmdPeriod {
		set.do({ arg server; server.prResumeThreads });
	}

	// ----------------- private instance methods -----------------

	init { arg argName, argAddr, argOptions, argClientID;
		name			= argName;
		addr			= argAddr;
		clientID		= argClientID;
		
		if( addr.isNil, {
			options 	= argOptions ?? { SwingOptions.new };
			isLocal	= true;
			addr		= NetAddr( if( options.loopBack.not, { SwingOSC.prMyIP }) ? "127.0.0.1", 57111 );
		}, {
			isLocal	= addr.addr == 2130706433;
			if( argOptions.isNil, {
				options	= SwingOptions.new.loopBack_( isLocal );
			}, {
				options	= argOptions;
				if( options.loopBack && isLocal.not, {
					"SwingOSC.new : loopBack option is true, but IP is not localhost!".warn;
				});
			});
			if( isLocal.not, {
				isLocal = addr.ip == SwingOSC.prMyIP;
			});
		});
		
//		serverRunning	= false;
		named.put( name, this );
		set.add( this );
		this.newAllocators;

		screenWidth	= 640;	// will be updated
		screenHeight	= 480;
		
		// note: this can fail when sclang doesn't get port 57120
		// which unfortunately happens from time to time. it would
		// be better to use rendezvous
		helloResp		= OSCpathResponder( nil, [ '/swing', \hello ], { arg time, resp, msg;
			var pingAddr, protocol;
			pingAddr = NetAddr( msg[ 2 ].asString, msg[ 3 ].asInteger );
			protocol	= msg[ 4 ];
//("pingAddr : "++pingAddr++"; req.addr "++addr).postln;
			if( pingAddr == addr and: { protocol == options.protocol }, {
//				this.initTree;
//				this.serverRunning_( false );
//				"----- from /swing, \\hello".postln;
				if( statusWatcher.notNil, {
					statusWatcher.action.value;	// resets 'lives'
				}, {
					this.serverRunning_( true );	// invokes initTree if necessary
				});
			});
		}).add;
		
		try {
			this.connect;
			{
				if( this.sendMsgSync([ '/query', \version, '[', '/field', 'de.sciss.swingosc.SwingOSC', \VERSION, ']' ],
					[ '/info', \version ]).notNil, {
					this.serverRunning_( true );
				});
			}.fork( clock );
		};
	}

//	addStatusWatcher {
//		statusWatcher = 
//			OSCpathResponder( addr, [ '/info', \status ], { arg time, resp, msg;
//				alive = true;
//				this.serverRunning_( true );
//			}).add;	
//	}
	
	// needs to be called inside a routine!
	prRetrieveScreenBounds {
		var reply = this.sendMsgSync(
			[ '/get', '[', '/local', \toolkit, '[', '/method', 'java.awt.Toolkit', \getDefaultToolkit, ']', ']',
					 'screenSize.width', 'screenSize.height' ], [ '/set', \toolkit ]
		);
		if( reply.notNil, {
			reply.copyToEnd( 2 ).pairsDo({ arg key, value;
				switch( key.asString,
					"screenSize.width", { screenWidth = value.asInt; },
					"screenSize.height", { screenHeight = value.asInt; }
				);
			});
		});			
	}
	
	serverRunning_ { arg val;
		gaga = gaga.add([ Main.elapsedTime, val ]);
		if( val != tempRunning, {
//			[ "serverRunning_", serverRunning, val ].postln;
//			thisMethod.dumpBackTrace;
			tempRunning = val;
//			serverRunning = val;
			if( tempRunning, {
				this.initTree({
					if( tempRunning, {
						"SwingOSC : server connected.".postln;
						serverRunning = true;
						this.changed( \serverRunning );
					});
				}, {
					"SwingOSC.initTree : timeout".error;
				});			
			}, {
//			[ "KIEKA" ].postln;
				clock.sched( 0.0, {
//			[ "KUUKA", tempRunning ].postln;
//					"".post;	// is totally weird XXX
					if( tempRunning.not, {
						serverRunning = false;
						this.changed( \serverRunning );
					});
				});
			});
		});
	}

	prResumeThreads {
		if( aliveThread.notNil, {
			this.stopAliveThread;
			this.startAliveThread( 0.7 );
		});
	}
	
	protEnsureApplication {
		if( application.not, {
			this.sendMsg( '/method', "de.sciss.swingosc.Application", \ensure,
				JSCWindow.nativeDecoration.not, JSCWindow.internalFrames, false );
			application = true;
		});
	}
}