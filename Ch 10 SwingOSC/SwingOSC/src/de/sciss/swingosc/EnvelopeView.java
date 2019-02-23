/*
 *  EnvelopeView.java
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
 *		05-Feb-07	created
 *		26-Jul-07	added support for connection shapes, added horizEditMode
 *		04-Apr-09	completed and fixed shapes drawing
 */

package de.sciss.swingosc;

import java.awt.BasicStroke;
import java.awt.Color;
//import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;

import javax.swing.event.MouseInputAdapter;

import de.sciss.net.OSCMessage;

/**
 *	Swing implementation and extension of SCEnvelopeView by Jan Truetzschler.
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.61, 23-Apr-09
 */
public class EnvelopeView
extends AbstractMultiSlider
{
	/**
	 * 	Horizontal editing mode: Points can be freely moved unconstrainted
	 */
	public static final int			HEDIT_FREE			= 0;
	/**
	 * 	Horizontal editing mode: Points can be moved only between adjectant nodes
	 */
	public static final int			HEDIT_CLAMP			= 1;
	/**
	 * 	Horizontal editing mode: Points can be freely moved, but the node indices
	 *	are relayed in order to maintain "causality"
	 */
	public static final int			HEDIT_RELAY			= 2;
	
	protected Node[] 				nodes			= new Node[ 0 ];
	// when setValues is called and the number of values
	// increases, the new nodes are created from the prototype
	// (thumb size + colour)
	private final Node				protoNode		= new Node( -1 );
	
	protected boolean 				clipThumbs;
	
	private Color					selectionColor	= Color.black;
	protected int					recentWidth		= -1;
	protected int					recentHeight	= -1;
	
	private int						index 			= -1;
	
	private boolean					connectionsUsed = false;
	protected int					horizEditMode	= HEDIT_FREE;
	protected boolean				lockBounds		= false;
	
//	private boolean					shiftMode		= false;
	
	private static final Stroke[]	strkRubber;
	private int						strkRubberIdx	= 0;
	protected final Rectangle		rubberRect		= new Rectangle();
	private static final Color		colrRubber		= new Color( 0x40, 0x40, 0x40 );
	
	// helper constants for shape calculation
	private static final float		EXPM1			= (float) Math.exp(-1);
	private static final float		EXPM1R			= (float) (1.0 - Math.exp(-1));

	private Node[] 					dirtyNodes		= new Node[ 0 ];
	private int						numDirty		= 0;
	protected int					lastIndex		= -1;
		
	static {
		final float[] dash = new float[] { 4f, 4f };
		strkRubber = new Stroke[ 8 ];
		for( int i = 0; i < 8; i++ ) {
			strkRubber[ i ] = new BasicStroke( 1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10f, dash, i );
		}
	}
	
	public EnvelopeView()
	{
		this( true );
	}

	public EnvelopeView( boolean clipThumbs )
	{
		super();
		this.clipThumbs = clipThumbs;
		drawLines		= true;

		final MouseAdapter ma = new MouseAdapter();
		addMouseListener( ma );
		addMouseMotionListener( ma );
	}
	
	public void setHorizontalEditMode( int mode )
	{
		horizEditMode = mode;
	}
	
	public void setLockBounds( boolean onOff ) {
		lockBounds = onOff;
	}

	protected void paintKnob( Graphics2D g2, int cw, int ch )
	{
//		final AffineTransform	atOrig	= g2.getTransform();
		final Shape				clipOrig	= g2.getClip();
		final int				h, w;
		final int 				numValues		= nodes.length;
		final boolean			reallyDrawLines = drawLines && hasStroke && (numValues > 0);
		final GeneralPath		gpLines;
		final GeneralPath		gpOutline;
		final boolean			invalidAll		= (cw != recentWidth) || (ch != recentHeight);
		final boolean			drawORects		= drawRects && hasStroke;
		final FontMetrics		fm				= g2.getFontMetrics();
		float					bx, by, thumbWidthH, thumbHeightH;
		Node					n, n2;
		Shape					subClip;
		
		if( invalidAll ) {
			recentWidth		= cw;
			recentHeight	= ch;
		}
		
//		g2.setColor( getBackground() );
//		g2.fillRect( 0, 0, getWidth(), getHeight() );
//		g2.clearRect( 0, 0, getWidth(), getHeight() );

		cw -= 2;
		ch -= 2;
		g2.translate( 1, 1 );
		g2.clipRect( 0, 0, cw, ch );
//		cw -= 2;
//		ch -= 2;
		
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		
		w	= cw; // getWidth() - 2;
		h	= ch; // getHeight() - 2;

		gpLines 	= reallyDrawLines ? new GeneralPath() : null;
		gpOutline	= (drawRects && hasStroke) ? new GeneralPath() : null;

		for( int i = 0; i < numValues; i++ ) {
			n = nodes[ i ];
			if( invalidAll || n.invalid ) {
				thumbWidthH		= n.thumbWidth / 2;
				thumbHeightH	= n.thumbHeight / 2;
				if( clipThumbs ) {
					n.cx	= w * n.x;
					n.cy	= h * (1f - n.y);
					bx		= n.cx - thumbWidthH;
					by		= n.cy - thumbHeightH;
				} else {
					bx		= (w - n.thumbWidth) * n.x;
					by		= (h - n.thumbHeight) * (1f - n.y);
					n.cx	= bx + thumbWidthH;
					n.cy	= by + thumbHeightH;
				}
				n.r.setFrame( bx, by, n.thumbWidth, n.thumbHeight );
				n.rr.setFrame( bx - 0.5f, by - 0.5f, n.thumbWidth, n.thumbHeight );
				
				if( n.label != null ) {
					n.tx = n.cx - fm.stringWidth( n.label ) / 2;
					n.ty = n.cy + (fm.getAscent() - fm.getDescent()) / 2 - 1;
				}
				
				n.invalid = false;
			}
			if( drawORects ) {
				gpOutline.append( n.rr, false );
			}
		}
		if( reallyDrawLines ) { // implies numValues >= 1
			if( connectionsUsed ) {	// -------------------- custom connections --------------------
				for( int i = 0; i < numValues; i++ ) {
					n = nodes[ i ];
					switch( n.shape ) {
					case Node.SHP_STEP:
						for( int j = 0; j < n.connections.length; j++ ) {
							n2 = n.connections[ j ];
							gpLines.moveTo( n.cx, n.cy );
							gpLines.lineTo( n.cx, n2.cy );
							gpLines.lineTo( n2.cx, n2.cy );
						}
						break;

					case Node.SHP_LINEAR:
						for( int j = 0; j < n.connections.length; j++ ) {
							n2 = n.connections[ j ];
							gpLines.moveTo( n.cx, n.cy );
							gpLines.lineTo( n2.cx, n2.cy );
						}
						break;

					case Node.SHP_SINE:
						for( int j = 0; j < n.connections.length; j++ ) {
							n2 = n.connections[ j ];
							gpLines.moveTo( n.cx, n.cy );
							gpLines.curveTo( n.cx * EXPM1R + n2.cx * EXPM1, n.cy, n.cx * EXPM1 + n2.cx * EXPM1R, n2.cy, n2.cx, n2.cy );
						}
						break;
	
					case Node.SHP_EXPONENTIAL:
					case Node.SHP_CURVE:
					case Node.SHP_WELCH:
					case Node.SHP_SQUARED:
					case Node.SHP_CUBED:
						for( int j = 0; j < n.connections.length; j++ ) {
							n2 = n.connections[ j ];
							final float oy1, oy2, sy1, sy2;
//							sx	= 1.0f / Math.max( 1, w );
//							final int   istep;
							final Node	n1s, n2s;
							if( n.x == n2.x ) {
								gpLines.moveTo( n.cx, n.cy );
								gpLines.lineTo( n2.cx, n2.cy );
								break;
							} else if( n.x < n2.x ) {
//								istep	= 2;
								n1s		= n;
								n2s		= n2;
							} else {
//								istep = -2;
								n1s		= n2;
								n2s		= n;
							}
							final float dx = n2s.cx - n1s.cx;
							if( clipThumbs ) {
								oy1 = 0f;
								oy2	= 0f;
								sy1	= h;
								sy2	= h;
							} else {
								oy1 = n1s.thumbHeight / 2;
								oy2	= n2s.thumbHeight / 2;
								sy1	= h - n1s.thumbHeight;
								sy2	= h - n2s.thumbHeight;
							}
							float ix = 0f; // n.cx - n1s.cx;
							float rx, ry;
							gpLines.moveTo( n1s.cx, n1s.cy );
							do {
//								ix = Math.max( 0, Math.min( dx, ix + istep ));
								ix = Math.min( dx, ix + 2 );
								rx = ix / dx;
								ry = (1f - envAt( n1s, n2s, rx ));
								gpLines.lineTo( ix + n1s.cx, (sy1 * ry + oy1) * (1 - rx) +
								                             (sy2 * ry + oy2) * rx );
							} while( ix != dx );
						}
						break;
					}
				}
			} else {	// -------------------- sequential connections --------------------
				n = nodes[ 0 ];
				for( int i = 1; i < numValues; i++ ) {
					n2 = nodes[ i ];
					switch( n.shape ) {
					case Node.SHP_STEP:
						gpLines.moveTo( n.cx, n.cy );
						gpLines.lineTo( n.cx, n2.cy );
						gpLines.lineTo( n2.cx, n2.cy );
//						n = n2;
						break;
						
					case Node.SHP_LINEAR:
						gpLines.moveTo( n.cx, n.cy );
						gpLines.lineTo( n2.cx, n2.cy );
						break;

					case Node.SHP_EXPONENTIAL:
					case Node.SHP_CURVE:
					case Node.SHP_WELCH:
					case Node.SHP_SQUARED:
					case Node.SHP_CUBED:
//						gpLines.moveTo( n.cx, n.cy );
						final float oy1, oy2, sy1, sy2;
//						sx	= 1.0f / Math.max( 1, w );
//						final int   istep;
						final Node	n1s, n2s;
						if( n.x == n2.x ) {
							gpLines.moveTo( n.cx, n.cy );
							gpLines.lineTo( n2.cx, n2.cy );
							break;
						} else if( n.x < n2.x ) {
//							istep	= 2;
							n1s		= n;
							n2s		= n2;
						} else {
//							istep = -2;
							n1s		= n2;
							n2s		= n;
						}
						final float dx = n2s.cx - n1s.cx;
						if( clipThumbs ) {
							oy1 = 0f;
							oy2	= 0f;
							sy1	= h;
							sy2	= h;
						} else {
							oy1 = n1s.thumbHeight / 2;
							oy2	= n2s.thumbHeight / 2;
							sy1	= h - n1s.thumbHeight;
							sy2	= h - n2s.thumbHeight;
						}
						float ix = 0f; // n.cx - n1s.cx;
						float rx, ry;
						gpLines.moveTo( n1s.cx, n1s.cy );
						do {
//							ix = Math.max( 0, Math.min( dx, ix + istep ));
							ix = Math.min( dx, ix + 2 );
							rx = ix / dx;
							ry = (1f - envAt( n1s, n2s, rx ));
							gpLines.lineTo( ix + n1s.cx, (sy1 * ry + oy1) * (1 - rx) +
							                             (sy2 * ry + oy2) * rx );
						} while( ix != dx );
						break;

					case Node.SHP_SINE:
						gpLines.moveTo( n.cx, n.cy );
						gpLines.curveTo( n.cx * EXPM1R + n2.cx * EXPM1, n.cy, n.cx * EXPM1 + n2.cx * EXPM1R, n2.cy, n2.cx, n2.cy );
						break;

//					case Node.SHP_WELCH:
//						// approximate... WRONG
//						gpLines.moveTo( n.cx, n.cy );
//						if( n.y < n2.y ) {
//							gpLines.curveTo( n.cx * 0.556f + n2.cx * 0.444f, n.cy * 0.278f + n2.cy * 0.722f, n.cx * 0.29f + n2.cx * 0.71f, n2.cy, n2.cx, n2.cy );
//						} else {
//							gpLines.curveTo( n2.cx * 0.29f + n.cx * 0.71f, n.cy, n2.cx * 0.556f + n.cx * 0.444f, n2.cy * 0.278f + n.cy * 0.722f, n2.cx, n2.cy );
//						}
//						break;

//					case Node.SHP_SQUARED:
//						// approximate... WRONG
//						gpLines.moveTo( n.cx, n.cy );
//						if( n.y < n2.y ) {
//							gpLines.curveTo( n.cx * 0.9f + n2.cx * 0.1f, n.cy, n.cx * 0.5f + n2.cx * 0.5f, n.cy, n2.cx, n2.cy );
//						} else {
//							gpLines.curveTo( n2.cx * 0.5f + n.cx * 0.5f, n2.cy, n2.cx * 0.9f + n.cx * 0.1f, n2.cy, n2.cx, n2.cy );
//						}
//						break;

//					case Node.SHP_CUBED:
//						// approximate... WRONG
//						gpLines.moveTo( n.cx, n.cy );
//						if( n.y < n2.y ) {
//							gpLines.curveTo( n.cx * 0.6667f + n2.cx * 0.3333f, n.cy, n.cx * 0.3333f + n2.cx * 0.6667f, n.cy, n2.cx, n2.cy );
//						} else {
//							gpLines.curveTo( n2.cx * 0.3333f + n.cx * 0.6667f, n2.cy, n2.cx * 0.6667f + n.cx * 0.3333f, n2.cy, n2.cx, n2.cy );
//						}
//						break;
					}
					n = n2;
				}
			}
			g2.setColor( strokeColor );
			g2.draw( gpLines );
		}
		if( drawRects ) {
			for( int i = 0; i < numValues; i++ ) {
				n = nodes[ i ];
				if( n.selected ) {
					g2.setColor( selectionColor );
					g2.fill( n.r );
				} else {
					if( n.fillColor != null ) {
						g2.setColor( n.fillColor );
						g2.fill( n.r );
					}
				}
				if( n.label !=  null ) {
					g2.setColor( strokeColor );
					subClip = g2.getClip();
					g2.clip( n.r );
					g2.drawString( n.label, n.tx, n.ty );
					g2.setClip( subClip );
				}
			}
			if( hasStroke ) {
				g2.setColor( strokeColor );
				g2.draw( gpOutline );
			}
		}
//			if( showIndex && (selectedIndex >= 0) && (n == 0) ) {
//				r.setFrame( (selectedIndex - startIndex) * xStep, 0, selectionSize * xStep - xOffset, h );
//				g2.setColor( indexColor );
//				g2.fill( r );
//			}
//		g2.setTransform( atOrig );
		
		if( !rubberRect.isEmpty() ) {
// XOR-mode not working properly on Linux / Windows !!
//			final Composite cmpOrig		= g2.getComposite();
			final Stroke	strkOrig	= g2.getStroke();
//			g2.setXORMode( colrRubber );
			g2.setColor( colrRubber );
			g2.setStroke( strkRubber[ (strkRubberIdx++) & 0x07 ]);
			g2.draw( rubberRect );
			g2.setStroke( strkOrig );
//			g2.setComposite( cmpOrig );
		}
		
		g2.setClip( clipOrig );
	}
	
	public void setClipThumbs( boolean onOff )
	{
		if( onOff != clipThumbs ) {
			clipThumbs = onOff;
			recentWidth	 = -1; // triggers re-calculations
			recentHeight = -1;
			repaint();
		}
	}
	
	public void setIndex( int idx )
	{
		index = idx;
	}
	
	public int getLastIndex()
	{
		return lastIndex;
	}
	
	public void setX( float x )
	{
		if( (index >= 0) && (index < nodes.length) ) {
			nodes[ index ].x = x;
			nodes[ index ].invalid = true;
			repaint();
		}
	}
	
	public void setY( float y )
	{
		if( (index >= 0) && (index < nodes.length) ) {
			nodes[ index ].y = y;
			nodes[ index ].invalid = true;
			repaint();
		}
	}
	
//	public void setSelectedIndex( int idx )
//	{
//		if( idx != selectedIndex ) {
//			selectedIndex = idx;
//			repaint();
//		}
//	}
	
	public void setSelectionColor( Color c )
	{
		selectionColor = c;
	}
	
	public void setLabel( int index, String label )
	{
		if( index == -1 ) {
			for( int i = 0; i < nodes.length; i++ ) {
				nodes[ i ].label = label;
				nodes[ i ].invalid = true;
			}
		} else {
			nodes[ index ].label = label;
			nodes[ index ].invalid = true;
		}
//		protoNode.label = label;
		repaint();
	}

	public void setShape( int index, int shape, float curve )
	{
		if( index == -1 ) {
			for( int i = 0; i < nodes.length; i++ ) {
				nodes[ i ].shape = shape;
				nodes[ i ].curve = curve;
//				nodes[ i ].invalid = true;
			}
		} else {
			nodes[ index ].shape = shape;
			nodes[ index ].curve = curve;
//			nodes[ index ].invalid = true;
		}
		protoNode.shape = shape;
		protoNode.curve = curve;
		repaint();
	}

	public void sendDirtyValues( Object id )
	{
		final SwingOSC		osc			= SwingOSC.getInstance();
		final SwingClient	client		= osc.getCurrentClient();
		final Object[] 		replyArgs	= new Object[ 2 + (numDirty * 4) ];
		Node n;
		replyArgs[ 0 ]					= id;
		replyArgs[ 1 ]					= new Integer( numDirty );

		for( int i = 0, j = 2; i < numDirty; i++ ) {
			n = dirtyNodes[ i ];
			replyArgs[ j++ ] = new Integer( n.idx );
			replyArgs[ j++ ] = new Float( n.x );
			replyArgs[ j++ ] = new Float( n.y );
			replyArgs[ j++ ] = new Integer( n.selected ? 1 : 0 );
		}
		
		try {
			client.reply( new OSCMessage( "/values", replyArgs ));
		}
		catch( IOException ex ) {
			SwingOSC.printException( ex, "sendDirtyValues" );
		}
	}
	
	public void sendDirtyValuesAndClear( Object id )
	{
		sendDirtyValues( id );
		clearDirty();
	}

	public void setStepSize( float stepSize )
	{	
		super.setStepSize( stepSize );
		
		Node n;
		
		if( stepSize > 0f ) {
			for( int i = 0; i < nodes.length; i++ ) {
				n = nodes[ i ];
				n.x = snap( n.x );
				n.y = snap( n.y );
				n.invalid = true;
			}
			repaint();
		}
	}
	
	public void setReadOnly( int index, boolean readOnly )
	{
		if( index == -1 ) {
			for( int i = 0; i < nodes.length; i++ ) {
				nodes[ i ].readOnly = readOnly;
			}
		} else {
			nodes[ index ].readOnly = readOnly;
		}
//		protoNode.readOnly = readOnly;
	}

	public void setSelected( int index, boolean selected )
	{
		if( index == -1 ) {
			for( int i = 0; i < nodes.length; i++ ) {
				nodes[ i ].selected = selected;
			}
		} else {
			nodes[ index ].selected = selected;
		}
		if( drawRects ) repaint();
	}

	public void setFillColor( int index, Color c )
	{
		if( c.getAlpha() == 0 ) c = null;
		if( index == -1 ) {
			for( int i = 0; i < nodes.length; i++ ) {
				nodes[ i ].fillColor = c;
			}
		} else {
			nodes[ index ].fillColor = c;
		}
		protoNode.fillColor = c;
		if( drawRects ) repaint();
	}

	public void setStrokeColor( Color c )
	{
		strokeColor	= c;
		repaint();
	}

	public void setThumbSize( int index, float size )
	{
		if( index == -1 ) {
			for( int i = 0; i < nodes.length; i++ ) {
				nodes[ i ].thumbWidth = size;
				nodes[ i ].thumbHeight = size;
				nodes[ i ].invalid = true;
			}
		} else {
			nodes[ index ].thumbWidth = size;
			nodes[ index ].thumbHeight = size;
			nodes[ index ].invalid = true;
		}
		protoNode.thumbWidth = size;
		protoNode.thumbHeight = size;
		repaint();
	}
	
	public void setThumbWidth( int index, float w )
	{
		if( index == -1 ) {
			for( int i = 0; i < nodes.length; i++ ) {
				nodes[ i ].thumbWidth = w;
				nodes[ i ].invalid = true;
			}
		} else {
			nodes[ index ].thumbWidth = w;
			nodes[ index ].invalid = true;
		}
		protoNode.thumbWidth = w;
		repaint();
	}

	public void setThumbHeight( int index, float h )
	{
		if( index == -1 ) {
			for( int i = 0; i < nodes.length; i++ ) {
				nodes[ i ].thumbHeight = h;
				nodes[ i ].invalid = true;
			}
		} else {
			nodes[ index ].thumbHeight = h;
			nodes[ index ].invalid = true;
		}
		protoNode.thumbHeight = h;
		repaint();
	}

	public void setValues( float[] x, float[] y )
	{
		final int	newNumVals = x.length;
		final int	oldNumVals = nodes.length;
		final int	minNum;
		Node[]		tmp;
		
		if( y.length != newNumVals ) throw new IllegalArgumentException();
		
//		if( selectedIndex >= 0 ) {
//			selectedIndex = Math.min( selectedIndex, newNumVals - 1 );
//			selectionSize = Math.min( selectionSize, newNumVals - selectedIndex );
//		}
		
		if( oldNumVals != newNumVals ) {
			tmp			= new Node[ newNumVals ];
			minNum		= Math.min( oldNumVals, newNumVals );
			System.arraycopy( nodes, 0, tmp, 0, minNum );
			for( int i = minNum; i < newNumVals; i++ ) {
				tmp[ i ] = new Node( i, protoNode );
			}
			nodes		= tmp;
			tmp			= new Node[ newNumVals ];
			System.arraycopy( dirtyNodes, 0, tmp, 0, minNum );
			dirtyNodes	= tmp;
			numDirty	= Math.min( numDirty, newNumVals ); 
		}
		
		for( int i = 0; i < newNumVals; i++ ) {
			nodes[ i ].x = x[ i ];
			nodes[ i ].y = y[ i ];
			nodes[ i ].invalid = true;
		}

		repaint();
	}

	public void setValues( float[] x, float[] y, int[] shapes, float[] curves )
	{
		final int	newNumVals = x.length;
		final int	oldNumVals = nodes.length;
		final int	minNum;
		Node[]		tmp;
		
		if( y.length != newNumVals ) throw new IllegalArgumentException();
		
//		if( selectedIndex >= 0 ) {
//			selectedIndex = Math.min( selectedIndex, newNumVals - 1 );
//			selectionSize = Math.min( selectionSize, newNumVals - selectedIndex );
//		}
		
		if( oldNumVals != newNumVals ) {
			tmp			= new Node[ newNumVals ];
			minNum		= Math.min( oldNumVals, newNumVals );
			System.arraycopy( nodes, 0, tmp, 0, minNum );
			for( int i = minNum; i < newNumVals; i++ ) {
				tmp[ i ] = new Node( i, protoNode );
			}
			nodes		= tmp;
			tmp			= new Node[ newNumVals ];
			System.arraycopy( dirtyNodes, 0, tmp, 0, minNum );
			dirtyNodes	= tmp;
			numDirty	= Math.min( numDirty, newNumVals ); 
		}
		
		for( int i = 0; i < newNumVals; i++ ) {
			nodes[ i ].x		= x[ i ];
			nodes[ i ].y		= y[ i ];
			nodes[ i ].shape	= shapes[ i ];
			nodes[ i ].curve	= curves[ i ];
			nodes[ i ].invalid	= true;
		}

		repaint();
	}

	public void setValues( Object[] x, Object[] y )
	{
		final float[] fx = new float[ x.length ];
		final float[] fy = new float[ y.length ];
		for( int i = 0; i < fx.length; i++ ) {
			fx[ i ] = ((Number) x[ i ]).floatValue();
			fy[ i ] = ((Number) y[ i ]).floatValue();
		}
		setValues( fx, fy );
	}

	public void setValues( Object[] x, Object[] y, Object[] shapes, Object[] curves )
	{
		final float[]	fx		= new float[ x.length ];
		final float[]	fy		= new float[ y.length ];
		final int[]		ishapes	= new int[ shapes.length ];
		final float[]	fcurves	= new float[ curves.length ];
		for( int i = 0; i < fx.length; i++ ) {
			fx[ i ]			= ((Number) x[ i ]).floatValue();
			fy[ i ] 		= ((Number) y[ i ]).floatValue();
			ishapes[ i ]	= ((Number) shapes[ i ]).intValue();
			fcurves[ i ]	= ((Number) curves[ i ]).floatValue();
		}
		setValues( fx, fy, ishapes, fcurves );
	}

	public void setValues( Object[] x, Object[] y, int shape, float curve )
	{
		final float[]	fx		= new float[ x.length ];
		final float[]	fy		= new float[ y.length ];
		final int[]		shapes	= new int[ x.length ];
		final float[]	curves	= new float[ x.length ];
		for( int i = 0; i < fx.length; i++ ) {
			fx[ i ] 	= ((Number) x[ i ]).floatValue();
			fy[ i ] 	= ((Number) y[ i ]).floatValue();
			shapes[ i ]	= shape;
			curves[ i ]	= curve;
		}
		setValues( fx, fy, shapes, curves );
	}

//	public float[] getValues()
//	{
//		return values;
//	}
	
	public void setConnections( int index, int[] targets )
	{
		final Node[] nTargets = new Node[ targets.length ];
		for( int i = 0; i < targets.length; i++ ) {
			nTargets[ i ] = nodes[ targets[ i ]];
		}
		nodes[ index ].connections = nTargets;
		if( drawLines && connectionsUsed ) repaint();
	}
	
	public void setConnections( int index, Object[] targets )
	{
		final int[] iTargets = new int[ targets.length ];
		for( int i = 0; i < targets.length; i++ ) {
			iTargets[ i ] = ((Number) targets[ i ]).intValue();
		}
		setConnections( index, iTargets );
	}
	
	public void setConnectionsUsed( boolean onOff )
	{
		if( onOff != connectionsUsed ) {
			connectionsUsed = onOff;
			repaint();
		}
	}
	
	public int getDirtySize()
	{
		return( numDirty );
	}
	
	public void clearDirty()
	{
		for( int i = 0; i < numDirty; i++ ) {
			dirtyNodes[ i ].dirty = false;
		}
		numDirty = 0;
	}

	protected void dirty( Node n )
	{
		if( !n.dirty ) {
			n.dirty = true;
			dirtyNodes[ numDirty++ ] = n;
		}
	}
	
	/*
	 * 	Analoguous to the code in PyrArrayPrimitives.cpp.
	 * 	
	 * 	@param	x	a relative position between 0.0 (at n1)
	 * 				and 1.0 (at n2)
	 */
	private float envAt( Node n1, Node n2, float pos )
	{
		if( nodes.length == 0 ) return 0f;
		
		final float /* pos, */ y;
		
//		for( idx = 0; idx < nodes.length; idx++ ) {
//			if( nodes[ idx ].x > x ) break;
//		}
//		n1	= nodes[ Math.max( 0, idx - 1 )];
//		n2	= nodes[ Math.min( nodes.length - 1, idx )];
//		if( n1.x < n2.x ) {
//			tmp	= n1;
//			n1	= n2;
//			n2	= tmp;
//		}
		
//		if( x <= n1.x ) return n1.y;
//		if( x >= n2.x ) return n2.y;
//		pos	= (x - n1.x) / (n2.x - n1.x);
		if( pos <= 0f ) return n1.y;
		if( pos >= 1f ) return n2.y;

		switch( n1.shape ) {
		case Node.SHP_STEP:
			y = n2.y;
			break;
			
		case Node.SHP_LINEAR:
		default:
			y = pos * (n2.y - n1.y) + n1.y;
			break;
			
		case Node.SHP_EXPONENTIAL:
			final float y1Lim = Math.max( 0.0001f, n1.y );
			y = (float) (y1Lim * Math.pow( n2.y / y1Lim, pos ));
			break;
			
		case Node.SHP_SINE:
			y = (float) (n1.y + (n2.y - n1.y) * (-Math.cos( Math.PI * pos ) * 0.5 + 0.5));
			break;
			
		case Node.SHP_WELCH:
			if( n1.y < n2.y ) {
				y = (float) (n1.y + (n2.y - n1.y) * Math.sin( Math.PI * 0.5 * pos ));
			} else { 
				y = (float) (n2.y - (n2.y - n1.y) * Math.sin( Math.PI * 0.5 * (1 - pos) ));
			}
			break;
			
		case Node.SHP_CURVE:
			if( Math.abs( n1.curve ) < 0.0001f ) {
				y = pos * (n2.y - n1.y) + n1.y;
			} else {
				final double denom	= 1.0 - Math.exp( n1.curve );
				final double numer	= 1.0 - Math.exp( pos * n1.curve );
				y = (float) (n1.y + (n2.y - n1.y) * (numer / denom));
			}
			break;
			
		case Node.SHP_SQUARED:
			final double y1Pow2	= Math.sqrt( n1.y );
			final double y2Pow2	= Math.sqrt( n2.y );
			final double yPow2	= pos * (y2Pow2 - y1Pow2) + y1Pow2;
			y = (float) (yPow2 * yPow2);
			break;

		case Node.SHP_CUBED:
			final double y1Pow3	= Math.pow( n1.y, 0.3333333 );
			final double y2Pow3	= Math.pow( n2.y, 0.3333333 );
			final double yPow3	= pos * (y2Pow3 - y1Pow3) + y1Pow3;
			y = (float) (yPow3 * yPow3 * yPow3);
			break;
		}

		return y;
	}

	
// --------------- internal classes ---------------
	
	private class MouseAdapter
	extends MouseInputAdapter
	{
		private boolean			shiftDrag		= false;
		private Point			dragFirstPt		= null;
		private boolean			dragRubber;
		private final Rectangle	oldRubberRect	= new Rectangle();
		private final Rectangle	mouseRect		= new Rectangle(); // e.g. rect around mouse for easier nav

		protected MouseAdapter() { /* empty */ }
		
		private Point insetMouse( MouseEvent e )
		{
			final Insets ins	= getInsets();
			final Point  pt		= e.getPoint();
			
			pt.translate( -(ins.left + 1), -(ins.top + 1) );
			
			return pt;
		}
		
		private Node findNode( Point pt )
		{
			Node n;
			// note that we do a little outline since we check against n.r not n.rr!
			mouseRect.setBounds( pt.x - 1, pt.y - 1, 4, 4 ); 
			
			for( int i = 0; i < nodes.length; i++ ) {
				n = nodes[ i ];
//				if( !n.invalid && n.r.contains( pt )) return n;
				if( !n.invalid && mouseRect.intersects( n.r )) return n;
			}
			return null;
		}
		
//		private Point2D screenToVirtual( Point screen )
//		{
//			
//		}

		public void mousePressed( MouseEvent e )
		{
			if( !isEnabled() || e.isControlDown() ) return;

			requestFocus();
			
//			final Point2D	pt	= screenToVirtual( e.getPoint() );
			dragFirstPt		= insetMouse( e );
			final Node		n		= findNode( dragFirstPt );
			boolean 		repaint = false;
			boolean 		action	= false;
			Node			n2;

			shiftDrag		= e.isShiftDown();
			dragRubber		= n == null;
			
			if( shiftDrag ) {
				if( dragRubber ) return;
				n.selected	= !n.selected;
				dirty( n );
				repaint		= true;
				action		= true;
			} else {
				if( dragRubber || !n.selected ) {
					lastIndex	= -1;
					for( int i = 0; i < nodes.length; i++ ) {
						n2 = nodes[ i ];
						if( (n != n2) && n2.selected ) {
							n2.selected	= false;
							dirty( n2 );
							repaint		= true;
							action		= true;
						}
					}
				}
				if( !dragRubber && !n.selected ) {
					n.selected	= true;
					dirty( n );
					repaint		= true;
					action		= true;
				}
			}
			if( !dragRubber ) {
				lastIndex	= n.idx;
				for( int i = 0; i < nodes.length; i++ ) {
					n2		= nodes[ i ];
					n2.oldX = n2.x;
					n2.oldY = n2.y;
				}
			}
			if( repaint ) repaint();
			if( action ) fireActionPerformed();
		}
		
		public void mouseReleased( MouseEvent e )
		{
			dragFirstPt = null;
// macht das sinn?
//			lastIndex	= -1;
			if( dragRubber ) {
				rubberRect.setBounds( 0, 0, 0, 0 );
				repaint();
			}
		}
		
//private boolean korrupt = false;
		
		public void mouseDragged( MouseEvent e )
		{
//if( korrupt ) return;
			if( !isEnabled() || (dragFirstPt == null) ) return;
			
			final Point dragCurrentPt = insetMouse( e );
//			final int idxStart, idxInc;
			Node n, n2, nPred = null, nSucc;
			boolean repaint	= false;
			boolean action	= false;
			boolean reallyNotLocked;
			
			if( dragRubber ) {		// ------------------ rubber band ------------------
				oldRubberRect.setBounds( rubberRect );
				rubberRect.setFrameFromDiagonal( dragFirstPt, dragCurrentPt );
				for( int i = 0; i < nodes.length; i++ ) {
					n = nodes[ i ];
					if( n.invalid ) continue;
					if( oldRubberRect.intersects( n.r ) != rubberRect.intersects( n.r )) {
						n.selected = !n.selected;
						dirty( n );
						action	= true;
					}
				}
				repaint = true;
			} else if( (recentWidth > 0) && (recentHeight > 0) ) {		// ------------------ move ------------------
				final float dx, dy;
				float x, y;
				if( clipThumbs ) {
					dx = (float) (dragCurrentPt.x - dragFirstPt.x) / recentWidth;
					dy = (float) (dragFirstPt.y - dragCurrentPt.y) / recentHeight;
				} else {
					dx = dragCurrentPt.x - dragFirstPt.x;
					dy = dragFirstPt.y - dragCurrentPt.y;
				}
//				if( dx >= 0f ) {
//					idxStart = 0;
//					idxInc   = 1;
//				} else {
//					idxStart = nodes.length - 1;
//					idxInc   = -1;
//				}
				final Node[] nodesBak = new Node[ nodes.length ];
				for( int i = 0; i < nodes.length; i++ ) {
					nodes[ i ].done = false;
					nodesBak[ i ] = new Node( nodes[ i ].idx, nodes[ i ]);
					nodesBak[ i ].x = nodes[ i ].x;
					nodesBak[ i ].y = nodes[ i ].y;
					nodesBak[ i ].selected = nodes[ i ].selected;
				}
				for( int i = 0; i < nodes.length; i++ ) {
					n = nodes[ i ];
					if( !n.selected || n.done || n.readOnly ) {
						n.done = true;
						continue;
					}

					reallyNotLocked = !lockBounds || ((i > 0) && (i < nodes.length - 1));
					if( clipThumbs ) {
						if( reallyNotLocked ) {
							x	= snap( Math.max( 0f, Math.min( 1f, n.oldX + dx )));
						} else {
							x	= n.x;
						}
						y		= snap( Math.max( 0f, Math.min( 1f, n.oldY + dy )));
					} else {
						if( reallyNotLocked ) {
							x	= snap( Math.max( 0f, Math.min( 1f, n.oldX +
									dx / Math.max( 1, recentWidth - n.thumbWidth ))));
						} else {
							x	= n.x;
						}
						y		= snap( Math.max( 0f, Math.min( 1f, n.oldY +
									dy / Math.max( 1, recentHeight - n.thumbHeight ))));
					}
					n2 = n;
					if( reallyNotLocked && (horizEditMode != HEDIT_FREE) ) {
						if( horizEditMode == HEDIT_CLAMP ) {	///////////////////
							if( x < n.x ) {
								for( int j = i - 1; j >= 0; j-- ) {
									if( !nodes[ j ].selected ) {
										nPred	= nodes[ j ];
										x = Math.max( x, nPred.x );
										break;
									}
								}
							} else if( x > n.x ) {
								for( int j = i + 1; j < nodes.length; j++ ) {
									if( !nodes[ j ].selected ) {
										nSucc	= nodes[ j ];
										x		= Math.min( x, nSucc.x );
										break;
									}
								}
							} // else nothing (no x change)
							
						} else if( horizEditMode == HEDIT_RELAY ) {	////////////////
							int pos = i;
							for( int j = i + 1; j < nodes.length; j++ ) {
								if( !nodes[ j ].selected || nodes[ j ].readOnly ) {
									if( nodes[ j ].x >= x ) break;
									pos = j;
								}
							}
							if( pos > i ) {	// shift left
								nPred = n;
								final float oldX = n.oldX;
								final float oldY = n.oldY;
								for( int j = i + 1; j <= pos; j++ ) {
									nSucc 		= nodes[ j ];
									nPred.x		= nSucc.x;
									nPred.y		= nSucc.y;
									nPred.selected = nSucc.selected;
									nPred.oldX	= nSucc.oldX;
									nPred.oldY	= nSucc.oldY;
									dirty( nPred );
									nPred.invalid	= true;
									nPred		= nSucc;
								}
								nPred.x			= x;
								nPred.y			= y;
								nPred.selected	= true;
								nPred.oldX		= oldX;
								nPred.oldY		= oldY;
								nPred.done		= true;
								dirty( nPred );
								nPred.invalid	= true;
								repaint			= true;
								action			= true;
								n2				= nPred;
								
							} else { // check backwards
								for( int j = i - 1; j >= 0; j-- ) {
									if( !nodes[ j ].selected || nodes[ j ].readOnly ) {
										if( nodes[ j ].x <= x ) break;
										pos = j;
									}
								}
								if( pos < i ) {	// shift right
									nSucc = n;
									final float oldX = n.oldX;
									final float oldY = n.oldY;
									for( int j = i - 1; j >= pos; j-- ) {
										nPred 		= nodes[ j ];
										nSucc.x		= nPred.x;
										nSucc.y		= nPred.y;
										nSucc.selected = nPred.selected;
										nSucc.oldX	= nPred.oldX;
										nSucc.oldY	= nPred.oldY;
										dirty( nSucc );
										nSucc.invalid	= true;
										nSucc		= nPred;
									}
									nSucc.x			= x;
									nSucc.y			= y;
									nSucc.selected	= true;
									nSucc.oldX		= oldX;
									nSucc.oldY		= oldY;
									nSucc.done		= true;
									dirty( nSucc );
									nSucc.invalid	= true;
									repaint			= true;
									action			= true;
									n2				= nSucc;
								}
							}
						}
					}
					if( (x != n2.x) || (y != n2.y) ) {
						n2.x		= x;
						n2.y		= y;
						dirty( n2 );
						n2.invalid	= true;
						repaint		= true;
						action		= true;
					}
				}

//				if( !korrupt ) {
//					for( int ii = 1; ii < nodes.length; ii++ ) {
//						if( nodes[ ii ].x < nodes[ ii - 1 ].x ) {
//							System.out.println( "KORRUPT. Bak = " );
//							korrupt = true;
//							for( int jj = 0; jj < nodes.length; jj++ ) {
//								System.out.println( "" + jj + " (" + nodesBak[ jj ].x + ", " + nodesBak[ jj ].y + ") " + nodesBak[ jj ].selected );
//							}
//							System.out.println( "         New = " );
//							for( int jj = 0; jj < nodes.length; jj++ ) {
//								System.out.println( "" + jj + " (" + nodes[ jj ].x + ", " + nodes[ jj ].y + ")" + nodes[ jj ].selected );
//							}
//						}
//					}
//				}

			}

			if( repaint ) repaint();
			if( action ) fireActionPerformed();

//			if( selectedIndex != -1 ) {
//				nodeJump( e );
//			}
//			processMouse( e, false );
		}
	}

	private static class Node
	{
		private static final Node[]		NO_CONNECTIONS	= new Node[ 0 ];
		
		private static final int		SHP_STEP		= 0;
		private static final int		SHP_LINEAR		= 1;
		private static final int		SHP_EXPONENTIAL	= 2;
		private static final int		SHP_SINE		= 3;
		private static final int		SHP_WELCH		= 4;
		private static final int		SHP_CURVE		= 5;
		private static final int		SHP_SQUARED		= 6;
		private static final int		SHP_CUBED		= 7;
		
		protected float					x				= 0f;
		protected float					y				= 0f;
		protected int					shape			= SHP_LINEAR;
		protected float					curve			= 0f;
		protected Color					fillColor 		= Color.black;
		protected float					thumbWidth 		= 5f; // 12f;
		protected float					thumbHeight		= 5f; // 12f;
		
		protected boolean				readOnly		= false;
		protected boolean				selected		= false;
		protected Node[]				connections		= NO_CONNECTIONS;
		protected String				label			= null;
		protected final Rectangle2D		r				= new Rectangle2D.Float();
		protected final RoundRectangle2D rr				= new RoundRectangle2D.Float( 0f, 0f, 0f, 0f, 2f, 2f );
		protected boolean				invalid			= true;
		protected boolean				done;	// for dnd
		protected float					cx, cy, tx, ty;
		
		// dnd
		protected boolean				dirty			= false;
		protected float					oldX, oldY;
		
		protected final int				idx;
		
		protected Node( int idx )
		{
			this.idx	= idx;
		}
		
		protected Node( int idx, Node orig )
		{
			this.fillColor		= orig.fillColor;
			this.thumbWidth		= orig.thumbWidth;
			this.thumbHeight	= orig.thumbHeight;
			this.shape			= orig.shape;
			this.curve			= orig.curve;
			this.idx			= idx;
		}
	}
}