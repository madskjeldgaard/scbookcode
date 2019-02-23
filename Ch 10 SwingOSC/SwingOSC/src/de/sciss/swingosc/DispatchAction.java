package de.sciss.swingosc;

import java.awt.AWTEventMulticaster;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;

public class DispatchAction
extends AbstractAction
{
	private ActionListener em	= null;
	
	public DispatchAction()
	{
		super();
	}
	
	public DispatchAction( String text, KeyStroke accel )
	{
		super( text );
		if( accel != null ) putValue( ACCELERATOR_KEY, accel );
	}

	public DispatchAction( String text )
	{
		super( text );
	}

	public DispatchAction( String text, Icon ic )
	{
		super( text, ic );
	}
	
    public synchronized void addActionListener( ActionListener l )
    {
    	em = AWTEventMulticaster.add( em, l );
    }
    
    public synchronized void removeActionListener( ActionListener l )
    {
        em = AWTEventMulticaster.remove( em, l );
	}

  	public void actionPerformed( ActionEvent e )
	{
// 		System.out.println( "actionPerformed" );
		if( em != null ) em.actionPerformed( e );
	}
}