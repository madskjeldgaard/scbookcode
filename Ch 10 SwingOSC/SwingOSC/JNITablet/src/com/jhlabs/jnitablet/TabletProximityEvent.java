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
 *	The class representing a tablet proximity event.
 *
 *	@author		Jerry Huxtable
 *	@author		Hanns Holger Rutz
 *	@version	0.11, 24-Feb-08
 */
public class TabletProximityEvent extends AWTEvent
{
	private final int		deviceID;
	private final int		capabilityMask;
	private final boolean	enteringProximity;
	private final int		pointingDeviceID;
	private final int		pointingDeviceSerialNumber;
	private final int		pointingDeviceType;
	private final int		systemTabletID;
	private final int		tabletID;
	private final int		uniqueID;
	private final int		vendorID;
	private final int		vendorPointingDeviceType;

	public TabletProximityEvent( Object source,
		int deviceID,
		int capabilityMask,
		boolean enteringProximity,
		int pointingDeviceID,
		int pointingDeviceSerialNumber,
		int pointingDeviceType,
		int systemTabletID,
		int tabletID,
		int uniqueID,
		int vendorID,
		int vendorPointingDeviceType
	) {
		super( source, 0 );
		this.deviceID					= deviceID;
		this.capabilityMask				= capabilityMask;
		this.enteringProximity			= enteringProximity;
		this.pointingDeviceID			= pointingDeviceID;
		this.pointingDeviceSerialNumber	= pointingDeviceSerialNumber;
		this.pointingDeviceType			= pointingDeviceType;
		this.systemTabletID				= systemTabletID;
		this.tabletID					= tabletID;
		this.uniqueID					= uniqueID;
		this.vendorID					= vendorID;
		this.vendorPointingDeviceType	= vendorPointingDeviceType;
	}

	public int getDeviceID() { return deviceID; }

	public int getCapabilityMask() { return capabilityMask; }

	public boolean isEnteringProximity() { return enteringProximity; }
	
	public int getPointingDeviceID() { return pointingDeviceID; }
	public int getPointingDeviceSerialNumber() { return pointingDeviceSerialNumber; }
	public int getPointingDeviceType() { return pointingDeviceType; }

	public int getSystemTabletID() { return systemTabletID; }
	public int getTabletID() { return tabletID; }
	public int getUniqueID() { return uniqueID; }

	public int getVendorID() { return vendorID; }

	public int getVendorPointingDeviceType() { return vendorPointingDeviceType; }
}