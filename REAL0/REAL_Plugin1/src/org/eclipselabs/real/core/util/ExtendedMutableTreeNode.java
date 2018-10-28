package org.eclipselabs.real.core.util;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

public class ExtendedMutableTreeNode extends DefaultMutableTreeNode {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ExtendedMutableTreeNode() {
    }

    public ExtendedMutableTreeNode(Object userObject) {
        super(userObject);
    }

    public ExtendedMutableTreeNode(Object userObject, List<ExtendedMutableTreeNode> addChildren) {
        super(userObject);
        if (addChildren != null) {
            addChildren.stream().forEach(ch -> this.add(ch));
        }
    }

    public void setNewChildren(List<ExtendedMutableTreeNode> newChildren) {
        if (newChildren != null) {
            newChildren.stream().forEach(ch -> this.add(ch));
        }
    }


}
