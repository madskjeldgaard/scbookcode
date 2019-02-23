/*
 *  ColliderLayout.java
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
 *		02-Jan-07	created
 *		14-Jan-08	calls calcSizes again and applies preferred dim to work with JSCScrollTopView
 *		27-Jan-08	conforms with java 1.4
 *		31-Jan-08	special handling for Panel in preferredSize calcluation
 */
package de.sciss.swingosc;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
//import java.awt.Point;
import java.awt.Rectangle;
//import java.awt.Rectangle;
import javax.swing.JComponent;

/**
 *	A layout manager mimicing the layout behaviour of
 *	SuperCollider cocoa GUI's SCCompositeView (resize property).
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.59, 25-Feb-08
 *  
 *  @todo		calcSizes : should rather read minWidth / minHeight etc. client properties
 */
public class ColliderLayout
implements LayoutManager
{
    private int preferredWidth, preferredHeight;
    private int minWidth, minHeight;
 	private final boolean resizeActive;

    private static final Insets	zeroInsets		= new Insets( 0, 0, 0, 0 );
    
//    private boolean sizeUnknown = false;

    public ColliderLayout()
    {
    	this( true );
    }
    
    public ColliderLayout( boolean resizeActive )
    {
    	this.resizeActive = resizeActive;
    }

	// Required by LayoutManager
    public void addLayoutComponent( String name, Component comp )
    {
    	/* empty */
    }

    // Required by LayoutManager
    public void removeLayoutComponent( Component comp )
    {
    	/* empty */
    }

    private void calcSizes( Container parent )
    {
//    	System.out.println( "ColliderLayout for " + parent + " : calcSizes" );

    	final int	nComps = parent.getComponentCount();
//     	Dimension	d;
        Component	c;
//      Point		p;
        Rectangle	r;

        preferredWidth	= 0;
        preferredHeight	= 0;
        minWidth		= 0;
        minHeight		= 0;

        for( int i = 0; i < nComps; i++ ) {
            c = parent.getComponent( i );
            if( c.isVisible() ) {
//              d	= c.getPreferredSize();
                r	= c.getBounds();
// 	            p	= c.getLocation();
                
                // note: we don't really deal with preferred sizes yet
                // except for Panel (which needs to adjust to children
                // being added or removed). otherwise,
                // we assume the sc client has explicitly set bounds,
                // so in order to determine "preferred" container dimensions,
                // we use the current gadgets' bounds
//                if( c instanceof Panel ) {
//                	d				= c.getPreferredSize();
//                	preferredWidth	= Math.max( preferredWidth, r.x + d.width );
//                	preferredHeight	= Math.max( preferredHeight, r.y + d.height );
//                } else {
                	preferredWidth	= Math.max( preferredWidth, r.x + r.width );
                	preferredHeight	= Math.max( preferredHeight, r.y + r.height );
//                }
                minWidth		= Math.max( minWidth, r.x + c.getMinimumSize().width );
                minHeight		= Math.max( minHeight, r.y + c.getMinimumSize().width );
            }
        }
//       sizeUnknown = false;
        
//       System.out.println( "preferredWidth  " + preferredWidth );
//       System.out.println( "preferredHeight " + preferredHeight );
    }

    public Dimension preferredLayoutSize( Container parent )
    {
//    	System.out.println( "ColliderLayout for " + parent + " : preferredLayoutSize" );

    	calcSizes( parent );

        final Insets insets = parent.getInsets();
        
        return new Dimension( preferredWidth + insets.left + insets.right,
			        		  preferredHeight + insets.top + insets.bottom );
    }

    public Dimension minimumLayoutSize( Container parent )
    {
//    	System.out.println( "minimumLayoutSize" );
//
    	calcSizes( parent );

        final Insets insets = parent.getInsets();
        
        return new Dimension( minWidth + insets.left + insets.right,
			        		  minHeight + insets.top + insets.bottom );
    }

    public void layoutContainer( Container parent )
    {
//		if( sizeUnknown ) {
			((JComponent) parent).setPreferredSize( preferredLayoutSize( parent ));
//		}

//System.out.println( "layoutContainer for " + parent + " : prefSize = " + preferredWidth + ", " + preferredHeight );
    	if( !resizeActive ) return;
    	
//      final Insets	insets 	= parent.getInsets();
        final int 		nComps 	= parent.getComponentCount();
        final Dimension	d		= parent.getSize();
        int				dx, dy, minW, minH, maxW, maxH, cw, ch;
        Component 		c;
        JComponent		jc;
        Dimension		sizeref;
        Insets			insets;
        Number			resize, prop;

//      not needed!
//      if( sizeUnknown ) calcSizes( parent );
      
        for( int i = 0 ; i < nComps ; i++ ) {
            c = parent.getComponent( i );
            if( c.isVisible() && (c instanceof JComponent) ) {
            	jc 		= (JComponent) c;
            	resize	= (Number) jc.getClientProperty( "resize" );
            	if( resize != null ) {
            		sizeref	= (Dimension) jc.getClientProperty( "sizeref" );
            		if( sizeref != null ) {
            			dx		= d.width - sizeref.width;
            			dy		= d.height - sizeref.height;
            			if( (dx != 0) || (dy != 0) ) {
                			prop	= (Number) jc.getClientProperty( "minWidth" );
                			minW	= prop == null ? 0 : prop.intValue();
                			prop	= (Number) jc.getClientProperty( "maxWidth" );
                			maxW	= prop == null ? Integer.MAX_VALUE : prop.intValue();
                			prop	= (Number) jc.getClientProperty( "minHeight" );
                			minH	= prop == null ? 0 : prop.intValue();
                			prop	= (Number) jc.getClientProperty( "maxHeight" );
                			maxH	= prop == null ? Integer.MAX_VALUE : prop.intValue();
                			insets	= (Insets) jc.getClientProperty( "insets" );
                			if( insets == null ) insets = zeroInsets;
                			cw		= c.getWidth() - (insets.left + insets.right);
                			ch		= c.getHeight() - (insets.top + insets.bottom);
                			
//	            			1  2  3
//	            			4  5  6
//	            			7  8  9
//	            			
//	            			1 - fixed to left, fixed to top
//	            			2 - horizontally elastic, fixed to top
//	            			3 - fixed to right, fixed to top
//	            			
//	            			4 - fixed to left, vertically elastic
//	            			5 - horizontally elastic, vertically elastic
//	            			6 - fixed to right, vertically elastic
//	            			
//	            			7 - fixed to left, fixed to bottom
//	            			8 - horizontally elastic, fixed to bottom
//	            			9 - fixed to right, fixed to bottom
	
	            			switch( resize.intValue() ) {
	            			case 1: // fixed to left, fixed to top
	            				break;
	            				
	            			case 2:	// horizontally elastic, fixed to top
	            				if( dx != 0 ) {
	            					cw += dx;
	            					if( cw > maxW ) {
	            						d.width += maxW - cw;
	            						cw = maxW;
	            					} else if( cw < minW ) {
	            						d.width += minW - cw;
	            						cw = minW;
	            					}
	            					c.setSize( cw + (insets.left + insets.right), c.getHeight() );
	            				}
	            				break;
	            			
	            			case 3: // fixed to right, fixed to top
	            				if( dx != 0 ) {
	            					c.setLocation( c.getX() + dx, c.getY() );
	            				}
	            				break;
		
		        			case 4:	// fixed to left, vertically elastic
		        				if( dy != 0 ) {
		        					ch += dy;
	            					if( ch > maxH ) {
	            						d.height += maxH - ch;
	            						ch = maxH;
	            					} else if( ch < minH ) {
	            						d.height += minH - ch;
	            						ch = minH;
	            					}
		        					c.setSize( c.getWidth(), ch + (insets.top + insets.bottom) );
		        				}
		        				break;
		        			
		           			case 5:	// horizontally elastic, vertically elastic
            					cw += dx;
            					if( cw > maxW ) {
            						d.width += maxW - cw;
            						cw = maxW;
            					} else if( cw < minW ) {
            						d.width += minW - cw;
            						cw = minW;
            					}
	        					ch += dy;
            					if( ch > maxH ) {
            						d.height += maxH - ch;
            						ch = maxH;
            					} else if( ch < minH ) {
            						d.height += minH - ch;
            						ch = minH;
            					}
	           					c.setSize( cw + (insets.left + insets.right),
	           							   ch + (insets.top + insets.bottom) );
	            				break;
	 
		           			case 6: // fixed to right, vertically elastic
		        				if( dy != 0 ) {
		        					ch += dy;
	            					if( ch > maxH ) {
	            						d.height += maxH - ch;
	            						ch = maxH;
	            					} else if( ch < minH ) {
	            						d.height += minH - ch;
	            						ch = minH;
	            					}
		        				}
	            				c.setBounds( c.getX() + dx, c.getY(),
	            							 c.getWidth(), ch + (insets.top + insets.bottom) );
		           				break;
		           				
		           			case 7: // fixed to left, fixed to bottom
		        				if( dy != 0 ) {
		        					c.setLocation( c.getX(), c.getY() + dy );
		        				}
		        				break;
	            			
	            			case 8: // 8 - horizontally elastic, fixed to bottom
	            				if( dx != 0 ) {
	            					cw += dx;
	            					if( cw > maxW ) {
	            						d.width += maxW - cw;
	            						cw = maxW;
	            					} else if( cw < minW ) {
	            						d.width += minW - cw;
	            						cw = minW;
	            					}
	            				}
	        					c.setBounds( c.getX(), c.getY() + dy, cw + (insets.left + insets.right),
	        							     c.getHeight() );
	            				break;
	            				
	            			case 9:	// 9 - fixed to right, fixed to bottom
            					c.setLocation( c.getX() + dx, c.getY() + dy );
	            				break;
	            				
	            			default:
	            				throw new IllegalArgumentException( "Illegal resize value (" + resize + ")" );
	            			}
	            			// now update
	            			jc.putClientProperty( "sizeref", d );
	            		}
            		}
            	}
            }
        }
    }
}
