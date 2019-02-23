/*
 *	JSCMenu
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
 *	@author		Hanns Holger Rutz
 *	@version		0.61, 01-Aug-08
 */
JSCMenuNode {
	var <server;
	var <id;
	var parent;
	var dataptr = 0;
	
	*new { arg parent;
		^super.new.prInit( parent );
	}
	
	prInit { arg argParent;
		parent = argParent;
	}

	remove {
		if( dataptr.notNil, {
			parent.prRemoveChild( this );
//			this.prRemove;
			this.prClose;
		}, {
			"JSCMenuNode-remove : this node was already removed.".debug( this );
		});
	}

//	isClosed { ^dataptr.isNil }

	prClose { arg preMsg, postMsg;
		var bndl;
		
		bndl = Array( preMsg.size + postMsg.size + 1 );
		bndl.addAll( preMsg );
		bndl.add([ '/free', this.id ]);
		bndl.addAll( postMsg );
		server.listSendBundle( nil, bndl );

		dataptr = nil;
//		onClose.value( this );
	}

	
	protRemoveMsg {
		 ^[ '/method', parent.id, \remove, '[', '/ref', this.id, ']' ];
	}
}

//JSCNamedMenuNode : JSCMenuNode {
//
//}

JSCMenuSeparator : JSCMenuNode {
	*new { arg parent, index;
		^super.new( parent ).prInitItem( index );
	}

	prInitItem { arg index;
		var bndl;
		
		server	= parent.server;
		id		= server.nextNodeID;
		parent.prAddChild( this );
		
		bndl		= Array( 2 );
		bndl.add([ '/local', this.id, '[', '/new', "de.sciss.gui.MenuSeparator", ']' ]);
		if( index.isNil, {
			bndl.add([ '/method', parent.id, \add, '[', '/ref', this.id, ']' ]);
		}, {
			bndl.add([ '/method', parent.id, \add, '[', '/ref', this.id, ']', index ]);
		});
		server.listSendBundle( nil, bndl );
	}
}

JSCMenuItem : JSCMenuNode {
	var <name;
	var <enabled = true;
	var <>action;
	var acResp;
	var <shortcut;

	*new { arg parent, name, index;
		^super.new( parent ).prInitItem( name, index );
	}
	
	prInitItem { arg name, index;
		var bndl, acVal;
		
		server	= parent.server;
		id		= server.nextNodeID;
		parent.prAddChild( this );
		acVal	= this.protCreateActionResponder;
		
		bndl		= Array( 2 );
		bndl.add([ '/local', this.id, '[', '/new', this.protJClass, this.id, name, ']',
			"ac" ++ this.id, '[', '/new', "de.sciss.swingosc.ActionResponder", this.id ] ++ acVal ++ [ ']' ]);
		if( index.isNil, {
			bndl.add([ '/method', parent.id, \add, '[', '/ref', this.id, ']' ]);
		}, {
			bndl.add([ '/method', parent.id, \add, '[', '/ref', this.id, ']', index ]);
		});
		server.listSendBundle( nil, bndl );
	}

	/**
	 *	Sets the shortcut key for the item.
	 *
	 *	@param	shortcut	the shortcut to use. this is a string either comprised of
	 *					a space separated modifiers, such as "meta" or "shift",
	 *					and a single character such as "M", or a descriptive string
	 *					such as "NUMPAD1".
	 */
	shortcut_ { arg descr;
		if( descr != shortcut, {
			shortcut = descr;
			server.sendMsg( '/set', this.id, \shortCut, descr );
		});
	}

	doAction { action.value( this )}

	enabled_ { arg bool;
		if( bool != enabled, {
			enabled = bool;
			server.sendMsg( '/set', this.id, \enabled, bool );
		});
	}
	
	name_ { arg string;
		if( string != name, {
			name = string;
			server.sendMsg( '/set', this.id, \name, string );
		});
	}
	
	protJClass { ^"de.sciss.swingosc.SwingMenuItem" }
	
	protCreateActionResponder {
		acResp = OSCpathResponder( server.addr, [ '/action', this.id ], { arg time, resp, msg;
			{ this.doAction }.defer;
		}).add;
		^nil;
	}
}

JSCMenuCheckItem : JSCMenuItem {
	var <selected = false;
	
	selected_ { arg bool;
		if( bool != selected, {
			selected = bool;
			server.sendMsg( '/set', this.id, \selected, bool );
		});
	}
	
	protJClass { ^"de.sciss.swingosc.SwingMenuCheckItem" }

	protCreateActionResponder {
		acResp = OSCpathResponder( server.addr, [ '/action', this.id ], { arg time, resp, msg;
			selected = msg[ 4 ] != 0;
			{ this.doAction }.defer;
		}).add;
		^[ \selected ];
	}
}

JSCMenuGroup : JSCMenuItem {
	var children;

//	*new { arg parent, name, index;
//		^super.new.prInit( parent, index, name );
//	}
//	
//	prInit { arg argParent, argIndex;
//	
//	}

	prAddChild { arg child;
		children = children.add( child );
	}

	protJClass { ^"de.sciss.swingosc.SwingMenuGroup" }

	prClose { arg preMsg, postMsg;
		super.prClose( preMsg, postMsg );
		children.do({ arg item; item.prClose });
	}

	prRemoveChild { arg child;
		children.remove( child );
		server.sendMsg( '/method', this.id, \remove, '[', '/ref', child.id, ']' );
	}

	removeAll {
		children.copy.do( _.remove );
	}
}

JSCMenuRoot : JSCMenuNode {
	classvar all;
	
	var children;

	*initClass {
		all = IdentityDictionary.new;
		UI.registerForShutdown({ this.prDisposeAll });
	}
	
	*new { arg server;
		var result;
		server = server ?? { SwingOSC.default };
		result = all.at( server );
		if( result.notNil, { ^result });
		if( server.serverRunning.not, {
			MethodError( "SwingOSC server not running", thisMethod ).throw;
		});
		^super.new( nil ).prInitRoot( server );
	}
	
	*prDisposeAll {
		var list;
		list	= all.values;
		all 	= IdentityDictionary.new;
		list.do( _.prDispose );
	}
	
	prDispose {
		all.removeAt( server );
//		("DISPOSING. children = " ++ children).postln;
		this.removeAll;
	}

	prInitRoot { arg argServer;
		server	= argServer;
		server.protEnsureApplication;
		id		= \menuRoot;
		all.put( server, this );
		UpdateListener.newFor( server, { arg upd, server;
			if( server.serverRunning.not, { upd.remove; this.prDispose });
		}, \serverRunning );
	}
	
	prAddChild { arg child;
		children = children.add( child );
	}

	prRemoveChild { arg child;
		children.remove( child );
		server.sendMsg( '/method', this.id, \remove, '[', '/ref', child.id, ']' );
	}
	
	remove { this.prDispose }
	
	removeAll {
		children.copy.do( _.remove );
	}
}