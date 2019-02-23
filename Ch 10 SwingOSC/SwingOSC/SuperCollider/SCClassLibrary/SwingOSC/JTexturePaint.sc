/**
 *	@version	0.60, 25-Feb-10
 *	@author	Hanns Holger Rutz
 */
// THIS IS EXPERIMENTAL AND SUBJECT TO CHANGES!!!
JTexturePaint : JavaObject {
	*fromImage { arg img;
		var bufImg, tex, width, height, cm, bufG, anchor, server;
		server	= img.server;
		width	= JavaObject.newFrom( img, \getWidth, nil );
		height	= JavaObject.newFrom( img, \getHeight, nil );
		cm		= 2;	// TYPE_INT_ARGB
		bufImg	= JavaObject( "java.awt.image.BufferedImage", server, width, height, cm );
		bufG		= JavaObject.newFrom( bufImg, \createGraphics );
		bufG.drawImage( img, 0, 0, nil );
		bufG.dispose;
		anchor	= JavaObject( "java.awt.Rectangle", server, 0, 0, width, height );
		tex		= this.new( "java.awt.TexturePaint", server, bufImg, anchor );
		width.destroy;
		height.destroy;
		bufG.destroy;
		anchor.destroy;
		bufImg.destroy;
		^tex;
	}
	
	destroy {
		server.sendBundle( nil, [ '/methodr', '[', '/method', id, \getImage, ']', \flush ], [ '/free', id ]);
		allObjects.remove( this );
	}
	
	doesNotUnderstand { arg selector ... args;
		DoesNotUnderstandError( this, selector, args ).throw;
	}
}