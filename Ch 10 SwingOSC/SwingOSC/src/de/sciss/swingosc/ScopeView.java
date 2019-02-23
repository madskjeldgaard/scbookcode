/*
 *  ScopeView.java
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
 *		22-Feb-05	created
 *		29-Apr-07	fixed TCP mode, updated for NetUtil API changes
 */
 
package de.sciss.swingosc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import javax.swing.JComponent;
import javax.swing.Timer;

import de.sciss.net.OSCClient;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
//import de.sciss.net.OSCPacket;
import de.sciss.net.OSCPacketCodec;

/**
 * @version	0.53, 29-Apr-07
 * @author	Hanns Holger Rutz
 */
public class ScopeView
extends JComponent
implements OSCListener, ActionListener
{
	// warning: while scsynth seems to have been updated to 64K buffers,
	// the /b_setn is only sent for sizes <= 8K for some reason!!
	private static final int	OSC_BUF_SIZE = 8192;
	private static final int	SLICE_SIZE	= (OSC_BUF_SIZE - 32) / 5; // 4bytes per float, 1byte per typetag : < 64K
	
	private float[]				vector		= null;
	private float[]				pntVector	= null;
	// note that since Graphics.drawPolyline is much
	// faster than Graphics2D.draw( GeneralPath ), we
	// use polies. to allow for some amount of antialiasing
	// both x and y coordinates are scaled up by a factor of 4
	// and the Graphics2D context will scale them down again
	private int[]				polyX, polyY;
	private int					size		= 0;
	private int					numFrames	= 0;
	private int					numChannels	= 0;
//	private int					sizeM1		= -1;
	private int					bufNum		= 0;
	private int					sliceID		= 0;
	private int					polySize	= 0;
	private int					polySizeC	= 0;

	private static final Color	colrFg		= Color.yellow;
//	private Color				colrGrid;
	private Color[]				colrWave	= new Color[ 0 ];
//	private boolean 			antiAlias 	= false;
	private final Stroke		strkPoly	= new BasicStroke( 4f );
	private int 				recentWidth	= -1;
	private float				xZoom		= 1.0f;
	private float				yZoom		= 1.0f;
	private boolean				sah			= false;
	private boolean				overlay		= false;
	private boolean				lissajou	= false;
	
	private int					style		= 0;
	
	private boolean				isListening	= false;
	private InetSocketAddress	addr		= null;
	private String				protocol;
//	private DatagramChannel		dch			= null;
//	private OSCTransmitter		trns		= null;
//	private OSCReceiver			rcv			= null;
	private OSCClient			client		= null;
	private OSCMessage[]		msgBufGetN	= null;
	private int[]				msgBufOff	= null;

	private final Object		sync		= new Object();
	
	private final Timer			timer;
	
	public ScopeView()
	{
		super();
	
		setOpaque( true );
		setBackground( Color.black );
		setFocusable( true );
		
		addMouseListener( new MouseAdapter() {
			public void mousePressed( MouseEvent e )
			{
				requestFocus();
			}
		});
		
		timer = new Timer( 1000, this );
		timer.setRepeats( true );
	}
	
	public void setServer( String hostName, int port, String protocol )
	{
		synchronized( sync ) {
			final boolean wasListening = isListening();
			if( wasListening ) {
				stopListening();
			}
			if( hostName.equals( "127.0.0.1" ) || hostName.equals( "localhost" )) {
				try {
					addr	= new InetSocketAddress( InetAddress.getLocalHost(), port );
//					addr	= new InetSocketAddress( "192.168.2.106", port );
				}
				catch( UnknownHostException e1 ) {
					addr	= new InetSocketAddress( hostName, port );
				}
			} else {
				addr		= new InetSocketAddress( hostName, port );
			}
//System.err.println( "host = " + hostName + " => " + addr.getHostName() );
			this.protocol	= protocol;
			if( wasListening ) {
				startListening();
			}
		}
	}
	
	public void startListening()
	{
		synchronized( sync ) {
			if( !isListening ) {
				if( addr == null ) {
					throw new IllegalStateException( "Server has not been specified" );
				}
//				final Map map = new HashMap( 1 );
				try {
					client	= OSCClient.newUsing( protocol );
					client.setBufferSize( OSC_BUF_SIZE );  // current scsynth versions!
//					client	= OSCClient.newUsing( protocol, 0, true );
//System.err.println( "size = " + size );
					if( size > 0 ) {
						setCustomDecoder();
					}
//client.dumpOSC( 1, System.out );
					client.setTarget( addr );
			        client.start();
			        client.addOSCListener( this );
					isListening = true;
					query();
					timer.restart();
//					System.out.println(" restarted ");
				}
				catch( IOException e1 ) {
					if( client != null ) {
						client.removeOSCListener( this );
						client.dispose();
						client = null;
					}
					msgBufGetN	= null;
					isListening	= false;
					
					System.out.println( e1 );
				}
			}
		}
	}
	
	public void stopListening()
	{
		synchronized( sync ) {
			if( isListening ) {
				timer.stop();
//System.out.println(" stopped ");
				client.removeOSCListener( this );
				msgBufGetN = null;
				try {
					client.stop();
				}
				catch( IOException e1 ) {
					System.out.println( e1 );
				}
				client.dispose();
				client = null;
				isListening	= false;
				vector		= null;
				pntVector	= null;
			}
		}
	}
	
	public boolean isListening()
	{
		return isListening;
	}
	
	public void setBuffer( int bufNum, int numFrames, int numChannels, float sampleRate )
	{
		synchronized( sync ) {
//System.out.println( "setBuffer "+bufNum+", "+numFrames+", "+numChannels + "; isListening? " + isListening );
			size			= numFrames * numChannels;
			vector 			= new float[ size ];
			pntVector 		= new float[ size ];
			this.bufNum		= bufNum;
			this.numFrames	= numFrames;
			this.numChannels= numChannels;
//			sizeM1			= size - 1;
			polyX			= new int[ numFrames << 1 ];
			polyY			= new int[ numFrames << 1 ];
			recentWidth		= -1;	// need to recalc X coords
			
			if( isListening ) {
				setCustomDecoder();
				query();
			}
		}
	}
	
	private void setCustomDecoder()
	{
//System.out.println( "setCustomDecoder" );
		final OSCSharedBufSetNMsg bufSetNMsg = new OSCSharedBufSetNMsg( vector );
		createMessages();
		client.setCodec( new OSCPacketCodec() {
			protected OSCMessage decodeMessage( String command, ByteBuffer b )
			throws IOException
			{
				if( command.equals( "/b_setn" )) {
//System.out.println( "/b_setn" );
					return bufSetNMsg.decodeSpecific( b );
				} else {
					return super.decodeMessage( command, b );
				}
			}
			
			protected int getMessageSize( OSCMessage msg )
            throws IOException
            {
				if( msg instanceof OSCSharedBufSetNMsg ) {
					return ((OSCSharedBufSetNMsg) msg).getSpecificSize();
				} else {
					return super.getMessageSize( msg );
				}
            }
		});
	}
	
	// sync: caller must be in synchronized( sync ) block!
	private void createMessages()
	{
		final int numSlices 	= (size + SLICE_SIZE - 1) / SLICE_SIZE;
		if( numSlices == 0 ) return;
		msgBufGetN			= new OSCMessage[ numSlices ];
		msgBufOff			= new int[ numSlices ];
		
		for( int i = 0, off = 0; i < numSlices; i++, off += SLICE_SIZE ) {
			msgBufOff[ i ] = off;
			msgBufGetN[ i ] = new OSCMessage( "/b_getn", new Object[] {
					new Integer( bufNum ), new Integer( off ), new Integer( Math.min( SLICE_SIZE, size - off ))});
		}
		
		sliceID				= 0;
	}

//	// sync: call in event thread only
//	public void setVectorSize( int size )
//	{
//		synchronized( sync ) {
//			vector 		= new float[ size ];
//			this.size	= size;
//			sizeM1		= size - 1;
//			polyX		= new int[ size ];
//			polyY		= new int[ size ];
//			
//			if( isListening ) {
//				final Map map = new HashMap( 1 );
//				map.put( "/b_setn", new OSCSharedBufSetNMsg( vector ));
//				rcv.setCustomMessageDecoders( map );
//
//				msgBufGetN = new OSCMessage( "/b_getn", new Object[] {
//						new Integer( bufNum ), new Integer( 0 ), new Integer( size )});
//			}
//		}
//	}
	
//	public void setBufNum( int bufNum )
//	{
//		synchronized( sync ) {
//			this.bufNum	= bufNum;
//			if( isListening ) {
//				msgBufGetN	= new OSCMessage( "/b_getn", new Object[] {
//						new Integer( bufNum ), new Integer( 0 ), new Integer( size )});
//			}
//		}
//	}
	
	public void setBufNum( int bufNum )
	{
		synchronized( sync ) {
			if( isListening ) {
				msgBufGetN 	= null;	// don't query until we've got the buffer specs
				this.bufNum	= bufNum;
				try {
					client.send( new OSCMessage( "/b_query", new Object[] { new Integer( bufNum )}));
				}
				catch( IOException e1 ) {
					System.out.println( e1 );
				}
			} else {
				throw new IllegalStateException( "ScopeView.setBufNum : call startListening before!" );
			}
		}
	}
	
	public void setStyle( int style )
	{
		this.style	= style;
		overlay		= style > 0;
		lissajou		= style == 2;
		recentWidth	= -1;
		repaint();
	}
	
	public int getStyle()
	{
		return style;
	}
	
	public void setXZoom( float f )
	{
		xZoom		= f;
		recentWidth	= -1;	// triggers recalc
		repaint();
	}

	public void setYZoom( float f )
	{
		yZoom	= f;
		repaint();
	}
	
	public float getXZoom()
	{
		return xZoom;
	}

	public float getYZoom()
	{
		return yZoom;
	}

//	public void setAntiAliasing( boolean onOff )
//	{
//		antiAlias = onOff;
//		repaint();
//	}
//	
//	public boolean getAntiAliasing()
//	{
//		return antiAlias;
//	}
	
	// sync: call in event thread only
	public void setWaveColors( Color[] c )
	{
		colrWave = c;
		repaint();
	}
	
	public void setObjWaveColors( Object[] o )
	{
		final Color[] c = new Color[ o.length ];
		
		for( int i = 0; i < o.length; i++ ) {
			c[ i ] = (Color) o[ i ];
		}
		
		setWaveColors( c );
	}
	
//	public void setGridColor( Color c )
//	{
//		colrGrid	= c;
//		repaint();
//	}
	
	// sync: caller must be in synchronized( sync ) block!!
	private void query()
	{
		if( msgBufGetN != null ) {
			try {
// scsynth is too stupid to handle all requests at once
//				for( int i = 0; i < msgBufGetN.length; i++ ) {
//					if( msgBufOff[ i ] > polySizeC ) break;
					client.send( msgBufGetN[ sliceID ]);
//System.out.println( "Sending request " + msgBufGetN[ i ].getArg( 1 )+"; "+msgBufGetN[ i ].getArg( 2 ));
//				}
				sliceID = sliceID + 1;
				if( (sliceID == msgBufGetN.length) || (msgBufOff[ sliceID ] > polySizeC) ) {
					sliceID = 0;
				}
			}
			catch( IOException e1 ) {
				System.out.println( e1 );
			}
		}
		timer.restart();
	}

	public void paintComponent( Graphics g )
	{
		super.paintComponent( g );
		
		final Graphics2D		g2		= (Graphics2D) g;
		final int				w		= getWidth();
		final int				h		= getHeight();
		final AffineTransform	atOrig	= g2.getTransform();
		final Stroke			strkOrig= g2.getStroke();
		final float				sy;
		float					offY	= 0f;
		int						x, y;
	
		g2.setColor( getBackground() );
		g2.fillRect( 0, 0, w, h );
		
		// XXX this sync becomes superfluous
		// if setBuffer was deferred to the event
		// thread from messageReceived ...
		// which could improve performance a bit
		synchronized( sync ) {
			if( (pntVector == null) || (numChannels == 0) ) return;
			
			if( lissajou ) {
				// the interpretation of the xZoom value is completely
				// stupid in cocoa scope. in lissajou mode increasing zoom
				// will increase scale, while in normal mode it's the other way round
				final int offX	= w << 1;
				final float sx	= offX * xZoom;
				sah				= false;
				polySize 		= numFrames;
				polySizeC		= polySize * numChannels;
				for( int i = 0, k = 0; i < polySize; i++, k += numChannels ) {
					x			= (int) (pntVector[ k ] * sx + offX);
					polyX[ i ]	= x;
				}
				recentWidth 	= w;
				
			} else if( w != recentWidth ) {	// have to recalc horiz. coord
	//			final float	sx = w * xZoom / sizeM1;
	//			final float	sx = (w << 2) * xZoom / sizeM1;
				final float	sx = 4 / xZoom;
	//			polySize 	= Math.min( numFrames, (int) (sizeM1 / xZoom) + 1 );
				polySize 	= Math.min( numFrames, (int) (w * xZoom) + 1 );
				polySizeC	= polySize * numChannels;
	//			sah			= sx > 3;
				sah			= sx > 12;
				if( sah ) {
					x = 0;
					for( int i = 0, j = 0; i < polySize; i++ ) {
						polyX[ j++ ]	= x;
						x			= (int) (i * sx);
						polyX[ j++ ]	= x;
					}
				} else {
					for( int i = 0; i < polySize; i++ ) {
						x			= (int) (i * sx);
						polyX[ i ]	= x;
					}
				}
				recentWidth 	= w;
			}	
			
			g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			
			if( overlay ) {
				offY		= h * 0.5f;
				sy		= -(h << 1) * yZoom;
			} else {
	//			sy		= -h * yZoom / (numChannels << 1);
				sy		= -(h << 1) * yZoom / numChannels;
			}
			
			for( int ch = lissajou ? 1 : 0; ch < numChannels; ch++ ) {
				g2.setColor( colrWave.length > ch ? colrWave[ ch ] : colrFg );
				g2.setStroke( strkPoly );
				if( overlay ) {
					g2.translate( 0, offY );
				} else {
					offY = (float) (((ch << 1) + 1) * h) / (numChannels << 1);
					g2.translate( 0, offY );
				}
				g2.scale( 0.25f, 0.25f );
				if( sah ) { // sample-and-hold
					for( int i = 0, j = 0, k = ch; i < polySize; i++, k += numChannels ) {
						y			= (int) (pntVector[ k ] * sy);
						polyY[ j++ ] = y; 
						polyY[ j++ ] = y; 
					}
				
					g2.drawPolyline( polyX, polyY, polySize << 1 );
				
				} else {
					for( int i = 0, k = ch; i < polySize; i++, k += numChannels ) {
						y			= (int) (pntVector[ k ] * sy);
						polyY[ i ] 	= y;
					}
				
					g2.drawPolyline( polyX, polyY, polySize );
				}
				
				g2.setTransform( atOrig );
				g2.setStroke( strkOrig );
			}
		}
	}

	// ------------- ActionListener interface -------------

	// called upon timeout
	public void actionPerformed( ActionEvent e )
	{
		synchronized( sync ) {
			if( isListening ) setBufNum( bufNum );
		}
	}
		
	// ------------- OSCListener interface -------------
	public void messageReceived( OSCMessage msg, SocketAddress addr, long when )
	{
		synchronized( sync ) {
			if( msg instanceof OSCSharedBufSetNMsg ) {
				final OSCSharedBufSetNMsg bufSetNMsg = (OSCSharedBufSetNMsg) msg;
				if( bufSetNMsg.getStartOffset() + bufSetNMsg.getNumSamples() >= polySizeC ) {
					System.arraycopy( vector, 0, pntVector, 0, size );
					repaint();	// paint complete waveform
				}
				if( isListening ) query();	// request next slice
			} else if( msg.getName().equals( "/b_info" )) {
//System.err.println( "/b_info" );
				try {
					final int msgBufNum = ((Number) msg.getArg( 0 )).intValue();
					if( msgBufNum != bufNum ) return;
					setBuffer( bufNum, ((Number) msg.getArg( 1 )).intValue(),
									  ((Number) msg.getArg( 2 )).intValue(), 
									  ((Number) msg.getArg( 3 )).floatValue() );
				}
				catch( ClassCastException e1 ) {
					System.out.println( e1 );
				}
			}
		}
	}
}