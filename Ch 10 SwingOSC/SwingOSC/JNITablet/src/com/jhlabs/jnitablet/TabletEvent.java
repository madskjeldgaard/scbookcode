/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/*
 *	Extended functionality and/or slight adjustments by Hanns Holger Rutz
 *	(C)opyright 2007-2008
 */

package com.jhlabs.jnitablet;

import java.awt.AWTEvent;

/**
 *	The class representing a tablet event.
 *
 *	@author		Jerry Huxtable
 *	@author		Hanns Holger Rutz
 *	@version	0.11, 24-Feb-08
 */
public class TabletEvent extends AWTEvent
{
	private final int		deviceID;
	private final float		x, y;
	private final int		absoluteX;
	private final int		absoluteY;
	private final int		absoluteZ;
	private final int		buttonMask;
//	private final int		buttonNumber;
	private final int		clickCount;
	private final float		pressure;
	private final float		rotation;
	private final float		tiltX;
	private final float		tiltY;
	private final float		tangentialPressure;
	private final short		vendorDefined1;
	private final short		vendorDefined2;
	private final short		vendorDefined3;
	
	public TabletEvent( Object source,
		int type, int deviceID,
		float x, float y,
		int absoluteX, int absoluteY, int absoluteZ,
		int buttonMask,
//		int buttonNumber,
		int clickCount,
		float pressure,
		float rotation,
		float tiltX,
		float tiltY,
		float tangentialPressure,
		short vendorDefined1,
		short vendorDefined2,
		short vendorDefined3
	) {
		super( source, type );
		this.deviceID			= deviceID;
		this.x					= x;
		this.y					= y;
		this.absoluteX			= absoluteX;
		this.absoluteY			= absoluteY;
		this.absoluteZ			= absoluteZ;
		this.buttonMask			= buttonMask;
//		this.buttonNumber		= buttonNumber;
		this.clickCount			= clickCount;
		this.pressure			= pressure;
		this.rotation			= rotation;
		this.tiltX				= tiltX;
		this.tiltY				= tiltY;
		this.tangentialPressure	= tangentialPressure;
		this.vendorDefined1		= vendorDefined1;
		this.vendorDefined2		= vendorDefined2;
		this.vendorDefined3		= vendorDefined3;
	}
	
	public int getDeviceID() { return deviceID; }

	public float getX() { return x; }
	public float getY() { return y; }

	public int getAbsoluteX() { return absoluteX; }
	public int getAbsoluteY() { return absoluteY; }
	public int getAbsoluteZ() { return absoluteZ; }

	public int getButtonMask() { return buttonMask; }
//	public int getButtonNumber() { return buttonNumber; }
	public int getClickCount() { return clickCount; }

	public float getPressure() { return pressure; }

	public float getRotation() { return rotation; }

	public float getTiltX() { return tiltX; }
	public float getTiltY() { return tiltY; }

	public float getTangentialPressure() { return tangentialPressure; }

	public short getVendorDefined1() { return vendorDefined1; }
	public short getVendorDefined2() { return vendorDefined2; }
	public short getVendorDefined3() { return vendorDefined3; }
}