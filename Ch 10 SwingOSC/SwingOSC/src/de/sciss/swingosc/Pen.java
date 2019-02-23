/*
 *  Pen.java
 *  SwingOSC
 *
 *  Copyright (c) 2005-2009 Hanns Holger Rutz. All rights reserved.
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
 *  Changelog:
 *  	03-Oct-06	modified string commands (font + colour are separate)
 *  	11-Dec-06	ConstrStringInRect takes additional halign and valign args.
 *  				ConstrDraw uses CmdFill instead of CmdDraw (making the
 *  				pixel coordinates identical to cocoa pen)
 *  	04-Feb-07	discovered a weird performance problem with complex shapes
 *  				generated through g2.fill( stroke.createStrokedShape( ... ))
 *  				 (probably due to
 *  				joining calculations). interestingly, this problem disappears
 *  				when we go back to g2.draw(). using a translation of -0.5,-0.5
 *  				we still get images pixel-compatible with cocoa.
 *  				; clip and matrix are concatenating
 *  	24-Nov-07	stroke is transformed according to current AffineTransform
 *  				at draw statement (behaves like cocoa counterpart)
 *  	25-Feb-08	image support
 *  	14-Jul-09	replacing hashmap with array (should be faster)
 */
 
package de.sciss.swingosc;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.Icon;
import javax.swing.JComponent;

import de.sciss.gui.GUIUtil;

/**
 *	@version	0.62, 14-Jul-09
 *	@author		Hanns Holger Rutz
 */
public class Pen
implements Icon
{
	protected Component 				c			= null;
	private Cmd[] 						cmds		= new Cmd[ 0 ];
	protected final Stack 				context		= new Stack();
	
	protected final List				recCmds		= new ArrayList();
	private final Constr[]				constrs		= new Constr[ 40 ];
	
	protected final float[]				pt			= new float[ 8 ];
	
	private static final float			kRad2DegM	= (float) (-180.0 / Math.PI);
	
	protected static final BasicStroke	strkDefault	= new BasicStroke( 1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER );
	protected static final Font			fntDefault	= new Font( "SansSerif", Font.PLAIN, 11 );
	
	protected final FontRenderContext	frc;
	protected GraphicsContext			gc;
	protected GeneralPath				gp;
	
	private boolean						absCoords;
	
	private Component					lastComp	= null;
	private Component					lastRef		= null;
	
	protected final static Map			antiAliasOn;
	protected final static Map			antiAliasOff;
	
	private final Point					ptOrigin	= new Point();
	protected Composite					compOrig;
	
	static {
		antiAliasOn		= new RenderingHints( null );
		antiAliasOff	= new RenderingHints( null );
		antiAliasOn.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		antiAliasOff.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
		antiAliasOn.put( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
		antiAliasOff.put( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
	}

	public Pen()
	{
		this( false );
	}

	public Pen( boolean absCoords )
	{
		constrs[  0 ] = new ConstrDraw();
		constrs[  1 ] = new ConstrFill();
		constrs[  2 ] = new ConstrFillDraw();
		constrs[  3 ] = new ConstrWidth();
		constrs[  4 ] = new ConstrDash();
		constrs[  5 ] = new ConstrJoin();
		constrs[  6 ] = new ConstrMoveTo();
		constrs[  7 ] = new ConstrLineTo();
		constrs[  8 ] = new ConstrQuadTo();
		constrs[  9 ] = new ConstrCurveTo();
		constrs[ 10 ] = new ConstrAddRect( new Rectangle2D.Float() );
		constrs[ 11 ] = new ConstrAddRect( new Ellipse2D.Float() );
		constrs[ 12 ] = new ConstrAddArc( Arc2D.OPEN, true );
		constrs[ 13 ] = new ConstrAddArc( Arc2D.PIE, false );
		constrs[ 14 ] = new ConstrAddCylSector();
		constrs[ 15 ] = new ConstrReset();
		constrs[ 16 ] = new ConstrTranslate();
		constrs[ 17 ] = new ConstrScale();
		constrs[ 18 ] = new ConstrRotate();
		constrs[ 19 ] = new ConstrShear();
		constrs[ 20 ] = new ConstrMatrix();
		constrs[ 21 ] = new ConstrDrawColor();
		constrs[ 22 ] = new ConstrFillColor();
		constrs[ 23 ] = new ConstrFont();
		constrs[ 24 ] = new ConstrFillRect();
		constrs[ 25 ] = new ConstrFillOval();
		constrs[ 26 ] = new ConstrDrawRect();
		constrs[ 27 ] = new ConstrDrawOval();
		constrs[ 28 ] = new ConstrStringAtPoint();
		constrs[ 29 ] = new ConstrStringInRect();
		constrs[ 30 ] = new ConstrPush();
		constrs[ 31 ] = new ConstrPop();
		constrs[ 32 ] = new ConstrClip();
		constrs[ 33 ] = new ConstrSmooth();
		constrs[ 34 ] = new ConstrAlpha();
		constrs[ 35 ] = new ConstrImage();
		constrs[ 36 ] = new ConstrCroppedImage();
		constrs[ 37 ] = new ConstrPaint();
		constrs[ 38 ] = new ConstrArcTo();
		constrs[ 39 ] = new ConstrFillAxialGrad();
		
		frc = new FontRenderContext( GraphicsEnvironment.
				getLocalGraphicsEnvironment().
				getDefaultScreenDevice().
				getDefaultConfiguration().
				getNormalizingTransform(), true, true );
//		frc	= new FontRenderContext( null, true, true );
		
		this.absCoords = absCoords;
	}
	
	public Pen( Component c )
	{
		this( c, false );
	}
	
	// absCoords : if true, all coordinates are
	// seen as relative to the window's top left
	// as is the case unfortunately with sc
	public Pen( Component c, boolean absCoords )
	{
		this( absCoords );
		setComponent( c );
	}
	
	public void setAbsCoords( boolean absCoords )
	{
		this.absCoords = absCoords;
	}
	
	public boolean getAbsCoords()
	{
		return absCoords;
	}
	
	public void beginRec()
	{
		recCmds.clear();
		context.clear();
		gc	= new GraphicsContext();
//		emptyGP.reset();
//		gp	= emptyGP;
		gp	= new GeneralPath();
	}
	
	public void setComponent( Component c )
	{
		this.c	= c;
	}
	
	public Component getComponent()
	{
		return c;
	}
	
	public void add( Object[] oscCmds )
	{
//		Object cmdID = null;
		int cmdID = -1;

		try {
			for( int off = 0; off < oscCmds.length; ) {
				cmdID	= ((Number) oscCmds[ off++ ]).intValue();
//				off 	= ((Constr) constrs.get( cmdID )).constr( oscCmds, off );
				off 	= constrs[ cmdID ].constr( oscCmds, off );
			}
		}
		catch( NullPointerException e1 ) {
			System.out.println( "Pen.add : unknown command " + cmdID );
		}
		catch( NumberFormatException e1 ) {
			System.out.println( "Pen.add : argument type mismatch for " + cmdID );
		}
		catch( IndexOutOfBoundsException e1 ) {
			System.out.println( "Pen.add : argument count mismatch for " + cmdID );
		}
	}
	
	public void stopRec()
	{
		final int numCmds = recCmds.size();
		cmds = new Cmd[ numCmds ];
		for( int i = 0; i < numCmds; i++ ) {
			cmds[ i ] = (Cmd) recCmds.get( i );
		}
		recCmds.clear();
		context.clear();
		gc = null;
		gp = null;
	}
	
	public void dispose()
	{
		gc = null;
		recCmds.clear();
		context.clear();
		cmds = new Cmd[ 0 ];
	}
	
	public void paintIcon( Component c, Graphics g, int x, int y )
	{
		final Graphics2D		g2			= (Graphics2D) g;
		final AffineTransform 	atOrig		= g2.getTransform();
		final Stroke			strkOrig	= g2.getStroke();
		final Shape				clipOrig	= g2.getClip();
		
		compOrig = g2.getComposite();

//System.out.println( "compOrig = " + compOrig );
		
//		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g2.addRenderingHints( antiAliasOn );
		if( absCoords ) {
//			final Component ref		= SwingUtilities.getRootPane( c ).getContentPane();
			final Point		ptCorr;
			if( (c != lastComp) || (lastRef == null) ) { 
				lastRef	= c;
			
				while( lastRef.getParent() != null &&
					(!(lastRef instanceof JComponent) ||
					(((JComponent) lastRef).getClientProperty( "origin" ) == null)) ) {
				
					lastRef = lastRef.getParent();
				}
				lastComp = c;
			}
			ptOrigin.setLocation( x, y );
			ptCorr = GUIUtil.convertPoint( lastRef, ptOrigin, c );
			
//			System.out.println( "translate " + x + ", " + y );
			
			if( (ptCorr.x != 0 ) || (ptCorr.y != 0) ) g2.translate( ptCorr.x, ptCorr.y );
		} else {
			if( (x != 0 ) || (y != 0) ) g2.translate( x, y );
		}
		for( int i = 0; i < cmds.length; i++ ) {
			cmds[ i ].perform( g2 );
		}

//System.out.println( "transform was " + g2.getTransform() + "; orig " + atOrig );
		g2.setComposite( compOrig );
		g2.setTransform( atOrig );
		g2.setStroke( strkOrig );
		g2.setClip( clipOrig );
	}
	
    public int getIconWidth()
	{
		return c == null ? 0 : c.getWidth();
	}

	public int getIconHeight()
	{
		return c == null ? 0 : c.getHeight();
	}

	private class GraphicsContext
	{
		protected Paint					pntDraw;
		protected Paint					pntFill;
		protected BasicStroke			strk;
		protected final AffineTransform	at;
		protected Shape					clip;
//		private final GeneralPath		gp;
//		protected GeneralPath			gp;
		protected Font					fnt;
		protected Map					hints;
		protected Composite				comp;
		
		protected GraphicsContext()
		{
			pntDraw	= Color.black;
			pntFill	= Color.black;
			strk	= strkDefault;
			at		= new AffineTransform();
			clip	= null;
//			gp		= new GeneralPath();
			fnt		= fntDefault;
			hints	= antiAliasOn;
		}
		
		protected GraphicsContext( GraphicsContext orig )
		{
			pntDraw	= orig.pntDraw;
			pntFill	= orig.pntFill;
			strk	= orig.strk;
			at		= new AffineTransform( orig.at );
			clip	= orig.clip;
//			gp		= new GeneralPath( orig.gp );
			fnt		= orig.fnt;
			hints	= orig.hints;
		}
		
		protected void restore( Graphics2D g2 )
		{
			g2.setClip( clip );
			g2.addRenderingHints( hints );
			g2.setComposite( comp == null ? compOrig : comp );
		}
	}
	
	private abstract class Cmd
	{
		protected Cmd() { /* empty */ }
		protected abstract void perform( Graphics2D g2 );
	}

	private class CmdFill
	extends Cmd
	{
		private final Shape 			shp;
		private final Paint				pnt;
		private final AffineTransform	at;
		
		protected CmdFill( Shape shp )
		{
			pnt		= gc.pntFill;
			
//			test:		if( (pnt.getClass() == Color.class) || 
//					((gc.at.getShearX() == 0.0) && (gc.at.getShearY() == 0.0) &&
//					 (gc.at.getScaleX() == 1.0) && (gc.at.getScaleY() == 1.0)) ) {
test:		if( pnt.getClass() == Color.class ) {
				this.shp	= shp;
				at			= null;
			} else {
				final AffineTransform atInv;
				try {
					atInv		= gc.at.createInverse();
				} catch( NoninvertibleTransformException e1 ) {
					System.err.println( "Pen->CmdFill : NoninvertibleTransformException" );
					// ... what can we do ...
					at			= null;
					this.shp	= shp;
					break test;
				}
				this.shp	= atInv.createTransformedShape( shp );
				at			= new AffineTransform( gc.at );
			}
		}
		
		protected void perform( Graphics2D g2 )
		{
			if( at == null ) {
				g2.setPaint( pnt );
				g2.fill( shp );
			} else {
				final AffineTransform atOrig = g2.getTransform();
				g2.transform( at );
				g2.setPaint( pnt );
				g2.fill( shp );
				g2.setTransform( atOrig );
			}
		}
	}

/*
	private class CmdFill
	extends Cmd
	{
		private final Shape 	shp;
		private final Paint		pnt;
		
		protected CmdFill( Shape shp )
		{
			this.shp	= shp;
			pnt			= gc.pntFill;
		}
		
//		private CmdFill( Shape shp, Paint pnt )
//		{
//			this.shp	= shp;
//			this.pnt	= pnt;
//		}
		
		protected void perform( Graphics2D g2 )
		{
			g2.setPaint( pnt );
			g2.fill( shp );
		}
	}
*/
	private class CmdDraw
	extends Cmd
	{
		private final Shape 			shp;
		private final Paint				pnt;
		private final Stroke			strk;
		private final AffineTransform	at;
		
		protected CmdDraw( Shape shp )
		{
			pnt = gc.pntDraw;
			
test:		if( (gc.at.getShearX() == 0.0) && (gc.at.getShearY() == 0.0) &&
				(gc.at.getScaleX() == gc.at.getScaleY()) ) {
				
				this.shp	= shp;
				if( gc.at.getScaleX() == 1.0 ) {
					strk	= gc.strk;
					at		= null;
				} else {
					final float scalex  = (float) Math.abs( gc.at.getScaleX() );
					final float[] dash  = gc.strk.getDashArray(); // might be null!
					final float[] dash2;
					
					if( (dash == null) || (dash.length < 2) ) {
						dash2 = dash;
					} else {
						dash2 = new float[ dash.length ];
						for( int i = 0; i < dash.length; i++ ) {
							dash2[ i ] = dash[ i ] * scalex;
						}
					}
					strk	= new BasicStroke( gc.strk.getLineWidth() * scalex,
											   gc.strk.getEndCap(), gc.strk.getLineJoin(),
											   gc.strk.getMiterLimit() * scalex,
											   dash2, gc.strk.getDashPhase() * scalex ); 
					at		= null;
				}
			} else {
				final AffineTransform atInv;
				strk = gc.strk;
				try {
					atInv		= gc.at.createInverse();
				} catch( NoninvertibleTransformException e1 ) {
					System.err.println( "Pen->CmdDraw : NoninvertibleTransformException" );
					// ... what can we do ...
					at			= null;
					this.shp	= shp;
					break test;
				}
				this.shp	= atInv.createTransformedShape( shp );
				at			= new AffineTransform( gc.at );
				at.translate( -0.5, -0.5 ); 
			}
		}
				
		protected void perform( Graphics2D g2 )
		{
			final AffineTransform atOrig = g2.getTransform();
			if( at != null ) {
				g2.transform( at );
			} else {
				g2.translate( -0.5, -0.5 );
			}
			g2.setPaint( pnt );
			g2.setStroke( strk );
			g2.draw( shp );
			g2.setTransform( atOrig );
		}
	}

	private class CmdClip
	extends Cmd
	{
		private final Shape 	shp;

		protected CmdClip( Shape shp )
		{
			this.shp 	= shp;
		}
		
		protected void perform( Graphics2D g2 )
		{
//			g2.setClip( shp );
			g2.clip( shp );
		}
	}

	private class CmdRestore
	extends Cmd
	{
		private final GraphicsContext gcOld;

		protected CmdRestore( GraphicsContext gc )
		{
			gcOld = gc;
		}
		
		protected void perform( Graphics2D g2 )
		{
			gcOld.restore( g2 );
		}
	}

	private class CmdHints
	extends Cmd
	{
		private final Map hints;

//		protected CmdHints( RenderingHints.Key key, Object value )
//		{
//			hints 		= new RenderingHints( key, value );
//		}
		
		protected CmdHints( Map hints )
		{
			this.hints	= hints;
		}
		
		protected void perform( Graphics2D g2 )
		{
			g2.addRenderingHints( hints );
		}
	}

	private class CmdComposite
	extends Cmd
	{
		private final Composite comp;

		protected CmdComposite( Composite comp )
		{
			this.comp = comp;
		}
		
		protected void perform( Graphics2D g2 )
		{
			g2.setComposite( comp );
		}
	}

	private class CmdImage
	extends Cmd
	{
		private final Image				img;
		private final AffineTransform	at;
		private final Shape				clip;
//		private final Map				hints;
		
		protected CmdImage( Image img, float tx, float ty )
		{
			this.img	= img;
			at			= new AffineTransform( gc.at );
			at.translate( tx, ty );
			clip		= null;
//			hints		= gc.hints;
		}
		
		protected CmdImage( Image img, float tx, float ty, float sx, float sy, float w, float h )
		{
			this.img	= img;
			at			= new AffineTransform( gc.at );
			at.translate( tx - sx, ty - sy );
			clip		= at.createTransformedShape( new Rectangle2D.Float( sx, sy, w, h ));
//			hints		= gc.hints;
		}
		
		protected void perform( Graphics2D g2 )
		{
//			g2.addRenderingHints( hints );
			if( clip != null ) {
				final Shape clipOrig = g2.getClip();
				g2.clip( clip );
				g2.drawImage( img, at, c );
				g2.setClip( clipOrig );
			} else { 
				g2.drawImage( img, at, c );
			}
		}
	}

	private abstract class Constr
	{
		protected Constr() { /* empty */ }

		protected abstract int constr( Object[] cmd, int off );
		
		protected final int transform( Object[] cmd, int off, int num )
		{
			for( int i = 0, j = num << 1; i < j; ) {
				pt[ i++ ] = ((Number) cmd[ off++ ]).floatValue();
			}
			
			gc.at.transform( pt, 0, pt, 0, num );

			return off;
		}
		
		protected final int decode( Object[] cmd, int off, int num )
		{
			for( int i = 0; i < num; ) {
				pt[ i++ ] = ((Number) cmd[ off++ ]).floatValue();
			}
			
			return off;
		}
		
		protected Color getColor( Object[] cmd, int off )
		{
			return new Color( Math.max( 0f, Math.min( 1f, ((Number) cmd[ off++ ]).floatValue() )),
					Math.max( 0f, Math.min( 1f, ((Number) cmd[ off++ ]).floatValue() )),
					Math.max( 0f, Math.min( 1f, ((Number) cmd[ off++ ]).floatValue() )),
					Math.max( 0f, Math.min( 1f, ((Number) cmd[ off++ ]).floatValue() )));			
		}
	}

	private class ConstrDrawColor
	extends Constr
	{
		protected ConstrDrawColor() { /* empty */ }
		
		protected int constr( Object[] cmd, int off )
		{
			gc.pntDraw = getColor( cmd, off );
			return off + 4;
		}
	}

	private class ConstrFillColor
	extends Constr
	{
		protected ConstrFillColor() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			gc.pntFill = getColor( cmd, off );
			return off + 4;
		}
	}

	private class ConstrFont
	extends Constr
	{
		protected ConstrFont() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			final String 	fntName;
			final int		fntSize, fntStyle;
			
			fntName		= cmd[ off++ ].toString();
			fntSize		= ((Number) cmd[ off++ ]).intValue();
			fntStyle	= ((Number) cmd[ off++ ]).intValue();
			gc.fnt		= new Font( fntName, fntStyle, fntSize );
			return off;
		}
	}

	private class ConstrMoveTo
	extends Constr
	{
		protected ConstrMoveTo() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			off = transform( cmd, off, 1 );
			gp.moveTo( pt[ 0 ], pt[ 1 ]);
			return off;
		}
	}

	private class ConstrLineTo
	extends Constr
	{
		protected ConstrLineTo() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			off = transform( cmd, off, 1 );
			gp.lineTo( pt[ 0 ], pt[ 1 ]);
			return off;
		}
	}

	private class ConstrQuadTo
	extends Constr
	{
		protected ConstrQuadTo() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			off = transform( cmd, off, 2 );
			gp.quadTo( pt[ 0 ], pt[ 1 ], pt[ 2 ], pt[ 3 ]);
			return off;
		}
	}

	private class ConstrCurveTo
	extends Constr
	{
		protected ConstrCurveTo() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			off = transform( cmd, off, 3 );
			gp.curveTo( pt[ 0 ], pt[ 1 ], pt[ 2 ], pt[ 3 ], pt[ 4 ], pt[ 5 ]);
			return off;
		}
	}

	private class ConstrArcTo
	extends Constr
	{
		private final Arc2D			arc = new Arc2D.Float();
//		private final Point2D		p1  = new Point2D.Float();
		private final Point2D.Float	p2  = new Point2D.Float();
		private final Point2D.Float	p3  = new Point2D.Float();
		
		protected ConstrArcTo() { /* empty */ }
		
		protected int constr( Object[] cmd, int off )
		{
			off = decode( cmd, off, 5 );
			final Point2D p1 = gp.getCurrentPoint();
			try {
				gc.at.inverseTransform( p1, p1 );
			}
			catch( NoninvertibleTransformException e1 ) { /* hmmm... ignore */ }
			p2.setLocation( pt[ 0 ], pt[ 1 ]);
			p3.setLocation( pt[ 2 ], pt[ 3 ]);
			arc.setArcByTangent( p1, p2, p3, pt[ 4 ]);
			gp.append( gc.at.createTransformedShape( arc ), true );
//			gp.moveTo( pt[ 0 ], pt[ 1 ]);	// behave like cocoa
			return off;
		}
	}

	private class ConstrReset
	extends Constr
	{
		protected ConstrReset() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			gp.reset();
			return off;
		}
	}

	private class ConstrDraw
	extends Constr
	{
		protected ConstrDraw() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			recCmds.add( new CmdDraw( gp ));
			gp = new GeneralPath();
			return off;
		}
	}

	private class ConstrFill
	extends Constr
	{
		protected ConstrFill() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			recCmds.add( new CmdFill( gp ));
			gp = new GeneralPath();
			return off;
		}
	}

	// 0: type
	private class ConstrFillDraw
	extends Constr
	{
		protected ConstrFillDraw() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			final int type = ((Number) cmd[ off++ ]).intValue();
			switch( type ) {
			case 0:	// fill (NON_ZERO)
				recCmds.add( new CmdFill( gp ));
				break;
			case 1:	// fill (EVEN_ODD)
				gp.setWindingRule( GeneralPath.WIND_EVEN_ODD );
				recCmds.add( new CmdFill( gp ));
				break;
			case 2:	// draw
				recCmds.add( new CmdDraw( gp ));
				break;
			case 3:	// fill (NON_ZERO) and draw
				recCmds.add( new CmdFill( gp ));
				recCmds.add( new CmdDraw( gp ));
				break;
			case 4:	// fill (EVEN_ODD) and draw
				gp.setWindingRule( GeneralPath.WIND_EVEN_ODD );
				recCmds.add( new CmdFill( gp ));
				recCmds.add( new CmdDraw( gp ));
				break;
			default:
				System.out.println( "JPen.fillDraw illegal type " + type );
				break;
			}
			gp = new GeneralPath();
			return off;
		}
	}
	
	private class ConstrClip
	extends Constr
	{
		protected ConstrClip() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			recCmds.add( new CmdClip( gp ));
			gc.clip	= gp;
			gp		= new GeneralPath();
			return off;
		}
	}

	private class ConstrTranslate
	extends Constr
	{
		protected ConstrTranslate() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			gc.at.translate( ((Number) cmd[ off++ ]).doubleValue(), ((Number) cmd[ off++ ]).doubleValue() );
			return off;
		}
	}

	private class ConstrScale
	extends Constr
	{
		protected ConstrScale() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			gc.at.scale( ((Number) cmd[ off++ ]).doubleValue(), ((Number) cmd[ off++ ]).doubleValue() );
			return off;
		}
	}

	private class ConstrRotate
	extends Constr
	{
		protected ConstrRotate() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			gc.at.rotate( ((Number) cmd[ off++ ]).doubleValue(), 
					  ((Number) cmd[ off++ ]).doubleValue(), ((Number) cmd[ off++ ]).doubleValue() );
			return off;
		}
	}

	private class ConstrShear
	extends Constr
	{
		protected ConstrShear() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			gc.at.shear( ((Number) cmd[ off++ ]).doubleValue(), ((Number) cmd[ off++ ]).doubleValue() );
			return off;
		}
	}

	// 0: sx, 1: shy, 2: shx, 3: sy, 4: tx, 5: ty
	private class ConstrMatrix
	extends Constr
	{
		protected ConstrMatrix() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			final double sx		= ((Number) cmd[ off++ ]).doubleValue();
			final double shx	= ((Number) cmd[ off++ ]).doubleValue();
			final double shy	= ((Number) cmd[ off++ ]).doubleValue();
			final double sy		= ((Number) cmd[ off++ ]).doubleValue();
			final double tx		= ((Number) cmd[ off++ ]).doubleValue();
			final double ty		= ((Number) cmd[ off++ ]).doubleValue();
			
//			gc.at.setTransform( sx, shx, shy, sy, tx, ty );
			gc.at.concatenate( new AffineTransform( sx, shx, shy, sy, tx, ty ));
			return off;
		}
	}

	// 0: width
	private class ConstrWidth
	extends Constr
	{
		protected ConstrWidth() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			final float width = ((Number) cmd[ off++ ]).floatValue();
			gc.strk	 = new BasicStroke( width,
						BasicStroke.CAP_BUTT, gc.strk.getLineJoin(),
						gc.strk.getMiterLimit(), gc.strk.getDashArray(),
						gc.strk.getDashPhase() );
			return off;
		}
	}

	// 0: type
	private class ConstrJoin
	extends Constr
	{
		protected ConstrJoin() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			// 0 = miter, 1 = round, 2 = bevel (same as in Java2D!)
			final int join = ((Number) cmd[ off++ ]).intValue();
			gc.strk	 = new BasicStroke( gc.strk.getLineWidth(),
						BasicStroke.CAP_BUTT, join,
						gc.strk.getMiterLimit(), gc.strk.getDashArray(),
						gc.strk.getDashPhase() );
			return off;
		}
	}

	// 0: N (num values), 1..N = float values
	private class ConstrDash
	extends Constr
	{
		protected ConstrDash() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			final int	dashNum = ((Number) cmd[ off++ ]).intValue();
			final float dash[]	= new float[ dashNum ];
			for( int i = 0; i < dashNum; i++ ) {
				dash[ i ] = ((Number) cmd[ off++ ]).floatValue();
			}
			gc.strk	 = new BasicStroke( gc.strk.getLineWidth(),
						BasicStroke.CAP_BUTT, gc.strk.getLineJoin(),
						gc.strk.getMiterLimit(), dash,
						gc.strk.getDashPhase() );
			return off;
		}
	}

	private class ConstrFillRect
	extends Constr
	{
		private final RectangularShape shp = new Rectangle2D.Float();
		
		protected ConstrFillRect() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			off = decode( cmd, off, 4 );
//			final Shape shp = new Rectangle2D.Float( pt[ 0 ], pt[ 1 ], pt[ 2 ], pt[ 3 ]);
			shp.setFrame( pt[ 0 ], pt[ 1 ], pt[ 2 ], pt[ 3 ]);
			recCmds.add( new CmdFill( gc.at.createTransformedShape( shp )));
			return off;
		}
	}

	private class ConstrDrawRect
	extends Constr
	{
		private final RectangularShape shp = new Rectangle2D.Float();

		protected ConstrDrawRect() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			off = decode( cmd, off, 4 );
//			final Shape shp = new Rectangle2D.Float( pt[ 0 ], pt[ 1 ], pt[ 2 ], pt[ 3 ]);
			shp.setFrame( pt[ 0 ], pt[ 1 ], pt[ 2 ], pt[ 3 ]);
			recCmds.add( new CmdDraw( gc.at.createTransformedShape( shp )));
			return off;
		}
	}

	private class ConstrFillOval
	extends Constr
	{
		private final RectangularShape shp = new Ellipse2D.Float();
		
		protected ConstrFillOval() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			off = decode( cmd, off, 4 );
//			final Shape shp = new Ellipse2D.Float( pt[ 0 ], pt[ 1 ], pt[ 2 ], pt[ 3 ]);
			shp.setFrame( pt[ 0 ], pt[ 1 ], pt[ 2 ], pt[ 3 ]);
			recCmds.add( new CmdFill( gc.at.createTransformedShape( shp )));
			return off;
		}
	}

	private class ConstrDrawOval
	extends Constr
	{
		private final RectangularShape shp = new Ellipse2D.Float();

		protected ConstrDrawOval() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			off = decode( cmd, off, 4 );
//			final Shape shp = new Ellipse2D.Float( pt[ 0 ], pt[ 1 ], pt[ 2 ], pt[ 3 ]);
			shp.setFrame( pt[ 0 ], pt[ 1 ], pt[ 2 ], pt[ 3 ]);
			recCmds.add( new CmdDraw( gc.at.createTransformedShape( shp )));
			return off;
		}
	}
	
	// 0: x, 1: y, 2: w, 3: h
	private class ConstrAddRect
	extends Constr
	{
		private final RectangularShape shp;

		protected ConstrAddRect( RectangularShape shp )
		{
			this.shp = shp;
		}

		protected int constr( Object[] cmd, int off )
		{
			off = decode( cmd, off, 4 );
			shp.setFrame( pt[ 0 ], pt[ 1 ], pt[ 2 ], pt[ 3 ]);
			gp.append( gc.at.createTransformedShape( shp ), false );
			return off;
		}
	}

	private class ConstrAddArc
	extends Constr
	{
		private final Arc2D		arc = new Arc2D.Float();
		private final int		type;
		private final boolean	connect;
		
		protected ConstrAddArc( int type, boolean connect )
		{
			this.type		= type;
			this.connect	= connect;
		}
		
		protected int constr( Object[] cmd, int off )
		{
			off = decode( cmd, off, 5 );
//			final Arc2D arc = new Arc2D.Float();
//			arc.setArcByCenter( pt[ 0 ], pt[ 1 ], pt[ 2 ],
//							    (pt[ 3 ] + pt[ 4 ]) * kRad2DegM, pt[ 4 ] * kRad2Deg, type );
			arc.setArcByCenter( pt[ 0 ], pt[ 1 ], pt[ 2 ],
							    pt[ 3 ] * kRad2DegM, pt[ 4 ] * kRad2DegM, type );
//			gc.gp.append( gc.at.createTransformedShape( arc ), false );
			gp.append( gc.at.createTransformedShape( arc ), connect );
			if( type == Arc2D.PIE ) {
				gp.moveTo( pt[ 0 ], pt[ 1 ]);	// behave like cocoa
			}
			return off;
		}
	}

	private class ConstrAddCylSector
	extends Constr
	{
		private final Arc2D		pie = new Arc2D.Float();
		private final Ellipse2D cyl = new Ellipse2D.Float();

		protected ConstrAddCylSector() { /* empty */ }
		
		// 0: cx, 1: cy, 2: ri, 3: ro, 4: angSt, 5, angExt
		protected int constr( Object[] cmd, int off )
		{
			off = decode( cmd, off, 6 );
//			final Arc2D pie = new Arc2D.Float();
			final float innerDiam = pt[ 2 ] * 2;
			pie.setArcByCenter( pt[ 0 ], pt[ 1 ], pt[ 3 ],
							    pt[ 4 ] * kRad2DegM, pt[ 5 ] * kRad2DegM, Arc2D.PIE );
//			final Ellipse2D cyl = new Ellipse2D.Float(
//			                      					pt[ 0 ] - pt[ 2 ], pt[ 1 ] - pt[ 2 ], innerDiam, innerDiam );
			cyl.setFrame( pt[ 0 ] - pt[ 2 ], pt[ 1 ] - pt[ 2 ], innerDiam, innerDiam );

			final Area shp = new Area( pie );
			shp.subtract( new Area( cyl ));
			gp.append( gc.at.createTransformedShape( shp ), false );
			gp.moveTo( pt[ 0 ] + (float) Math.cos( pt[ 4 ]) * pt[ 2 ],
			              pt[ 1 ] + (float) Math.sin( pt[ 4 ]) * pt[ 2 ]);	// behave like cocoa 
			return off;
		}
	}

	// 0: str, 1: x, 2: y, NOT ANY MORE: 3: fntName, 4: fntSize, 5: fntStyle,
	// 6: colrRed, 7: colrGreen, 8: colrBlue, 9: colrAlpha
	private class ConstrStringAtPoint
	extends Constr
	{
		protected ConstrStringAtPoint() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			final String		str;
//			final String		fntName;
//			final Paint			pnt;
//			final int			fntSize, fntStyle;
//			final Font			fnt;
			final GlyphVector	glyph;
			final LineMetrics	lineMetr;
			final Shape			shp;
			
			str 		= cmd[ off++ ].toString();
			off 		= decode( cmd, off, 2 );
//			off 		= transform( cmd, off, 1 );
//			fntName		= cmd[ off++ ].toString();
//			fntSize		= ((Number) cmd[ off++ ]).intValue();
//			fntStyle	= ((Number) cmd[ off++ ]).intValue();
//			fnt			= new Font( fntName, fntStyle, fntSize );
//			pnt			= getColor( cmd, off );
//			off	  	   += 4;

//System.out.println( "text '"+str+"'; at "+pt[0]+","+pt[1]+"; font "+fnt+"; colr "+pnt );
			
			glyph	= gc.fnt.createGlyphVector( frc, str );
			lineMetr= gc.fnt.getLineMetrics( str, frc );
			shp		= glyph.getOutline( pt[ 0 ], pt[ 1 ] + lineMetr.getAscent() + lineMetr.getDescent() ); // ???

			recCmds.add( new CmdFill( gc.at.createTransformedShape( shp )));

			return off;
		}
	}

	// 0: str, 1: x, 2: y, 3: w, 4: h, 5: halign, 6: valign
	private class ConstrStringInRect
	extends Constr
	{
		private final GeneralPath		gpStr	= new GeneralPath();
		private final AffineTransform	atPos	= new AffineTransform();
		
		protected ConstrStringInRect() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			final String		str;
			
			str 		= cmd[ off++ ].toString();
			off 		= decode( cmd, off, 6 );

			final AttributedCharacterIterator styledText = new AttributedString( str, gc.fnt.getAttributes() ).getIterator();
			final LineBreakMeasurer lbm = new LineBreakMeasurer( styledText, frc);
			final float w = pt[ 2 ];
			float x = pt[ 0 ];               
			float y = pt[ 1 ];
			final float yStop = y + pt[ 3 ];
			final float halign = pt[ 4 ];
			final float valign = pt[ 5 ];
//			final GeneralPath gp = new GeneralPath();
//			final AffineTransform atPos = new AffineTransform();
			final float dy;
			float dx;
			TextLayout txtLay;
		    
			try {
				while( lbm.getPosition() < styledText.getEndIndex() ) {
					txtLay	= lbm.nextLayout( w );
					y  	   += txtLay.getAscent();
					if( y + txtLay.getDescent() > yStop ) break;
					dx		= (w - txtLay.getVisibleAdvance()) *
						(txtLay.isLeftToRight() ? halign : (1.0f - halign));
					
					atPos.setToTranslation( x + dx, y );
					gpStr.append( txtLay.getOutline( atPos ), false );
					y 	   += txtLay.getDescent() + txtLay.getLeading();
				}
				dy = (yStop - y) * valign;
				if( dy != 0f ) gpStr.transform( AffineTransform.getTranslateInstance( 0, dy ));

				recCmds.add( new CmdFill( gc.at.createTransformedShape( gpStr )));

				return off;
			} finally {
				gpStr.reset();
			}
		}
	}

	private class ConstrPush
	extends Constr
	{
		protected ConstrPush() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			context.push( gc );
			gc = new GraphicsContext( gc );
			return off;
		}
	}

	private class ConstrPop
	extends Constr
	{
		protected ConstrPop() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			gc = (GraphicsContext) context.pop();
			recCmds.add( new CmdRestore( gc ));
			return off;
		}
	}

	// 0: on/off
	private class ConstrSmooth
	extends Constr
	{
		protected ConstrSmooth() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
//			final boolean	onOff;
			final Map		hints;

			if( ((Number) cmd[ off++ ]).intValue() != 0 ) {
				hints	= antiAliasOn;
			} else {
				hints	= antiAliasOff;
			}
			
			gc.hints	= hints;
//			key			= RenderingHints.KEY_RENDERING;
//			value		= onOff ? RenderingHints.VALUE_RENDER_QUALITY : RenderingHints.VALUE_RENDER_SPEED;
//			key			= RenderingHints.KEY_DITHERING;
//			value		= onOff ? RenderingHints.VALUE_DITHER_ENABLE : RenderingHints.VALUE_DITHER_DISABLE;
//			key			= RenderingHints.KEY_COLOR_RENDERING;
//			value		= onOff ? RenderingHints.VALUE_COLOR_RENDER_QUALITY : RenderingHints.VALUE_COLOR_RENDER_SPEED;
			recCmds.add( new CmdHints( hints ));
			return off;
		}
	}

	// 0: opacity
	private class ConstrAlpha
	extends Constr
	{
		protected ConstrAlpha() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			final float opacity = ((Number) cmd[ off++ ]).floatValue();
			final Composite c;
			if( opacity == 1f ) {
				c = AlphaComposite.SrcOver;
			} else {
				c = AlphaComposite.getInstance( AlphaComposite.SRC_OVER, opacity );
			}
			gc.comp = c;
			recCmds.add( new CmdComposite( c ));
			return off;
		}
	}

	private abstract class ConstrAbstractImage
	extends Constr
	{
		protected ConstrAbstractImage() { /* empty */ }

		protected Image getImage( Object id )
		{
			final Object		img;
			final SwingOSC		osc;
			final SwingClient	client;

			osc		= SwingOSC.getInstance();
			client	= osc.getCurrentClient();
			img		= client.getObject( id );
			if( (img == null) || !(img instanceof Image) ) {
				System.out.println( "ERROR: Pen: image '" + id + "' not found" );
				return null;
			}
			return (Image) img;
		}
	}

	// 0: id, 1: x, 2: y
	private class ConstrImage
	extends ConstrAbstractImage
	{
		protected ConstrImage() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			final Object 		id;
			final float			x, y;
			final Image			img;
			
			id		= cmd[ off++ ];
			off		= decode( cmd, off, 2 );
			x		= pt[ 0 ];
			y		= pt[ 1 ];
			img		= getImage( id );
			if( img != null ) {
				recCmds.add( new CmdImage( img, x, y ));
			}
			return off;
		}
	}

	// 0: id, 1: dx, 2: dy, 3: sx, 4: sy, 5: w, 6: h
	private class ConstrCroppedImage
	extends ConstrAbstractImage
	{
		protected ConstrCroppedImage() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			final Object 		id;
			final float			dx, dy, sx, sy, w, h;
			final Image			img;
			
			id		= cmd[ off++ ];
			off		= decode( cmd, off, 6 );
			dx		= pt[ 0 ];
			dy		= pt[ 1 ]; 
			sx		= pt[ 2 ];
			sy		= pt[ 3 ];
			w		= pt[ 4 ];
			h		= pt[ 5 ];
			img		= getImage( id );
			if( img != null ) {
				recCmds.add( new CmdImage( img, dx, dy, sx, sy, w, h ));
			}
			return off;
		}
	}

	// 0: id
	private class ConstrPaint
	extends Constr
	{
		protected ConstrPaint() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			final Object		id, pnt;
			final SwingOSC		osc;
			final SwingClient	client;

			osc		= SwingOSC.getInstance();
			client	= osc.getCurrentClient();
			id		= cmd[ off++ ];
			pnt		= client.getObject( id );
			if( (pnt == null) || !(pnt instanceof Paint) ) {
				System.out.println( "ERROR: Pen: paint '" + id + "' not found" );
			} else {
				gc.pntFill = (Paint) pnt;
			}
			return off;
		}
	}

	// 0: id
	private class ConstrFillAxialGrad
	extends Constr
	{
		protected ConstrFillAxialGrad() { /* empty */ }

		protected int constr( Object[] cmd, int off )
		{
			final Color	colr1, colr2;
			final Paint	pntOld, pnt;

			off			= decode( cmd, off, 4 );
			colr1		= getColor( cmd, off );
			off		   += 4;
			colr2		= getColor( cmd, off );
			off		   += 4;
			
			pnt			= new GradientPaint( pt[ 0 ], pt[ 1 ], colr1, pt[ 2 ], pt[ 3 ], colr2 );
			pntOld		= gc.pntFill; 
			gc.pntFill	= pnt;
			recCmds.add( new CmdFill( gp ));
			gp			= new GeneralPath();
			gc.pntFill	= pntOld;

			return off;
		}
	}
}