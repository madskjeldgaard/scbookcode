/*
 *  DummyTransferHandler.java
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
 *		25-Dec-05	created
 *		25-Mar-06	uses OSCClient
 *		15-Jan-08	handles cut/copy/paste from/to clipboard
 *		30-Jan-08	adds file-list import
 */

package de.sciss.swingosc;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.event.MouseInputListener;

import de.sciss.net.OSCMessage;

/**
 *	A proxy transfer handler to attach to any swing
 *	gadget ; the actual drag+drop handling should
 *	be done by the OSC client.
 *	<p>
 *	How it works: You create a <code>DummyTransferHandler</code> for a <code>JComponent</code>
 *	registered with the <code>SwingOSC</code> server. When the user initiates a
 *	drag gesture with this component being the drag source, an OSC message
 *	<pre>
 *	[ &quot;/transfer&quot;, <var>&lt;objectID&gt;</var>, &quot;export&quot; ]
 *	</pre>
 *	is sent to the client who
 *	created the transfer handler. This client should then immediately reply
 *	with a
 *	<pre>
 *	[ &quot;/set&quot;, <var>&lt;transferHandlerID&gt;</var>, \string, <var>&lt;stringRepresentation&gt;</var> ]#
 *	</pre>
 *	message if it wishes that the component's drag content can be exported
 *	to other applications as a string ; otherwise it should set that string
 *	to <code>null</code>, sending
 *	<pre>
 *	[ &quot;/set&quot;, <var>&lt;transferHandlerID&gt;</var>, \string, [ &quot;/ref&quot;, \null ]]
 *	</pre>
 *	<p>
 *	On the other hand, if a string is dragged onto the <code>JComponent</code>, a message
 *	<pre>
 *	[ &quot;/transfer&quot;, <var>&lt;objectID&gt;</var>, &quot;import&quot;, &quot;string&quot;, <var>&lt;draggedString&gt;</var> ]
 *	</pre>
 *	is sent to
 *	the client. If the drag-and-drop happens within SwingOSC, using two <code>JComponent</code>s
 *	with two <code>DummyTransferHandler</code>s, instead the message
 *	<pre>
 *	[ &quot;/transfer&quot;, <var>&lt;objectID&gt;</var>, &quot;import&quot;, &quot;dummy&quot; ]
 *	</pre>
 *	is sent ; in this case the
 *	client is assumed to have recognized the most recent export message and &quot;knows&quot;
 *	what kind of object is being transferred. Have a look at the SuperCollider class
 *	<code>JSCView</code> and its usage of the <code>currentDrag</code> field to find out how this works.
 *	<p>
 *	Note that this class encompasses both the handler and the transferable.
 *	This will never generate separate transferables which means that setting the
 *	<code>stringData</code> to a value will always effect the ongoing drag-and-drop!
 *
 *	@author		Hanns Holger Rutz
 *	@version	0.59, 25-Feb-08
 */
public class DummyTransferHandler
extends TransferHandler
implements Transferable, MouseInputListener
{
	private static final DataFlavor 	dummyFlavor 	= new DataFlavor( Object.class, "Dummy" );
	private static final Object			dummy			= new Object();
	private static final DataFlavor[]	flavors	 		= new DataFlavor[] {
		dummyFlavor, DataFlavor.stringFlavor, DataFlavor.javaFileListFlavor };
	
	// "Meta" key modifier is only available on Mac OS X
//	private static final boolean		isMac			= System.getProperty( "os.name" ).indexOf( "Mac OS" ) >= 0;

	private final SwingOSC			osc;
//	private final Object			objectID;
	private TransferHandler			oldHandler		= null;
	private final JComponent		object;
	private final SwingClient		client;
	protected boolean				isListening		= false;
	private final int				modifiers;

	private MouseEvent				dndFirstEvent	= null;
	private final OSCMessage		exportMsg;
	private final OSCMessage		importDummyMsg;
	private final Object[]			importStringArgs;
	
	private String					stringData		= null;

	/**
	 * 	Creates and registers a new transfer handler for an existing <code>JComponent</code>.
	 * 	This will also start listening to mouse events fired from that component and
	 * 	initiate a drag export automatically when a mouse drag with the specified modifier
	 * 	keys occurs.
	 * 	
	 *	@param	objectID			SwingOSC reference pointing to a </code>JComponent<code> object
	 *	@param	modifiers			mask of modifiers that need to be pressed to initiate a drag ; like
	 * 								<code>2</code> for <code>InputEvent.CTRL_MASK</code>, or
	 *								<code>8</code> for <code>InputEvent.ALT_MASK</code>
	 *	@throws	ClassCastException	if <code>objectID</code> doesn't denote a <code>JComponent</code>
	 */
	public DummyTransferHandler( Object objectID, int modifiers )
	{
		super();
		
		osc					= SwingOSC.getInstance();
		client				= osc.getCurrentClient();
		object				= (JComponent) client.getObject( objectID );
		
		exportMsg			= new OSCMessage( getOSCCommand(), new Object[] { objectID, "export" });
		importDummyMsg		= new OSCMessage( getOSCCommand(), new Object[] { objectID, "import", "dummy" });
		importStringArgs	= new Object[] { objectID, "import", "string", null };
		this.modifiers		= modifiers;
		
		add();
		installSuperBehavior();
	}

	public void add()
	{
		if( !isListening ) {
			oldHandler = object.getTransferHandler();
			object.setTransferHandler( this );
			object.addMouseListener( this );
			object.addMouseMotionListener( this );
			isListening = true;
		}
	}
	
	public void remove()
	{
		if( isListening ) {
			object.setTransferHandler( oldHandler );
			object.removeMouseListener( this );
			object.removeMouseMotionListener( this );
			isListening = false;
		}
	}
	
	private void installSuperBehavior()
	{
		final ActionMap amap						= object.getActionMap();
		final Action	actionPasteFromClipboard	= amap.get( "paste-from-clipboard" );
		final Action	actionCopyToClipboard		= amap.get( "copy-to-clipboard" );
		final Action	actionCutToClipboard		= amap.get( "cut-to-clipboard" );
		if( actionPasteFromClipboard != null ) {
			amap.put( "paste-from-clipboard", new ActionSuperBehavior( actionPasteFromClipboard ));
		}
		if( actionCopyToClipboard != null ) {
			amap.put( "copy-to-clipboard", new ActionSuperBehavior( actionCopyToClipboard ));
		}
		if( actionCutToClipboard != null ) {
			amap.put( "cut-to-clipboard", new ActionSuperBehavior( actionCutToClipboard ));
		}
	}
	
	/**
	 *  Sets the string representation of the <code>Transferable</code>.
	 *  This allows the drag to be exported to other applications in the
	 *  operating system.
	 *  
	 *	@param	s	the string representation or <code>null</code> to
	 *				disallow the drag to be exported to the OS
	 */
	public void setString( String s )
	{
		stringData = s;
	}
	
	private String getOSCCommand()
	{
		return "/transfer";
	}

	// --------------- TransferHandler methods ---------------

	public void exportAsDrag( JComponent c, InputEvent e, int action )
	{
//System.err.println( "exportAsDrag " + action );
		try {
			client.reply( exportMsg );
		}
		catch( IOException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
		super.exportAsDrag( c, e, action );
	}
	
	public boolean importData( JComponent c, Transferable t )
	{
//System.out.println( "import " + t );
		try {
			if( t.isDataFlavorSupported( dummyFlavor )) {
				client.reply( importDummyMsg );
				return true;
			} else if( t.isDataFlavorSupported( DataFlavor.stringFlavor )) {
				importStringArgs[ 3 ] = t.getTransferData( DataFlavor.stringFlavor );
				client.reply( new OSCMessage( getOSCCommand(), importStringArgs ));
				return true;
			} else if( t.isDataFlavorSupported( DataFlavor.javaFileListFlavor )) {
				final List fileList = (List) t.getTransferData( DataFlavor.javaFileListFlavor );
//System.out.println( "fileList.size() = " + fileList.size() );
				if( !fileList.isEmpty() ) {
					final Object[] importFilesArgs = new Object[ fileList.size() + 3 ];
					importFilesArgs[ 0 ] = importStringArgs[ 0 ];
					importFilesArgs[ 1 ] = importStringArgs[ 1 ];
					importFilesArgs[ 2 ] = "files";
					for( int i = 0, j = 3; i < fileList.size(); i++, j++ ) {
						importFilesArgs[ j ] = ((File) fileList.get( i )).getAbsolutePath();
					}
//System.out.println( "replying..." );
					client.reply( new OSCMessage( getOSCCommand(), importFilesArgs ));
					return true;
				}
			}
//System.out.println( "...none" );
			return oldHandler == null ? super.importData( c, t ) : oldHandler.importData( c, t );
		}
		catch( IOException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
		catch( UnsupportedFlavorException ex ) {
			SwingOSC.printException( ex, getOSCCommand() );
		}
		return false;
	}
	
	public boolean canImport( JComponent c, DataFlavor[] f )
	{
//System.out.println( "canImport" );
		for( int i = 0; i < f.length; i++ ) {
//System.out.println( "  " +  f[ i ]);
			for( int j = 0; j < flavors.length; j++ ) {
				if( f[ i ].equals( flavors[ j ])) return true;
			}
		}
//System.out.println( "... no!" );
		return oldHandler == null ? super.canImport( c, f ) : oldHandler.canImport( c, f );
	}

	public int getSourceActions( JComponent c )
	{
//		return( COPY | (oldHandler == null ? super.getSourceActions( c ) : oldHandler.getSourceActions( c )));
		return( COPY );
	}
	
//	public Icon getVisualRepresentation( Transferable t )
//	{
//		if( t.isDataFlavorSupported( dummyFlavor )) {
//			return this;
//		} else {
////			return oldHandler == null ? super.getVisualRepresentation( t ) : oldHandler.getVisualRepresentation( t );
//			return super.getVisualRepresentation( t );
//		}
//	}

	protected Transferable createTransferable( JComponent c )
	{
//System.out.println( "createTransferable" );
		return this;
	}
	
//	public void exportToClipboard( JComponent comp, Clipboard clip, int action )
//	{
//		System.out.println( "exportToClipboard" );
//		super.exportToClipboard( comp, clip, action );
//	}

	//	protected void exportDone( JComponent c, Transferable t,  int action )
//	{
////System.out.println( "exportDone " + t + "; " + action );		
//		super.exportDone( c, t, action );
//	}

//	protected void exportDone( JComponent c, Transferable t,  int action )
//	{
//		if( t.isDataFlavorSupported( dummyFlavor )) {
//			// XXX send OSC message here
//		} else {
//			super.exportDone( c, t, action );
//		}
//	}
	
	private boolean hasDragModifiers( MouseEvent e )
	{
		return( (e.getModifiers() & modifiers) == modifiers );
	}
	
	private void checkDrag( MouseEvent e )
	{
		if( dndFirstEvent == null ) return;
		
		if( e.getPoint().distanceSq( dndFirstEvent.getPoint() ) > 9 ) {
//			e.consume();
//			dndFirstEvent.consume();
//			if( object instanceof AbstractButton ) {
//				// fix a problem with buttons staying armed
//				((AbstractButton) object).getModel().setArmed( false );
//			}
			exportAsDrag( object, dndFirstEvent, COPY );
			dndFirstEvent = null;
		}
	}

	// --------------- Transferable interface ---------------

	public DataFlavor[] getTransferDataFlavors()
	{
		return flavors;
	}
	
	public boolean isDataFlavorSupported( DataFlavor f )
	{
		for( int i = 0; i < flavors.length; i++ ) {
			if( f.equals( flavors[ i ])) return true;
		}
		return false;
	}

	public Object getTransferData( DataFlavor f )
    throws UnsupportedFlavorException, IOException
    {
		if( f.equals( dummyFlavor )) {
			return dummy;
		} else if( f.equals( DataFlavor.stringFlavor )) {
			return stringData;
		} else {
			throw new UnsupportedFlavorException( f );
		}
    }
	
	// --------------- MouseInputListener interface ---------------
	
	public void mouseClicked( MouseEvent e )
	{
		if( hasDragModifiers( e )) {
//			e.consume();
		}
	}

	public void mousePressed( MouseEvent e )
	{
		if( e.getComponent().isEnabled() && hasDragModifiers( e )) {
			dndFirstEvent = e;
//			e.consume();
		}
	}
	
	public void mouseReleased( MouseEvent e )
	{
		dndFirstEvent = null;
		if( hasDragModifiers( e )) {
//			e.consume();
		}
	}

	public void mouseEntered( MouseEvent e ) { /* ignored */ }
	public void mouseExited( MouseEvent e ){  /* ignored */ }
	
	public void mouseMoved( MouseEvent e )
	{
		checkDrag( e );
	}
	
	public void mouseDragged( MouseEvent e )
	{
		checkDrag( e );
	}
	
//	// --------------- Icon interface ---------------
//	
//	public void paintIcon( Component c, Graphics g, int x, int y )
//	{
//		g.translate( x, y );
//		g.setColor( Color.red );
//		g.fillRect( 0, 0, 32, 32 );
//		g.translate( -x, -y );
//	}
//
//	public int getIconWidth()
//	{
//		return 32;
//	}
//
//	public int getIconHeight()
//	{
//		return 32;
//	}
	
	private class ActionSuperBehavior
	extends AbstractAction
	{
		private final Action superBehavior;
		
		protected ActionSuperBehavior( Action superBehavior )
		{
			super();
			this.superBehavior = superBehavior;
		}
		
		public void actionPerformed( ActionEvent e )
		{
			if( isListening ) {
				remove();
				try {
					superBehavior.actionPerformed( e );
				} finally {
					add();
				}
				return;
			}
			superBehavior.actionPerformed( e );
		}
	}
}