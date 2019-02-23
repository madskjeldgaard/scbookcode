Figure 10.19

import java.io.File;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SoundFileChooser extends javax.swing.JFileChooser
implements java.beans.PropertyChangeListener
{
    private final java.util.List listeners = new java.util.ArrayList();
    
    public SoundFileChooser() {
        super();        
        final FileFilter filter = new FileFilter() {            
            public boolean accept( File f ) {
                if( f.isDirectory() ) return true;
                try {
                    return( de.sciss.io.AudioFile.retrieveType( f ) !=
                            de.sciss.io.AudioFileDescr.TYPE_UNKNOWN );
                }
                catch( java.io.IOException e ) { return false; }
            }
        
            public String getDescription() { return "Audio Files"; }
        };
        addChoosableFileFilter( filter );
        setFileFilter( filter );
        setControlButtonsAreShown( false );
        addPropertyChangeListener( SELECTED_FILE_CHANGED_PROPERTY, this );
        addPropertyChangeListener( DIRECTORY_CHANGED_PROPERTY, this );
    }
    
    public String getSelectedPath() {
        final File f = getSelectedFile();
        return f == null ? "" : f.getAbsolutePath();
    }
    
    public void setSelectedPath( String path ) {
        setSelectedFile( new File( path ));
    }
    
    public String getCurrentDirectoryPath() {
        return getCurrentDirectory().getAbsolutePath();
    }
    
    public void setCurrentDirectoryPath( String path ) {
        setCurrentDirectory( new File( path ));
    }
    
    public void addChangeListener( ChangeListener l ) { listeners.add( l ); }
    public void removeChangeListener( ChangeListener l ) { listeners.remove( l ); }

    public void propertyChange( java.beans.PropertyChangeEvent pce ) {
        final ChangeEvent ce = new ChangeEvent( this );
        for( int i = 0; i < listeners.size(); i++ ) {
            ((ChangeListener) listeners.get( i )).stateChanged( ce );
        }
    }
}

