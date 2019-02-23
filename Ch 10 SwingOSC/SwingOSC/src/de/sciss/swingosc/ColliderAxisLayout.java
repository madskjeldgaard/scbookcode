/*
 *  ColliderHLayout.java
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
 *		21-Jan-07	created
 */
package de.sciss.swingosc;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;

/**
 *	A layout manager mimicing the layout behaviour of
 *	SuperCollider cocoa GUI's SCHLayoutView.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.59, 25-Feb-08
 */
public class ColliderAxisLayout
implements LayoutManager
{
    public static final int 	X_AXIS			= 0;
    public static final int 	Y_AXIS			= 1;

    private int					preferredWidth, preferredHeight;
    private int					minWidth, minHeight;
    private int					spacing;
    private final boolean		horiz;
    private final int[]			elasticResizeTypes;
    private final String		minProp, maxProp;
    private Insets				margin			= new Insets( 0, 0, 0, 0 );
    private static final Insets	zeroInsets		= new Insets( 0, 0, 0, 0 );

	public ColliderAxisLayout()
	{
		this( X_AXIS, 0 );
	}

	public ColliderAxisLayout( int orient )
	{
		this( orient, 0 );
	}

	/**
     * @param orient	either of <code>X_AXIS</code> or <code>Y_AXIS</code>
     */
	public ColliderAxisLayout( int orient, int spacing )
	{
		switch( orient ) {
		case X_AXIS:
			elasticResizeTypes	= new int[] { 2, 5, 7 };
			horiz				= true;
			minProp				= "minWidth";
			maxProp				= "maxWidth";
			break;
		case Y_AXIS:
			elasticResizeTypes	= new int[] { 4, 5, 6 };
			horiz				= false;
			minProp				= "minHeight";
			maxProp				= "maxHeight";
			break;
		default:
			throw new IllegalArgumentException( String.valueOf( orient ));
		}
		this.spacing = spacing;
	}
	
	public void setSpacing( int spacing )
	{
		this.spacing = spacing;
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
        final int	nComps = parent.getComponentCount();
        Dimension	d;
        Component	c;
        Point		p;
//    	Rectangle	r;

        preferredWidth	= 0;
        preferredHeight	= 0;
        minWidth		= 0;
        minHeight		= 0;

        for( int i = 0; i < nComps; i++ ) {
            c = parent.getComponent( i );
            if( c.isVisible() ) {
                d	= c.getPreferredSize();
//                r	= c.getBounds();
                p	= c.getLocation();
                
                preferredWidth	= Math.max( preferredWidth, p.x + d.width );
                preferredHeight	= Math.max( preferredHeight, p.y + d.height );
                minWidth		= Math.max( minWidth, p.x + c.getMinimumSize().width );
                minHeight		= Math.max( minHeight, p.y + c.getMinimumSize().width );
            }
        }
//       sizeUnknown = false;
    }

    public Dimension preferredLayoutSize( Container parent )
    {
    	calcSizes( parent );

        final Insets insets = parent.getInsets();
        
        return new Dimension( preferredWidth + insets.left + insets.right,
			        		  preferredHeight + insets.top + insets.bottom );
    }

    public Dimension minimumLayoutSize( Container parent )
    {
        calcSizes( parent );

        final Insets insets = parent.getInsets();
        
        return new Dimension( minWidth + insets.left + insets.right,
			        		  minHeight + insets.top + insets.bottom );
    }

    // 1. totalExtent - allFixedExtents - ((numComp - 1) * spacing) = totalElasticExtent
    // 2. normElasticExtent for elastic component = totalElasticExtent / numElastic
    // 3. do( for all elastic : if minExtent > normElasticExtent : width = minExtent, numElastic--, totalElasticExtent -= minExtent;  else break )
    // 4. do( for all elastic : if maxExtent < normElasticExtent : width = maxExtent, numElastic--, totalElasticExtent -= maxExtent;  else break )
    public void layoutContainer( Container parent )
    {
//		((JComponent) parent).setPreferredSize( preferredLayoutSize( parent ));

		final int 		nComps 				= parent.getComponentCount();
        final Set		elastic				= new HashSet( nComps );
        final Insets	parentInsets		= parent.getInsets();
        final int		parentWidth			= parent.getWidth() - (margin.left + margin.right + parentInsets.left + parentInsets.right);
        final int		parentHeight		= parent.getHeight() - (margin.top + margin.bottom + parentInsets.top + parentInsets.bottom);
        final int		fill				= horiz ? parentHeight : parentWidth;
        Component 		c;
        JComponent		jc;
        Number			num;
        Insets			insets;
        int				numi;
        int				totalElasticExtent	= (horiz ? parentWidth : parentHeight) - (nComps - 1) * spacing;
        int				normElasticExtent	= 0;
        int				offX				= margin.left + parentInsets.left;
        int				offY				= margin.top + parentInsets.top;
        int				numElastic			= 0;
        int				numDiv, elasticLeft;

checkElastic:
		for( int i = 0; i < nComps; i++ ) {
            c = parent.getComponent( i );
            if( c.isVisible() ) {
        		numElastic++; // stupid concept from cocoa : all components
        					  // count in the normElasticExtent even the non-elastic ones
        		if( c instanceof JComponent ) {
	            	jc 	= (JComponent) c;
	            	num	= (Number) jc.getClientProperty( "resize" );
	            	if( num != null ) {
	            		numi = num.intValue();
        				for( int j = 0; j < elasticResizeTypes.length; j++ ) {
        					if( numi == elasticResizeTypes[ j ]) {
        						elastic.add( jc );
        						continue checkElastic;
        					}
        				}
	            	}
        		}
            }
		}
        
        // check against minimum / maximum
       	for( boolean rescan = true; !elastic.isEmpty() && rescan; ) {
        	elasticLeft			= totalElasticExtent;
        	numDiv				= numElastic;
           	rescan				= false;
  scan:     		
	  		for( Iterator iter = elastic.iterator(); iter.hasNext(); ) {
    			jc 					= (JComponent) iter.next();
        		normElasticExtent	= elasticLeft / numDiv;
        		elasticLeft			= elasticLeft - normElasticExtent;
        		numDiv--;
    			num					= (Number) jc.getClientProperty( minProp );
    			if( num != null ) {
    				numi = num.intValue();
    				if( numi > normElasticExtent ) {
    					iter.remove();
    					numElastic--;
    					totalElasticExtent -= numi;
        				insets	= (Insets) jc.getClientProperty( "insets" );
        				if( insets == null ) insets = zeroInsets;
    					if( horiz ) {
    						jc.setSize( numi + (insets.left + insets.right),
    								    fill + (insets.top + insets.bottom ));
    					} else {
    						jc.setSize( fill + (insets.left + insets.right),
    								    numi + (insets.top + insets.bottom ));
    					}
    					rescan = true;
    					continue scan;
    				}
    			}
    			num	= (Number) jc.getClientProperty( maxProp );
    			if( num != null ) {
    				numi = num.intValue();
    				if( numi < normElasticExtent ) {
    					iter.remove();
    					numElastic--;
    					totalElasticExtent -= numi;
        				insets	= (Insets) jc.getClientProperty( "insets" );
        				if( insets == null ) insets = zeroInsets;
    					if( horiz ) {
    						jc.setSize( numi + (insets.left + insets.right),
								    	fill + (insets.top + insets.bottom ));
    					} else {
    						jc.setSize( fill + (insets.left + insets.right),
								    	numi + (insets.top + insets.bottom ));
    					}
    					rescan = true;
    				}
    			}
    		}
       	}

		elasticLeft	= totalElasticExtent;
		numDiv		= numElastic;
		if( horiz ) {  // --- horizontal ---
			for( int i = 0; i < nComps; i++ ) {
	            c = parent.getComponent( i );
	            if( c.isVisible() ) {
	        		if( elastic.contains( c )) {
	            		normElasticExtent	= elasticLeft / numDiv;
	            		elasticLeft			= elasticLeft - normElasticExtent;
	            		numDiv--;
	            		insets				= (Insets) ((JComponent) c).getClientProperty( "insets" );
	            		if( insets == null ) insets = zeroInsets;
        				if( (c.getX() + insets.left != offX) || (c.getY() + insets.top != offY) ||
        					(c.getWidth() - (insets.left + insets.right) != normElasticExtent) ||
        					(c.getHeight() - (insets.top + insets.bottom) != fill) ) {
        					
        					c.setBounds( offX - insets.left, offY - insets.top,
        							     normElasticExtent + (insets.left + insets.right),
        							     fill + (insets.top + insets.bottom ));
        				}
	        		} else { // --- horiz non-elastic ---
	        			if( c instanceof JComponent ) {
	                		insets = (Insets) ((JComponent) c).getClientProperty( "insets" );
	                		if( insets == null ) insets = zeroInsets;
	        			} else {
	        				insets = zeroInsets;
	        			}
        				if( (c.getX() + insets.left != offX) || (c.getY() + insets.top != offY) ||
        					(c.getHeight() - (insets.top + insets.bottom) != fill) ) {
        					c.setBounds( offX - insets.left, offY - insets.top,
        							     c.getWidth(), fill + (insets.top + insets.bottom ));
        				}
	        		}
	            	offX += c.getWidth() - (insets.left + insets.right) + spacing;
	            }
			}

			((JComponent) parent).setPreferredSize( new Dimension( offX, offY + fill ));

		} else {  // --- vertical ---
			for( int i = 0; i < nComps; i++ ) {
	            c = parent.getComponent( i );
	            if( c.isVisible() ) {
	        		if( elastic.contains( c )) {
	            		normElasticExtent	= elasticLeft / numDiv;
	            		elasticLeft			= elasticLeft - normElasticExtent;
	            		numDiv--;
	            		insets				= (Insets) ((JComponent) c).getClientProperty( "insets" );
	            		if( insets == null ) insets = zeroInsets;
        				if( (c.getX() + insets.left != offX) || (c.getY() + insets.top != offY) ||
            				(c.getWidth() - (insets.left + insets.right) != fill) ||
            				(c.getHeight() - (insets.top + insets.bottom) != normElasticExtent) ) {
            					
            				c.setBounds( offX - insets.left, offY - insets.top,
            						     fill + (insets.left + insets.right), 
            						     normElasticExtent + (insets.top + insets.bottom ));
            			}
	        		} else { // --- vert non-elastic ---
	        			if( c instanceof JComponent ) {
	                		insets = (Insets) ((JComponent) c).getClientProperty( "insets" );
	                		if( insets == null ) insets = zeroInsets;
	        			} else {
	        				insets = zeroInsets;
	        			}
        				if( (c.getX() + insets.left != offX) || (c.getY() + insets.top != offY) ||
        					(c.getWidth() - (insets.left + insets.right) != fill) ) {
        					c.setBounds( offX - insets.left, offY - insets.top,
        							     fill + (insets.left + insets.right), c.getHeight() );
        				}
	        		}
	            	offY += c.getHeight() - (insets.top + insets.bottom) + spacing;
              	}
            }
			
			((JComponent) parent).setPreferredSize( new Dimension( offX + fill, offY ));
		}
    }
}