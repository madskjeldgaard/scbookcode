package de.sciss.swingosc.test;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 *	@author		Hanns Holger Rutz
 *	@version	0.10, 24-Jan-08
 */
public class TreeView
extends JScrollPane // JTree
{
	private final List				updateParents = new ArrayList();
	private final DefaultTreeModel	model;
	private final JTree				tree;
	
	public TreeView()
	{
		super();
		model	= new DefaultTreeModel( new DefaultMutableTreeNode() );
		tree	= new JTree( model );
		tree.setCellRenderer( new CellRenderer() );
		tree.setRootVisible( false );
		this.setViewportView( tree );
	}
	
	public void beginDataUpdate()
	{
		updateParents.clear();
		updateParents.add( new DefaultMutableTreeNode() );
	}
	
	// triplets: (i) indent, (i) flags, (s) userObject
	// where flags: 0x01	allowsChildren
	//				0x02	enabled
	public void addData( Object[] update )
	{
		final int				numNodes = update.length / 3;
		int						indent, flags;
		boolean					allowsChildren, enabled;
		Object					userObject;
		MutableTreeNode			tn;
		DefaultMutableTreeNode	parent;
		for( int i = 0, j = 0; i < numNodes; i++ ) {
			indent			= ((Number) update[ j++ ]).intValue();
			flags			= ((Number) update[ j++ ]).intValue();
			allowsChildren	= (flags & 0x01) != 0;
			enabled			= (flags & 0x02) != 0;
			userObject		= update[ j++ ];
			tn				= new Node( userObject, allowsChildren, enabled );
			parent			= (DefaultMutableTreeNode) updateParents.get( indent );
			parent.add( tn );
			if( allowsChildren ) {
				while( (indent + 1) >= updateParents.size() ) {
					updateParents.add( null );
				}
				updateParents.set( indent + 1, tn );
			}
		}
	}

	public void endDataUpdate()
	{
		model.setRoot( (TreeNode) updateParents.get( 0 ));
//		setListData( data );
	}
	
	private static class Node
	extends DefaultMutableTreeNode
	{
		protected final boolean enabled;
		
		protected Node( Object userObject, boolean allowsChildren, boolean enabled )
		{
			super( userObject, allowsChildren );
			this.enabled	= enabled;
		}
	}

	protected static class CellRenderer
	extends DefaultTreeCellRenderer
	{
		public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
			
	        super.getTreeCellRendererComponent(
	                                           tree, value, sel,
	                                           expanded, leaf, row,
	                                           hasFocus);
//	        System.out.println( "for row '" + row + "' bounds are " + this.getBounds() );
	        if( value instanceof Node ) {
	        	final Node n = (Node) value;
//	        	this.setEnabled( n.enabled );
	        	this.setForeground(  n.enabled ? Color.black : Color.red );
	        }
	        return this;
		}
	}
}
