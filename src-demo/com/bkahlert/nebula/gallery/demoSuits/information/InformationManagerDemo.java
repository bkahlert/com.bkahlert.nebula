package com.bkahlert.nebula.gallery.demoSuits.information;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.about.AboutAction;

import com.bkahlert.devel.nebula.colors.ColorUtils;
import com.bkahlert.devel.nebula.widgets.SimpleRoundedComposite;
import com.bkahlert.devel.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.devel.nebula.widgets.editor.Editor;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.information.EnhanceableInformationControl;
import com.bkahlert.nebula.information.EnhanceableInformationControl.Delegate;
import com.bkahlert.nebula.information.EnhanceableInformationControl.DelegateFactory;
import com.bkahlert.nebula.information.IInformationControlExtender;
import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.information.InformationControl;
import com.bkahlert.nebula.information.InformationControlCreator;
import com.bkahlert.nebula.information.InformationControlManager;

@Demo
public class InformationManagerDemo extends AbstractDemo {

	public static class InformationControlExtender implements
			IInformationControlExtender<InformationControlDemoInput> {

		private InformationControl<InformationControlDemoInput> informationControl = null;
		private Editor<String> editor = null;

		@Override
		public void extend(
				InformationControl<InformationControlDemoInput> informationControl,
				Composite parent) {
			this.informationControl = informationControl;
			this.editor = new Editor<String>(parent, SWT.NONE, 500,
					ToolbarSet.DEFAULT) {
				@Override
				public String getHtml(String objectToLoad,
						IProgressMonitor monitor) {
					InformationManagerDemo.this.addConsoleMessage("Loaded: "
							+ objectToLoad);
					return objectToLoad;
				}

				@Override
				public void setHtml(String loadedObject, String html,
						IProgressMonitor monitor) {
					InformationManagerDemo.this.addConsoleMessage("Saved: "
							+ html);
				}
			};
			this.editor.setLayoutData(GridDataFactory.fillDefaults()
					.grab(true, true).minSize(400, 400).create());
		}

		@Override
		public void extend(InformationControlDemoInput information) {
			this.informationControl.setSize(300, 300);
			this.informationControl.layout();
			this.editor.load(information.toString());
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
		area.setBackground(new Color(Display.getCurrent(), ColorUtils
				.getRandomRGB().toClassicRGB()));
		area.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.create());

		InformationControlCreator<InformationControlDemoInput> creator = new InformationControlCreator<InformationManagerDemo.InformationControlDemoInput>() {
			@Override
			protected InformationControl<InformationControlDemoInput> doCreateInformationControl(
					Shell parent) {
				return new EnhanceableInformationControl<InformationControlDemoInput, Delegate<InformationControlDemoInput>>(
						parent,
						new DelegateFactory<Delegate<InformationControlDemoInput>>() {
							@Override
							public Delegate<InformationControlDemoInput> create() {
								return new Delegate<InformationControlDemoInput>() {
									private Label label;

									@Override
									public void build(Composite parent) {
										parent.setLayout(GridLayoutFactory
												.fillDefaults().create());
										this.label = new Label(parent,
												SWT.BORDER);
										this.label
												.setLayoutData(GridDataFactory
														.fillDefaults()
														.grab(true, false)
														.create());
									}

									@Override
									public boolean load(
											InformationControlDemoInput input,
											ToolBarManager toolBarManager) {
										if (input == null) {
											return false;
										}

										InformationManagerDemo.this
												.addConsoleMessage(this.label
														.hashCode() + "");

										if (toolBarManager != null) {
											toolBarManager
													.add(new AboutAction(
															PlatformUI
																	.getWorkbench()
																	.getActiveWorkbenchWindow()));
										}
										InformationManagerDemo.this
												.addConsoleMessage(input
														.toString());
										this.label.setText(input.toString());
										return true;
									}
								};
							}
						});
			}
		};

		ISubjectInformationProvider<Composite, InformationControlDemoInput> provider = new ISubjectInformationProvider<Composite, InformationManagerDemo.InformationControlDemoInput>() {
			@Override
			public void register(Composite subject) {
				InformationManagerDemo.this.addConsoleMessage("registered");
			}

			@Override
			public void unregister(Composite subject) {
				InformationManagerDemo.this.addConsoleMessage("unregistered");
			}

			@Override
			public InformationControlDemoInput getInformation() {
				Point pos = Display.getCurrent().getCursorLocation();
				return new InformationControlDemoInput(pos.x + " - " + pos.y);
			}

			@Override
			public Point getHoverArea() {
				return new Point(10, 10);
			}
		};

		InformationControlManager<Composite, InformationControlDemoInput> manager = new InformationControlManager<Composite, InformationManagerDemo.InformationControlDemoInput>(
				creator, provider);
		manager.install(area);
	}

}
