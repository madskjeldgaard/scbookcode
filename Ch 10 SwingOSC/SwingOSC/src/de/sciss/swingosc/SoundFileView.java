/*
 *  SoundFileView.java
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
 *		18-Nov-06	created
 *		28-Jul-07	added cache support
 *		26-Aug-08	uses java default tmp dir instead of user prefs tmp dir
 */
package de.sciss.swingosc;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
//import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
//import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
//import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
//import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;

//import de.sciss.app.AbstractApplication;
import de.sciss.app.BasicEvent;
import de.sciss.app.EventManager;
//import de.sciss.eisenkraut.io.AudioStake;
//import de.sciss.eisenkraut.io.InterleavedAudioStake;
//import de.sciss.eisenkraut.io.MultiMappedAudioStake;
//import de.sciss.eisenkraut.io.PrefCacheManager;
//import de.sciss.eisenkraut.io.DecimatedTrail.AudioFileCacheInfo;
import de.sciss.gui.AquaFocusBorder;
import de.sciss.io.AudioFile;
import de.sciss.io.AudioFileCacheInfo;
import de.sciss.io.AudioFileDescr;
import de.sciss.io.CacheManager;
//import de.sciss.io.IOUtil;
import de.sciss.io.InterleavedStreamFile;
import de.sciss.io.Span;
import de.sciss.util.MutableInt;
import de.sciss.util.MutableLong;

/**
 *	@version	0.61, 17-Oct-08
 *	@author		Hanns Holger Rutz
 */
public class SoundFileView
extends JComponent
implements FocusListener
{
	public static final int			MODEL_PCM				= 0;
	public static final int			MODEL_HALFWAVE_PEAKRMS	= 1;
	public static final int			MODEL_MEDIAN			= 2;
	public static final int			MODEL_FULLWAVE_PEAKRMS	= 3;

	private int						decimChannels		= 0;

	private static final Color		colrFg				= new Color( 0xFF, 0xFF, 0x00, 0xFF );
//	private Color					colrFg2				= colrFg;
	private static Color			colrFg2				= new Color( 0xAA, 0xAA, 0x00, 0xAA );
	private Color[]					colrWave			= new Color[ 0 ];
	private Color[]					colrWave2			= new Color[ 0 ];
	private int 					recentWidth			= -1;
	private int 					recentHeight		= -1;
//	private float					xZoom				= 1.0f;
	protected Span					viewSpan			= new Span();
	private float					yZoom				= 1.0f;
	private boolean					overlay				= false;
	private boolean					lissajou			= false;
	
	private int						style				= 0;

	private boolean					gridOn				= true;
	private Color					colrGrid			= Color.blue;
	private long					gridOffset			= 0;
	private float					gridResolution		= 1.0f;
	protected boolean				timeCursorEditable	= true;
	protected boolean				timeCursorOn		= false;
	private Color					colrTimeCursor		= Color.blue;
	protected long					timeCursorPos		= 0;
	private boolean					waveOn				= true;
	private double					sampleRate			= 44100.0;
//	private int						decimation			= 64;
	
	private final Line2D			shpCursor			= new Line2D.Float();
	private final GeneralPath		shpGrid				= new GeneralPath();

	// ------- borrowing from Eisenkraut -------
	
	private final int[]				decimations			= { 8, 12, 16 };
	protected final DecimationHelp[] decimHelps;
	private final int				SUBNUM;
	private final int				MAXSHIFT;
	protected final int				MAXCOARSE;
	private final long				MAXMASK;
	private final int				MAXCEILADD;
	private final Decimator			decimator;
	private final Object			fileSync			= new Object();
	protected EventManager			asyncManager		= null;
	protected final Object			bufSync				= new Object();
	protected Thread				threadAsync			= null;
	protected boolean				keepAsyncRunning	= false;
    private AudioFile[]				tempFAsync			= null;	// lazy

    protected float[][]				tmpBuf				= null;	// lazy
	private final int				tmpBufSize;
	protected float[][]				tmpBuf2				= null;	// lazy
	private final int				tmpBufSize2;
	protected int					fullChannels;

	private final List				busyList			= new ArrayList();
	private final int				model;
	private final int				modelChannels;

	private static final Paint		pntBusy;
	private static final int[]		busyPixels			= { 0xFFCBCBCB, 0xFFC0C0C0, 0xFFA8A8A8, 0xFFE6E6E6, 0xFFB2B2B2, 0xFFCACACA,
															0xFFB1B1B1, 0xFFD5D5D5, 0xFFC0C0C0 };
//	private static final Paint		pntRMS;
//	private static final int[]		rmsPixels			= { 0xFFFFFF00, 0xFFFFFF00, 0xFFFFFF00, 0xFFFFFF00, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 };
//	private static final Stroke		strkLine			= new BasicStroke( 2.0f );
	private static final Stroke		strkLine			= new BasicStroke( 4f );

	protected AudioFile				fullScale			= null;
	private boolean					deleteFullScale		= false;
	private DecimatedStake			decimatedStake		= null;
	
//	private Insets					insets				= new Insets( 0, 0, 0, 0 );
	private int						vGap				= 1;
	private Rectangle				r					= new Rectangle();

	protected volatile float		readProgress		= 0f;
	
	// selecting
	protected final Selection[]		selections			= new Selection[ 64 ];
	protected int					selectionIndex		= 0;
	protected final Color			colrSelection		= new Color( 0x00, 0x00, 0xFF, 0x7F );
	
	// cached waveform snapshot
	protected boolean				needsImageUpdate	= true;
//	private Image					img					= null;
//	private VolatileImage			img					= null;
	private BufferedImage			img					= null;
	
	protected Span					totalSpan			= new Span();
	
	private final List				listeners			= new ArrayList();
	
	protected CacheManager			cm;
	
	static {
		BufferedImage img;
		img = new BufferedImage( 3, 3, BufferedImage.TYPE_INT_ARGB );
		img.setRGB( 0, 0, 3, 3, busyPixels, 0, 3 );
		pntBusy = new TexturePaint( img, new Rectangle( 0, 0, 3, 3 ));
//		img = new BufferedImage( 8, 1, BufferedImage.TYPE_INT_ARGB );
//		img.setRGB( 0, 0, 8, 1, rmsPixels, 0, 8 );
//		pntRMS = new TexturePaint( img, new Rectangle( 0, 0, 8, 1 ));
	}

	public SoundFileView()
	{
		super();
	
//		setOpaque( true );
		setBackground( Color.black );
		setFocusable( true );
		setBorder( new AquaFocusBorder() );
		putClientProperty( "insets", getInsets() );
		
		final MouseAdapter ma = new MouseAdapter();
		addMouseListener( ma );
		addMouseMotionListener( ma );
		addFocusListener( this );
		setCursor( new Cursor( Cursor.TEXT_CURSOR ));
		
		model		= MODEL_FULLWAVE_PEAKRMS;
		switch( model ) {
		case MODEL_HALFWAVE_PEAKRMS:
			modelChannels	= 4;
			decimator		= new HalfPeakRMSDecimator();
			break;
//		case MODEL_MEDIAN:	
//			modelChannels	= 1;
//			decimator		= new MedianDecimator();
//			break;
		case MODEL_FULLWAVE_PEAKRMS:
			modelChannels	= 3;
			decimator		= new FullPeakRMSDecimator();
			break;
		default:
			throw new IllegalArgumentException( "Model " + model );
		}
		SUBNUM		= decimations.length;	// the first 'subsample' is actually fullrate
		decimHelps	= new DecimationHelp[ SUBNUM ];
//		for( int i = 0; i < SUBNUM; i++ ) {
//			this.decimations[ i ] = new DecimationHelp( fullScale.getRate(), decimations[ i ]);
//		}
		MAXSHIFT			= decimations[ SUBNUM - 1 ];
		MAXCOARSE			= 1 << MAXSHIFT;
		MAXMASK				= -MAXCOARSE;
		MAXCEILADD			= MAXCOARSE - 1;

		tmpBufSize			= Math.max( 4096, MAXCOARSE << 1 );
		tmpBufSize2			= SUBNUM > 0 ? Math.max( 4096, tmpBufSize >> decimations[ 0 ]) : tmpBufSize;
	}
	
	public void setSelectionIndex( int idx )
	{
		ensureSelection( idx );
		selectionIndex	= idx;
	}
	
	public void setSelectionColor( int idx, Color c )
	{
		ensureSelection( idx );
		selections[ idx ].colr = c;
		efficientRepaint( selections[ idx ].span );
	}
	
	public void setSelectionSpan( int idx, long start, long stop )
	{
		setSelectionSpan( idx, new Span( start, stop ));
	}
	
	public void setSelectionSpan( int idx, Span span )
	{
		final Span dirty;
		ensureSelection( idx );
		dirty = selections[ idx ].span.isEmpty() ? span : span.union( selections[ idx ].span );
		selections[ idx ].span = span;
		efficientRepaint( dirty );
	}
	
	public void setSelectionStartEditable( int idx, boolean editable )
	{
		ensureSelection( idx );
		selections[ idx ].editableStart	= editable;
	}

	public void setSelectionSizeEditable( int idx, boolean editable )
	{
		ensureSelection( idx );
		selections[ idx ].editableSize	= editable;
	}

	public void setSelectionEditable( int idx, boolean editableStart, boolean editableSize )
	{
		ensureSelection( idx );
		selections[ idx ].editableStart	= editableStart;
		selections[ idx ].editableSize	= editableSize;
	}
		
	protected void ensureSelection( int idx )
	{
		if( (idx < 0) || (idx >= selections.length) )
			throw new IllegalArgumentException( String.valueOf( idx ));
		
		if( selections[ idx ] == null ) {
			selections[ idx ] = new Selection();
		}
	}
	
	public void setStyle( int style )
	{
		this.style			= style;
		overlay				= style > 0;
		lissajou			= style == 2;
		needsImageUpdate	= true;
		repaint();
	}
	
	public int getStyle()
	{
		return style;
	}
	
//	public void setXZoom( float f )
//	{
//		xZoom		= f;
//		recentWidth	= -1;	// triggers recalc
//		repaint();
//	}

	public void setYZoom( float f )
	{
		yZoom				= f;
		needsImageUpdate	= true;
		repaint();
	}
	
//	public float getXZoom()
//	{
//		return xZoom;
//	}

	public float getYZoom()
	{
		return yZoom;
	}
	
	public void setViewSpan( Span span )
	{
		viewSpan			= span;
		needsImageUpdate	= true;
		repaint();
	}

	public void setViewSpan( long start, long stop )
	{
		setViewSpan( new Span( start, stop ));
	}
	
	public void setWaveColor( Color c )
	{
		final Color[] cs = new Color[ fullChannels ];
		for( int i = 0; i < cs.length; i++ ) {
			cs[ i ] = c;
		}
		setWaveColors( cs );
	}

	// sync: call in event thread only
	public void setWaveColors( Color[] c )
	{
		colrWave = c;
		recalcDarkColors();
		if( waveOn ) {
			needsImageUpdate = true;
			repaint();
		}
	}
	
	public void setBackground( Color c )
	{
		super.setBackground( c );
		recalcDarkColors();
//		needsImageUpdate	= true;
	}
	
	private void recalcDarkColors()
	{
		colrWave2 = new Color[ colrWave.length ];
		final Color colrBg = getBackground();
		Color colrNorm;
		for( int i = 0; i < colrWave.length; i++ ) {
			colrNorm = colrWave[ i ];
			colrWave2[ i ] = new Color( ((colrNorm.getRed() << 1) + colrBg.getRed()) / 3,
			    						((colrNorm.getGreen() << 1) + colrBg.getGreen()) / 3,
			    						((colrNorm.getBlue() << 1) + colrBg.getBlue()) / 3,
			    						((colrNorm.getAlpha() << 1) + colrBg.getAlpha()) / 3 );
		}
	}
	
	public void setObjWaveColors( Object[] o )
	{
		final Color[] c = new Color[ o.length ];
		
		for( int i = 0; i < o.length; i++ ) {
			c[ i ] = (Color) o[ i ];
		}
		
		setWaveColors( c );
	}
	
	public void setGridColor( Color c )
	{
		colrGrid	= c;
		if( gridOn ) {
			needsImageUpdate = true;
			repaint();
		}
	}
	
	public void setGridPainted( boolean onOff )
	{
		if( onOff != gridOn ) {
			gridOn	= onOff;
			needsImageUpdate = true;
			repaint();
		}
	}
	
	public void setGridResolution( float res )
	{
		if( res != gridResolution ) {
			gridResolution	= res;
			if( gridOn ) {
				needsImageUpdate = true;
				repaint();
			}
		}
	}
	
	public void setTimeCursorColor( Color c )
	{
		colrTimeCursor	= c;
		if( timeCursorOn ) {
			efficientRepaint( new Span( timeCursorPos, timeCursorPos ));
		}
	}

	public void setTimeCursorEditable( boolean onOff )
	{
		timeCursorEditable = onOff;
	}
	
	public void setTimeCursorPainted( boolean onOff )
	{
		if( onOff != timeCursorOn ) {
			timeCursorOn	= onOff;
			efficientRepaint( new Span( timeCursorPos, timeCursorPos ));
		}
	}

	public void setTimeCursorPosition( long frame )
	{
		if( frame != timeCursorPos ) {
			final Span dirty = new Span( Math.min( frame, timeCursorPos ), Math.max( frame, timeCursorPos ));
			timeCursorPos = frame;
			if( timeCursorOn ) {
				efficientRepaint( dirty );
			}
		}
	}

	public void setWavePainted( boolean onOff )
	{
		if( onOff != waveOn ) {
			waveOn				= onOff;
			needsImageUpdate 	= true;
			repaint();
		}
	}
	
	public void setCacheManager( CacheManager cm )
	{
		this.cm	= cm;
	}

	protected void subsampleWrite( float[][] inBuf, float[][] outBuf, DecimatedStake das, int len, AudioFile cacheAF )
	throws IOException
	{
		int	decim;
	
		if( SUBNUM < 1 ) return;
		
		decim			= decimHelps[ 0 ].shift;
		// calculate first decimation from fullrate PCM
		len				>>= decim;
		if( inBuf != null ) {
			decimator.decimatePCM( inBuf, outBuf, 0, len, 1 << decim );
			das.continueWrite( 0, outBuf, 0, len );
			if( cacheAF != null ) {
				cacheAF.writeFrames( outBuf, 0, len );
//				cacheOff += len;
			}
		}

		subsampleWrite2( outBuf, das, len );

//		// calculate remaining decimations from preceding ones
//		for( int i = 1; i < SUBNUM; i++ ) {
//			decim			  = decimHelps[ i ].shift - decimHelps[ i - 1 ].shift;
//			len				>>= decim;
//			decimator.decimate( outBuf, outBuf, 0, len, 1 << decim );
//			das.continueWrite( i, outBuf, 0, len );
//		} // for( SUBNUM )
	}

	// same as subsampleWrite but input is already at first decim stage
	protected void subsampleWrite2( float[][] buf, DecimatedStake das, int len )
	throws IOException
	{
		int	decim;
		
		// calculate remaining decimations from preceding ones
		for( int i = 1; i < SUBNUM; i++ ) {
			decim			  = decimHelps[ i ].shift - decimHelps[ i - 1 ].shift;
			len				>>= decim;
//			framesWritten	>>= decim;
			decimator.decimate( buf, buf, 0, len, 1 << decim );
//			ste[i].continueWrite( ts[i], framesWritten, outBuf, 0, len );
			das.continueWrite( i, buf, 0, len );
		} // for( SUBNUM )
	}

	// @synchronization	caller must have sync on fileSync !!!
 	private DecimatedStake allocAsync( Span span )
	throws IOException
	{
		if( !Thread.holdsLock( fileSync )) throw new IllegalMonitorStateException();

		final long floorStart		= span.start & MAXMASK;
		final long ceilStop			= (span.stop + MAXCEILADD) & MAXMASK;
		final Span extSpan			= (floorStart == span.start) && (ceilStop == span.stop) ? span : new Span( floorStart, ceilStop );
		final Span[] fileSpans		= new Span[ SUBNUM ];
		final Span[] biasedSpans	= new Span[ SUBNUM ];
		long fileStart;
		long fileStop;
			
        if( tempFAsync == null ) {
			// XXX THIS IS THE PLACE TO OPEN WAVEFORM CACHE FILE
			tempFAsync = createTempFiles();
        }
		synchronized( tempFAsync ) {
			for( int i = 0; i < SUBNUM; i++ ) {
				fileStart			= tempFAsync[ i ].getFrameNum();
				fileStop			= fileStart + (extSpan.getLength() >> decimHelps[ i ].shift);
				tempFAsync[ i ].setFrameNum( fileStop );
				fileSpans[ i ]		= new Span( fileStart, fileStop );
				biasedSpans[ i ]	= extSpan;
			}
		}
		return new DecimatedStake( extSpan, tempFAsync, fileSpans, biasedSpans, decimHelps );
	}

	/*
	 *	@synchronization	call within synchronoized( bufSync ) block
	 */
	private void createBuffers()
	{
		if( !Thread.holdsLock( bufSync )) throw new IllegalMonitorStateException();
	
		if( tmpBuf == null ) {
			tmpBuf		= new float[ fullChannels ][ tmpBufSize ];
			tmpBuf2		= new float[ decimChannels ][ tmpBufSize2 ];
		}
	}

	private void freeBuffers()
	{
		synchronized( bufSync ) {
			tmpBuf	= null;
			tmpBuf2	= null;
		}
	}

	private AudioFile[] createTempFiles()
    throws IOException
	{
		// simply use an AIFC file with float format as temp file
		final AudioFileDescr proto	= new AudioFileDescr();
		final AudioFile[] tempF		= new AudioFile[ SUBNUM ];
		AudioFileDescr afd;
		proto.type					= AudioFileDescr.TYPE_AIFF;
		proto.channels				= decimChannels;
		proto.bitsPerSample			= 32;
		proto.sampleFormat			= AudioFileDescr.FORMAT_FLOAT;
		try {
			for( int i = 0; i < SUBNUM; i++ ) {
				afd						= new AudioFileDescr( proto );
				afd.file				= File.createTempFile( "swing", null, null );
				afd.file.deleteOnExit();
				afd.rate				= decimHelps[ i ].rate;
				tempF[ i ]				= AudioFile.openAsWrite( afd );
			}
			return tempF;
		}
		catch( IOException e1 ) {
			for( int i = 0; i < SUBNUM; i++ ) {
				if( tempF[ i ] != null ) tempF[ i ].cleanUp();
			}
			throw e1;
		}
	}
	
	private void deleteTempFiles( AudioFile[] tempF )
	{
		for( int i = 0; i < tempF.length; i++ ) {
			if( tempF[ i ] != null ) {
				tempF[ i ].cleanUp();
				tempF[ i ].getFile().delete();
			}
		}
	}
	
	private void disposeFullScale()
	{
		if( fullScale != null ) {
			fullScale.cleanUp();
			if( deleteFullScale ) {
				final File f = fullScale.getFile();
				if( f.exists() && !f.delete() ) {
					System.err.println( "Warning: temp file '" + f.getAbsolutePath() +
						"could not be deleted" );
					// f.deleteOnExit() ?
				}
			}
			fullScale = null;
		}
	}
	
	public void readSndFile( String path, long startFrame, long numFrames )
	throws IOException
	{
		readSndFile( path, startFrame, numFrames, false );
	}
	
	public void readSndFile( String path, long startFrame, long numFrames,
			                 boolean deleteWhenDisposed )
	throws IOException
	{
		if( threadAsync != null ) throw new IllegalStateException();
	
		boolean launched = false;
		
		try {
			disposeFullScale();
			freeBuffers();
			freeTempFiles();
			
			fullScale						= AudioFile.openAsRead( new File( path ));
			deleteFullScale					= deleteWhenDisposed;
			final AudioFileDescr	afd		= fullScale.getDescr();
			
			fullChannels	= afd.channels;
			decimChannels	= fullChannels * modelChannels;
			startFrame		= Math.max( 0, Math.min( afd.length, startFrame ));
			numFrames		= Math.max( 0, Math.min( afd.length - startFrame, numFrames ));
			viewSpan		= new Span( startFrame, startFrame + numFrames );
			totalSpan		= new Span( viewSpan );
		
			final DecimatedStake		das;
			final Span					extSpan;
			final long					fullrateStop, fullrateLen;
			final int					numFullBuf;
			final Object				enc_this	= this;
			final AudioFile				cacheReadAF;
			final AudioFile				cacheWriteAF;
	
			for( int i = 0; i < SUBNUM; i++ ) {
				this.decimHelps[ i ] = new DecimationHelp( afd.rate, decimations[ i ]);
			}
	
			synchronized( fileSync ) {
				das			= allocAsync( viewSpan );
			}
			extSpan			= das.getSpan();
	
	// XXX SWINGOSC XXX
	//		fullrateStop	= Math.min( extSpan.getStop(), fullScale.editGetSpan( ce ).stop );
			fullrateStop	= Math.min( extSpan.stop, viewSpan.stop );
			fullrateLen		= fullrateStop - extSpan.start;

//			numFullBuf		= (int) (fullrateLen >> MAXSHIFT);
			
			cacheReadAF		= openCacheForRead( model );
			if( cacheReadAF == null ) {
//				cacheWriteAS = fullScale.openCacheForWrite( model,
//						decimHelps[ 0 ].fullrateToSubsample( union.getLength() ));
				cacheWriteAF = openCacheForWrite( model, (fullrateLen + MAXCEILADD) & MAXMASK );
				numFullBuf	= (int) (fullrateLen >> MAXSHIFT);
			} else {
				numFullBuf	= (int) ((fullrateLen + MAXCEILADD) >> MAXSHIFT);	// cached files always have integer fullBufs!
				cacheWriteAF = null;
			}

			synchronized( bufSync ) {
				createBuffers();
			}
	
	// XXX SWINGOSC XXX		
	//		editClear( source, das.getSpan(), ce );
	//		editAdd( source, das, ce );
	decimatedStake = das;
			launched = true;
		
			threadAsync = new Thread( new Runnable() {
				public void run() {
					final int	minCoarse;
					boolean		success				= false;
					long		pos					= extSpan.getStart();
//					long		framesWrittenCache 	= 0;
					boolean		cacheWriteComplete	= false;
					long		framesWritten		= 0;
					Span		tag2;
					float		f1;
					int			len, repaint		= 0;
					long		time, nextTime		= System.currentTimeMillis() + 100;
	
					minCoarse	= MAXCOARSE >> decimHelps[ 0 ].shift;

					try {
						for( int i = 0; (i < numFullBuf) && keepAsyncRunning; i++ ) {
							synchronized( bufSync ) {
								if( cacheReadAF != null ) {
									tag2			 = new Span( pos, pos + minCoarse );
									cacheReadAF.readFrames( tmpBuf2, 0, minCoarse );
									das.continueWrite( 0, tmpBuf2, 0, minCoarse );
									subsampleWrite2( tmpBuf2, das, minCoarse );
									pos				+= minCoarse;
								} else {
									tag2			 = new Span( pos, pos + MAXCOARSE );
			//						fullScale.readFrames( tmpBuf, 0, tag2, ce );
									fullScale.readFrames( tmpBuf, 0, MAXCOARSE );
//	for( int k = 0; k < tmpBuf.length; k++ ) { for( int j = 0; j < MAXCOARSE; j++ ) { tmpBuf[ k ][ j ] = 0.125f; }}
									subsampleWrite( tmpBuf, tmpBuf2, das, MAXCOARSE, cacheWriteAF );
									pos				+= MAXCOARSE;
//									framesWrittenCache += minCoarse;
								}
								framesWritten += MAXCOARSE;
							}
							time = System.currentTimeMillis();
							if( time >= nextTime ) {
								readProgress = (float) ((double) framesWritten / (double) fullrateLen);
								nextTime = time + 100;
								if( asyncManager != null ) asyncManager.dispatchEvent( new AsyncEvent( enc_this, AsyncEvent.UPDATE, time ));
	// XXX SWINGOSC XXX
								repaint = (repaint + 1) % 20;
								if( repaint == 0 ) {
									needsImageUpdate = true;
									repaint();
								}
							}
						}
	
						if( (cacheReadAF == null) && keepAsyncRunning ) { // cached files always have integer fullBufs!
							len = (int) (fullrateStop - pos);
							if( len > 0 ) {
								synchronized( bufSync ) {
									tag2 = new Span( pos, pos + len );
		// XXX SWINGOSC XXX
		//							fullScale.readFrames( tmpBuf, 0, tag2, null );
									if( fullScale.getFramePosition() != tag2.start ) {
										fullScale.seekFrame( tag2.start );
									}
									fullScale.readFrames( tmpBuf, 0, (int) tag2.getLength() );
									for( int ch = 0; ch < fullChannels; ch++ ) {
										f1 = tmpBuf[ ch ][ len - 1 ];
										for( int i = len; i < MAXCOARSE; i++ ) {
											tmpBuf[ ch ][ i ] = f1;
										}
									}
									subsampleWrite( tmpBuf, tmpBuf2, das, MAXCOARSE, cacheWriteAF );
									pos				+= MAXCOARSE;
									framesWritten	+= MAXCOARSE;
//									framesWrittenCache += minCoarse;
								}
							}
						}
						cacheWriteComplete = true;
						if( cacheWriteAF != null ) cm.addFile( cacheWriteAF.getFile() );
						success	= true;
					}
					catch( IOException e1 ) {
	// XXX SWINGOSC XXX
	//					System.err.println( e1 );
//						System.out.println( e1 );
						e1.printStackTrace( System.out );
					}
					finally {
						if( cacheReadAF != null ) cacheReadAF.cleanUp();
						if( cacheWriteAF != null ) {
							cacheWriteAF.cleanUp();
							if( !cacheWriteComplete ) { // indicates process was aborted ...
								final File f = createCacheFileName();
								if( (cm != null) && (f != null) ) {		// ... therefore delete incomplete cache files!
									cm.removeFile( f );
								}
							}
						}

						if( asyncManager != null ) {
							asyncManager.dispatchEvent( new AsyncEvent( enc_this,
								success ? AsyncEvent.FINISHED : (keepAsyncRunning ? AsyncEvent.FAILED : AsyncEvent.CANCELLED),
								System.currentTimeMillis() ));
						}
						
						synchronized( threadAsync ) {
							threadAsync.notifyAll();
							threadAsync = null;
						}

						needsImageUpdate = true;
						repaint();
					}
				}
			});
			
			keepAsyncRunning 	= true;
			threadAsync.start();
			launched			= true;
		}
		finally {
			if( !launched ) {
				keepAsyncRunning = false;
//				 XXX should be fused with dispose()
				disposeFullScale();
				freeBuffers();
				freeTempFiles();
				if( asyncManager != null ) {
					asyncManager.dispatchEvent( new AsyncEvent( this, AsyncEvent.FAILED, System.currentTimeMillis()  ));
				}
			}
		}
	}
	
	public AudioFile getSoundFile()
	{
		return fullScale;
	}
	
	public float getReadProgress()
	{
		return readProgress;
	}

	// XXX should be fused with dispose()
	public void cancelAsyncRead()
	{
		killAsyncThread();
		disposeFullScale();
		freeBuffers();
		freeTempFiles();
	}
	
	private void killAsyncThread()
	{
		if( threadAsync != null ) {
			synchronized( threadAsync ) {
				if( threadAsync.isAlive() ) {
					keepAsyncRunning = false;
					try {
						threadAsync.wait();
					}
					catch( InterruptedException e1 ) {
						System.err.println( e1 );
					}
				}
			}
			threadAsync = null;
		}
	}
	
	public boolean isBusy()
	{
		return( (threadAsync != null) && threadAsync.isAlive() );
	}

	public void addAsyncListener( AsyncListener l )
	{
// XXX SWINGOSC XXX
//		if( !isBusy() ) {
//			l.asyncFinished( new AsyncEvent( this, AsyncEvent.FINISHED, System.currentTimeMillis() ));
//			return;
//		}
		if( asyncManager == null ) asyncManager = new EventManager( new EventManager.Processor() {
			public void processEvent( BasicEvent e )
			{
				AsyncListener		l;
				final AsyncEvent	ae = (AsyncEvent) e;
				
				for( int i = 0; i < asyncManager.countListeners(); i++ ) {
					l = (AsyncListener) asyncManager.getListener( i );
					switch( e.getID() ) {
					case AsyncEvent.UPDATE:
						l.asyncUpdate( ae );
						break;
					case AsyncEvent.FINISHED:
						l.asyncFinished( ae );
						break;
					case AsyncEvent.FAILED:
						l.asyncFailed( ae );
						break;
					case AsyncEvent.CANCELLED:
						l.asyncCancelled( ae );
						break;
					default:
						assert false : e.getID();
						break;
					}
				}
			}
		});
		asyncManager.addListener( l );
	}

	public void removeAsyncListener( AsyncListener l )
	{
		if( asyncManager != null ) asyncManager.removeListener( l );
	}

	public void dispose()
	{
		listeners.clear();
		killAsyncThread();	// this has to be the first step
		if( asyncManager != null ) asyncManager.dispose();
// XXX SWINGOSC XXX
		disposeImage();
//		fullScale.removeDependant( this );
		disposeFullScale();
		freeBuffers();
		freeTempFiles();
// XXX SWINGOSC XXX
//		super.dispose();
	}

	private void freeTempFiles()
	{
		synchronized( fileSync ) {
// XXX SWINGOSC XXX
if( decimatedStake != null ) {
	decimatedStake.dispose();
	decimatedStake = null;
}
// XXX SWINGOSC XXX
//			if( tempF != null ) {
//				deleteTempFiles( tempF );
//			}
			// XXX THIS IS THE PLACE TO KEEP WAVEFORM CACHE FILE
			if( tempFAsync != null ) {
				deleteTempFiles( tempFAsync );
				tempFAsync = null;
			}
		}
	}

	protected File createCacheFileName()
    {
    	if( (cm == null) || !cm.isActive() ) return null;
    	
		return cm.createCacheFileName( fullScale.getFile() );
    }
	
//	private int[][] createCacheChannelMaps()
//	{
//		final int[][] fullChanMaps	= fullScale.getChannelMaps();
//		final int[][] cacheChanMaps	= new int[ fullChanMaps.length ][];
//		
//		for( int i = 0; i < fullChanMaps.length; i++ ) {
//			cacheChanMaps[ i ] = new int[ fullChanMaps[ i ].length * modelChannels ];
//			for( int j = 0; j < cacheChanMaps[ i ].length; j++ ) {
//				cacheChanMaps[ i ][ j ] = j;
//			}
//		}
//		
//		return cacheChanMaps;
//	}

	/*
	 * 	@returns	the cached stake or null if no cache file is available
	 */
	private AudioFile openCacheForRead( int model )
	throws IOException
	{
		final File			f			= createCacheFileName();
		if( f == null ) return null;
		
		final String		ourCode		= "EisK"; // AbstractApplication.getApplication().getMacOSCreator();
		AudioFile			cacheAF		= null;
		AudioFileDescr		afd;
		boolean				success		= false;
		byte[]				appCode;
		AudioFileCacheInfo	infoA, infoB;

		try {
			if( !f.isFile() ) return null;
			cacheAF			= AudioFile.openAsRead( f );
			cacheAF.readAppCode();
			afd				= cacheAF.getDescr();
			appCode			= (byte[]) afd.getProperty( AudioFileDescr.KEY_APPCODE );
			if( ourCode.equals( afd.appCode ) && (appCode != null) ) {
				infoA		= AudioFileCacheInfo.decode( appCode );
				if( infoA != null ) {
					infoB	= new AudioFileCacheInfo( fullScale, model, fullScale.getFrameNum() );
					if( !infoA.equals( infoB )) {
						return null;
					}
				}
			} else {
				return null;
			}
			success = true;
			return cacheAF;
		}
		finally {
			if( !success ) {
				if( cacheAF != null ) {
					cacheAF.cleanUp();
				}
			}
		}
	}

	private AudioFile openCacheForWrite( int model, long decimFrameNum )
	throws IOException
	{
		final File					f			= createCacheFileName();
		if( f == null ) return null;

		final AudioFileDescr		afd			= new AudioFileDescr();
		final String				ourCode		= "EisK"; // AbstractApplication.getApplication().getMacOSCreator();
		final AudioFileCacheInfo	info;

		afd.type			= AudioFileDescr.TYPE_AIFF;
		afd.bitsPerSample	= 32;
		afd.sampleFormat	= AudioFileDescr.FORMAT_FLOAT;
		afd.rate			= decimHelps[ 0 ].rate; // getRate();
		afd.appCode			= ourCode;

		cm.removeFile( f );		// in case it existed
		afd.channels		= fullScale.getChannelNum() * modelChannels;
		afd.file			= f;
		info				= new AudioFileCacheInfo( fullScale, model, fullScale.getFrameNum() );
		afd.setProperty( AudioFileDescr.KEY_APPCODE, info.encode() );
		return AudioFile.openAsWrite( afd );
	}
		
	private int drawPCM( float[] frames, int len, int[] polyX, int[] polyY, int off,
			 			 float offX, float scaleX, float scaleY, boolean sampleAndHold )
	{
		int		x, y;

		if( sampleAndHold ) {
			x = (int) offX;
			for( int i = 0; i < len; ) {
				y				= (int) (frames[ i ] * scaleY);
				polyX[ off ]	= x;
				polyY[ off ]	= y;
				off++;
				i++;
				x				= (int) (i * scaleX + offX);
				polyX[ off ]	= x;
				polyY[ off ]	= y;
				off++;
			}
		} else {
			for( int i = 0; i < len; i++, off++ ) {
				x				= (int) (i * scaleX + offX);
				polyX[ off ]	= x;
				polyY[ off ]	= (int) (frames[ i ] * scaleY);
			}
		}
		
		return off;
	}

	private int drawHalfWavePeakRMS( float[] sPeakP, float[] sPeakN, float[] sRMSP, float[] sRMSN, int len,
									 int[] peakPolyX, int[] peakPolyY, int[] rmsPolyX, int[] rmsPolyY, int off,
									 float offX, float scaleX, float scaleY )
	{
		final float	scaleYN	= -scaleY;
		int			x;
		
		for( int i = 0, k = peakPolyX.length - 1 - off; i < len; i++, off++, k-- ) {
			x					= (int) (i * scaleX + offX);
			peakPolyX[ off ]	= x;
			peakPolyX[ k ]		= x;
			rmsPolyX[ off ]		= x;
			rmsPolyX[ k ]		= x;
			peakPolyY[ off ]	= (int) (sPeakP[ i ] * scaleY);
			peakPolyY[ k ]		= (int) (sPeakN[ i ] * scaleY);
			rmsPolyY[ off ]		= (int) ((float) Math.sqrt( sRMSP[ i ]) * scaleY);
			rmsPolyY[ k ]		= (int) ((float) Math.sqrt( sRMSN[ i ]) * scaleYN);
		}
		
		return off;
	}

	private int drawFullWavePeakRMS( float[] sPeakP, float[] sPeakN, float[] sRMS, int len,
			  int[] peakPolyX, int[] peakPolyY, int[] rmsPolyX, int[] rmsPolyY, int off,
			  float offX, float scaleX, float scaleY )
	{
//		final float	scaleYN	= -scaleY;
		int			x;
		float		peakP, peakN, rms;
		
		for( int i = 0, k = peakPolyX.length - 1 - off; i < len; i++, off++, k-- ) {
			x					= (int) (i * scaleX + offX);
			peakPolyX[ off ]	= x;
			peakPolyX[ k ]		= x;
			rmsPolyX[ off ]		= x;
			rmsPolyX[ k ]		= x;
			peakP				= sPeakP[ i ];
			peakN				= sPeakN[ i ];
			peakPolyY[ off ]	= (int) (peakP * scaleY) + 2;
			peakPolyY[ k ]		= (int) (peakN * scaleY) - 2;
//			peakC				= (peakP + peakN) / 2;
			rms					= (float) Math.sqrt( sRMS[ i ]); // / 2;
			rmsPolyY[ off ]		= (int) (Math.min( peakP, rms ) * scaleY);
			rmsPolyY[ k ]		= (int) (Math.max( peakN, -rms ) * scaleY);
		}
	
		return off;
	}

	private Rectangle rectForChannel( int ch )
	{
		final int 		y, h;
		final Insets	ins		= getInsets();
		final int 		ht		= getHeight() - (ins.top + ins.bottom);
		final int		w		= getWidth() - (ins.left + ins.right);

		if( overlay || lissajou ) {
			y					= 0; // insets.top;
			h					= ht;
		} else {
			final int temp		= ht * ch / fullChannels;
			y					= temp; // insets.top + temp;
			h					= (ht * (ch + 1) / fullChannels) - temp - vGap;
		}			
		r.setBounds( 0, y, w, h );
		return r;
	}

//	private Rectangle rectForChannel( int ch )
//	{
//		final int y, h;
//		final int ht			= getHeight();
//	
//		if( overlay || lissajou ) {
//			y					= insets.top;
//			h					= ht;
//		} else {
//			final int temp		= ht * ch / fullChannels;
//			y					= insets.top + temp;
//			h					= (ht * (ch + 1) / fullChannels) - temp - vGap;
//		}			
//		r.setBounds( insets.left, y, getWidth(), h );
//		
//		return r;
//	}

	/*
	 *	Speed measurements (feb 2006): for HalfwavePeakRMS, using g2.fillPolygon is about twice as fast as using
	 *	GeneralPath objects. The integer resolution can be compensated for by scaling the points by
	 *	factor 4.0 and scaling the Graphics2D by 1/4 at no significant CPU cost.
	 *
	 *	@synchronization	must be called in the event thread
	 */
//	private void drawWaveform( DecimationInfo info, WaveformView view, Graphics2D g2 )
	private void drawWaveform( DecimationInfo info, Graphics2D g2 )
	{
		final boolean		fromPCM			= info.idx == -1;
		final boolean		toPCM			= fromPCM && (info.inlineDecim == 1);
		final long			maxLen			= toPCM ? tmpBufSize :
												(fromPCM ? Math.min( tmpBufSize, tmpBufSize2 * info.getDecimationFactor() ) :
												tmpBufSize2 << info.shift);
		final int			polySize		= (int) (info.sublength << 1);
		final AffineTransform atOrig		= g2.getTransform();
		final Shape			clipOrig		= g2.getClip();
		
		final int[][]		peakPolyX		= new int[ fullChannels ][ polySize ];
		final int[][]		peakPolyY		= new int[ fullChannels ][ polySize ];
		final int[][]		rmsPolyX		= toPCM ? null : new int[ fullChannels ][ polySize ];
		final int[][]		rmsPolyY		= toPCM ? null : new int[ fullChannels ][ polySize ];
		final boolean[]		sampleAndHold	= toPCM ? new boolean[ fullChannels ] : null;
// XXX SWINGOSC XXX
//		final float			deltaYN			= 4f / (view.getMin() - view.getMax());
		final float			deltaYN			= -2f * yZoom;
		
		float[]				sPeakP, sPeakN, sRMSP, sRMSN;
		float				offX, scaleX, scaleY;
		long				start			= info.span.start;
		long				totalLength		= info.getTotalLength();
		Span				chunkSpan;
		long				fullLen, fullStop;
		int					chunkLen, decimLen;
		final int[]			off				= new int[ fullChannels ];
		Rectangle			r;
		
		try {
			busyList.clear();	// "must be called in the event thread"
		
			synchronized( bufSync ) {
				createBuffers();

				while( totalLength > 0 ) {
					fullLen		= Math.min( maxLen, totalLength );
					chunkLen	= (int) (fromPCM ? fullLen : decimHelps[ info.idx ].fullrateToSubsample( fullLen ));
					decimLen	= chunkLen / info.inlineDecim;
					chunkLen    = decimLen * info.inlineDecim;
					fullLen		= (long) chunkLen << info.shift;
					chunkSpan	= new Span( start, start + fullLen );

					if( fromPCM ) {
// XXX SWINGOSC XXX
//						fullStop = fullScale.getSpan().stop;
						fullStop = fullScale.getFrameNum();
						if( start + fullLen <= fullStop ) {
							chunkSpan	= new Span( start, start + fullLen );
// XXX SWINGOSC
//							fullScale.readFrames( tmpBuf, 0, chunkSpan );
							if( fullScale.getFramePosition() != chunkSpan.start ) {
								fullScale.seekFrame( chunkSpan.start );
							}
							fullScale.readFrames( tmpBuf, 0, (int) chunkSpan.getLength() );
						} else {
							chunkSpan	= new Span( start, fullStop );
//  XXX SWINGOSC
//							fullScale.readFrames( tmpBuf, 0, chunkSpan );
							if( fullScale.getFramePosition() != chunkSpan.start ) {
								fullScale.seekFrame( chunkSpan.start );
							}
							fullScale.readFrames( tmpBuf, 0, (int) chunkSpan.getLength() );
							// duplicate last frames
							for( int i = (int) chunkSpan.getLength(), j = i - 1; i < (int) fullLen; i++ ) {
								for( int ch = 0; ch < fullChannels; ch++ ) {
									sPeakP = tmpBuf[ ch ];
									sPeakP[ i ] = sPeakP[ j ];
								}
							}
						}
						if( !toPCM ) decimator.decimatePCM( tmpBuf, tmpBuf2, 0, decimLen, info.inlineDecim );
					} else {
						chunkSpan	= new Span( start, start + fullLen );
// XXX SWINGOSC XXX
//						readFrames( info.idx, tmpBuf2, 0, busyList, chunkSpan, null );
						readFrames( info.idx, tmpBuf2, 0, busyList, chunkSpan );
						if( info.inlineDecim > 1 ) decimator.decimate( tmpBuf2, tmpBuf2, 0, decimLen, info.inlineDecim );
					}
					if( toPCM ) {
						for( int ch = 0; ch < fullChannels; ch++ ) {
							sPeakP				= tmpBuf[ ch ];
// XXX SWINGOSC XXX
//							r					= view.rectForChannel( ch );
							r					= rectForChannel( ch );
							scaleX				= 4 * r.width / (float) (info.sublength - 1);
							scaleY				= r.height * deltaYN;
							offX				= scaleX * off[ ch ];
							sampleAndHold[ ch ]	= scaleX > 16;

							off[ ch ]			= drawPCM( sPeakP, decimLen, peakPolyX[ ch ], peakPolyY[ ch ], off[ ch ], offX, scaleX, scaleY, sampleAndHold[ ch ]);
						}
					} else {
						switch( model ) {
						case MODEL_HALFWAVE_PEAKRMS:
							for( int ch = 0, chPeakN = fullChannels, chRMSP = 2 * fullChannels, chRMSN = 3 * fullChannels; ch < fullChannels;
						 	 ch++, chPeakN++, chRMSP++, chRMSN++ ) {
							 
							sPeakP	= tmpBuf2[ ch ];
							sPeakN	= tmpBuf2[ chPeakN ];
							sRMSP	= tmpBuf2[ chRMSP ];
							sRMSN	= tmpBuf2[ chRMSN ];

//XXX SWINGOSC XXX
//							r		= view.rectForChannel( ch );
							r		= rectForChannel( ch );
							scaleX  = 4 * r.width / (float) (info.sublength - 1);
							scaleY	= r.height * deltaYN;
							offX	= scaleX * off[ ch ];

							off[ ch ] = drawHalfWavePeakRMS( sPeakP, sPeakN, sRMSP, sRMSN, decimLen,
															 peakPolyX[ ch ], peakPolyY[ ch ], rmsPolyX[ ch ], rmsPolyY[ ch ],
															 off[ ch ], offX, scaleX, scaleY );
						}
						break;
// XXX SWINGOSC XXX
//						case MODEL_MEDIAN:
//							throw new IllegalStateException( "Median drawing not yet working" );
//							
						case MODEL_FULLWAVE_PEAKRMS:
							for( int ch = 0, chPeakN = fullChannels, chRMS = 2 * fullChannels; ch < fullChannels;
							 ch++, chPeakN++, chRMS++ ) {
							 
							sPeakP	= tmpBuf2[ ch ];
							sPeakN	= tmpBuf2[ chPeakN ];
							sRMSP	= tmpBuf2[ chRMS ];

//XXX SWINGOSC XXX
//							r		= view.rectForChannel( ch );
							r		= rectForChannel( ch );
							scaleX  = 4 * r.width / (float) (info.sublength - 1);
							scaleY	= r.height * deltaYN;
							offX	= scaleX * off[ ch ];

							off[ ch ] = drawFullWavePeakRMS( sPeakP, sPeakN, sRMSP, decimLen,
															  peakPolyX[ ch ], peakPolyY[ ch ], rmsPolyX[ ch ], rmsPolyY[ ch ],
															  off[ ch ], offX, scaleX, scaleY );
						}
						break;

						default:
							assert false : model;
							break;
						}
					}
					start		+= fullLen;
					totalLength -= fullLen;
				}
			} // synchronized( bufSync )

			if( toPCM ) {
				final Stroke strkOrig = g2.getStroke();
				g2.setStroke( strkLine );
// XXX SWINGOSC XXX
//				g2.setPaint( Color.black );
				for( int ch = 0; ch < fullChannels; ch++ ) {
// XXX SWINGOSC XXX
					g2.setColor( colrWave.length > ch ? colrWave[ ch ] : colrFg );
// XXX SWINGOSC XXX
//					r		= view.rectForChannel( ch );
					r		= rectForChannel( ch );
					g2.clipRect( r.x, r.y, r.width, r.height );
					g2.translate( r.x, r.y + r.height * 0.5f );
					g2.scale( 0.25f, 0.25f );
					g2.drawPolyline( peakPolyX[ ch ], peakPolyY[ ch ], off[ ch ]);
					g2.setTransform( atOrig );
					g2.setClip( clipOrig );
				}
				g2.setStroke( strkOrig );
			} else {
				for( int ch = 0; ch < fullChannels; ch++ ) {
//					 XXX SWINGOSC XXX
//					r		= view.rectForChannel( ch );
					r		= rectForChannel( ch );
					g2.clipRect( r.x, r.y, r.width, r.height );
					if( !busyList.isEmpty() ) {
						g2.setPaint( pntBusy );
						for( int i = 0; i < busyList.size(); i++ ) {
							chunkSpan = (Span) busyList.get( i );
							scaleX  = r.width / (float) info.getTotalLength(); // (info.sublength - 1);
							g2.fillRect( (int) ((chunkSpan.start - info.span.start) * scaleX) + r.x, r.y, (int) (chunkSpan.getLength() * scaleX), r.height );
						}
					}
					g2.translate( r.x, r.y + r.height * 0.5f );
					g2.scale( 0.25f, 0.25f );
// XXX SWINGOSC XXX
//					g2.setColor( Color.gray );
					g2.setColor( colrWave2.length > ch ? colrWave2[ ch ] : colrFg2 );
					g2.fillPolygon( peakPolyX[ ch ], peakPolyY[ ch ], polySize );
// XXX SWINGOSC XXX
//					g2.setColor( Color.black );
//					g2.setColor( colrWave.length > ch ? colrWave[ ch ] : colrFg );
//g2.setPaint( pntRMS );
//g2.setColor( getBackground() );
					g2.setColor( colrWave.length > ch ? colrWave[ ch ] : colrFg );
					g2.fillPolygon( rmsPolyX[ ch ], rmsPolyY[ ch ], polySize );
					g2.setTransform( atOrig );
					g2.setClip( clipOrig );
				}
			}
		}
		catch( IOException e1 ) {
			System.err.println( e1 );
		}
	}

    private void readFrames( int sub, float[][] data, int dataOffset, List busyList, Span readSpan )
    throws IOException
    {
    	final DecimatedStake 	stake		= decimatedStake;
		final long				startR		= decimHelps[ sub ].roundAdd - readSpan.start;
		final MutableInt		readyLen	= new MutableInt( 0 );
		final MutableInt		busyLen		= new MutableInt( 0 );
		int						chunkLen, discrepancy;
		Span					subSpan;
		int						readOffset, nextOffset = dataOffset;
		int						len			= (int) (readSpan.getLength() >> decimHelps[ sub ].shift);
		
		subSpan		= new Span( Math.max( stake.getSpan().start, readSpan.start ),
								Math.min( stake.getSpan().stop, readSpan.stop ));
		stake.readFrames( sub, data, nextOffset, subSpan, readyLen, busyLen );
		chunkLen	= readyLen.value() + busyLen.value();
		readOffset	= nextOffset + readyLen.value(); // chunkLen;
		nextOffset	= (int) ((subSpan.stop + startR) >> decimHelps[ sub ].shift) + dataOffset;
		discrepancy	= nextOffset - readOffset;
		len		   -= readyLen.value() + discrepancy;
		if( busyLen.value() == 0 ) {
			if( discrepancy > 0 ) {
				if( readOffset > 0 ) {
					for( int i = readOffset, k = readOffset - 1; i < nextOffset; i++ ) {
						for( int j = 0; j < data.length; j++ ) {
							data[ j ][ i ] = data[ j ][ k ];
						}
					}
				}
			}
		} else {
			busyList.add( new Span( subSpan.stop - (subSpan.getLength() * busyLen.value() / chunkLen), subSpan.stop ));
			for( int i = Math.max( 0, readOffset ); i < nextOffset; i++ ) {
				for( int j = 0; j < data.length; j++ ) {
					data[ j ][ i ] = 0f;
				}
			}
		}
    }

	public DecimationInfo getBestSubsample( Span tag, int minLen )
	{
		final DecimationInfo	info;
		final boolean			fromPCM, toPCM;
		long					subLength, n;
		int						idx, inlineDecim;
		
		subLength		= tag.getLength();
		for( idx = 0; idx < SUBNUM; idx++ ) {
			n			= decimHelps[ idx ].fullrateToSubsample( tag.getLength() );
			if( n < minLen ) break;
			subLength	= n;
		}
		idx--;
		// had to change '>= minLen' to '> minLen' because minLen could be zero!
		switch( model ) {
		case MODEL_HALFWAVE_PEAKRMS:
		case MODEL_FULLWAVE_PEAKRMS:
			for( inlineDecim = 2; subLength / inlineDecim > minLen; inlineDecim++ ) ;
			inlineDecim--;
			break;
		
		case MODEL_MEDIAN:
			inlineDecim = 1;
			break;
		
		default:
			assert false : model;
			inlineDecim = 1;	// never gets here
		}
		subLength /= inlineDecim;
		fromPCM		= idx == -1;
		toPCM		= fromPCM && inlineDecim == 1;
		info		= new DecimationInfo( tag, subLength,
										  toPCM ? fullChannels : decimChannels,
										  idx, fromPCM ? 0 : decimHelps[ idx ].shift, 
										  inlineDecim, toPCM ? MODEL_PCM : model );
		return info;
	}
	
	protected void efficientRepaint( Span dirty )
	{	
		final double 	hScale;
		final Insets	ins	= getInsets();
		final int		w	= getWidth() - ins.left - ins.right;
		final int		h	= getHeight() - ins.top - ins.bottom;
		final int		x, x2;
		
		hScale 	= (double) w / (double) Math.max( 1, viewSpan.getLength() );
		x		= Math.max( 0, (int) ((dirty.start - viewSpan.start) * hScale) );
		x2		= Math.min( w, (int) ((dirty.stop - viewSpan.start) * hScale) + 1 );
		if( x2 >= x ) {
			repaint( new Rectangle( x + ins.left, ins.top, x2 - x + 1, h ));
		}
	}
	
	private void disposeImage()
	{
		if( img != null ) {
			img.flush();
			img = null;
		}
	}

	private void updateImage( int w, int h )
	{
		final double hScale;
		
		if( img != null && ((img.getWidth( this ) != w) || (img.getHeight( this ) != h ))) {
//		if( img != null ) {
			disposeImage();
		}
		if( img == null ) {
// doesn't support alpha composite on window + linux!!
//			img = createImage( w, h );
// doesn't support alpha either
//			img = createVolatileImage( w, h );
			img = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
		}
//		final Graphics2D g2 = (Graphics2D) img.getGraphics();
		final Graphics2D g2 = img.createGraphics();
//g2.setColor( getBackground() );
//g2.setColor( new Color( 0, 0, 0, 0 ));
//g2.setColor( Color.green );
//g2.fillRect( 0, 0, w, h );
		
// trick from here: http://www-128.ibm.com/developerworks/library/j-begjava/index.html#h2
//		Clear image with transparent alpha by drawing a rectangle
final Composite cmpOrig = g2.getComposite();
		g2.setComposite( AlphaComposite.getInstance( AlphaComposite.CLEAR, 0.0f ));
//		Rectangle2D.Double rect = new Rectangle2D.Double(0,0,spriteSize,spriteSize); 
//		g2.fill(rect);
		g2.fillRect( 0, 0, w, h );
g2.setComposite( cmpOrig );
		
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

//		decZoomReciprocal = 1.0 / (decimation * xZoom);
		hScale 	= (double) w / (double) Math.max( 1, viewSpan.getLength() );
//		hOffset	= viewSpan.start * hScale;
		
		// ------------------------ draw grid ------------------------

		if( gridOn ) {
			final double step 		= gridResolution * sampleRate * hScale;
			float gridOff			= (float) ((gridOffset - viewSpan.start) * hScale);
			shpGrid.reset();
			g2.setColor( colrGrid );
			if( step > 1.0 ) {
				if( gridOff < 0 ) {
					gridOff = gridOff += Math.ceil( -gridOff / step ) * step;
				}
				float gridX = gridOff;
				for( int i = 0; gridX < w; i++ ) {
					gridX = gridOff + (float) (i * step);
					shpGrid.moveTo( gridX, 0 );
					shpGrid.lineTo( gridX, h );
				}
				g2.draw( shpGrid );
			} else {
				shpGrid.append( new Rectangle2D.Float( gridOff, 0, w - gridOff, h ), false );
				g2.fill( shpGrid );
			}
		}
		
		// ------------------------ draw waveform(s) ------------------------

//		if( waveOn && (numChannels > 0) && (decimatedStake != null) ) {
		if( waveOn && (decimatedStake != null) ) {
			final DecimationInfo info;
			info = getBestSubsample( new Span( viewSpan.start, viewSpan.stop + 1 ), w );
			drawWaveform( info, g2 );
		} // if( waveOn )
		
		g2.dispose();
		needsImageUpdate = false;
	}
	
	public void paintComponent( Graphics g )
	{
		super.paintComponent( g );
		
		final Graphics2D		g2			= (Graphics2D) g;
		final Insets			ins 		= getInsets();
		final int				w			= getWidth() - (ins.left + ins.right);
		final int				h			= getHeight() - (ins.top + ins.bottom);
		final AffineTransform	atOrig		= g2.getTransform();
//		final Stroke			strkOrig	= g2.getStroke();
//		final float				sy;
		final boolean			hResized	= recentWidth != w;
		final boolean			vResized	= recentHeight != h;
		final boolean			resized		= hResized || vResized;
//		float					offY		= 0f;
		int						x, x2;
		float					fx;
		final double			hScale 		= (double) w / (double) Math.max( 1, viewSpan.getLength() );
		Selection				sel;

		g2.translate( ins.left, ins.top );
		
		// ------------------------ draw background ------------------------

		g2.setColor( getBackground() );
//g2.setColor( Color.black );
		g2.fillRect( 0, 0, w, h );

		// ------------------------ draw selections ------------------------
		
		for( int i = 0; i < selections.length; i++ ) {
			sel = selections[ i ];
			if( (sel != null) && !sel.span.isEmpty() && sel.span.touches( viewSpan )) {
				g2.setColor( sel.colr );
				x 	= (int) ((sel.span.start - viewSpan.start) * hScale + 0.5);
				x2 	= (int) ((sel.span.stop - viewSpan.start) * hScale + 0.5);
				g2.fillRect( x, 0, x2 - x, h );
			}
		}

		// ------------------------ draw grid / waveform ------------------------

//		if( resized || needsImageUpdate
//				|| (img.validate( getGraphicsConfiguration() ) == VolatileImage.IMAGE_INCOMPATIBLE)
//				|| img.contentsLost() ) {
		if( resized || needsImageUpdate ) {
			recentWidth		= w;
			recentHeight	= h;
			updateImage( w, h );
		}
		g2.drawImage( img, 0, 0, this );

		// ------------------------ draw cursor ------------------------
		
		if( timeCursorOn ) {
//			if( updateCursor || resized )
			fx = (float) ((timeCursorPos - viewSpan.start) * hScale);
			g2.setColor( colrTimeCursor );
			shpCursor.setLine( fx, 0, fx, h );
			g2.draw( shpCursor );
		}
		
		g2.setTransform( atOrig );
	}

	public void addListener( Listener l ) {
		// really simple now!
		listeners.add( l );
	}
	

	public void removeListener( Listener l ) {
		// really simple now!
		listeners.remove( l );
	}

	protected void dispatchCursorChange()
	{
		for( int i = 0; i < listeners.size(); i++ ) {
			((Listener) listeners.get( i )).cursorChanged( this, timeCursorPos );
		}
	}

	protected void dispatchSelectionChange()
	{
		final Selection sel = selections[ selectionIndex ];
		for( int i = 0; i < listeners.size(); i++ ) {
			((Listener) listeners.get( i )).selectionChanged( this, selectionIndex, sel.span );
		}
	}

//	 ---------------- FocusListener interface ----------------

	public void focusGained( FocusEvent e )
	{
		repaint();
	}

	public void focusLost( FocusEvent e )
	{
		repaint();
	}

//	 ---------------- internal classes ----------------

	public static interface Listener
	{
		public void cursorChanged( SoundFileView v, long newPosition );
		public void selectionChanged( SoundFileView v, int selectionIndex, Span newSpan );
	}
	
	private class MouseAdapter
	extends MouseInputAdapter
	{
		private boolean shiftDrag, ctrlDrag, validDrag = false, dragStarted = false;
		private long startPos;
		private int startX;
		private long dragOffset;
		
		protected MouseAdapter() { /* empty */ }
		
		public void mousePressed( MouseEvent e )
		{
			if( !isEnabled() ) return;

			requestFocus();

			if( e.isMetaDown() ) {
//				editSelectAll( null );
				selectRegion( e );
				dragStarted = false;
				validDrag	= false;
			} else {
				shiftDrag	= e.isShiftDown();
				ctrlDrag	= e.isControlDown();
				dragStarted = false;
				validDrag	= true;
				startX		= e.getX();
				processDrag( e, false );
			}
		}
		
		public void mouseReleased( MouseEvent e )
		{
			dragStarted = false;
			validDrag	= false;
		}
		
		public void mouseDragged( MouseEvent e )
		{
			if( validDrag ) {
				if( !dragStarted ) {
					if( shiftDrag || ctrlDrag || Math.abs( e.getX() - startX ) > 2 ) {
						dragStarted = true;
					} else return;
				}
				processDrag( e, true );
			}
		}
		
		public void mouseMoved( MouseEvent e )
		{
			// on mac, ctrl+press and moving will
			// not generate mouseDragged messages
			// but mouseMoved instead
			mouseDragged( e );
		}
		
		private long screenToVirtual( int x )
		{
			final double 	hScale;
			final Insets	ins	= getInsets();
			final int		w	= getWidth() - ins.left - ins.right;
			
			hScale = (double) viewSpan.getLength() / Math.max( 1, w );
			
			return( Math.max( totalSpan.start, Math.min( totalSpan.stop, 
					(long) ((x - ins.left) * hScale + viewSpan.start + 0.5) )));
		}

		private void selectRegion( MouseEvent e )
		{
			final Selection	sel;
			
			ensureSelection( selectionIndex );
			sel 		= selections[ selectionIndex ];
			
			if( (sel.span.start == totalSpan.start || sel.editableStart) && sel.editableSize &&
					!sel.span.equals( totalSpan )) {
				sel.span = new Span( totalSpan );
				dispatchSelectionChange();
				repaint();
			}
		}
		
		private void processDrag( MouseEvent e, boolean hasStarted )
		{
			final long 		position;
			final Selection	sel;
			Span			span, span2;
			long			n;
			
			ensureSelection( selectionIndex );
			sel 		= selections[ selectionIndex ];
		   
			span        = viewSpan;
			span2		= sel.span;
			position    = screenToVirtual( e.getX() );
			if( !hasStarted && !ctrlDrag ) {
				if( shiftDrag ) {
					if( !sel.editableSize ) return;
					
					if( span2.isEmpty() ) {
						span2 = new Span( timeCursorPos, timeCursorPos );
					}
					if( !sel.editableStart ) {
						startPos = span2.start;
						span2	= new Span( sel.span.start,
									Math.max( sel.span.start, position ));
					} else {
						startPos = Math.abs( span2.start - position ) >
									Math.abs( span2.stop - position ) ?
									span2.start : span2.stop;
									span2	= new Span( Math.min( startPos, position ),
											Math.max( startPos, position ));
					}
//					edit	= TimelineVisualEdit.select( this, doc, span2 ).perform();
					if( !span2.equals( sel.span )) {
						span		= sel.span.union( span2 );
						sel.span	= span2;
						dispatchSelectionChange();
						efficientRepaint( span );
					}
				} else {
					startPos = position;
					if( span2.isEmpty() || !sel.editableStart || !sel.editableSize ) {
//						edit = TimelineVisualEdit.position( this, doc, position ).perform();
						if( timeCursorEditable && timeCursorOn && (position != timeCursorPos) ) {
							span = new Span( Math.min( position, timeCursorPos ), Math.max( position, timeCursorPos ));
							timeCursorPos = position;
							dispatchCursorChange();
							efficientRepaint( span );
						}
					} else {
//						edit = new CompoundEdit();
//						edit.addEdit( TimelineVisualEdit.select( this, doc, new Span() ).perform() );
//						edit.addEdit( TimelineVisualEdit.position( this, doc, position ).perform() );
//						((CompoundEdit) edit).end();
						if( sel.editableStart && sel.editableSize ) {
							sel.span = new Span( position, position );
							dispatchSelectionChange();
						}
						span = span2.union( new Span( Math.min( position, timeCursorPos ), Math.max( position, timeCursorPos )));
						if( timeCursorOn && (timeCursorPos != position )) {
							timeCursorPos = position;
							dispatchCursorChange();
						}
						efficientRepaint( span );
					}
				}
			} else {
				if( ctrlDrag ) {
					if( shiftDrag ) {
						if( !sel.editableStart ) return;
						if( !hasStarted ) {
							dragOffset = sel.span.start - position;
						}
						n = Math.max( totalSpan.start, Math.min( totalSpan.stop - sel.span.getLength(),
								dragOffset + position ));
						span = new Span( n, n + sel.span.getLength() );
						if( !span.equals( sel.span )) {
							span2 = span.union( sel.span );
							sel.span = span;
							dispatchSelectionChange();
							efficientRepaint( span2 );
						}
					} else {
	//					edit	= TimelineVisualEdit.position( this, doc, position ).perform();
						if( timeCursorOn && (position != timeCursorPos) ) {
							span = new Span( Math.min( position, timeCursorPos ), Math.max( position, timeCursorPos ));
							timeCursorPos = position;
							dispatchCursorChange();
							efficientRepaint( span );
						}
					}
				} else {
					if( !sel.editableSize ) return;
					if( !sel.editableStart ) {
						span2	= new Span( sel.span.start,
								Math.max( sel.span.start, position ));
					} else {
						span2	= new Span( Math.min( startPos, position ),
								Math.max( startPos, position ));
					}
//					edit		= TimelineVisualEdit.select( this, doc, span2 ).perform();
					span		= sel.span.union( span2 );
					sel.span	= span2;
					dispatchSelectionChange();
					efficientRepaint( span );
				}
			}
		}
	}

	private static class DecimationHelp
	{
		public final double	rate;
		public final int	shift;
		public final int	factor;
		public final int	roundAdd;
		public final long	mask;
		
		public DecimationHelp( double fullRate, int shift )
		{
			this.shift		= shift;
			factor			= 1 << shift;
			this.rate		= fullRate / factor;
			roundAdd		= factor >> 1;
			mask			= -factor;
		}
		
		/**
		 *  Converts a frame length from full rate to
		 *  decimated rate and rounds to nearest integer.
		 *
		 *  @param  full	number of frame at full rate
		 *  @return number of frames at this editor's decimated rate
		 */
		public long fullrateToSubsample( long full )
		{
			return( (full + roundAdd) >> shift );
		}
	}

	private abstract class Decimator
	{
		protected Decimator() { /* empty */ }
		protected abstract void decimate( float[][] inBuf, float[][] outBuf, int outOff, int len, int decim );
		protected abstract void decimatePCM( float[][] inBuf, float[][] outBuf, int outOff, int len, int decim );
//		protected abstract void decimatePCMFast( float[][] inBuf, float[][] outBuf, int outOff, int len, int decim );
	}

	private class HalfPeakRMSDecimator
	extends Decimator
	{
		protected HalfPeakRMSDecimator() { /* empty */ }

		protected void decimate( float[][] inBuf, float[][] outBuf, int outOff, int len, int decim )
		{
			int		stop, j, k, m, ch, ch2;
			float   f1, f2, f3, f4, f5;
			float[] inBufCh1, inBufCh2, inBufCh3, inBufCh4, outBufCh1, outBufCh2, outBufCh3, outBufCh4;

			for( ch = 0; ch < fullChannels; ch++ ) {
				inBufCh1	= inBuf[ ch ];
				outBufCh1	= outBuf[ ch ];
				ch2			= ch + fullChannels;
				inBufCh2	= inBuf[ ch2 ];
				outBufCh2	= outBuf[ ch2 ];
				ch2		   += fullChannels;
				inBufCh3	= inBuf[ ch2 ];
				outBufCh3	= outBuf[ ch2 ];
				ch2		   += fullChannels;
				inBufCh4	= inBuf[ ch2 ];
				outBufCh4	= outBuf[ ch2 ];

				for( j = outOff, stop = outOff + len, k = 0; j < stop; j++ ) {
					f1 = inBufCh1[ k ];
					f2 = inBufCh2[ k ];
					f3 = inBufCh3[ k ];
					f4 = inBufCh4[ k ];
					for( m = k + decim, k++; k < m; k++ ) {
						f5 = inBufCh1[ k ];
						if( f5 > f1 ) f1 = f5;
						f5 = inBufCh2[ k ];
						if( f5 < f2 ) f2 = f5;
						f3 += inBufCh3[ k ];
						f4 += inBufCh4[ k ];
					}
					outBufCh1[ j ]	= f1;			// positive halfwave peak
					outBufCh2[ j ]	= f2;			// negative halfwave peak
					outBufCh3[ j ]	= f3 / decim;	// positive halfwave mean square
					outBufCh4[ j ]	= f4 / decim;	// negative halfwave mean square
				}
			} // for( ch )
		}
		
		protected void decimatePCM( float[][] inBuf, float[][] outBuf, int outOff, int len, int decim )
		{
			int		stop, j, k, m, ch, ch2;
			float   f1, f2, f3, f4, f5;
			float[] inBufCh1, outBufCh1, outBufCh2, outBufCh3, outBufCh4;

			for( ch = 0; ch < fullChannels; ch++ ) {
				inBufCh1	= inBuf[ ch ];
				outBufCh1	= outBuf[ ch ];
				ch2			= ch + fullChannels;
				outBufCh2	= outBuf[ch2];
				ch2		   += fullChannels;
				outBufCh3	= outBuf[ ch2 ];
				ch2		   += fullChannels;
				outBufCh4	= outBuf[ ch2 ];

				for( j = outOff, stop = outOff + len, k = 0; j < stop; j++ ) {
					f5 = inBufCh1[ k++ ];
					if( f5 >= 0.0f ) {
						f1 = f5;
						f3 = f5 * f5;
						f2 = 0.0f;
						f4 = 0.0f;
					} else {
						f2 = f5;
						f4 = f5 * f5;
						f1 = 0.0f;
						f3 = 0.0f;
					}
					for( m = 1; m < decim; m++ ) {
						f5 = inBufCh1[ k++ ];
						if( f5 >= 0.0f ) {
							if( f5 > f1 ) f1 = f5;
							f3 += f5 * f5;
						} else {
							if( f5 < f2 ) f2 = f5;
							f4 += f5 * f5;
						}
					}
					outBufCh1[ j ]	= f1;			// positive halfwave peak
					outBufCh2[ j ]	= f2;			// negative halfwave peak
					outBufCh3[ j ]	= f3 / decim;	// positive halfwave mean square
					outBufCh4[ j ]	= f4 / decim;	// negative halfwave mean square
				}
			} // for( ch )
		}
	} // class HalfPeakRMSDecimator

	private class FullPeakRMSDecimator
	extends Decimator
	{
		protected FullPeakRMSDecimator() { /* empty */ }
		
		protected void decimate( float[][] inBuf, float[][] outBuf, int outOff, int len, int decim )
		{
			int		stop, j, k, m, ch, ch2;
			float   f1, f2, f3, f5;
			float[] inBufCh1, inBufCh2, inBufCh3, outBufCh1, outBufCh2, outBufCh3;

			for( ch = 0; ch < fullChannels; ch++ ) {
				inBufCh1	= inBuf[ ch ];
				outBufCh1	= outBuf[ ch ];
				ch2			= ch + fullChannels;
				inBufCh2	= inBuf[ ch2 ];
				outBufCh2	= outBuf[ ch2 ];
				ch2		   += fullChannels;
				inBufCh3	= inBuf[ ch2 ];
				outBufCh3	= outBuf[ ch2 ];

				for( j = outOff, stop = outOff + len, k = 0; j < stop; j++ ) {
					f1 = inBufCh1[ k ];
					f2 = inBufCh2[ k ];
					f3 = inBufCh3[ k ];
					for( m = k + decim, k++; k < m; k++ ) {
						f5 = inBufCh1[ k ];
						if( f5 > f1 ) f1 = f5;
						f5 = inBufCh2[ k ];
						if( f5 < f2 ) f2 = f5;
						f3 += inBufCh3[ k ];
					}
					outBufCh1[ j ]	= f1;			// positive halfwave peak
					outBufCh2[ j ]	= f2;			// negative halfwave peak
					outBufCh3[ j ]	= f3 / decim;	// fullwave mean square
				}
			} // for( ch )
		}

		protected void decimatePCM( float[][] inBuf, float[][] outBuf, int outOff, int len, int decim )
		{
			int		stop, j, k, m, ch, ch2;
			float   f1, f2, f3, f5;
			float[] inBufCh1, outBufCh1, outBufCh2, outBufCh3;

			for( ch = 0; ch < fullChannels; ch++ ) {
				inBufCh1	= inBuf[ ch ];
				outBufCh1	= outBuf[ ch ];
				ch2			= ch + fullChannels;
				outBufCh2	= outBuf[ch2];
				ch2		   += fullChannels;
				outBufCh3	= outBuf[ ch2 ];

				for( j = outOff, stop = outOff + len, k = 0; j < stop; j++ ) {
					f5 = inBufCh1[ k++ ];
					f1 = f5;
					f2 = f5;
					f3 = f5 * f5;
					for( m = 1; m < decim; m++ ) {
						f5 = inBufCh1[ k++ ];
						if( f5 > f1 ) f1 = f5;
						if( f5 < f2 ) f2 = f5;
						f3 += f5 * f5;
					}
					outBufCh1[ j ]	= f1;			// positive halfwave peak
					outBufCh2[ j ]	= f2;			// negative halfwave peak
					outBufCh3[ j ]	= f3 / decim;	// fullwave mean square
				}
			} // for( ch )
		}
	} // class FullPeakRMSDecimator

	private static class DecimatedStake
//	extends BasicStake
	{
		private final InterleavedStreamFile[]	fs;
		private final Span[]					fileSpans;
//		 XXX SWINGOSC XXX
//		private final Span[]					maxFileSpans;
		private final MutableLong[]				framesWritten;
		private final Span[]					biasedSpans;
		private final DecimationHelp[]			decimations;
// XXX SWINGOSC XXX
//		private final int						SUBNUM;
		
		// XXX SWINGOSC XXX
		private final Span						span;

		public DecimatedStake( Span span, InterleavedStreamFile[] fs, Span[] fileSpans, Span[] biasedSpans,
							   DecimationHelp[] decimations )
		{
			this( span, fs, fileSpans, fileSpans, null, biasedSpans, decimations );
		}
		
		private DecimatedStake( Span span, InterleavedStreamFile[] fs, Span[] fileSpans,
								Span[] maxFileSpans, MutableLong[] framesWritten, Span[] biasedSpans,
								DecimationHelp[] decimations )
		{
// XXX SWINGOSC XXX
//			super( span );
			
			this.span			= span;

			this.fs				= fs;
			this.fileSpans		= fileSpans;
// XXX SWINGOSC XXX
//			this.maxFileSpans	= maxFileSpans;
			if( framesWritten == null ) {
				this.framesWritten = new MutableLong[ fs.length ];
				for( int i = 0; i < fs.length; i++ ) this.framesWritten[ i ] = new MutableLong( 0L );
			} else {
				this.framesWritten	= framesWritten;
			}
			this.biasedSpans	= biasedSpans;
			this.decimations	= decimations;
			
// XXX SWINGOSC XXX
//			SUBNUM				= decimations.length;
		}
		
		// XXX SWINGOSC XXX
		public Span getSpan()
		{
			return span;
		}
		
		public void dispose()
		{
			// XXX
// XXX SWINGOSC XXX
//			super.dispose();
		}
		
//		 XXX SWINGOSC XXX
//		public Stake duplicate()
//		{
//			return new DecimatedStake( span, fs, fileSpans, maxFileSpans, framesWritten, biasedSpans, decimations );
//		}
//
//		public Stake replaceStart( long newStart )
//		{
//			final Span[] newBiasedSpans	= new Span[ SUBNUM ];
//			final Span[] newFileSpans	= new Span[ SUBNUM ];
//			long testBias, newBiasedStart, delta;
//			DecimationHelp decim;
//			
//			for( int i = 0; i < SUBNUM; i++ ) {
//				decim				= decimations[ i ];
//				testBias			= biasedSpans[ i ].start + ((newStart - span.start + decim.roundAdd) & decim.mask) - newStart;
//				newBiasedStart		= newStart + (testBias < -decim.roundAdd ? testBias + decim.factor :
//												 (testBias > decim.roundAdd ? testBias - decim.factor : testBias));
//				delta				= (newBiasedStart - biasedSpans[ i ].start) >> decim.shift;
//				newBiasedSpans[ i ]	= biasedSpans[ i ].replaceStart( newBiasedStart );
//				newFileSpans[ i ]	= fileSpans[ i ].replaceStart( fileSpans[ i ].start + delta );
//				// XXX modify framesWritten ?
//			}
//			return new DecimatedStake( span.replaceStart( newStart ), fs, newFileSpans, maxFileSpans, framesWritten, newBiasedSpans, decimations );
//		}
//
//		public Stake replaceStop( long newStop )
//		{
//			final Span[] newBiasedSpans	= new Span[ SUBNUM ];
//			final Span[] newFileSpans	= new Span[ SUBNUM ];
//			long testBias, newBiasedStop, delta;
//			int	startBias;
//			DecimationHelp decim;
//
//			for( int i = 0; i < SUBNUM; i++ ) {
//				decim				= decimations[ i ];
//				startBias			= (int) (biasedSpans[ i ].start - span.start);
//				testBias			= (int) (((startBias + newStop + decim.roundAdd) & decim.mask) - newStop);
//				newBiasedStop		= newStop + (testBias < -decim.roundAdd ? testBias + decim.factor :
//												(testBias > decim.roundAdd ? testBias - decim.factor : testBias));
//				newBiasedSpans[ i ]	= biasedSpans[ i ].replaceStop( newBiasedStop );
//				newFileSpans[ i ]	= fileSpans[ i ].replaceStop( fileSpans[ i ].start + newBiasedSpans[ i ].getLength() ); // XXX richtig?
//			}
//			return new DecimatedStake( span.replaceStop( newStop ), fs, newFileSpans, maxFileSpans, framesWritten, newBiasedSpans, decimations );
//		}
//
//		public Stake shiftVirtual( long delta )
//		{
//			final Span[] newBiasedSpans	= new Span[ SUBNUM ];
//
//			for( int i = 0; i < SUBNUM; i++ ) {
//				newBiasedSpans[ i ]	= biasedSpans[ i ].shift( delta );
//			}
//			return new DecimatedStake( span.shift( delta ), fs, fileSpans, maxFileSpans, framesWritten, newBiasedSpans, decimations );
//		}

		public void readFrames( int sub, float[][] data, int dataOffset, Span readSpan, MutableInt framesRead, MutableInt framesBusy )
		throws IOException
		{
			if( data.length == 0 ) {
				framesRead.set( 0 );
				framesBusy.set( 0 );
				return;
			}
			
			final DecimationHelp decim	= decimations[ sub ];
			final int	startBias		= (int) (biasedSpans[ sub ].start - span.start);
			final int	newStartBias	= (int) (((readSpan.start + decim.roundAdd) & decim.mask) - readSpan.start) + startBias;
			final long	newBiasedStart	= readSpan.start + (newStartBias < -decim.roundAdd ? newStartBias + decim.factor :
														   (newStartBias > decim.roundAdd ? newStartBias - decim.factor : newStartBias));
			final long	fOffset			= fileSpans[ sub ].start + ((newBiasedStart - (span.start + startBias)) >> decim.shift);
			final int	newStopBias		= (int) (((startBias + readSpan.stop + decim.roundAdd) & decim.mask) - readSpan.stop);
			final long	newBiasedStop	= readSpan.stop + (newStopBias < -decim.roundAdd ? newStopBias + decim.factor :
														  (newStopBias > decim.roundAdd ? newStopBias - decim.factor : newStopBias));
			final int	len				= (int) Math.min( data[0].length - dataOffset, (newBiasedStop - newBiasedStart) >> decim.shift );
			final int	readyLen;
			
			if( len <= 0 ) {
				framesRead.set( 0 );
				framesBusy.set( 0 );
				return;
			}

			synchronized( fs ) {
				readyLen = (int) Math.min( len, Math.max( 0, fileSpans[ sub ].start + framesWritten[ sub ].value() - fOffset ));
				if( readyLen > 0 ) {
					if( fs[ sub ].getFramePosition() != fOffset ) {
						fs[ sub ].seekFrame( fOffset );
					}
					fs[ sub ].readFrames( data, dataOffset, readyLen );
				}
			}
			
			framesRead.set( readyLen );
			framesBusy.set( len - readyLen );
		}

		public void continueWrite( int sub, float[][] data, int dataOffset, int len )
		throws IOException
		{
			if( len == 0 ) return; // return 0;
			synchronized( fs ) {
				final long	fOffset = fileSpans[ sub ].start + framesWritten[ sub ].value();
		
				if( (fOffset < fileSpans[ sub ].start) || ((fOffset + len) > fileSpans[ sub ].stop) ) {
					throw new IllegalArgumentException( fOffset + " ... " + (fOffset + len) + " not within " + fileSpans[ sub ].toString() );
				}

				if( fs[ sub ].getFramePosition() != fOffset ) {
					fs[ sub ].seekFrame( fOffset );
				}
				fs[ sub ].writeFrames( data, dataOffset, len );
				
				framesWritten[ sub ].set( framesWritten[ sub ].value() + len );
			}
		}

//		public void flush()
//		throws IOException
//		{
//			synchronized( fs ) {
//				for( int i = 0; i < fs.length; i++ ) {
//					fs[ i ].flush();
//				}
//			}
//		}

//		public void debugDump()
//		{
//			debugDumpBasics();
//			for( int i = 0; i < SUBNUM; i++ ) {
//				System.err.println( "  decim "+decimations[i].factor+" biased span "+biasedSpans[i].toString()+
//					"; f = " + fs[i].getFile().getName() + " (file span " + fileSpans[i].toString() + " )" );
//			}
//		}
//		
//		protected void debugDumpBasics()
//		{
//			System.err.println( "Span " + span.toString() );
//		}
	}
	
	private static class DecimationInfo
	{
		/**
		 *  Internal index for MultirateTrackEditor
		 */
		protected final int	idx;
		protected final int	shift;
		protected final int	inlineDecim;
		/**
		 *  Time span (in fullrate frames) covered by this subsample
		 */
		public final Span	span;
		/**
		 *  Length (rounded) of the time span decimated through subsampling
		 */
		public final long	sublength;
//		public final int	model;
//		public final int	channels;

		/**
		 *  Creates a new <code>DecimationInfo</code>
		 *  data structure with the given decimation.
		 *
		 *  @param  idx			internal index for <code>MultirateTrackEditor</code>
		 *  @param  span		the originally covered time span
		 *  @param  sublength   the translated span length in deimated
		 *						frames (rounded to integer)
		 */
		protected DecimationInfo( Span span, long sublength, int channels,
								  int idx, int shift, int inlineDecim, int model )
		{
			this.span			= span;
			this.sublength		= sublength;
//			this.channels		= channels;
			this.idx			= idx;
			this.shift			= shift;
			this.inlineDecim	= inlineDecim;
//			this.model			= model;
		}

		/**
		 *  Returns the decimation
		 *  rate factor.
		 *
		 *  @return the factor by which the full rate is decimated,
		 *			that is, <code>decimatedRate = fullRate / returnedFactor</code>
		 */
		public int getDecimationFactor()
		{
			return( (1<<shift) * inlineDecim );
		}
		
		public long getTotalLength()
		{
			return( (sublength * inlineDecim) << shift );
		}
	}
	
	private class Selection
	{
		protected Span		span			= new Span();
		protected boolean	editableStart	= true;
		protected boolean	editableSize	= true;
		protected Color		colr			= colrSelection;
		
		protected Selection() { /* empty */ }
	}
}