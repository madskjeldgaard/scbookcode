/*
 *	JFont
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
 *	Replacement for the cocoa font class.
 *
 *	@author		Hanns Holger Rutz
 *	@version		0.65, 10-May-10
 */
JFont {
	classvar <>verbose = false;

	classvar <>default;
	
	classvar defaultSansFace, defaultSerifFace, defaultMonoFace;
//	classvar names;
	
	var <>name, <>size, <>style;
		
	*initClass {
//		StartUp.add({ this.prCreateDefaults });
//	}
//	
//	*prCreateDefaults {
		switch( thisProcess.platform.name,
		\osx, {
			default			= JFont( "LucidaGrande", 11 );
			defaultSansFace	= "LucidaGrande";
			defaultSerifFace	= "Times";
			defaultMonoFace	= "Monaco";
		},
		\linux, {
			default			= JFont( "Bitstream Vera Sans", 12 );
			defaultSansFace	= "Bitstream Vera Sans";
			defaultSerifFace	= "Bitstream Vera Serif";
			defaultMonoFace	= "Bitstream Vera Sans Mono";
		}, 
		\windows, {
			default			= JFont( "Tahoma", 11 );
			defaultSansFace	= "Tahoma";
			defaultSerifFace	= "Serif";
			defaultMonoFace	= "Monospaced";
		}, {
			default			= JFont( "SansSerif", 12 );
			defaultSansFace	= "SansSerif";
			defaultSerifFace	= "Serif";
			defaultMonoFace	= "Monospaced";
		});
	}
		
	*new { arg name, size, style = 0;
		^super.newCopyArgs( name, size, style );
	}
	
	setDefault {
		default = this;
// ??? should we do this ??? cocoa doesn't
		SwingOSC.set.do({ arg server; server.listSendMsg([ '/local', \font ] ++ this.asSwingArg )});
	}

	asSwingArg {
		^([ '[', '/new', 'java.awt.Font', this.name, this.style, this.size, ']' ]);
	}
	
	*availableFonts { arg server;
		var servers, result;
		servers 	= Archive.global[ \swingOSCFontNames ];
		server	= server ?? { SwingOSC.default };
		result	= servers !? { servers[ server.name ]};
		if( result.notNil, { ^result });
		"JFont.availableFonts : font cache not yet available".warn;
		^[ "Dialog", "DialogInput", "Monospaced", "SansSerif", "Serif" ];
	}
	
	*deleteCache {
		Archive.global.put( \swingOSCFontNames, nil );
	}

	// called by SwingOSC upon startup inside a Routine	
	*prMakeFontsAvailable { arg server;
		var servers, result;
		servers = Archive.global[ \swingOSCFontNames ];
		if( servers.isNil, {
			servers = IdentityDictionary.new;
			Archive.global[ \swingOSCFontNames ] = servers;
		});
		if( servers.includesKey( server.name ), { ^this });
		result = this.prQueryFontNames( server );
		servers[ server.name ] = result;
	}

	*antiAliasing_ { arg flag = false;
		if( verbose, { "JFont.antiAliasing : has no effect".error; });
	}
	
	*smoothing_ { arg flag = false;
		if( verbose, { "JFont.smoothing : has no effect".error; });
	}

	storeArgs { ^[ name, size, style ] }

	boldVariant {
		^this.class.new( name, size, style | 1 );
	}

	*defaultSansFace {
		^defaultSansFace;
	}
	
	*defaultSerifFace {
		^defaultSerifFace;
	}
	
	*defaultMonoFace {
		^defaultMonoFace;
	}

	*prQueryFontNames { arg server;
		var qid, fonts, numFonts, reply, off, chunkSize, fontNames, success = true;
		
		if( verbose, { "JFont.availableFonts : querying...".postln });
		server	= server ?? SwingOSC.default;
		server.sendMsg( '/method', '[', '/local', \fnt, '[', '/new', 'java.util.ArrayList', ']', ']', \addAll,
			'[', '/method', 'java.util.Arrays', \asList,
				'[', '/methodr', '[', '/method', 'java.awt.GraphicsEnvironment', \getLocalGraphicsEnvironment, ']', \getAvailableFontFamilyNames, ']',
			']' );
		qid		= UniqueID.next;
		reply	= server.sendMsgSync([ '/query', qid, '[', '/method', \fnt, \size, ']' ], [ '/info', qid ]);
		if( reply.notNil, {
			numFonts	= reply[ 2 ];
		}, {
			"JFont.availableFonts : timeout".error;
			numFonts 	= 0;
			success	= false;
		});
		off		= 0;
		fontNames	= Array( numFonts );
		while({ (off < numFonts) && success }, {
			// 128 queries is about 4.5 KB sending and probably < 8 KB receiving
			// (worst case: all font names have a length of 64 chars)
			chunkSize	= min( 128, numFonts - off );
			reply	= server.sendMsgSync([ '/query' ] ++ Array.fill( chunkSize, { arg i; [ qid, '[', '/method', \fnt, \get, off + i, ']' ]}).flatten,
									  [ '/info', qid ]);
			if( reply.notNil, {
				chunkSize.do({ arg i; fontNames.add( reply[ (i << 1) + 2 ].asString )});
				off = off + chunkSize;
			}, {
				"JFont.availableFonts : timeout".error;
				success	= false; // leave loop
			});
		});
		server.sendMsg( '/free', \fnt );
		if( verbose, { "JFont.availableFonts : query done.".postln });
		^if( success, fontNames );
	}
}