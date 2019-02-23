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
 *		30-Mar-10		does not extend JavaObject any more
 */

/**
 *	@version	0.64, 30-Mar-10
 *	@author	Hanns Holger Rutz
 */
JSCImage {
	var jImg, <id, <server;

	// this polymorphous constructor _really sucks_.
//	*new { arg multiple, height = nil;
//		if( multiple.isKindOf( Point ), {
//			^super.new.init(multiple.x, multiple.y);
//		});
//		if(multiple.isKindOf(Number), {
//			^super.new.init(multiple, height ? multiple);
//		});
//		if(multiple.isKindOf(String), {
//			if ( multiple.beginsWith("http://").not
//				and:{ multiple.beginsWith("file://").not }
//				and:{ multiple.beginsWith("ftp://").not  }) {
//				^this.open(multiple);
//			};
//			^this.openURL( multiple );
//		});
//		^nil;
//	}

	*prNew { arg jImg; ^super.new.prInit( jImg )}

	/**
	 *	Creates a writeable image, initially filled
	 *	homogeneously with one color. Note that this
	 *	method is more strict than the Cocoa variant,
	 *	in that the arguments must be in the given
	 *	order below. This is to not complicate things
	 *	even more with the last argument being the
	 *	SwingOSC server.
	 */
	*color { arg width, height, color, server;
		var img, gid;
		server	= server ?? { SwingOSC.default };
		img		= JavaObject( "de.sciss.swingosc.SwingImage", server, width, height, 2 ); // 2 = TYPE_INT_ARGB
		gid		= server.nextNodeID;
		color	= color ?? { Color.black };
		server.sendBundle( nil,
			[ '/set', '[', '/local', gid, '[', '/method', img.id, \createGraphics, ']', ']', \color ] ++ color.asSwingArg,
			[ '/method', gid, \fillRect, 0, 0, width, height ],
			[ '/method', gid, \dispose ],
			[ '/free', gid ]);
		^this.prNew( img );
	}
	
	/**
	 *	Creates a read-only image by reading in
	 *   an image file from a given URL (with file or http protocol)
	 */
	*openURL { arg url, server;
		var jURL, img;
		server	= server ?? { SwingOSC.default };
		jURL		= JavaObject( "java.net.URL", server, url );
		img		= JavaObject.newFrom( JavaObject.basicNew( \toolkit, server ), \createImage, jURL );
		jURL.destroy;
		^this.prNew( img );
	}

	/**
	 *	Creates a read-only image by reading in
	 *   an image file from a given path
	 */
	*open { arg path, server;
		var jFile, img;
		server	= server ?? { SwingOSC.default };
		jFile	= JavaObject( "java.io.File", server, path );
		img		= JavaObject.newFrom( JavaObject.basicNew( \toolkit, server ), \createImage, jFile );
		jFile.destroy;
		^this.prNew( img );
	}

	/**
	 *	Converts a Color instance into
	 *	a pixel datatype suitable for JSCImage.
	 *	This is a 32bit packed Integer in
	 *	the ARGB format.
	 */
	*colorToPixel { arg col;
		^(((col.alpha * 0xFF).asInteger << 24) | ((col.red * 0xFF).asInteger << 16) | ((col.green * 0xFF).asInteger << 8) | (col.blue * 0xFF).asInteger)
	}

	setPixels { arg array, rect, start = 0;
		server.listSendMsg([ '/method', this.id, \setRGB, rect.left, rect.top, rect.width, rect.height ] ++ array.asSwingArg ++ [ start, rect.width  ]);
	}

	/**
	 *	@deprecated	use 'free' instead
	 */	
	destroy {
		this.deprecated( thisMethod, this.class.findRespondingMethodFor( \free ));
		^this.free;
	}

	/**
	 *	Deallocates resources associated with
	 *	with this image.
	 */	
	free {
		jImg.flush;
		jImg.destroy;
	}
	
	asSwingArg { ^[ '[', '/ref', id, ']' ]}

	// ---- private ----

	prInit { arg argJImg;
		jImg		= argJImg;
		id		= argJImg.id;
		server	= argJImg.server;
	}
}