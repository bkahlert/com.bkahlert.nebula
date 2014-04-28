package com.bkahlert.nebula.gallery.demoSuits.browser.jointjs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.IFocusListener;
import com.bkahlert.nebula.widgets.jointjs.JointJS;
import com.bkahlert.nebula.widgets.jointjs.JointJS.IJointJSListener;

@Demo
public class JointJSDemo extends AbstractDemo {

	private JointJS browser;

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
							JointJSDemo.this.browser
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
							JointJSDemo.this.json = JointJSDemo.this.browser
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
		this.browser = new JointJS(parent, SWT.BORDER);
		this.browser.addAnkerListener(new IAnkerListener() {
			@Override
			public void ankerHovered(IAnker anker, boolean entered) {
				log("hovered " + (entered ? "over" : "out") + " " + anker);
			}

			@Override
			public void ankerClicked(IAnker anker) {
				log("clicked on " + anker);
			}
		});
		this.browser.addFocusListener(new IFocusListener() {
			@Override
			public void focusLost(IElement element) {
				log("focus lost " + element);
			}

			@Override
			public void focusGained(IElement element) {
				log("focus gained " + element);
			}
		});
		this.browser.addJointJSListener(new IJointJSListener() {
			@Override
			public void loaded(String json) {
				log("loaded  " + json);
			}

			@Override
			public void save(String json) {
				log("save " + json);
			}
		});
	}
}
