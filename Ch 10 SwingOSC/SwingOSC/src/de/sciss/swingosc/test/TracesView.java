package de.sciss.swingosc.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;

public class TracesView
extends JComponent
{
	private Color[] 		colrs;
	private int[] 			traceX		= new int[ 0 ];
	private int[] 			traceY		= new int[ 0 ];
	private final Stroke	stroke		= new BasicStroke( 8 );
	
	public TracesView()
	{
		colrs = new Color[ 64 ];
		for( int i = 0; i < 64; i++ ) colrs[ i ] = new Color( 0x00, 0xFF, 0x00, i << 2 );
		setOpaque( true );
		setBackground( Color.black );
	}
	
	public void setTrace( Object[] pairs )
	{
		// scaling up by four is a trick since we will
		// use scale( 1/4, 1/4 ) in the paintComponent.
		// that gives us some fractional coordinates for
		// "free" (i.e. without having to use Shape and Java2D,
		// and drawLine is a lot faster!)
		final int w = getWidth() * 4;
		final int h = getHeight() * 4;
		traceX = new int[ pairs.length >> 1 ];
		traceY = new int[ traceX.length ];
		for( int i = 0, j = 0; j < traceX.length; j++ ) {
			traceX[ j ] = (int) (((Number) pairs[ i++ ]).floatValue() * w);
			traceY[ j ] = (int) (((Number) pairs[ i++ ]).floatValue() * h);
		}
		repaint();
	}
	
	public void paintComponent( Graphics g )
	{
		g.setColor( getBackground() );
		g.fillRect( 0, 0, getWidth(), getHeight() );
		
		if( traceX.length == 0 ) return;
		
		int x1, y1, x2, y2;
		
		final Graphics2D g2 = (Graphics2D) g;
		final AffineTransform atOrig = g2.getTransform();
		g2.scale(  0.25, 0.25 );
		g2.setStroke(  stroke );
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		
		x2 = traceX[ 0 ];
		y2 = traceY[ 0 ];
		
		for( int i = 1; i < traceX.length; i++ ) {
			if( i < 64 ) g.setColor( colrs[ i ]);
			x1 = x2; y1 = y2;
			x2 = traceX[ i ];
			y2 = traceY[ i ];
			g.drawLine( x1, y1, x2, y2 );
		}
	
		g2.setTransform( atOrig );
	}
}
