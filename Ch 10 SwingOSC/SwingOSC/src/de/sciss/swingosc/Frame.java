/*
 *  Frame.java
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
 *		14-Oct-06	created
 *		27-Jul-08	copied from de.sciss.eisenkraut.net.PlugInWindow
 */

package de.sciss.swingosc;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;

import de.sciss.common.AppWindow;
import de.sciss.gui.GUIUtil;
import de.sciss.gui.MenuAction;

public class Frame
extends AppWindow
{
	public static final int	FLAG_UNDECORATED	= 0x01;
	public static final int	FLAG_SCROLLPANE		= 0x02;
	public static final int	FLAG_NORESIZE		= 0x04;

//	private final ShowWindowAction	actionShowWindow;
//	private final BasicMenuFactory	mf;
	
	private final Map				winL		= new HashMap();
	private final JComponent		topView;
	private final MenuAction		actionClose;

	private List		collMouseResp		= null;
	private boolean 	acceptsMouseOver	= false;

	public Frame( String title, Rectangle cocoaBounds, int flags )
	{
		super( REGULAR );
//		final BasicApplication app = (BasicApplication) AbstractApplication.getApplication(); 
//		mf = app.getMenuFactory();
//		actionShowWindow = new ShowWindowAction( this );
//		mf.addToWindowMenu( actionShowWindow );	// MUST BE BEFORE INIT()!!
		
		actionClose = new ActionClose();
//		mf.putMimic( "file.close", this, actionClose );
//		mf.putMimic( "window.minimize", this, new ActionMinimize() );
		final InputMap	imap = this.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );
		final ActionMap	amap = this.getActionMap();
		final int		modif = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_W, modif ), "close" );
		amap.put( "close", actionClose );
		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_M, modif ), "minimize" );
		amap.put( "minimize", new ActionMinimize() );
		
		init();
		setTitle( title );	// needs to be after init. WHY?

		if( (flags & FLAG_UNDECORATED) != 0 ) {
			setUndecorated( true );
		}
		if( (flags & FLAG_NORESIZE) != 0 ) {
			setResizable( false );
		}
		try {
//			final ClassLoader cl = OSCRoot.getInstance().getGUI().getSwingOSC().getClass().getClassLoader();
//			final ClassLoader cl = getClass().getClassLoader();
//			topView		= (JComponent) Class.forName( "de.sciss.swingosc.ContentPane", true, cl ).getConstructor( new Class[] { Boolean.TYPE }).newInstance( new Object[] { new Boolean( (flags & FLAG_SCROLLPANE) == 0) });
			topView = new ContentPane( (flags & FLAG_SCROLLPANE) == 0 );
			if( (flags & FLAG_SCROLLPANE) != 0 ) {
//				final JComponent scrollPane = (JComponent) Class.forName( "de.sciss.swingosc.ScrollPane", true, cl ).getConstructor( new Class[] { Component.class }).newInstance( new Object[] { topView });
				final JComponent scrollPane = new ScrollPane( topView );
				setContentPane( scrollPane );
			} else {
				setContentPane( topView );
			}
		}
		catch( Exception e ) {
//			mf.removeFromWindowMenu( actionShowWindow );
			e.printStackTrace();
			throw new IllegalStateException();
		}

		topView.setPreferredSize( cocoaBounds.getSize() );
		pack();	// frame is made displayable
		final Rectangle screenBounds = getWindow().getGraphicsConfiguration().getBounds();
		final Insets insets = getInsets();
		setLocation( new Point(
		    screenBounds.x + cocoaBounds.x - insets.left,
		    (screenBounds.y + screenBounds.height) - (cocoaBounds.y + cocoaBounds.height) - insets.top ));
		
//		topView.requestFocus();
	}
	
//	public JComponent getTopView()
//	{
//		return topView;
//	}
	
	public void setDefaultCloseOperation( int mode )
	{
		super.setDefaultCloseOperation( mode );
		actionClose.setEnabled( mode != WindowConstants.DO_NOTHING_ON_CLOSE );
	}

	public void setCocoaBounds( Rectangle r )
	{
		final Rectangle	screenBounds	= getWindow().getGraphicsConfiguration().getBounds();
		final Insets	insets			= getInsets();
		
		setBounds( new Rectangle(
		    screenBounds.x + r.x - insets.left,
		    (screenBounds.y + screenBounds.height) - (r.y + r.height) - insets.top,
		    r.width + (insets.left + insets.right),
		    r.height + (insets.top + insets.bottom) ));
	}
	
	public void registerMouseResponder( AbstractMouseResponder r )
	{
		if( collMouseResp == null ) collMouseResp = new ArrayList();
		collMouseResp.add( r );
	}

	public void unregisterMouseResponder( AbstractMouseResponder r )
	{
		collMouseResp.remove( r );
	}
	
	public void setAcceptMouseOver( boolean onOff )
	{
		if( acceptsMouseOver != onOff ) {
			acceptsMouseOver = onOff;
			if( collMouseResp != null ) {
				for( int i = 0; i < collMouseResp.size(); i++ ) {
					((AbstractMouseResponder) collMouseResp.get( i )).setAcceptMouseOver( onOff );
				}
			}
		}
	}

	public boolean getAcceptMouseOver()
	{
		return acceptsMouseOver;
	}
	
	public void addComponentListener( ComponentListener l )
	{
		getWindow().addComponentListener( l );
	}
	
	public void removeComponentListener( ComponentListener l )
	{
		getWindow().removeComponentListener( l );
	}

	protected boolean alwaysPackSize()
	{
		return false;
	}
	
	public void setTitle( String title )
	{
		super.setTitle( title );
//		actionShowWindow.putValue( Action.NAME, title );
	}

//	public void dispose()
//	{
//		mf.removeFromWindowMenu( actionShowWindow );
//		actionShowWindow.dispose();
//		super.dispose();
//	}
	
	protected WindowEvent windowEvent( Event e )
	{
//		return new WindowEvent( e.getWindow(), e.getID() );
// THROWS NULL SOURCE:
//		return new WindowEvent( null, e.getID() );	// dirty

		return null;	// extra cheesy
	}
	
	public void addWindowListener( final WindowListener wl )
	{
		final Listener l;
		
		l = new Adapter() {
			public void windowOpened( Event e )
			{
				wl.windowOpened( windowEvent( e ));
			}

			public void windowClosing( Event e )
			{
				wl.windowClosing( windowEvent( e ));
			}

			public void windowClosed( Event e )
			{
				wl.windowClosed( windowEvent( e ));
			}

			public void windowIconified( Event e )
			{
				wl.windowIconified( windowEvent( e ));
			}
			
			public void windowDeiconified( Event e )
			{
				wl.windowDeiconified( windowEvent( e ));
			}

			public void windowActivated( Event e )
			{
				wl.windowActivated( windowEvent( e ));
			}

			public void windowDeactivated( Event e )
			{
				wl.windowDeactivated( windowEvent( e ));
			}
		};
		
		addListener( l );
		winL.put( wl, l );
	}
	
	public void removeWindowListener( WindowListener wl )
	{
		final Listener l = (Listener) winL.remove( wl );
		removeListener( l );
	}
	
	public void addWindowFocusListener( WindowFocusListener l )
	{
		// XXX nothing
	}

	public void removeWindowFocusListener( WindowFocusListener l )
	{
		// XXX nothing
	}
	
	public void setAlwaysOnTop( boolean onTop )
	{
		GUIUtil.setAlwaysOnTop( getWindow(), onTop );
	}
	
	public void repaint()
	{
		getWindow().repaint();
	}
	
	public void minimize()
	{
		final Component c = getWindow();
		if( c instanceof java.awt.Frame ) {
			((java.awt.Frame) c).setExtendedState( java.awt.Frame.ICONIFIED );
		} else if( c instanceof JInternalFrame ) {
			try {
				((JInternalFrame) c).setIcon( true );
			}
			catch( PropertyVetoException pve ) { /* well... */ }
		} else {
			assert false : c.getClass();
		}
	}

	public void unminimize()
	{
		final Component c = getWindow();
		if( c instanceof java.awt.Frame ) {
			((java.awt.Frame) c).setExtendedState( java.awt.Frame.NORMAL );
		} else if( c instanceof JInternalFrame ) {
			try {
				((JInternalFrame) c).setIcon( false );
			}
			catch( PropertyVetoException pve ) { /* well... */ }
		} else {
			assert false : c.getClass();
		}
	}
	
    /**
     *	A slightly modified version of what was published here
     *	http://www.beatniksoftware.com/jujitsu/svn/trunk/src/e/util/GuiUtilities.java
     */
    public void setAlpha( float alpha )
    {
    	try {
	    	final Field peerField = Component.class.getDeclaredField( "peer" );
	    	peerField.setAccessible( true );
	    	final Object peer = peerField.get( getWindow() );
	    	if( peer == null ) {
//	    		System.err.println( "peer == null" );
	    		return;
	    	}
	    	
	    	if( SwingOSC.isMacOS() ) {
	    		final Class cWindowClass = Class.forName("apple.awt.CWindow");
	    		if( cWindowClass.isInstance( peer )) {
	    			// ((apple.awt.CWindow) peer).setAlpha( alpha );
	    			final Method setAlphaMethod = cWindowClass.getMethod( "setAlpha", new Class[] { float.class });
	                setAlphaMethod.invoke( peer, new Object[] { new Float( alpha )});
	    		}
	    	} else if( SwingOSC.isWindows() ) {
	    		// FIXME: can we do this on Windows?
	        } else {
	           	// long windowId = peer.getWindow();
	        	final Class xWindowPeerClass = Class.forName( "sun.awt.X11.XWindowPeer" );
	        	final Method getWindowMethod = xWindowPeerClass.getMethod( "getWindow", new Class[ 0 ]);
	        	final long windowId = ((Long) getWindowMethod.invoke( peer, new Object[ 0 ])).longValue();
	        	final long value = (int) (0xFF * alpha) << 24;
	            // sun.awt.X11.XAtom.get("_NET_WM_WINDOW_OPACITY").setCard32Property(windowId, value);
	        	final Class xAtomClass = Class.forName("sun.awt.X11.XAtom");
	            final Method getMethod = xAtomClass.getMethod( "get", new Class[] { String.class });
	            final Method setCard32PropertyMethod = xAtomClass.getMethod( "setCard32Property", new Class[] { long.class, long.class });
	            setCard32PropertyMethod.invoke( getMethod.invoke( null, new Object[] { "_NET_WM_WINDOW_OPACITY" }), new Object[] { new Long( windowId ), new Long( value )});
	        }
    	} catch( Exception ex ) {
    		ex.printStackTrace();
    		return;
        }
    }

    private class ActionClose
	extends MenuAction
	{
		protected ActionClose()
		{
			super();
		}
		
		public void actionPerformed( ActionEvent e )
		{
			final Component c = getWindow();
			if( c instanceof Window ) {
//				dispatchEvent( new AbstractWindow.Event( Frame.this,
//					                         			    AbstractWindow.Event.WINDOW_CLOSING )
				c.dispatchEvent( new WindowEvent( (Window) c, WindowEvent.WINDOW_CLOSING ));
			} else if( c instanceof JInternalFrame ) {
				c.dispatchEvent( new InternalFrameEvent( (JInternalFrame) c, InternalFrameEvent.INTERNAL_FRAME_CLOSING ));
//				((JInternalFrame) c).dispatchEvent( e )
			} else {
				assert false : c.getClass();
			}
		}
	}

	private class ActionMinimize
	extends MenuAction
	{
		protected ActionMinimize()
		{
			super();
		}
		
		public void actionPerformed( ActionEvent e )
		{
			minimize();
		}
	}
}