package com.bkahlert.nebula.gallery.demoExplorer;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

import com.bkahlert.nebula.gallery.ImageManager;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.gallery.util.deprecated.TreeElement;

/**
 * Wrapper for {@link AbstractDemo}s for use with {@link Viewer Viewers}
 * 
 * @author bkahlert
 */
public class DemoElement extends TreeElement {
	protected Class<? extends AbstractDemo> demo;

	public DemoElement(Class<? extends AbstractDemo> demo) {
		this.demo = demo;
	}

	@Override
	public StyledString getStyledText() {
		StyledString styledString = new StyledString();

		String name = this.demo.getSimpleName().replaceAll("(Demo|DemoSuite)$",
				"");
		String spacedName = name.replaceAll("([a-z])([A-Z])", "$1 $2");
		styledString.append(spacedName);

		return styledString;
	}

	@Override
	public Image getImage() {
		if (this.demo.getAnnotation(DemoSuite.class) != null) {
			return ImageManager.DEMO_SUITE;
		}
		if (this.demo.getAnnotation(Demo.class) != null) {
			return ImageManager.DEMO;
		}
		return null;
	}

	public Class<? extends AbstractDemo> getDemo() {
		return this.demo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.demo == null) ? 0 : this.demo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		DemoElement other = (DemoElement) obj;
		if (this.demo == null) {
			if (other.demo != null) {
				return false;
			}
		} else if (!this.demo.equals(other.demo)) {
			return false;
		}
		return true;
	}

}
