package com.bkahlert.nebula.information;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;

/**
 * This is a variant of the {@link InformationControl} you want to use if your
 * enhancedCreator {@link InformationControl} equals the normal version.<br>
 * In this case you don't have to override
 * {@link InformationControl#getInformationPresenterControlCreator()}.
 * 
 * @author bkahlert
 * 
 * @param <INFORMATION>
 */
public class EnhanceableInformationControl<INFORMATION, DELEGATE extends EnhanceableInformationControl.Delegate<INFORMATION>>
		extends InformationControl<INFORMATION> {

	/**
	 * Instances of this class create {@link Delegate}.<br>
	 * {@link EnhanceableInformationControl} needs two delegates: one for the
	 * normal and another one for the enhancedCreator version.
	 * 
	 * @author bkahlert
	 * 
	 * @param <DELEGATE>
	 * @param <INFORMATION>
	 */
	public static interface DelegateFactory<DELEGATE> {
		public DELEGATE create();
	}

	/**
	 * Instances of this class are responsible to create the contents of a
	 * {@link InformationControl}.
	 * 
	 * @author bkahlert
	 * 
	 * @param <INFORMATION>
	 */
	public static interface Delegate<INFORMATION> {
		/**
		 * Is called when a new {@link InformationControl} is being constructed.
		 * 
		 * @param parent
		 * @return the {@link Composite} that may be extended
		 */
		public Composite build(Composite parent);

		/**
		 * Is called when a constructed {@link InformationControl} needs to be
		 * filled with information.
		 * 
		 * @param information
		 *            to be used to load
		 * @param toolBarManager
		 *            is null if the standard version should be loaded; is a
		 *            proper {@link ToolBarManager} if the enhancedCreator
		 *            version should be loaded.
		 * @return
		 */
		public boolean load(INFORMATION information,
				ToolBarManager toolBarManager);
	}

	private Class<INFORMATION> informationClass;
	private Shell parentShell;
	private DELEGATE delegate;
	private DELEGATE enhancedDelegate;

	private InformationControlCreator<INFORMATION> enhancedCreator = null;

	private EnhanceableInformationControl(Class<INFORMATION> informationClass,
			Shell parentShell, ToolBarManager toolBarManager,
			DELEGATE enhancedDelegate) {
		super(informationClass, parentShell, toolBarManager, null);
		this.informationClass = informationClass;
		this.parentShell = parentShell;
		this.delegate = enhancedDelegate;
		this.create();
	}

	/**
	 * Constructs a new {@link EnhanceableInformationControl} that show a
	 * {@link ToolBar} as soon as it becomes enhancedCreator.
	 * 
	 * @param informationClass
	 * @param parentShell
	 * @param delegateFactory
	 */
	public EnhanceableInformationControl(Class<INFORMATION> informationClass,
			Shell parentShell, DelegateFactory<DELEGATE> delegateFactory) {
		super(informationClass, parentShell, "Press 'F2' for focus", null);
		this.informationClass = informationClass;
		this.parentShell = parentShell;
		this.delegate = delegateFactory.create();
		this.enhancedDelegate = delegateFactory.create();
		this.create();
	}

	@Override
	public final Composite create(Composite parent) {
		return this.delegate.build(parent);
	}

	@Override
	public boolean load(INFORMATION input) {
		ToolBarManager toolBarManager = this.getToolBarManager();
		if (toolBarManager != null) {
			toolBarManager.removeAll();
			this.addMenuServiceContributions(toolBarManager);
		}
		boolean load = this.delegate.load(input, toolBarManager);
		if (load && toolBarManager != null) {
			// the toolbar was already create in the creation step
			// reflect contributions
			toolBarManager.update(true);
		}
		return load;
	}

	@Override
	public InformationControlCreator<INFORMATION> getInformationPresenterControlCreator() {
		if (this.enhancedDelegate == null) {
			return null;
		} else {
			if (this.enhancedCreator == null) {
				this.enhancedCreator = new InformationControlCreator<INFORMATION>() {
					@Override
					protected InformationControl<INFORMATION> doCreateInformationControl(
							Shell parent) {
						return new EnhanceableInformationControl<INFORMATION, DELEGATE>(
								EnhanceableInformationControl.this.informationClass,
								EnhanceableInformationControl.this.parentShell,
								new ToolBarManager(),
								EnhanceableInformationControl.this.enhancedDelegate);
					}
				};
			}
			return this.enhancedCreator;
		}
	}

	@Override
	public void dispose() {
		if (this.getToolBarManager() != null) {
			this.getToolBarManager().dispose();
		}
		super.dispose();
	}

	@SuppressWarnings("unchecked")
	public List<DELEGATE> getDelegates() {
		if (this.enhancedDelegate != null) {
			return Arrays.asList(this.delegate, this.enhancedDelegate);
		} else {
			return Arrays.asList(this.delegate);
		}
	}

	/**
	 * Returns true if this {@link EnhanceableInformationControl} instance is
	 * the enhanced version.
	 * 
	 * @return
	 */
	public boolean isEnhanced() {
		return this.enhancedDelegate == null;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " ("
				+ (this.isEnhanced() ? "enhanced" : "standard") + ")";
	}
}