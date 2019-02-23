package de.sciss.swingosc;

//import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Action;
//import javax.swing.KeyStroke;

import de.sciss.app.AbstractApplication;
import de.sciss.app.Document;
import de.sciss.app.DocumentHandler;
import de.sciss.app.DocumentListener;
import de.sciss.common.BasicApplication;
import de.sciss.common.BasicMenuFactory;
import de.sciss.common.BasicWindowHandler;
//import de.sciss.gui.MenuGroup;
//import de.sciss.gui.MenuItem;

public class Application
extends BasicApplication
{
	private final boolean	lafDeco;
	private final boolean	internalFrames;
	private final boolean	floating;
	
	public Application()
	{
		this( false, false, false );
	}
	
	public Application( boolean lafDeco, boolean internalFrames, boolean floating )
	{
		super( Application.class, "SwingOSC" );
		
		this.lafDeco		= lafDeco;
		this.internalFrames	= internalFrames;
		this.floating		= floating;
		init();
	}
	
	public static void ensure()
	{
		ensure( false, false, false );
	}
	
	public static void ensure( boolean lafDeco, boolean internalFrames, boolean floating )
	{
		BasicApplication app = (BasicApplication) AbstractApplication.getApplication(); 
		if( app == null ) {
			app = new Application( lafDeco, internalFrames, floating );
		}
		SwingOSC.getInstance().getCurrentClient().locals.put(
			"menuRoot", app.getMenuBarRoot() );
	}
	
	public double getVersion() { return SwingOSC.VERSION; }
	public String getMacOSCreator() { return "????"; };
	
	protected BasicWindowHandler createWindowHandler()
	{
		return new BasicWindowHandler( this, lafDeco, internalFrames, floating );
	}
	
	protected BasicMenuFactory createMenuFactory()
	{
		return new BasicMenuFactory( this ) {
			public void addMenuItems() {
//				MenuGroup	mg;
//				MenuItem	mi;
				
				remove( get( "file" ));
//				mg = new MenuGroup( "file", getResourceString( "menuFile" ));
//				add( mg, 0 );
//				mi = new MenuItem( "close", getResourceString( "menuClose" ),
//				   KeyStroke.getKeyStroke( KeyEvent.VK_W, MENU_SHORTCUT ));
//				mg.add( mi );
				remove( get( "edit" ));
//				mi = new MenuItem( "minimize", getResourceString( "menuMinimize" ),
//					KeyStroke.getKeyStroke( KeyEvent.VK_M, MENU_SHORTCUT ));
//				mg = (MenuGroup) get( "window" );
//				mg.add( mi, 0 );
				remove( get( "window" ));
				remove( get( "help" ));
			}
			
			public void showPreferences() { /* none */ }
			public void openDocument( File f ) { /* none */ }
			public Action getOpenAction() { return null; }
		};
	}
	
	protected DocumentHandler createDocumentHandler()
	{
		return new DocumentHandler() {
			public void addDocument( Object source, Document doc ) { /* nothing */ }
			public void removeDocument( Object source, Document doc ) { /* nothing */ }
			public void setActiveDocument( Object source, Document doc ) { /* nothing */ }
			public void addDocumentListener( DocumentListener l ) { /* nothing */ }
			public void removeDocumentListener( DocumentListener l ) { /* nothing */ }
			public Document getActiveDocument() { return null; }
			public Document getDocument( int i ) { return null; }
			public int getDocumentCount() { return 0; }
			public boolean isMultiDocumentApplication() { return false; }
		};
	}
}
