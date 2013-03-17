package com.bkahlert.devel.nebula.utils.information;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;

/**
 * This is a variant of the {@link InformationControl} you want to use if your
 * enhanced {@link InformationControl} equals the normal version.<br>
 * In this case you don't have to override
 * {@link InformationControl#getInformationPresenterControlCreator()}.
 * 
 * @author bkahlert
 * 
 * @param <INFORMATION>
 */
public class EnhanceableInformationControl<INFORMATION> extends
		InformationControl<INFORMATION> {

	public static interface Delegate<INFORMATION> {
		public void build(Composite parent);

		public boolean load(INFORMATION information);
	}

	private boolean isEnhanced;
	private Shell parentShell;
	private Delegate<INFORMATION> delegate;
	private ToolBarManager toolBarManager;

	private EnhanceableInformationControl(Shell parentShell,
			ToolBarManager toolBarManager, Delegate<INFORMATION> content,
			Object empty) {
		super(parentShell, toolBarManager, null);
		Assert.isNotNull(content);
		this.isEnhanced = true;
		this.parentShell = parentShell;
		this.delegate = content;
		this.toolBarManager = toolBarManager;
		this.create();
	}

	/**
	 * Constructs a new {@link EnhanceableInformationControl} that show a
	 * {@link ToolBar} as soon as it becomes enhanced.
	 * 
	 * @param parentShell
	 * @param toolBarManager
	 * @param delegate
	 */
	public EnhanceableInformationControl(Shell parentShell,
			ToolBarManager toolBarManager, Delegate<INFORMATION> delegate) {
		super(parentShell, "Press 'F2' for focus", null);
		Assert.isNotNull(delegate);
		this.isEnhanced = false;
		this.parentShell = parentShell;
		this.delegate = delegate;
		this.toolBarManager = toolBarManager;
		this.create();
	}

	@Override
	protected final void createContent(Composite parent) {
		this.delegate.build(parent);
	}

	@Override
	public boolean setTypedInput(INFORMATION input) {
		return this.delegate.load(input);
	}

	@Override
	public InformationControlCreator<INFORMATION> getInformationPresenterControlCreator() {
		if (this.isEnhanced) {
			return null;
		} else {
			return new InformationControlCreator<INFORMATION>() {
				@Override
				protected InformationControl<INFORMATION> doCreateInformationControl(
						Shell parent) {
					return new EnhanceableInformationControl<INFORMATION>(
							EnhanceableInformationControl.this.parentShell,
							EnhanceableInformationControl.this.toolBarManager,
							EnhanceableInformationControl.this.delegate, null);
				}
			};
		}
	}
}