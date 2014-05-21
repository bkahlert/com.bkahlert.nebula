package com.bkahlert.nebula.viewer.jointjs;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.graphics.Point;

public interface JointJSContentProvider extends IContentProvider {

	/**
	 * Returns the id for the given <NODE>.
	 * 
	 * @param element
	 * @return
	 */
	public String getNodeId(Object node);

	/**
	 * Returns the id for the given <LINK>.
	 * 
	 * @param element
	 * @return
	 */
	public String getLinkId(Object link);

	public Object[] getNodes();

	public Point getNodePosition(Object node);

	public Object[] getPermanentLinks();

	public Object[] getLinks();

	public String getLinkSourceId(Object link);

	public String getLinkTargetId(Object link);

}
