package com.bkahlert.nebula.information;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.IMenuService;

import com.bkahlert.nebula.SourceProvider;

/**
 * This is a typed version of the {@link IInformationControlExtension2}.<br>
 * Instead of having to override
 * {@link IInformationControlExtension2#setInput(Object)} you have to override
 * {@link #load(Object)} that returns a boolean whether to show information or
 * not.
 * <p>
 * <strong>Make sure to override
 * {@link #getInformationPresenterControlCreator()} if you want to allow the
 * user to hover the popup and get an enhanced version.</strong>
 * 
 * @author bkahlert
 * 
 * @param <INFORMATION>
 */
public abstract class InformationControl<INFORMATION> extends
		AbstractInformationControl implements IInformationControlExtension2 {

	private static final Logger LOGGER = Logger
			.getLogger(InformationControl.class);

	@SuppressWarnings("unchecked")
	protected static <INFORMATION> List<IInformationControlExtender<INFORMATION>> getExtenders() {
		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor("com.bkahlert.nebula.information");
		final List<IInformationControlExtender<INFORMATION>> extenders = new ArrayList<IInformationControlExtender<INFORMATION>>();
		for (IConfigurationElement configElement : config) {
			try {
				Object o = configElement.createExecutableExtension("class");
				if (o instanceof IInformationControlExtender) {
					try {
						extenders
								.add((IInformationControlExtender<INFORMATION>) o);
					} catch (ClassCastException ex) {
						LOGGER.error(
								IInformationControlExtender.class
										.getSimpleName()
										+ " could not be cast.", ex);
					}
				}
			} catch (CoreException e1) {
				LOGGER.error("Error retrieving a currently registered "
						+ InformationControl.class.getSimpleName(), e1);
				return null;
			}
		}
		return extenders;
	}

	private List<IInformationControlExtender<INFORMATION>> extenders = null;
	private boolean hasContents = false;
	private Composite parent = null;

	protected InformationControl(Shell parentShell, String statusFieldText,
			Object noCreate) {
		super(parentShell, statusFieldText);
	}

	protected InformationControl(Shell parentShell,
			ToolBarManager toolBarManager, Object noCreate) {
		super(parentShell, toolBarManager);
		toolBarManager.add(new GroupMarker(
				IWorkbenchActionConstants.MB_ADDITIONS));
		this.addMenuServiceContributions(toolBarManager);
	}

	public InformationControl(Shell parentShell, String statusFieldText) {
		this(parentShell, statusFieldText, null);
		this.create();
	}

	/**
	 * Constructs a new {@link InformationControl} using the specified
	 * {@link ToolBarManager}.
	 * <p>
	 * You can make contributions to your toolBarManager using the
	 * <code>plugin.xml-menuContributions</code> with location set to
	 * <code>toolbar:com.bkahlert.nebula.information</code>.
	 * <p>
	 * An {@link IWorkbenchActionConstants#MB_ADDITIONS} is automatically added
	 * to the end of the {@link ToolBarManager}.
	 * 
	 * @param parentShell
	 * @param toolBarManager
	 */
	public InformationControl(Shell parentShell, ToolBarManager toolBarManager) {
		this(parentShell, toolBarManager, null);
		this.create();
	}

	protected void addMenuServiceContributions(ToolBarManager toolBarManager) {
		IMenuService menuService = (IMenuService) PlatformUI.getWorkbench()
				.getService(IMenuService.class);
		menuService.populateContributionManager(toolBarManager,
				"toolbar:com.bkahlert.nebula.information");
	}

	@Override
	protected final void createContent(Composite parent) {
		this.parent = parent;
		this.extenders = InformationControl.<INFORMATION> getExtenders();
		this.create(parent);
		for (IInformationControlExtender<INFORMATION> extender : this.extenders) {
			extender.extend(this, parent);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void setInput(Object input) {
		try {
			INFORMATION information = (INFORMATION) input;
			SourceProvider.controlChanged(this);
			SourceProvider.inputChanged(information);
			this.hasContents = this.load(information);
			this.parent.layout();
			for (IInformationControlExtender<INFORMATION> extender : this.extenders) {
				extender.extend(information);
			}
		} catch (ClassCastException e) {
			this.hasContents = false;
		}
	}

	@Override
	public void setVisible(boolean visible) {
		if (!visible) {
			SourceProvider.controlChanged(null);
			SourceProvider.inputChanged(null);
		}
		super.setVisible(visible);
	}

	public abstract void create(Composite parent);

	public abstract boolean load(INFORMATION input);

	/**
	 * This implementation delegates this method's return value computation to
	 * {@link #load(Object)}.
	 */
	@Override
	public final boolean hasContents() {
		return this.hasContents;
	}

	@Override
	public Point computeSizeHint() {
		// currently ignores size constraints
		return this.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		// return super.computeSizeHint();
	}

	@Override
	public InformationControlCreator<INFORMATION> getInformationPresenterControlCreator() {
		return null;
	}

	public void layout() {
		Shell shell = this.getShell();
		Rectangle oldBounds = shell.getBounds();
		shell.layout();
		shell.pack();
		Point newSize = shell.getSize();
		System.err.println(newSize);
		Rectangle newBounds = new Rectangle(oldBounds.x, oldBounds.y
				+ oldBounds.height - newSize.y, newSize.x, newSize.y);
		int fixedX = newBounds.x;
		int fixedY = newBounds.y;
		Geometry.moveInside(newBounds, Display.getCurrent().getBounds());
		if (newBounds.x < fixedX) {
			newBounds.width -= fixedX - newBounds.x;
		}
		if (newBounds.y != fixedY) {
			newBounds.height -= fixedY - newBounds.y;
		}
		newBounds.x = fixedX;
		newBounds.y = fixedY;
		shell.setBounds(newBounds);
		// TODO subject area des closers aktualisieren
	}

}