/*
 *	JSCSynth
 *	(SwingOSC)
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
 *	@version		0.63, 02-Oct-09
 *	@author		Hanns Holger Rutz
 */
JSCSynth {
	classvar all;
	var <id;
	var <swing, <scsynth;
	
	*newFrom { arg swing, scsynth;
		var perSwing, jSCSynth;
		
		if( all.isNil, {
			all = IdentityDictionary.new;
		});
		perSwing	= all[ swing ];
		if( perSwing.isNil, {
			perSwing = IdentityDictionary.new;
			all[ swing ] = perSwing;
		});
		jSCSynth = perSwing[ scsynth ];
		if( jSCSynth.isNil, {
			jSCSynth	= super.new.prInit( swing, scsynth );
			perSwing[ scsynth ] = jSCSynth;
		});
		^jSCSynth;
	}
	
	*get { arg swing, scsynth;
		var perSwing;
		if( all.isNil, { ^nil });
		perSwing	= all[ swing ];
		^if( perSwing.notNil, { perSwing[ scsynth ]});
	}
	
	prInit { arg argSwing, argSCSynth;
		var optionsID, bndl, addr;
		
		swing	= argSwing;
		scsynth	= argSCSynth;
		
		id		= swing.nextNodeID;
		optionsID	= swing.nextNodeID;
		bndl		= Array( 3 );
		addr		= if( swing.isLocal or: { scsynth.isLocal.not }, {
			scsynth.addr.asSwingArg;
		}, {
			[ '[', '/new', "java.net.InetSocketAddress",
				'[', '/methodr', '[', '/methodr', '[', '/method', \swing, \getCurrentClient, ']',
					\getReplyAddress, ']', \getHostName, ']', scsynth.addr.port, ']' ]
		});
		bndl.add([ '/method', '[', '/local', optionsID, '[', '/new', "de.sciss.jcollider.ServerOptions", ']', ']',
					\setProtocol, scsynth.options.protocol ]);
		bndl.add([ '/method', '[', '/local', id, '[', '/new', "de.sciss.jcollider.Server", scsynth.name ] ++ addr ++
					[ '[', '/ref', optionsID, ']', scsynth.clientID, ']', ']', \start ]);
		bndl.add([ '/free', optionsID ]);
//		bndl.postcs;
//~bndl = bndl;
		swing.listSendBundle( nil, bndl );
	}
	
	asSwingArg {
		^[ '[', '/ref', this.id, ']' ];
	}
}