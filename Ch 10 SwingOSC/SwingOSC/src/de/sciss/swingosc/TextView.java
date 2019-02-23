/*
 *  TextView.java
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
 *		08-Feb-07	created
 */
package de.sciss.swingosc;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.MutableAttributeSet;
//import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 *	Extends <code>JTextPane</code> by a buffering
 *	mechanism for data updates and utility methods for styling.
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.63, 31-Dec-09
 */
public class TextView
extends JTextPane
implements UndoableEditListener
{
	private final StringBuffer				updateData			= new StringBuffer();
	protected final List					collDocListeners	= new ArrayList();
//	private boolean useUndoMgr = false;
	protected final UndoManager				undo		 		= new UndoManager();
	private TabStop[]						tabs;

	public TextView()
	{
		super();
		
		// this makes the font sizes from CSS appear
		// correctly. unfortunately has only effect
		// in java 1.5+
		putClientProperty( "JEditorPane.w3cLengthUnits", Boolean.TRUE );
		
//		addHyperlinkListener( new HyperlinkListener() {
//			public void hyperlinkUpdate( HyperlinkEvent e )
//			{
//				System.out.println( "RECEIVED : " + e.getEventType() );
//			}
//		});
		
		// install undo/redo shortcuts
		final AbstractAction undoAction = new AbstractAction( "Undo" ) {
			public void actionPerformed( ActionEvent e )
			{
				try {
					if( undo.canUndo() ) undo.undo();
				} catch( CannotUndoException e1 ) { /* ignored */ }
			}
		};
		final AbstractAction redoAction = new AbstractAction( "Redo" ) {
			public void actionPerformed( ActionEvent e )
			{
				try {
					if( undo.canRedo() ) undo.redo();
				} catch( CannotRedoException e1 ) { /* ignored */ }
			}
		};
		final ActionMap amap = getActionMap();
		final InputMap imap	 = getInputMap();
		final int meta		 = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		amap.put( "undo", undoAction );
	 	imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_Z, meta ), "undo" );
		amap.put( "redo", redoAction );
		final boolean isMacOS = System.getProperty( "os.name" ).indexOf( "Mac OS" ) >= 0;
		final int redoKey	 = isMacOS ? KeyEvent.VK_Z : KeyEvent.VK_Y;
		final int redoMod 	 = isMacOS ? meta | InputEvent.SHIFT_MASK : meta;
	 	imap.put( KeyStroke.getKeyStroke( redoKey, redoMod ), "redo" );

	 	final Document doc = getDocument(); 
	 	doc.addUndoableEditListener( this );
	 	
	 	// create default tabs
		tabs = new TabStop[ 30 ];
		for( int i = 0; i < 30; i++ ) {
			tabs[ i ] = new TabStop( i * 28 );
		}
	 	setTabs( doc, -1, 0 );
		
		// this automatically moves document listeners to
		// a new doc
		addPropertyChangeListener( "page", new PropertyChangeListener() {
			public void propertyChange( PropertyChangeEvent pce )
			{
//				System.out.println( "PAGE! " + pce.getNewValue() );
				// massage
				final Document doc = getDocument();
				if( doc != null ) setTabs( doc, -1, 0 );

				// note: this goes _after_ the tab
				// configuration, because otherwise
				// the user could undo that configuration
				undo.discardAllEdits();
			}
		});
		
		// this automatically moves document listeners to
		// a new doc
		addPropertyChangeListener( "document", new PropertyChangeListener() {
			public void propertyChange( PropertyChangeEvent pce )
			{
//System.out.println( "propertyChange : doc" );
				undo.discardAllEdits();
				
				// unregister old
				final Document oldDoc = (Document) pce.getOldValue();
				if( oldDoc != null ) {
					oldDoc.removeUndoableEditListener( TextView.this );
					for( int i = 0; i < collDocListeners.size(); i++ ) {
						oldDoc.removeDocumentListener( (DocumentListener) collDocListeners.get( i ));
//System.out.println( "remove " + collDocListeners.get( i ));
					}
					if( oldDoc instanceof AbstractDocument ) {
						final AbstractDocument adoc = (AbstractDocument) oldDoc;
						final DocumentEvent de =
							adoc.new DefaultDocumentEvent( oldDoc.getStartPosition().getOffset(),
							                               oldDoc.getLength(),
							                               DocumentEvent.EventType.REMOVE );
						// simulate clear
						for( int i = 0; i < collDocListeners.size(); i++ ) {
							final DocumentListener l = (DocumentListener) collDocListeners.get( i );
							l.removeUpdate( de );
						}
					}
				}
								
				// re-register new
				final Document newDoc = (Document) pce.getNewValue();
				if( newDoc != null ) {
					newDoc.addUndoableEditListener( TextView.this );
					for( int i = 0; i < collDocListeners.size(); i++ ) {
						newDoc.addDocumentListener( (DocumentListener) collDocListeners.get( i ));
//System.out.println( "add " + collDocListeners.get( i ));
					}
					if( newDoc instanceof AbstractDocument ) {
						final AbstractDocument adoc = (AbstractDocument) newDoc;
						final DocumentEvent de =
							adoc.new DefaultDocumentEvent( newDoc.getStartPosition().getOffset(),
							                               newDoc.getLength(),
							                               DocumentEvent.EventType.INSERT );
						// simulate clear
						for( int i = 0; i < collDocListeners.size(); i++ ) {
							final DocumentListener l = (DocumentListener) collDocListeners.get( i );
							l.insertUpdate( de );
						}
					}
				}
			}
		});
	}
	
	// apply tabs to (new) document
	protected void setTabs( Document doc, int rangeStart, int len )
	{
		if( doc instanceof StyledDocument ) {
			final StyledDocument sdoc = (StyledDocument) doc;
			final SimpleAttributeSet attrs = new SimpleAttributeSet();
			StyleConstants.setTabSet( attrs, new TabSet( tabs ));
			if( rangeStart == -1 ) {
				rangeStart	= 0;
				len			= sdoc.getLength();
			}
			sdoc.setParagraphAttributes( rangeStart, len, attrs, false );
			
//		} else if( doc instanceof PlainDocument ) {
//			doc.putProperty( PlainDocument.tabSizeAttribute, new Integer( 4 ));
		}
	}
	
	// for easy swingOSC access
	public void setTabs( int rangeStart, int len, Object[] args )
	{
		final int		numTabs	= args.length >> 1;
		final TabStop[] t	= new TabStop[ numTabs ];
		for( int i = 0, j = 0; i < numTabs; i++ ) {
//			final float pos		= ((Number) args[ j++ ]).floatValue();
			// Warning: there is a problem with certain float values,
			// hence trunc them to ints!
			final float pos		= ((Number) args[ j++ ]).intValue();
			final int   mode	= ((Number) args[ j++ ]).intValue();
			final int   align	= (mode >> 8) & 0xFF;
			final int   leader	= mode & 0xFF;
			t[ i ] = new TabStop( pos, align, leader );
		}
		setTabs( rangeStart, len, t );
	}
	
	public void setTabs( int rangeStart, int len, TabStop[] tabs )
	{
		this.tabs = tabs;
		setTabs( getDocument(), rangeStart, len );
	}
	
	public void beginDataUpdate()
	{
		updateData.setLength( 0 );
	}
	
	public void addData( String update )
	{
		updateData.append( update );
	}
	
	public void endDataUpdate( int insertPos, int replaceLen )
	throws BadLocationException
	{
		setString( insertPos, replaceLen, updateData.toString() );
		updateData.setLength( 0 );
	}
	
	public void read( String path )
	throws IOException
	{
		readURL( new File( path ).toURL() );
	}
	
//	public void readURL( URL url )
//	throws IOException
//	{
//		
//	}
	
	// e.g. 'text/html; charset=utf-8'
	private String extractType( String fullType )
	{
		final int i = fullType.indexOf( ';' );
		return i < 0 ? fullType : fullType.substring( 0, i );
	}
	
//	private String extractCharset( String fullType )
//	{
//		final int i = fullType.indexOf( "charset=" );
//		if( i >= 0 ) {
//			final int j = fullType.indexOf( ";", i + 8 );
//			return fullType.substring( i + 8, j >= 0 ? j : fullType.length() );
//		} else {
//			return null;
//		}
//	}

	public EditorKit getEditorKitForContentType( String type ) {
		final EditorKit kit = super.getEditorKitForContentType( overrideContentType == null ? type : overrideContentType );
//		System.out.println( "getEditorKitForContentType( " + type + " ) => " + kit );
		return kit;
	}
	
	private String overrideContentType = null;
	
	public void readURL( URL url )
	throws IOException
	{
		final URLConnection con = url.openConnection();
//		final String ctyp = con.getContentType();
		final String ctyp = extractType( con.getContentType() );
		
//		final String mime;
		if( ctyp == null || ctyp.equals( "content/unknown" )) {
			final String path = url.getPath();
			final int i = path.lastIndexOf( '.' ) + 1;
			final String ext = path.substring( i );
			final String mime;
			if( ext.equals( "htm" ) ||
				ext.equals( "html" )) {
				mime = "text/html";
			} else if( ext.equals( "rtf" )) {
				mime = "text/rtf"; 
			} else {
				mime = "text/plain";
			}
			try {
				overrideContentType = mime;  // tricky shit to get RTF to work...
//				undo.discardAllEdits();
				setPage( url );
			}
			finally {
				overrideContentType = null;
			}
		} else {
			setPage( url );
//			mime = ctyp;
		}
	}

	public void setString( int insertPos, int replaceLen, String str )
	throws BadLocationException
	{
		final Document doc = getDocument();
		if( insertPos == -1 ) {
			insertPos = 0;
			replaceLen = doc.getLength();
		} else {
			insertPos 	= Math.max( 0, Math.min( insertPos, doc.getLength () ));
			replaceLen	= Math.max( 0, Math.min( replaceLen, doc.getLength() - insertPos ));
		}
		doc.remove( insertPos, replaceLen );
		doc.insertString( insertPos, str, null );
	}
	
	public void setFont( int rangeStart, int len, Font f )
	{
		if( rangeStart == -1 ) {
			// fixes a bug with empty documents where
			// the font is not properly applied through setCharacterAttributes...
			setFont( f );
// NOTE: don't return, because we still need to execute the below
// with HTML pages!
//			return;
		}
		final MutableAttributeSet as = new SimpleAttributeSet();
		StyleConstants.setFontFamily( as, f.getFamily() );
		StyleConstants.setFontSize( as, f.getSize() );
		StyleConstants.setBold( as, f.isBold() );
		StyleConstants.setItalic( as, f.isItalic() );
		applyCharacterAttr( rangeStart, len, as );
	}
	
	public void setForeground( int rangeStart, int len, Color c )
	{
		if( rangeStart == -1 ) {
			// fixes a bug with empty documents where
			// the colour is not properly applied through setCharacterAttributes...
			setForeground( c );
// NOTE: don't return, because we still need to execute the below
// with HTML pages!
//			return;
		}
		final MutableAttributeSet as = new SimpleAttributeSet();
		StyleConstants.setForeground( as, c );
		applyCharacterAttr( rangeStart, len, as );
	}
	
	public void setLeftIndent( int rangeStart, int len, float indent )
	{
		final MutableAttributeSet as = new SimpleAttributeSet();
		StyleConstants.setLeftIndent( as, indent );
		applyParagraphAttr( rangeStart, len, as );
	}

	public void setRightIndent( int rangeStart, int len, float indent )
	{
		final MutableAttributeSet as = new SimpleAttributeSet();
		StyleConstants.setRightIndent( as, indent );
		applyParagraphAttr( rangeStart, len, as );
	}

	public void setSpaceAbove( int rangeStart, int len, float space )
	{
		final MutableAttributeSet as = new SimpleAttributeSet();
		StyleConstants.setSpaceAbove( as, space );
		applyParagraphAttr( rangeStart, len, as );
	}

	public void setSpaceBelow( int rangeStart, int len, float space )
	{
		final MutableAttributeSet as = new SimpleAttributeSet();
		StyleConstants.setSpaceBelow( as, space );
		applyParagraphAttr( rangeStart, len, as );
	}

	public void setLineSpacing( int rangeStart, int len, float spacing )
	{
		final MutableAttributeSet as = new SimpleAttributeSet();
		StyleConstants.setLineSpacing( as, spacing );
		applyParagraphAttr( rangeStart, len, as );
	}
	
	public void setAlignment( int rangeStart, int len, int align )
	{
		final MutableAttributeSet as = new SimpleAttributeSet();
		StyleConstants.setAlignment( as, align );
		applyParagraphAttr( rangeStart, len, as );
	}
	
	// this is here to make DocumentResponder less complex
	// (because now it can connect both Caret and Document listeners to the same object)
	// ; this just forwards the request to the Document.
	// it takes care of removing and adding the listeners
	// automatically if the document changes
	public void addDocumentListener( DocumentListener l )
	{
		collDocListeners.add( l );
		getDocument().addDocumentListener( l );
	}
	
//	public void addHyperlinkListener( HyperlinkListener l )
//	{
//		System.out.println( "addHyperlinkListener" );
//		super.addHyperlinkListener( l );
//	}

	public void removeDocumentListener( DocumentListener l )
	{
		collDocListeners.remove( l );
		getDocument().removeDocumentListener( l );
	}

	private void applyCharacterAttr( int rangeStart, int len, AttributeSet as )
	{
		final StyledDocument doc = getStyledDocument();
		if( rangeStart == -1 ) {
			rangeStart	= 0;
			len			= doc.getLength();
		}
		doc.setCharacterAttributes( rangeStart, len, as, false );
	}

	private void applyParagraphAttr( int rangeStart, int len, AttributeSet as )
	{
		final StyledDocument doc = getStyledDocument();
		if( rangeStart == -1 ) {
			rangeStart	= 0;
			len			= doc.getLength();
		}
		doc.setParagraphAttributes( rangeStart, len, as, false );
	}
	
	public void paintComponent( Graphics g )
	{
		final Color colrBg	= getBackground();

		if( (colrBg != null) && (colrBg.getAlpha() > 0) ) {
			g.setColor( colrBg );
			g.fillRect( 0, 0, getWidth(), getHeight() );
		}
		super.paintComponent( g );
	}

	public void setBackground( Color c )
	{
		setOpaque( (c != null) && (c.getAlpha() == 0xFF) );
		super.setBackground( c );
	}

	// ---- UndoableEditListener interface ----
	
	public void undoableEditHappened( UndoableEditEvent e )
	{
//		System.out.println( "edit: " + e.getEdit() + "; " + e.getEdit().isSignificant() );
        undo.addEdit( e.getEdit() );
//      undoAction.updateUndoState();
//      redoAction.updateRedoState();
	}
}
