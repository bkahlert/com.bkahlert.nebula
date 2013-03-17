package com.bkahlert.devel.nebula.utils.information;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/**
 * Instances of this class can return information from a registered
 * {@link Control}.
 * 
 * @author bkahlert
 * 
 * @param <CONTROL>
 * @param <INFORMATION>
 */
public interface ISubjectInformationProvider<CONTROL extends Control, INFORMATION> {
	public void register(CONTROL subject);

	public void unregister(CONTROL subject);

	public Point getInformationSize();

	public INFORMATION getInformation();
}