/*
 *  RangeSlider.java
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
 *		15-Dec-05	created
 *		02-Feb-07	added vertical orientation
 *		14-Jan-08	more pretty knob image scaling
 */

package de.sciss.swingosc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import javax.swing.SwingConstants;

/**
 *	A slider that can have variable width.
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.57, 14-Jan-08
 *
 *	@todo		vertical orientation
 */
public class RangeSlider
extends MonoSlider
implements SwingConstants
{
	private static Image 	progImg 		= null;
	private static Image 	progImgD 		= null;

	private Image 			progImgColr		= null;

	private BufferedImage 	progBufImg 		= null;
	private BufferedImage 	progBufImgD 	= null;
	private Paint			pntProg			= null;
	private Paint			pntProgD		= null;
	private int				recentHeight	= -1;
	private int				recentWidth		= -1;
	
	private int				orientation		= -1; // will be set in the constructor
	
	private static final Insets valInsets = new Insets( 1, 1, 1, 1 );

	public RangeSlider()
	{
		this( HORIZONTAL );
	}
	
	public RangeSlider( int orient )
	{
		super();

		setOrientation( orient );
		setKnobExtent( 1.0f );
		
		if( progImg == null ) {
			try {
				progImg = Toolkit.getDefaultToolkit().getImage( getClass().getResource( "aquaprog.png" ));
				waitForImage( progImg );
			}
			catch( NullPointerException e1 ) { System.out.println( e1 );}
		}
	}

	public void dispose()
	{
		if( progImgColr != null ) {
			progImgColr.flush();
			progImgColr = null;
		}
		if( progBufImg != null ) {
			progBufImg.flush();
			progBufImg = null;
		}
		if( progBufImgD != null ) {
			progBufImgD.flush();
			progBufImgD = null;
		}
		pntProg		= null;
		pntProgD	= null;
		
		super.dispose();
	}
	
	public void setOrientation( int orient )
	{
		final float pos;
		final float extent;

		if( orient != orientation ) {
			orientation = orient;
			recentWidth = -1;
			recentHeight = -1;
			if( orient == HORIZONTAL ) {
				pos 	= getKnobY();	// _was_ vertical
				extent	= getKnobHeight();
				setHorizontalSlideEnabled( true );
				setHorizontalResizeEnabled( true );
				setVerticalSlideEnabled( false );
				setVerticalResizeEnabled( false );
				setKnobX( pos );
				setKnobWidth( extent );
				setKnobY( 0f );
				setKnobHeight( 0f );
			} else {
				pos 	= getKnobX();	// _was_ horizontal
				extent	= getKnobWidth();
				setHorizontalSlideEnabled( false );
				setHorizontalResizeEnabled( false );
				setVerticalSlideEnabled( true );
				setVerticalResizeEnabled( true );
				setKnobX( 0f );
				setKnobWidth( 0f );
				setKnobY( pos );
				setKnobHeight( extent );
			}
			repaint();
		}
	}
	
	public void setKnobPos( float pos )
	{
		if( orientation == HORIZONTAL ) {
			setKnobX( pos );
		} else {
			setKnobY( pos );
		}
	}
	
	public void setKnobExtent( float extent )
	{
		if( orientation == HORIZONTAL ) {
			setKnobWidth( extent );
		} else {
			setKnobHeight( extent );
		}
	}
	
	public float getKnobPos()
	{
		return( (orientation == HORIZONTAL) ? getKnobX() : getKnobY() );
	}

	public float getKnobExtent()
	{
		return( (orientation == HORIZONTAL) ? getKnobWidth() : getKnobHeight() );
	}

	public void setKnobColor( Color c )
	{
		if( progImgColr != null ) {
			progImgColr.flush();
			progImgColr = null;
		}

		recentHeight = -1;	// triggers re-creation
		recentWidth  = -1;
		
		super.setKnobColor( c );
	}
	
	protected Insets getValueInsets()
	{
		return valInsets;
	}

	protected void paintKnob( Graphics2D g2, int w, int h )
	{
		if( orientation == HORIZONTAL ) {
			paintHKnob( g2, w, h );
		} else {
			paintVKnob( g2, w, h );
		}
	}
	
	private void paintHKnob( Graphics2D g2, int w, int h )
	{
		final int cw = w - 2;
		final int kw = Math.max( 1, (int) ((knobX + knobWidth) * cw) - (int) (knobX * cw) );
		final int kh = h - 2;
		final int kx = (int) (knobX * (w - 2)) + 1;
		final int ky = 1;

		if( kh != recentHeight ) {
			if( progBufImg != null ) {
				pntProg = null;
				progBufImg.flush();
				progBufImg = null;
			}
			if( progBufImgD != null ) {
				pntProgD = null;
				progBufImgD.flush();
				progBufImgD = null;
			}
			
			recentHeight = kh;
		}
		
		if( progImg != null ) {
			if( isEnabled() ) {
				if( pntProg == null ) {
					final Graphics2D gImg;
					progBufImg = new BufferedImage( 16, kh, BufferedImage.TYPE_INT_ARGB );
					gImg = progBufImg.createGraphics();
					if( colrKnob != null ) {
						if( progImgColr == null ) {
							progImgColr = createImage( new FilteredImageSource(
									progImg.getSource(), fltKnob ));
							// please don't ask me why
							// we need a media tracker here
							// ; if we omit mit, the image
							// will sometimes not be painted
							waitForImage( progImgColr );
						}
						drawImage( gImg, progImgColr, kh );
					} else {
						drawImage( gImg, progImg, kh );
					}
					gImg.dispose();
					pntProg = new TexturePaint( progBufImg, new Rectangle( 0, 0, 16, kh ));
				}
				g2.setPaint( pntProg );
			} else {
				if( pntProgD == null ) {
					final Graphics2D gImg;
					if( progImgD == null ) {
						progImgD = createImage( new FilteredImageSource(
							progImg.getSource(), fltKnobD ));
						// please don't ask me why
						// we need a media tracker here
						// ; if we omit it, the image
						// will sometimes not be painted
						waitForImage( progImgD );
					}
					progBufImgD = new BufferedImage( 16, kh, BufferedImage.TYPE_INT_ARGB );
					gImg = progBufImgD.createGraphics();
					drawImage( gImg, progImgD, kh );
					gImg.dispose();
					pntProgD = new TexturePaint( progBufImgD, new Rectangle( 0, 0, 16, kh ));
				}
				g2.setPaint( pntProgD );
			}
		}
		g2.fillRect( kx, ky, kw, kh );
		g2.setPaint( colrBdLRSh );
		g2.drawLine( kx, ky + 1, kx, kh - 2 );
		g2.fillRect( kx + kw - 1, ky + 1, 2, kh - 1 );
	}
	
	private void paintVKnob( Graphics2D g2, int w, int h )
	{
		final int ch = h - 2;
		// tricky scheiss
		final int ky = (int) ((1f - (knobY + knobHeight)) * ch) + 1;
		final int kh = Math.max( 1, (int) ((1f - knobY) * ch) - ky + 1 );
		final int kw = w - 2;
		final int kx = 1;

		//return( Math.max( 1, (int) ((knobY + knobHeight) * ch) - (int) (knobY * ch) ));

		if( kw != recentWidth ) {
			if( progBufImg != null ) {
				pntProg = null;
				progBufImg.flush();
				progBufImg = null;
			}
			if( progBufImgD != null ) {
				pntProgD = null;
				progBufImgD.flush();
				progBufImgD = null;
			}
			
			recentWidth = kw;
		}
		
		if( progImg != null ) {
			if( isEnabled() ) {
				if( pntProg == null ) {
					final Graphics2D gImg;
					progBufImg = new BufferedImage( kw, 16, BufferedImage.TYPE_INT_ARGB );
					gImg = progBufImg.createGraphics();
					gImg.rotate( -Math.PI / 2 );
					gImg.translate( -16, 0 );
					if( colrKnob != null ) {
						if( progImgColr == null ) {
							progImgColr = createImage( new FilteredImageSource(
									progImg.getSource(), fltKnob ));
							// please don't ask me why
							// we need a media tracker here
							// ; if we omit it, the image
							// will sometimes not be painted
							waitForImage( progImgColr );
						}
						drawImage( gImg, progImgColr, kw );
					} else {
						drawImage( gImg, progImgColr, kw );
					}
					gImg.dispose();
					pntProg = new TexturePaint( progBufImg, new Rectangle( 0, 0, kw, 16 ));
				}
				g2.setPaint( pntProg );
			} else {
				if( pntProgD == null ) {
					final Graphics2D gImg;
					if( progImgD == null ) {
						progImgD = createImage( new FilteredImageSource(
							progImg.getSource(), fltKnobD ));
						// please don't ask me why
						// we need a media tracker here
						// ; if we omit mit, the image
						// will sometimes not be painted
						waitForImage( progImgD );
					}
					progBufImgD = new BufferedImage( kw, 16, BufferedImage.TYPE_INT_ARGB );
					gImg = progBufImgD.createGraphics();
					gImg.rotate( -Math.PI / 2 );
					gImg.translate( -16, 0 );
					drawImage( gImg, progImgD, kw );
					gImg.dispose();
					pntProgD = new TexturePaint( progBufImgD, new Rectangle( 0, 0, kw, 16 ));
				}
				g2.setPaint( pntProgD );
			}
		}
		g2.fillRect( kx, ky, kw, kh );
		g2.setPaint( colrBdLRSh );
		g2.drawLine( kx + 1, ky, kw - 2, ky );
		g2.fillRect( kx + 1, ky + kh - 1, kw - 1, 2 );
	}

	private void drawImage( Graphics2D gImg, Image img, int ext )
	{
		if( ext <= 15 ) {
			gImg.drawImage( img, 0, 0, 16, ext, this );
		} else {
			gImg.drawImage( img, 0,       0, 16,       6, 0, 0, 16,  6, this );
			gImg.drawImage( img, 0,       6, 16, ext - 8, 0, 6, 16,  7, this );
			gImg.drawImage( img, 0, ext - 8, 16,     ext, 0, 7, 16, 15, this );
		}
	}
}