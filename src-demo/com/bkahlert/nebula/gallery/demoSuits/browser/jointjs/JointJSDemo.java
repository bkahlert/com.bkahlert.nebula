package com.bkahlert.nebula.gallery.demoSuits.browser.jointjs;

import java.util.concurrent.Callable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.IFocusListener;
import com.bkahlert.nebula.widgets.jointjs.JointJS;
import com.bkahlert.nebula.widgets.jointjs.JointJS.IJointJSListener;

@Demo
public class JointJSDemo extends AbstractDemo {

	private JointJS jointjs;

	private String json = null;

	@Override
	public void createControls(Composite composite) {
		Button loadButton = new Button(composite, SWT.PUSH);
		loadButton.setText("load");
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("loading");
						try {
							JointJSDemo.this.jointjs
									.load(JointJSDemo.this.json);
						} catch (Exception e) {
							log(e.toString());
						}
						log("loaded");
					}
				}).start();
			}
		});

		Button saveButton = new Button(composite, SWT.PUSH);
		saveButton.setText("save");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("loading");
						try {
							JointJSDemo.this.json = JointJSDemo.this.jointjs
									.save().get();
						} catch (Exception e) {
							log(e.toString());
						}
						log("loaded");
					}
				}).start();
			}
		});
	}

	@Override
	public void createDemo(Composite parent) {
		this.jointjs = new JointJS(parent, SWT.BORDER);
		this.jointjs.addAnkerListener(new IAnkerListener() {
			@Override
			public void ankerHovered(IAnker anker, boolean entered) {
				log("hovered " + (entered ? "over" : "out") + " " + anker);
			}

			@Override
			public void ankerClicked(IAnker anker) {
				log("clicked on " + anker);
			}
		});
		this.jointjs.addFocusListener(new IFocusListener() {
			@Override
			public void focusLost(IElement element) {
				log("focus lost " + element);
			}

			@Override
			public void focusGained(IElement element) {
				log("focus gained " + element);
			}
		});
		this.jointjs.addJointJSListener(new IJointJSListener() {
			@Override
			public void loaded(String json) {
				log("loaded  " + json);
			}

			@Override
			public void save(String json) {
				log("save " + json);
			}

			@Override
			public void linkTitleChanged(String id, String title) {
				log("link title changed: " + id + " = " + title);
			}
		});

		ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				String id1 = JointJSDemo.this.jointjs.createNode("sua://test3",
						"Hello Java", "bla<b> b</b>la", new Point(150, 300),
						new Point(200, 100)).get();

				String id2 = JointJSDemo.this.jointjs.createNode("sua://test4",
						"Hello Java", "bla bla", new Point(50, 30),
						new Point(120, 80)).get();

				String id3 = JointJSDemo.this.jointjs
						.createLink(null, id1, id2).get();

				JointJSDemo.this.jointjs.setLinkTitle(id3, "dssdööl sdldslkö ")
						.get();

				JointJSDemo.this.jointjs.setColor("sua://test3", new RGB(255,
						0, 0));
				JointJSDemo.this.jointjs.setBackgroundColor("sua://test3",
						new RGB(255, 0, 255));
				JointJSDemo.this.jointjs.setBorderColor("sua://test3", new RGB(
						255, 128, 0));

				return null;
			}
		});

	}
}
