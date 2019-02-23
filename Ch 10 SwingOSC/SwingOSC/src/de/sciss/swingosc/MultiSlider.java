/*
 *  MultiSlider.java
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
 *		27-Oct-06	created
 *		05-Feb-07	extends AbstractMultiSlider
 */

package de.sciss.swingosc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import javax.swing.event.MouseInputAdapter;

import de.sciss.net.OSCMessage;

/**
 *	Swing implementation of SCMultiSliderView by Jan Truetzschler.
 *
 *	@author		Hanns Holger Rutz
 *	@author		Tim Blechmann
 *	@version	0.64, 14-Mar-10
 */
public class MultiSlider
extends AbstractMultiSlider
{
	protected float		thumbWidth 		= 12f;
	protected float		thumbHeight		= 12f;
	private Color		fillColor 		= Color.black;
	private Color		indexColor 		= new Color( 0x00, 0x00, 0x00, 0x55 );
	private boolean 	hasFill			= true;
	private float		xOffset			= 1f;
	protected float		xStep			= 13f;
	protected boolean	showIndex		= false;
	protected boolean	isFilled		= false;
	protected boolean	horizontal		= true;
	protected boolean	readOnly		= false;
	
	protected float[]	values			= new float[ 0 ];
	protected int		startIndex		= 0;
	private float[][]	drawValues		= new float[][] { values };
	
	protected int		dirtyStart		= -1;
	protected int		dirtyStop		= -1;

	protected int		selectedIndex	= -1;
	protected int		selectionSize	= 1;

	private static final double	PIH = Math.PI / 2;
//	protected static final int precisionModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	protected static final int precisionModifier = SwingOSC.isMacOS() ?
		InputEvent.META_MASK : InputEvent.CTRL_MASK | InputEvent.ALT_MASK;  

	protected boolean	steady			= false;
	protected float		highPrecision	= 0.05f;
	
//	private static final Stroke strkOutline =
//		new BasicStroke( 1f, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND );

	public MultiSlider()
	{
		super();
//		setOpaque( true );

		final MouseAdapter ma = new MouseAdapter();
		addMouseListener( ma );
		addMouseMotionListener( ma );
	}
	
	protected void paintKnob( Graphics2D g2, int cw, int ch )
	{
//		final AffineTransform	atOrig	= g2.getTransform();
		final Shape				clipOrig	= g2.getClip();
		final int				h, w;
		final float				hm;
		final Rectangle2D		r	= new Rectangle2D.Float();
		final RoundRectangle2D	rr;
		float					thumbWidth, thumbWidthH, xOffset, xStep;
		float					x, y, y2, lastX;
		GeneralPath				gpOutline, gpFill, gpLines;
		float[]					values;
		int						numValues;
		
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
		
		if( horizontal ) {
			w	= cw; // getWidth() - 2;
			h	= ch; // getHeight() - 2;
			g2.translate( 0, h );
			g2.scale( 1f, -1f );
		} else {
			w	= ch; // getHeight() - 2;
			h	= cw; // getWidth() - 2;
//			g2.translate( 1, 1 );
			g2.scale( -1f, 1f );
			g2.rotate( PIH, 0, 0 );
		}
		hm			= h - thumbHeight;
		
		gpOutline 	= new GeneralPath();
		gpFill	 	= new GeneralPath();
		gpLines		= new GeneralPath();
		rr			= new RoundRectangle2D.Float( 0f, 0f, 0f, 0f, 2f, 2f );

		for( int n = drawValues.length - 1; n >= 0; n-- ) {
			values		= drawValues[ n ];
			numValues	= values.length;
			if( elasticResize ) {
				final float scale = w / (numValues * this.xStep);
				thumbWidth	= this.thumbWidth; // * scale;
				xOffset		= this.xOffset * scale;
				xStep		= this.xStep * scale;
			} else {
				thumbWidth	= this.thumbWidth;
				xOffset		= this.xOffset;
				xStep		= this.xStep;
			}
			thumbWidthH = thumbWidth * 0.5f;
			
			if( drawLines && (values.length > 0) ) {
				y2	= values[ 0 ] * hm + thumbHeight + 1;
				gpLines.moveTo( thumbWidthH, y2 );
			}
			lastX = 0f;
			for( int i = 0, j = startIndex; j < numValues; i++, j++ ) {
				x	= i * xStep;
				if( lastX > w ) break;
				y	= values[ j ] * hm;
				y2	= y + thumbHeight;
				if( isFilled ) {
					r.setFrame( x, -1f, thumbWidth, y2 + 1 );
					rr.setFrame( x - 0.5f, -0.5f, thumbWidth, y2 + 1 );
				} else {
					r.setFrame( x, y, thumbWidth, thumbHeight );
					rr.setFrame( x - 0.5f, y + 0.5f, thumbWidth, thumbHeight );
				}
				if( drawRects ) {
					if( hasFill ) gpFill.append( r, false );
					if( hasStroke ) gpOutline.append( rr, false );
				}
				if( drawLines ) {
					gpLines.lineTo( x + thumbWidthH, y2 );
				}
				lastX = x;
			}
			if( drawLines && isFilled && (numValues > 0) ) {
				x	= (numValues - 1 - startIndex) * xStep;
				y2	= values[ numValues - 1 ] * hm + thumbHeight;
				gpLines.lineTo( x + thumbWidthH, y2 );
				gpLines.closePath();
			}
			if( hasFill ) {
				g2.setColor( fillColor );
				g2.fill( gpFill );
			}
			if( hasStroke ) {
				g2.setColor( strokeColor );
	//			g2.setStroke( strkOutline );
				g2.draw( gpOutline );
			}
			if( drawLines ) {
				if( isFilled ) {
					g2.setColor( fillColor );
					g2.fill( gpLines );
				} else {
					g2.setColor( strokeColor );
					g2.draw( gpLines );
				}
			}
			if( showIndex && (selectedIndex >= 0) && (n == 0) ) {
				r.setFrame( (selectedIndex - startIndex) * xStep, 0, selectionSize * xStep - xOffset, h );
				g2.setColor( indexColor );
				g2.fill( r );
			}
		}
//		g2.setTransform( atOrig );
		g2.setClip( clipOrig );
	}
	
	public void setValues( float[] values )
	{
		this.values	= values;
		if( selectedIndex >= 0 ) {
			selectedIndex = Math.min( selectedIndex, values.length - 1 );
			selectionSize = Math.min( selectionSize, values.length - selectedIndex );
		}
		drawValues[ 0 ] = values;
		repaint();
	}

	public void setValues( Object[] values )
	{
		final float[] fValues = new float[ values.length ];
		for( int i = 0; i < values.length; i++ ) {
			fValues[ i ] = ((Number) values[ i ]).floatValue();
		}
		setValues( fValues );
	}
	
	public float[] getValues()
	{
		return values;
	}
	
	public void setReferenceValues( float[] refValues )
	{
		if( (refValues != null) && (refValues.length > 0) ) {
			drawValues = new float[][] { values, refValues };
		} else {
			drawValues = new float[][] { values };
		}
		repaint();
	}

	public void setReferenceValues( Object[] values )
	{
		final float[] fValues = new float[ values.length ];
		for( int i = 0; i < values.length; i++ ) {
			fValues[ i ] = ((Number) values[ i ]).floatValue();
		}
		setReferenceValues( fValues );
	}
	
	public void sendValues( Object id, int offset, int num )
	{
		offset 	= Math.max( 0, Math.min( values.length - 1, offset ));
		num		= Math.min( values.length - offset, num );

		final SwingOSC		osc			= SwingOSC.getInstance();
		final SwingClient	client		= osc.getCurrentClient();
		final Object[] 		replyArgs	= new Object[ 3 + num ];
		replyArgs[ 0 ]					= id;
		replyArgs[ 1 ]					= new Integer( offset );
		replyArgs[ 2 ]					= new Integer( num );

		for( int i = offset, j = 3; j < replyArgs.length; i++, j++ ) {
			replyArgs[ j ]				= new Float( values[ i ]);
		}
		
		try {
			client.reply( new OSCMessage( "/values", replyArgs ));
		}
		catch( IOException ex ) {
			SwingOSC.printException( ex, "sendValues" );
		}
	}
	
	public void sendValuesAndClear( Object id )
	{
		sendValues( id, dirtyStart, dirtyStop - dirtyStart );
		clearDirty();
	}
	
	public void setThumbWidth( float w )
	{
		thumbWidth	= w;
//		thumbWidthH	= w / 2;
		xStep		= thumbWidth + xOffset;
		hasStroke	= (strokeColor.getAlpha() > 0x00) && (thumbWidth > 1);
		repaint();
	}
	
	public float getThumbWidth()
	{
		return thumbWidth;
	}

	public void setThumbHeight( float h )
	{
		thumbHeight = h;
		repaint();
	}
	
	public float getThumbHeight()
	{
		return thumbHeight;
	}
	
	public void setThumbSize( float size )
	{
		setThumbWidth( size );
		setThumbHeight( size );
	}

	public void setFillColor( Color c )
	{
		fillColor	= c;
		hasFill		= fillColor.getAlpha() > 0x00;
		indexColor	= new Color( fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(),
						fillColor.getAlpha() / 3 );
		repaint();
	}
	
	public Color getFillColor()
	{
		return fillColor;
	}

	public void setStrokeColor( Color c )
	{
		strokeColor	= c;
		hasStroke	= (strokeColor.getAlpha() > 0x00) && (thumbWidth > 1);
		repaint();
	}
	
	public Color getStrokeColor()
	{
		return strokeColor;
	}

	public void setXOffset( float off )
	{
		xOffset	= off;
		xStep	= thumbWidth + xOffset;
		repaint();
	}
	
	public float getXOffset()
	{
		return xOffset;
	}
	
	public void setShowIndex( boolean onOff )
	{
		showIndex	= onOff;
		repaint();
	}
	
	public boolean getShowIndex()
	{
		return showIndex;
	}
	
	public void setFilled( boolean onOff )
	{
		isFilled	= onOff;
		repaint();
	}

	public boolean getFilled()
	{
		return isFilled;
	}
	
	public void setOrientation( int orient )
	{
		if( orient == HORIZONTAL ) {
			horizontal = true;
		} else if( orient == VERTICAL ) {
			horizontal = false;
		} else {
			throw new IllegalArgumentException( String.valueOf( orient ));
		}
	}
	
	public int getOrientation()
	{
		return horizontal ? HORIZONTAL : VERTICAL;
	}

	public void setStepSize( float stepSize )
	{	
		super.setStepSize( stepSize );
		
		if( stepSize > 0f ) {
			for( int i = 0; i < values.length; i++ ) {
				values[ i ] = snap( values[ i ]);
			}
			repaint();
		}
	}

	public void setStartIndex( int idx )
	{
		startIndex	= Math.max( 0, idx );
		repaint();
	}
	
	public int getStartIndex()
	{
		return startIndex;
	}

	public void setReadOnly( boolean onOff )
	{
		readOnly	= onOff;
	}

	public boolean getReadOnly()
	{
		return readOnly;
	}
	
	public void setSelectedIndex( int idx )
	{
		selectedIndex = idx;
		if( showIndex ) repaint();
	}
	
	public int getSelectedIndex()
	{
		return selectedIndex;
	}

	public void setSelectionSize( int numIndices )
	{
		selectionSize = numIndices;
		if( showIndex ) repaint();
	}
	
	public int getSelectionSize()
	{
		return selectionSize;
	}
	
	public int getDirtyIndex()
	{
		return dirtyStart;
	}
	
	public int getDirtySize()
	{
		return( dirtyStop - dirtyStart );
	}
	
	public void clearDirty()
	{
		dirtyStart 	= -1;
		dirtyStop	= -1;
	}

	public boolean getSteady()
	{
		return steady;
	}
	
	public void setSteady( boolean stdy )
	{
		steady = stdy;
	}
	
	public float getPrecision()
	{
		return highPrecision;
	}
	
	public void setPrecision( float hghPrcsn )
	{
		highPrecision = hghPrcsn;
	}

	// --------------- internal classes ---------------
	
	private class MouseAdapter
	extends MouseInputAdapter
	{
		private boolean	dragExtend		= false;
		private int		lastDragIdx		= -1;
		private boolean	dragExtendDir	= false;
		private float	lastDragY		= 0.f;

		protected MouseAdapter() { /* empty */ }
		
		private float computeVal( MouseEvent e, float y, float h, int dragIdx, boolean isDrag )
		{
			if( steady && (lastDragIdx == -1) ) {
				return values[ dragIdx ];
			}
			final boolean meta = (e.getModifiers() & precisionModifier) == precisionModifier;
			final float valueRaw;
			if( steady || (meta && isDrag )) {
				// todo: how to handle sliders index changes?
				// 
				// if (lastDragIdx != dragIdx) lastDragIdx = dragIdx;
				
				final float precisionFactor = meta ? highPrecision : 1f;

				final float lastVal = values[ lastDragIdx ];
				final float diffY	= y - lastDragY;
	
				final float diffVal = (diffY * precisionFactor) / (h - thumbHeight);
				valueRaw = lastVal + diffVal;
			} else {
				final float localThumbHeight = isFilled ? thumbHeight : thumbHeight / 2;
				valueRaw = (y - localThumbHeight) / (h - thumbHeight);
			}
			return Math.max( 0f, Math.min( 1f, valueRaw ));
		}

		private void processMouse( MouseEvent e, boolean init )
		{
			if( values.length == 0 ) return;
			
			final int 		x, w, y, h;
			final Insets	ins	= getInsets();
			final float 	scale, xSub, value;
			final int 		dragIdx, dragIdx2;
			final int 		newSelStop;
			boolean			action	= false;
			boolean			repaint	= false;
	
			if( horizontal ) {
				w	= getWidth() - ins.left - ins.right - 2;
				h	= getHeight() - ins.top - ins.bottom - 2;
				x	= e.getX() - ins.left - 1;
				y	= h - (e.getY() - ins.top - 1);
			} else {
				h	= getWidth() - ins.left - ins.right - 2;
				w	= getHeight() - ins.top - ins.bottom - 2;
				x	= e.getY() - ins.top - 1;				
				y	= e.getX() - ins.left - 1;
			}
//			if( isFilled ) {
//				val	= Math.max( 0f, Math.min( 1f, (y - thumbHeight) / (h - thumbHeight) ));
//			} else {
//				val	= Math.max( 0f, Math.min( 1f, (y - thumbHeight/2) / (h - thumbHeight) ));
//			}

			if( elasticResize ) {
				scale 	= values.length / (float) w;
				xSub	= thumbWidth/2 * (w / (values.length * xStep));
			} else {
				scale 	= 1 / xStep;
				xSub	= thumbWidth/2;
			}
			dragIdx2	= (int) ((x - xSub) * scale + 0.5f) + startIndex;
			dragIdx		= Math.max( 0, Math.min( values.length - 1, dragIdx2 ));
			// values might have been modified!!!
			if( lastDragIdx >= values.length ) lastDragIdx = -1;
			if( init ) dragExtend = e.isShiftDown();
			
			value = computeVal( e, y, h, dragIdx, !init );
			lastDragY = y;
			
			if( dragExtend ) {
				if( selectedIndex >= 0 ) {
					if( init ) dragExtendDir = dragIdx2 > (selectedIndex + (selectionSize >> 1));
					if( dragExtendDir ) {
						newSelStop = dragIdx2; // + 1;
						if( newSelStop != selectedIndex + selectionSize ) {
							selectionSize	= newSelStop - selectedIndex;
							if( selectionSize < 0 ) {
								selectionSize = -selectionSize;
								selectedIndex = newSelStop - selectionSize + 1;
								dragExtendDir	= !dragExtendDir;
//								System.out.println( "A ");
							}
							selectedIndex	= Math.max( 0, Math.min( values.length - 1, selectedIndex ));
							selectionSize 	= Math.min( values.length - selectedIndex, selectionSize ); 
							action	= true;
							repaint	|= showIndex;
						}
					} else {
						if( dragIdx != selectedIndex ) {
							selectionSize += selectedIndex - dragIdx2;
							selectedIndex	= dragIdx2;
							if( selectionSize < 0 ) {
//								newSelStop 		= selectedIndex + selectionSize;
								selectionSize	= -selectionSize;
								selectedIndex	= dragIdx2 - selectionSize;
								dragExtendDir	= !dragExtendDir;
							}
							selectedIndex	= Math.max( 0, Math.min( values.length - 1, selectedIndex ));
							selectionSize 	= Math.min( values.length - selectedIndex, selectionSize ); 
							action	= true;
							repaint	|= showIndex;
						}
					}
				} else {
					selectedIndex	= dragIdx;
					action			= true;
					repaint		   |= showIndex;
				}
			} else if( !readOnly ) {
				if( (lastDragIdx == -1) || (lastDragIdx == dragIdx) ) {
					if( snap( value ) != values[ dragIdx ]) {
						values[ dragIdx ] = snap( value );
						action	= true;
						repaint	= true;
						if( dirtyStart == -1 ) {
							dirtyStart  = dragIdx;
							dirtyStop	= dirtyStart + 1;
						} else {
							dirtyStart	= Math.min( dirtyStart, dragIdx );
							dirtyStop	= Math.max( dirtyStop, dragIdx + 1 );
						}
					}
				} else {
					final int	step	= dragIdx < lastDragIdx ? -1 : 1;
					final float	valOff	= values[ lastDragIdx ];
					final float valScale= (value - valOff) / Math.abs( lastDragIdx - dragIdx );
					for( int i = lastDragIdx + step, j = 1; i != dragIdx; i += step, j++ ) {
						values[ i ] = snap( j * valScale + valOff );
					}
					values[ dragIdx ] = snap( value );
					action	= true;
					repaint	= true;
					if( dirtyStart == -1 ) {
						dirtyStart  = Math.min( dragIdx, lastDragIdx );
						dirtyStop	= Math.max( dragIdx, lastDragIdx ) + 1;
					} else {
						dirtyStart	= Math.min( dirtyStart, Math.min( dragIdx, lastDragIdx ));
						dirtyStop	= Math.max( dirtyStop, Math.max( dragIdx, lastDragIdx ) + 1 );
					}
				}
			}
			lastDragIdx 	= dragIdx;
			if( !dragExtend && (selectedIndex != lastDragIdx) ) {
				selectedIndex 	= lastDragIdx;
				action			= true;
				selectionSize	= Math.min( selectionSize, values.length - selectedIndex );
				repaint		   |= showIndex;
			}

			if( action ) fireActionPerformed();
			if( repaint ) repaint();	// XXX should use painting rectangle here!!
		}

		public void mousePressed( MouseEvent e )
		{
			if( !isEnabled() || e.isControlDown() ) return;

			requestFocus();

			processMouse( e, true );
		}
		
		public void mouseReleased( MouseEvent e )
		{
			lastDragIdx = -1;
		}
		
		public void mouseDragged( MouseEvent e )
		{
			if( !isEnabled() ) return;

			processMouse( e, false );
		}
	}
}