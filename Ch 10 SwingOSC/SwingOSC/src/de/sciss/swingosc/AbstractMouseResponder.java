package de.sciss.swingosc;

import java.lang.reflect.InvocationTargetException;

public abstract class AbstractMouseResponder
extends AbstractResponder
{
	private final Frame		f;
	protected boolean		acceptsMouseOver;
	
	protected AbstractMouseResponder( Object objectID, int numReplyArgs, Object frameID  )
	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
		super( objectID, numReplyArgs );

		final Object o = frameID == null ? null : client.getObject( frameID );
		if( (o != null) && (o instanceof Frame) ) {
			f = (Frame) o;
			f.registerMouseResponder( this );
			acceptsMouseOver = f.getAcceptMouseOver();
		} else {
			f = null;
			acceptsMouseOver = true;
		}
	}

	public void setAcceptMouseOver( boolean onOff ) {
		acceptsMouseOver = onOff;
	}
	
	public boolean getAcceptMouseOver()
	{
		return acceptsMouseOver;
	}

	public void remove()
	throws IllegalAccessException, InvocationTargetException
	{
		if( f != null ) f.unregisterMouseResponder( this );
		super.remove();
	}
}
