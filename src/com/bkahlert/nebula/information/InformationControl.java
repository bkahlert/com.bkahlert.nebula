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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.IMenuService;

import com.bkahlert.nebula.SourceProvider;
import com.bkahlert.nebula.information.extender.IInformationControlExtender;

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
 * <p>
 * Using the {@link IInformationControlExtender}s {@link InformationControl}s
 * can be extended.
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
	protected static <INFORMATION> List<IInformationControlExtender<INFORMATION>> getExtenders(
			ClassLoader classLoader, Class<INFORMATION> targetInformationClass) {
		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor("com.bkahlert.nebula.information");
		final List<IInformationControlExtender<INFORMATION>> extenders = new ArrayList<IInformationControlExtender<INFORMATION>>();
		for (IConfigurationElement configElement : config) {
			try {
				Object instance = configElement
						.createExecutableExtension("extenderClass");
				if (instance instanceof IInformationControlExtender<?>) {
					IInformationControlExtender<?> extender = (IInformationControlExtender<?>) instance;
					Class<?> informationClass = extender.getInformationClass();

					boolean sameClass = informationClass == targetInformationClass;
					if (!sameClass) {
						boolean sameClassByForeignClassLoader = false;
						if (classLoader != null) {
							try {
								sameClassByForeignClassLoader = classLoader
										.loadClass(informationClass.getName()) == targetInformationClass;
							} catch (ClassNotFoundException e) {
							}
						}
						if (!sameClassByForeignClassLoader) {
							boolean sameClassByOwnClassLoader = false;
							try {
								sameClassByOwnClassLoader = Class
										.forName(informationClass.getName()) == targetInformationClass;
							} catch (ClassNotFoundException e) {
								LOGGER.error(e);
							}
							if (!sameClassByOwnClassLoader) {
								LOGGER.info(IInformationControlExtender.class
										.getSimpleName()
										+ " for type "
										+ extender.getInformationClass()
												.getSimpleName()
										+ " is not compatible with "
										+ targetInformationClass
												.getSimpleName());
								continue;
							}
						}
					}

					try {
						extenders
								.add((IInformationControlExtender<INFORMATION>) extender);
					} catch (ClassCastException ex) {
						LOGGER.error(
								IInformationControlExtender.class
										.getSimpleName()
										+ " could not be cast.", ex);
					}
				}
			} catch (CoreException e2) {
				LOGGER.error("Error instantiating "
						+ configElement.getAttribute("extenderClass"));
			}
		}
		return extenders;
	}

	private ClassLoader extenderInformationClassLoader;
	private List<IInformationControlExtender<INFORMATION>> extenders = null;
	private Class<INFORMATION> informationClass = null;
	private boolean hasContents = false;
	private Composite parent = null;

	protected InformationControl(ClassLoader extenderInformationClassLoader,
			Class<INFORMATION> informationClass, Shell parentShell,
			String statusFieldText, Object noCreate) {
		super(parentShell, statusFieldText);
		this.extenderInformationClassLoader = extenderInformationClassLoader;
		this.informationClass = informationClass;
	}

	protected InformationControl(ClassLoader extenderInformationClassLoader,
			Class<INFORMATION> informationClass, Shell parentShell,
			ToolBarManager toolBarManager, Object noCreate) {
		super(parentShell, toolBarManager);
		this.informationClass = informationClass;
		toolBarManager.add(new GroupMarker(
				IWorkbenchActionConstants.MB_ADDITIONS));
		this.addMenuServiceContributions(toolBarManager);
	}

	public InformationControl(ClassLoader extenderInformationClassLoader,
			Class<INFORMATION> informationClass, Shell parentShell,
			String statusFieldText) {
		this(extenderInformationClassLoader, informationClass, parentShell,
				statusFieldText, null);
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
	public InformationControl(ClassLoader extenderInformationClassLoader,
			Class<INFORMATION> informationClass, Shell parentShell,
			ToolBarManager toolBarManager) {
		this(extenderInformationClassLoader, informationClass, parentShell,
				toolBarManager, null);
		this.create();
	}

	protected void addMenuServiceContributions(ToolBarManager toolBarManager) {
		IMenuService menuService = (IMenuService) PlatformUI.getWorkbench()
				.getService(IMenuService.class);
		menuService.populateContributionManager(toolBarManager,
				"toolbar:com.bkahlert.nebula.information");
	}

	@Override
	protected final void createContent(final Composite parent) {
		this.parent = parent;
		this.extenders = InformationControl.<INFORMATION> getExtenders(
				this.extenderInformationClassLoader, this.informationClass);
		Composite extensionComposite = this.create(parent);
		if (extensionComposite != null) {
			for (IInformationControlExtender<INFORMATION> extender : this.extenders) {
				extender.extend(this, extensionComposite);
			}
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
			for (IInformationControlExtender<INFORMATION> extender : this.extenders) {
				extender.extend(this, information);
			}
			this.parent.layout();
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

	@Override
	public boolean isVisible() {
		return this.getShell().isVisible();
	}

	/**
	 * Creates the popup's user content (not the technical stuff like the
	 * toolbar)
	 * 
	 * @param parent
	 * @return the part that may be extended
	 */
	public abstract Composite create(Composite parent);

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

}