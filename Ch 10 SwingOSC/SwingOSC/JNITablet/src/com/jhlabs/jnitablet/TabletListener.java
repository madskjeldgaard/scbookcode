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

/**
 *	The interface for receiving tablet events.
 *
 *	@author		Jerry Huxtable
 *	@author		Hanns Holger Rutz
 *	@version	0.11, 24-Feb-08
 */
public interface TabletListener
{
    public void tabletEvent( TabletEvent e );
    public void tabletProximity( TabletProximityEvent e );
}
