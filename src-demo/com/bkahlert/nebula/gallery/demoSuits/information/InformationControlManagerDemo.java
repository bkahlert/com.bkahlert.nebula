package com.bkahlert.nebula.gallery.demoSuits.information;

import java.util.Date;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

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
import com.bkahlert.nebula.information.InformationControlManagerUtils;
import com.bkahlert.nebula.widgets.image.Image;
import com.bkahlert.nebula.widgets.image.Image.FILL_MODE;

@Demo(title = "Press F3 to replace the contents of an information control")
public class InformationControlManagerDemo extends AbstractDemo {

	public static void replaceInformationControlContent() {
		log("Replacing...");
		InformationControlManagerUtils
				.getCurrentManager(String.class)
				.setInformation(
						"New information...\nTimestamp: "
								+ new Date().getTime()
								+ "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nEND");
	}

	private Listener listener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			if (event.keyCode == SWT.F3) {
				replaceInformationControlContent();
			}
		}
	};

	@Override
	public void createDemo(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginWidth = fillLayout.marginHeight = fillLayout.spacing = 10;
		composite.setLayout(fillLayout);

		SimpleRoundedComposite leftComposite = new SimpleRoundedComposite(
				composite, SWT.BORDER);
		leftComposite.setText("Hover over me!");
		leftComposite.setBackground(ColorUtils.createRandomColor());

		SimpleRoundedComposite rightComposite = new SimpleRoundedComposite(
				composite, SWT.BORDER);
		rightComposite.setText("Hover over me!");
		rightComposite.setBackground(ColorUtils.createRandomColor());

		InformationControlManager<Composite, String> leftManager = new InformationControlManager<Composite, String>(
				String.class, this.creator, this.provider);
		leftManager.install(leftComposite);

		InformationControlManager<Composite, String> rightManager = new InformationControlManager<Composite, String>(
				String.class, this.creator, this.provider);
		rightManager.install(rightComposite);

		Display.getCurrent().addFilter(SWT.KeyDown, this.listener);
	}

	@Override
	public void dispose() {
		Display.getCurrent().removeFilter(SWT.KeyDown, this.listener);
	};

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
								private Image image;
								private Label label;

								@Override
								public Composite build(Composite parent) {
									parent.setLayout(GridLayoutFactory
											.fillDefaults().create());
									this.image = new Image(parent, SWT.NONE,
											FILL_MODE.INNER_FILL);
									this.image
											.load("http://www.fantom-xp.com/wallpapers/23/Butterfly_abstract_wallpaper.jpg",
													null);
									this.image.setLayoutData(GridDataFactory
											.fillDefaults().grab(true, true)
											.minSize(300, 300).create());
									this.image.setBackground(Display
											.getCurrent().getSystemColor(
													SWT.COLOR_INFO_BACKGROUND));
									this.image.limitToOriginalSize();
									this.label = new Label(parent, SWT.BORDER);
									this.label.setLayoutData(GridDataFactory
											.fillDefaults().grab(true, true)
											.hint(400, 500).minSize(100, 280)
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
										Action showCurrentManagerAction = new Action() {
											@Override
											public String getText() {
												return "Curr. Mngr";
											};

											@Override
											public void run() {
												log("Current manager: "
														+ InformationControlManagerUtils
																.getCurrentManager());
											};
										};
										Action showAgainAction = new Action() {
											@Override
											public String getText() {
												return "Show Again";
											};

											@Override
											public void run() {
												replaceInformationControlContent();
											};
										};
										toolBarManager
												.add(showCurrentManagerAction);
										toolBarManager.add(showAgainAction);
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
			InformationControlManagerDemo.log("registered");
		}

		@Override
		public void unregister(Composite subject) {
			InformationControlManagerDemo.log("unregistered");
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

}
