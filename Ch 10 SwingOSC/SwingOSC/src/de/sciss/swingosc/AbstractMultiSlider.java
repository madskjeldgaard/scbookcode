/*
 *  AbstractMultiSlider.java
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
 *		05-Feb-07	transformed from MultiSlider to AbstractMultiSlider
 */

package de.sciss.swingosc;

import java.awt.Color;
import javax.swing.SwingConstants;

/**
 *	Common superclass of MultiSlider and EnvelopeView.
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.45, 05-Feb-07
 */
public abstract class AbstractMultiSlider
extends SliderBase
implements SwingConstants
{
//	private float		thumbWidth 		= 12f;
//	private float		thumbHeight		= 12f;
//	private Color		fillColor 		= Color.black;
//	private Color		indexColor 		= new Color( 0x00, 0x00, 0x00, 0x55 );
//	private boolean 	hasFill			= true;
	protected Color		strokeColor		= Color.black;
	protected boolean 	hasStroke		= true;
//	private float		xOffset			= 1f;
//	private float		xStep			= 13f;
//	private boolean		showIndex		= false;
	protected boolean	drawLines		= false;	// closed polygon in fillColor
	protected boolean	drawRects		= true;
//	private boolean		isFilled		= false;
//	protected float		step			= 0f;
//	private boolean		horizontal		= true;
//	private boolean		readOnly		= false;
	protected boolean	elasticResize	= false;
	
//	private float[]		values			= new float[ 0 ];
//	private int			startIndex		= 0;
//	private float[][]	drawValues		= new float[][] { values };
	
//	private int			dirtyStart		= -1;
//	private int			dirtyStop		= -1;
	
//	private static final double	PIH = Math.PI / 2;
	
	protected AbstractMultiSlider()
	{
		super();
	}
		
	public final void setDrawLines( boolean onOff )
	{
		drawLines	= onOff;
		repaint();
	}
	
	public final boolean getDrawLines()
	{
		return drawLines;
	}
	
	public final void setDrawRects( boolean onOff )
	{
		drawRects	= onOff;
		repaint();
	}
	
	public final boolean getDrawRects()
	{
		return drawRects;
	}
	
//	public final void setStep( float step )
//	{
//		this.step	= step;
//		// XXX recalc values
//	}
	
//	public final float getStep()
//	{
//		return step;
//	}
	
	public final void setElasticResizeMode( boolean onOff )
	{
		elasticResize = onOff;
		repaint();
	}

	public final boolean getElasticResizeMode()
	{
		return elasticResize;
	}
}