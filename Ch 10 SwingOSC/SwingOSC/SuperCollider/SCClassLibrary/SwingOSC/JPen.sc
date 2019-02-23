/*
 *	JPen
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
 *	- 03-Oct-06	separate font_ method, font and colour
 *				removed in string methods!!
 *	- 01-Jan-07	bundle size increased to 8K again
 *				; fixes missing List -> asSwingArg ; setSmoothing
 *	- 25-Feb-08	added image methods
 *	- 04-Aug-08	incorporated some of the additions from charles picasso
 */

/**
 *	Implementation note: JSCWindow and JSCUserView
 *	call protRefresh when the draw func should be
 *	re-executed. In protRefresh, the draw func is
 *	executed, resulting in an array of short OSC
 *	sub commands, bracketed by special begin/end
 *	statements which allow the OSC commands to
 *	be spread over more than one bundle.
 *
 *	These get send to a java Pen object
 *	which maps them to Java2D operations. The Pen
 *	object implements the Icon interface and hence
 *	it can be added to a JLabel or the special Frame
 *	class for example.
 *
 *	@version		0.64, 28-Jan-10
 *	@author		Hanns Holger Rutz
 *
 *	@todo		check if String.bounds is cross platform or not
 *				(might have to replace)
 */
JPen {
	classvar cmds;
//	classvar currentView;
		
	*use { arg function;
		var res;
		this.push;
		res = function.value;
		this.pop;
		^res
	}

// ------------- affine transforms -------------

	*translate { arg x = 0, y = 0;
		cmds = cmds.add([ 16, x, y ]);
	}

	*scale { arg x = 0, y = 0;
		cmds = cmds.add([ 17, x, y ]);
	}

	*skew { arg x = 0, y = 0;
		cmds = cmds.add([ 19, x, y ]);
	}

	*rotate { arg angle = 0, x = 0, y = 0;
		cmds = cmds.add([ 18, angle, x, y ]);
	}

	*matrix_ { arg array;
		cmds = cmds.add([ 20 ] ++ array );
	}

// ------------- color and stroke customization (setter methods) -------------

	*strokeColor_ { arg color;
		cmds = cmds.add([ 21, color.red, color.green, color.blue, color.alpha ]);
	}

	*fillColor_ { arg color;
		cmds = cmds.add([ 22, color.red, color.green, color.blue, color.alpha ]);
	}
	
	*color_ { arg color;
		this.strokeColor_( color );
		this.fillColor_( color );
	}
	
	*width_ { arg width=1;
		cmds = cmds.add([ 3, width ]);
	}

	*font_ { arg font;
		cmds = cmds.add([ 23, font.name, font.size, font.style ]);
	}

	/**
	 *	@deprecated
	 */
	*setSmoothing { arg flag = true;
		this.deprecated( thisMethod, Meta_JPen.findRespondingMethodFor( \smoothing_ ));
		this.smoothing = flag;
	}

	*smoothing_ { arg flag = true;
		cmds = cmds.add([ 33, flag.binaryValue ]);
	}
	
	*paint_ { arg paint;
		cmds = cmds.add([ 37, paint.id ]);
	}

	*joinStyle_ { arg style = 0; // 0 = miter, 1 = round, 2 = bevel
		cmds = cmds.add([ 5, style ]);
	}
	
	*lineDash_ { arg pattern; // should be a FloatArray
		cmds = cmds.add([ 4, pattern.size ] ++ pattern );
	}

// ------------- path composition -------------

	*path { arg function;
		var res;
		this.beginPath;
		res = function.value;
		this.endPath;
		^res
	}

	*beginPath {
		cmds = cmds.add([ 15 ]);
	}

	*moveTo { arg point;
		cmds = cmds.add([ 6, point.x, point.y ]);
	}

	*lineTo { arg point;
		cmds = cmds.add([ 7, point.x, point.y ]);
	}

	*line { arg p1, p2;
		^this.moveTo( p1 ).lineTo( p2 );
	}

	*curveTo { arg point, cpoint1, cpoint2;
		cmds = cmds.add([ 9, cpoint1.x, cpoint1.y, cpoint2.x, cpoint2.y, point.x, point.y ]);
	}

	*quadCurveTo { arg point, cpoint1;
		cmds = cmds.add([ 8, cpoint1.x, cpoint1.y, point.x, point.y ]);
	}

	*arcTo { arg point1, point2, radius;
		cmds = cmds.add([ 38, point1.x, point1.y, point2.x, point2.y, radius ]);
	}

	*addArc { arg center, radius, startAngle, arcAngle;
		cmds = cmds.add([ 12, center.x, center.y, radius, startAngle, arcAngle ]);
	}

	*addWedge { arg center, radius, startAngle, arcAngle;
		cmds = cmds.add([ 13, center.x, center.y, radius, startAngle, arcAngle ]);
	}

	*addAnnularWedge { arg center, innerRadius, outerRadius, startAngle, arcAngle;
		cmds = cmds.add([ 14, center.x, center.y, innerRadius, outerRadius,
					        startAngle, arcAngle ]);
	}

	*addRect { arg rect;
		cmds = cmds.add([ 10, rect.left, rect.top, rect.width, rect.height ]);
	}

	*addOval { arg rect;
		cmds = cmds.add([ 11, rect.left, rect.top, rect.width, rect.height ]);
	}

	*stroke {
		cmds = cmds.add([ 0 ]);
	}

	*fill {
		cmds = cmds.add([ 1 ]);
	}

	*clip {
		cmds = cmds.add([ 32 ]);
	}

	*fillStroke {
		this.draw( 3 );
	}
	
	*draw { arg option = 0; // 0 = fill, 1 = eofill, 2 = stroke, 3 = fillstroke, 4 = eofillstroke
		cmds = cmds.add([ 2, option ]);
	}

	*fillAxialGradient { arg startPoint, endPoint, color0, color1;
		cmds = cmds.add([ 39, startPoint.x, startPoint.y, endPoint.x, endPoint.y,
		                      color0.red, color0.green, color0.blue, color0.alpha,
		                      color1.red, color1.green, color1.blue, color1.alpha  ]);
	}

// ------------- direct drawing commands -------------

	*strokeRect { arg rect;
		cmds = cmds.add([ 26, rect.left, rect.top, rect.width, rect.height ]);
	}

	*fillRect { arg rect;
		cmds = cmds.add([ 24, rect.left, rect.top, rect.width, rect.height ]);
	}

	*strokeOval { arg rect;
		cmds = cmds.add([ 27, rect.left, rect.top, rect.width, rect.height ]);
	}

	*fillOval { arg rect;
		cmds = cmds.add([ 25, rect.left, rect.top, rect.width, rect.height ]);
	}
	
//	*drawAquaButton { arg rect, type=0, down=false, on=false;
//		// XXX
//		(thisMethod.name ++ " not implemented").warn;
//	}

// ------------- string commands -------------

	*string { arg str;
		this.stringAtPoint( str, Point( 0, 0 ));
	}
	
	*stringAtPoint { arg str, point;
		cmds = cmds.add([ 28, str, point.x, point.y ]);
	}
	
	*stringInRect { arg str, rect;
		cmds = cmds.add([ 29, str, rect.left, rect.top, rect.width, rect.height, 0, 0 ]);
	}
	
	*stringCenteredIn { arg str, rect;
		cmds = cmds.add([ 29, str, rect.left, rect.top, rect.width, rect.height, 0.5, 0.5 ]);
	}
	
	*stringLeftJustIn { arg str, rect;
		cmds = cmds.add([ 29, str, rect.left, rect.top, rect.width, rect.height, 0, 0.5 ]);
	}
	
	*stringRightJustIn { arg str, rect;
		cmds = cmds.add([ 29, str, rect.left, rect.top, rect.width, rect.height, 1, 0.5 ]);
	}
	
// ------------- image commands THESE ARE EXPERIMENTAL AND SUBJECT TO CHANGES!!! -------------

	*image { arg img;
		this.imageAtPoint( img, Point( 0, 0 ));
	}
	
	*imageAtPoint { arg img, point;
		cmds = cmds.add([ 35, img.id, point.x, point.y ]);
	}
	
	*imageSlice { arg img, point, rect;
		cmds = cmds.add([ 36, img.id, point.x, point.y, rect.left, rect.top, rect.width, rect.height ]);
	}

// ------------ gradients, translucency, composites ------------

//	*fillAxialGradient { arg startPoint, endPoint, color0, color1;
//		cmds = cmds.add([ "fag", startPoint.x, startPoint.y, endPoint.x, endPoint.y, color0.red, color0.green, color0.blue, color0.alpha, color1.red, color1.green, color1.blue, color1.alpha ]);
//	}

	*alpha_ { arg opacity;
		cmds = cmds.add([ 34, opacity ]);
	}

	*blendMode_{ arg mode;
		"Meta_JPen:blendMode_ : not yet implemented".warn;
	}
	
	*setShadow { arg offsetPoint = Point( 2, 2 ), blur = 0.5, color = Color.black;
		"Meta_JPen:setShadow : not yet implemented".warn;
	}

// ------------ from extPlot2D (swiki) ------------

	*addField { arg array, bounds, selector = \fillRect, colorFunc = Color.grey(_), legato = 1.0;
		var rows, cols, width, height, y, l;
		if( array.rank != 2, { Error( "array not a 2D matrix" ).throw });
		#rows, cols = array.shape;
		height = bounds.height;
		width = bounds.width;
		this.use({
			rows.do({ arg i;
				cols.do({ arg j;
					var y = array[ i ][ j ];
					this.color = colorFunc.( y );
					l = legato.( y );
					this.perform( selector,
						Rect(
							width / cols * j, 
							height / rows * i, 
							width / cols * l + 1, // "trapping"
							height / rows * l + 1
						)
					);
				});
			});
		});
	}

// ------------ private ------------

	*push {
		cmds = cmds.add([ 30 ]);
	}

	*pop {
		cmds = cmds.add([ 31 ]);
	}
	
	// called by JSCWindow, JSCUserView
	*protRefresh { arg func, view, server, penID, cmpID;
		var bndl, off, stop, len, numCmd, nextLen, maxBndlSize, floatSize;
	
// this is about 2% faster but won't deal with different draw funcs
//		cmds 		= Array( cmds.size.max( 8 ));
		cmds 		= nil;
//		currentView	= view;
		func.value( view );
		bndl			= [[ '/method', penID, \beginRec ]];
		off			= 0;
		len			= 92;	// [ #bundle, [ '/method', int, \beginRec ] (48)
						// + [ '/method', int, \add, '[', '/array', ']' ]] (44)
		stop			= off;
		numCmd		= cmds.size;
		maxBndlSize	= server.options.oscBufSize - 60;  // 8132 = 8192 - 56 (see below) - 4 (max. boundary alignment)
		floatSize		= if( server.useDoubles, 8, 4 );

		while({ stop < numCmd }, {
//			nextLen	= cmds[ stop ].size * 5;
			nextLen	= cmds[ stop ].size;	// i.e. type tags section size
			// note: aNumber.size == 0, so will become 4! XXX beware if using NetAddr.useDoubles_( true ) !!
//			cmds[ stop ].do({ arg cmd; nextLen = nextLen + ((cmd.size + 4) & -4) });
			cmds[ stop ].do({ arg cmd; nextLen = nextLen + if( cmd.isFloat, floatSize, { (cmd.size + 4) & -4 })});
			if( len > maxBndlSize, {
// WARNING: COPYRANGE USES AN INCLUSIVE STOP INDEX!!!!
				bndl = bndl.add([ '/method', penID, \add ] ++ cmds.copyRange( off, stop - 1 ).flatten.asSwingArg );
//("FLUSHING LEN = "++len).postln;
				server.listSendBundle( nil, bndl );
				bndl = nil;
				len	= 60;	// [ #bundle, [ '/method', int, \add, '[', '/array', ']' ]] (60)
				off	= stop;
			});
			len	= len + nextLen;
			stop = stop + 1;
		});
		if( off < stop, {
			bndl = bndl.add([ '/method', penID, \add ] ++ cmds.copyRange( off, stop - 1 ).flatten.asSwingArg );
		});
		// these are 56 additional bytes:
		bndl = bndl.add([ '/method', penID, \stopRec ]);
//		[ "cmpID", cmpID ].postln;
		if( cmpID.notNil, { bndl = bndl.add([ '/method', cmpID, \repaint ])});

//("FLUSHING FINAL LEN = "++(len+56)).postln;
		server.listSendBundle( nil, bndl );
		cmds 		= nil;
//		currentView	= nil;
	}
}
