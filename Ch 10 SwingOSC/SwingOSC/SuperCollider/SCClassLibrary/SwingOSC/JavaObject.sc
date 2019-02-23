/*
 *	JavaObject
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
 *		26-Jan-08  added double underscore syntax
 *		12-Aug-08  added JavaObjectD
 */

/**
 *	Based on the fact that Object.sc "catches" all
 *	calls to unknown methods in "doesNotUnderstand",
 *	we exploit this behaviour to create an easy wrapper
 *	class for Java object control in SwingOSC.
 *
 *	@version	0.64, 28-Jan-10
 *	@author	Hanns Holger Rutz
 */
JavaObject {
	classvar allObjects;
	classvar nextTimeOut = 4.0;
	var <server, <id;

	*initClass {
		UI.registerForShutdown({ this.destroyAll });
	}

	// ----------------- constructor -----------------

	*new { arg className, server ... args;
		^super.new.prInitJavaObject( className, server, args );
	}
	
	// ----------------- public class methods -----------------

	*getClass { arg className, server;
		^super.new.prInitJavaClass( className, server );
	}

	*getField { arg javaObject, fieldName;
		^super.new.prInitJavaField( javaObject, fieldName );
	}

	*newFrom { arg javaObject, selector ... args;
		^super.new.prInitJavaResult( javaObject, selector, args );
	}
	
	*basicNew { arg id, server;
		^super.newCopyArgs( server, id );
	}
	
	*destroyAll {
		var list;
		list = allObjects.copy;
		allObjects = Array.new( 8 );
		list.do({ arg obj; obj.destroy });
	}
	
	/**
	 *	Executes a function where the first
	 *	asynchronous call will be made with a given
	 *	timeout in seconds. E.g. to facilitate
	 *
	 *	JavaObject.withTimeOut( inf, { jOptionPaneClass.showInputDialog_( ... )});
	 */
	*withTimeOut { arg timeout = 30.0, func;
		nextTimeOut = timeout;
		^func.value;
	}
	
	// ----------------- public instance methods -----------------

	destroy {
		server.sendMsg( '/free', id );
		allObjects.remove( this );
	}
	
	print {
		server.sendMsg( '/print', id );
	}
	
	isNull {
		^this.notNull.not;
	}
	
	notNull {
		var result, queryID;
		
		queryID	= UniqueID.next;
		result	= server.sendMsgSync([ '/query', queryID, '[', '/method', "de.sciss.swingosc.SwingOSC", \notNull, '[', '/ref', id, ']', ']' ], ['/info', queryID ]);
		^result.asArray.last.booleanValue;
	}

	// ----------------- private instance methods -----------------

	prInitJavaObject { arg className, argServer, args;
		var msg;
		
		server		= argServer ?? { SwingOSC.default };
		allObjects	= allObjects.add( this );	// array grows
		id			= server.nextNodeID;
		
		msg			= this.prAddArgs([ '/local', id, '[', '/new', className ], args ).add( ']' );
		
//		server.sendBundle( nil, [ '/local', id, '[', '/new', className ] ++ args ++ [ ']' ]);
//msg.postln;
		server.listSendMsg( msg );
	}
	
	prInitJavaClass { arg className, argServer;
		server		= argServer ?? { SwingOSC.default };
		allObjects	= allObjects.add( this );	// array grows
		id			= server.nextNodeID;
// this doesn't use the dynamic classloader!
//		server.sendMsg( '/local', id, '[', '/method', 'java.lang.Class', \forName, className, ']' );
		server.sendMsg( '/local', id, '[', '/ref', className, ']' );
	}

	prInitJavaField { arg javaObject, fieldName;
		server		= javaObject.server;
		allObjects	= allObjects.add( this );	// array grows
		id			= server.nextNodeID;
		server.sendMsg( '/local', id, '[', '/field', javaObject.id, fieldName, ']' );
	}

	prInitJavaResult { arg javaObject, selector, args;
		var msg;
		
		server		= javaObject.server;
		allObjects	= allObjects.add( this );	// array grows
		id			= server.nextNodeID;
		msg			= javaObject.prMethodCall([ '/local', id, '[' ], selector, args ).add( ']' );
		server.listSendMsg( msg );
	}
	
	prRemove {
		allObjects.remove( this );
	}
		
	doesNotUnderstand { arg selector ... args;
		var selStr;
		
		selStr = selector.asString;
		if( selStr.last === $_, {
			if( selStr.at( selStr.size - 2 ) === $_, { // shortcut for *newFrom
				selector = selStr.copyFromStart( selStr.size - 3 );
				^this.class.newFrom( this, selector, *args );
			}, {
				selector = selStr.copyFromStart( selStr.size - 2 );
				if( thisThread.isKindOf( Routine ), {
					^this.prMethodCallAsync( selector, args );
				}, {
					"JavaObject : asynchronous call outside routine".warn;
					{ ("RESULT: " ++ this.prMethodCallAsync( selector,
						args )).postln; }.fork( SwingOSC.clock );
				});
			});
		}, {
			server.listSendMsg( this.prMethodCall( nil, selector, args ));
		});
	}
	
	prMethodCallAsync { arg selector, args;
		var id, msg, timeout;
		timeout		= nextTimeOut;
		nextTimeOut	= 4.0;	// reset
		id			= UniqueID.next;
		msg			= this.prMethodCall([ '/query', id, '[' ], selector, args ).add( ']' );
		msg			= server.sendMsgSync( msg, [ '/info', id ], nil, timeout );
		^if( msg.notNil, { msg[ 2 ]}, nil );
	}
	
	prAddArgs { arg msg, args;
		args.do({ arg x;
			if( x.respondsTo( \id ), {
				msg = msg ++ [ '[', '/ref', x.id, ']' ];
			}, {
				msg = msg ++ x.asSwingArg;
			});
		});
		^msg
	}
	
	asSwingArg {
		^[ '[', '/ref', this.id, ']' ];
	}

	prMethodCall { arg msg, selector, args;
		^this.prAddArgs( msg ++ [ '/method', id, selector ], args );
	}
	
	// ---- now override a couple of methods in Object that ----
	// ---- might produce name conflicts with java methods  ----
		
	size { arg ... args; this.doesNotUnderstand( \size, *args ); }
	do { arg ... args; this.doesNotUnderstand( \do, *args ); }
	generate { arg ... args; this.doesNotUnderstand( \generate, *args ); }
	copy { arg ... args; this.doesNotUnderstand( \copy, *args ); }
	dup { arg ... args; this.doesNotUnderstand( \dup, *args ); }
	poll { arg ... args; this.doesNotUnderstand( \poll, *args ); }
	value { arg ... args; this.doesNotUnderstand( \value, *args ); }
	next { arg ... args; this.doesNotUnderstand( \next, *args ); }
	reset { arg ... args; this.doesNotUnderstand( \reset, *args ); }
	first { arg ... args; this.doesNotUnderstand( \first, *args ); }
	iter { arg ... args; this.doesNotUnderstand( \iter, *args ); }
	stop { arg ... args; this.doesNotUnderstand( \stop, *args ); }
	free { arg ... args; this.doesNotUnderstand( \free, *args ); }
	repeat { arg ... args; this.doesNotUnderstand( \repeat, *args ); }
	loop { arg ... args; this.doesNotUnderstand( \loop, *args ); }
	throw { arg ... args; this.doesNotUnderstand( \throw, *args ); }
	rank { arg ... args; this.doesNotUnderstand( \rank, *args ); }
	slice { arg ... args; this.doesNotUnderstand( \slice, *args ); }
	shape { arg ... args; this.doesNotUnderstand( \shape, *args ); }
	obtain { arg ... args; this.doesNotUnderstand( \obtain, *args ); }
	switch { arg ... args; this.doesNotUnderstand( \switch, *args ); }
	yield { arg ... args; this.doesNotUnderstand( \yield, *args ); }
	release { arg ... args; this.doesNotUnderstand( \release, *args ); }
	update { arg ... args; this.doesNotUnderstand( \update, *args ); }
	layout { arg ... args; this.doesNotUnderstand( \layout, *args ); }
	inspect { arg ... args; this.doesNotUnderstand( \inspect, *args ); }
	crash { arg ... args; this.doesNotUnderstand( \crash, *args ); }
	freeze { arg ... args; this.doesNotUnderstand( \freeze, *args ); }
	blend { arg ... args; this.doesNotUnderstand( \blend, *args ); }
	pair { arg ... args; this.doesNotUnderstand( \pair, *args ); }
	source { arg ... args; this.doesNotUnderstand( \source, *args ); }
	clear { arg ... args; this.doesNotUnderstand( \clear, *args ); }
}

/**
 *	A variant of JavaObject that allows you
 *	to register a destroyAction that is called
 *	before the server reference is freed.
 */
JavaObjectD : JavaObject {
	var <>destroyAction;
	
	destroy {
		try {
			destroyAction.value( this );
		} { arg e;
			e.reportError;
		};
		super.destroy;
	}
}