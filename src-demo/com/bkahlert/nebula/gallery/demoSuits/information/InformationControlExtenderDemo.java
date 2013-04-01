package com.bkahlert.nebula.gallery.demoSuits.information;

import org.eclipse.core.runtime.IProgressMonitor;
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
import com.bkahlert.nebula.information.extender.EditorInformationControlExtender;

@Demo
public class InformationControlExtenderDemo extends AbstractDemo {

	/*
	 * This class is instantiated by plugin.xml
	 */
	public static class InformationControlExtender extends
			EditorInformationControlExtender<InformationControlDemoInput> {

		public InformationControlExtender() {
			super(GridDataFactory.fillDefaults().grab(true, true)
					.minSize(400, 400));
		}

		@Override
		public String getHtml(InformationControlDemoInput objectToLoad,
				IProgressMonitor monitor) {
			log("Loading " + objectToLoad);
			return objectToLoad.toString();
		}

		@Override
		public void setHtml(InformationControlDemoInput loadedObject,
				String html, IProgressMonitor monitor) {
			log("Saving " + html);
		}

	}

	public static class InformationControlDemoInput {
		private String value;

		public InformationControlDemoInput(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return this.value;
		}
	}

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

		InformationControlCreator<InformationControlDemoInput> creator = new InformationControlCreator<InformationControlExtenderDemo.InformationControlDemoInput>() {
			@Override
			protected InformationControl<InformationControlDemoInput> doCreateInformationControl(
					Shell parent) {
				return new EnhanceableInformationControl<InformationControlDemoInput, Delegate<InformationControlDemoInput>>(
						InformationControlDemoInput.class,
						parent,
						new DelegateFactory<Delegate<InformationControlDemoInput>>() {
							@Override
							public Delegate<InformationControlDemoInput> create() {
								return new Delegate<InformationControlDemoInput>() {
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
									public boolean load(
											InformationControlDemoInput input,
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

		ISubjectInformationProvider<Composite, InformationControlDemoInput> provider = new ISubjectInformationProvider<Composite, InformationControlExtenderDemo.InformationControlDemoInput>() {
			@Override
			public void register(Composite subject) {
				InformationControlExtenderDemo.log("registered");
			}

			@Override
			public void unregister(Composite subject) {
				InformationControlExtenderDemo.log("unregistered");
			}

			@Override
			public InformationControlDemoInput getInformation() {
				Point pos = Display.getCurrent().getCursorLocation();
				return new InformationControlDemoInput(
						"Cursor position:<br>x: " + pos.x + "<br>y: " + pos.y);
			}

			@Override
			public Point getHoverArea() {
				return new Point(10, 10);
			}
		};

		InformationControlManager<Composite, InformationControlDemoInput> manager = new InformationControlManager<Composite, InformationControlExtenderDemo.InformationControlDemoInput>(
				InformationControlDemoInput.class, creator, provider);
		manager.install(area);
	}

}
