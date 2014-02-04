package com.bkahlert.nebula.gallery.demoSuits.information;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.about.AboutAction;

import com.bkahlert.devel.nebula.colors.ColorUtils;
import com.bkahlert.devel.nebula.widgets.SimpleRoundedComposite;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.information.EnhanceableInformationControl;
import com.bkahlert.nebula.information.EnhanceableInformationControl.Delegate;
import com.bkahlert.nebula.information.EnhanceableInformationControl.DelegateFactory;
import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.information.InformationControl;
import com.bkahlert.nebula.information.InformationControlCreator;
import com.bkahlert.nebula.information.InformationControlManager;

@SuppressWarnings("restriction")
@Demo
public class InformationControlDemo extends AbstractDemo {

	@Override
	public void createDemo(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.swtDefaults().create());

		SimpleRoundedComposite area = new SimpleRoundedComposite(composite,
				SWT.BORDER);
		area.setText("Hover over me!");
		area.setBackground(ColorUtils.createRandomColor());
		area.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.create());

		InformationControlCreator<String> creator = new InformationControlCreator<String>() {
			@Override
			protected InformationControl<String> doCreateInformationControl(
					Shell parent) {
				return new EnhanceableInformationControl<String, Delegate<String>>(
						String.class, parent,
						new DelegateFactory<Delegate<String>>() {
							@Override
							public Delegate<String> create() {
								return new Delegate<String>() {
									private Label label;

									@Override
									public Composite build(Composite parent) {
										parent.setLayout(GridLayoutFactory
												.fillDefaults().create());
										this.label = new Label(parent,
												SWT.BORDER);
										this.label
												.setLayoutData(GridDataFactory
														.fillDefaults()
														.grab(true, false)
														.create());
										return parent;
									}

									@Override
									public boolean load(String input,
											ToolBarManager toolBarManager) {
										if (input == null) {
											return false;
										}

										if (toolBarManager != null) {
											toolBarManager
													.add(new AboutAction(
															PlatformUI
																	.getWorkbench()
																	.getActiveWorkbenchWindow()));
										}
										this.label.setText(input.toString());
										return true;
									}
								};
							}
						});
			}
		};

		ISubjectInformationProvider<Composite, String> provider = new ISubjectInformationProvider<Composite, String>() {
			@Override
			public void register(Composite subject) {
				InformationControlDemo.log("registered");
			}

			@Override
			public void unregister(Composite subject) {
				InformationControlDemo.log("unregistered");
			}

			@Override
			public String getInformation() {
				Point pos = Display.getCurrent().getCursorLocation();
				return new String("Cursor position:<br>x: " + pos.x + "<br>y: "
						+ pos.y);
			}

			@Override
			public Point getHoverArea() {
				return new Point(10, 10);
			}
		};

		InformationControlManager<Composite, String> manager = new InformationControlManager<Composite, String>(
				String.class, creator, provider);
		manager.install(area);
	}

}
