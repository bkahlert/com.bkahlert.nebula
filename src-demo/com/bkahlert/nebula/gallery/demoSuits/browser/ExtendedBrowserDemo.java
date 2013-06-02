package com.bkahlert.nebula.gallery.demoSuits.browser;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.extended.JQueryEnabledBrowserComposite;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class ExtendedBrowserDemo extends AbstractDemo {

	private JQueryEnabledBrowserComposite jQueryBrowserComposite;
	private Integer x = 50;
	private Integer y = 100;

	@Override
	public void createControls(Composite composite) {
		Button scrollTo = new Button(composite, SWT.PUSH);
		scrollTo.setText("scrollTo");
		scrollTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ExecutorUtil.nonUIAsyncExec(new Runnable() {
					@Override
					public void run() {
						log("scrolling to " + ExtendedBrowserDemo.this.x + ", "
								+ ExtendedBrowserDemo.this.y);
						try {
							if (ExtendedBrowserDemo.this.jQueryBrowserComposite
									.scrollTo(ExtendedBrowserDemo.this.x,
											ExtendedBrowserDemo.this.y).get()) {
								log("Scrolled");
							} else {
								log("Already at desired position");
							}
							log("scrolled to " + ExtendedBrowserDemo.this.x
									+ ", " + ExtendedBrowserDemo.this.y);
						} catch (Exception e) {
							log(e.getMessage());
						}
					}
				});
			}
		});

		Text xText = new Text(composite, SWT.BORDER);
		xText.setText(this.x + "");
		xText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				ExtendedBrowserDemo.this.x = Integer.valueOf(((Text) e
						.getSource()).getText());
			}
		});

		Text yText = new Text(composite, SWT.BORDER);
		yText.setText(this.y + "");
		yText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				ExtendedBrowserDemo.this.y = Integer.valueOf(((Text) e
						.getSource()).getText());
			}
		});
	}

	@Override
	public void createDemo(Composite parent) {
		this.jQueryBrowserComposite = new JQueryEnabledBrowserComposite(parent,
				SWT.BORDER);
		try {
			this.jQueryBrowserComposite.open(new URI(
					"http://agilemanifesto.org"), 5000);
			ExtendedBrowserDemo.this.jQueryBrowserComposite.scrollTo(50, 50);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		log(this.jQueryBrowserComposite.getBrowser().getUrl());
	}
}
