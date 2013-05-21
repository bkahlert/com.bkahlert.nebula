package com.bkahlert.nebula.gallery.demoSuits.browser;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.bkahlert.devel.nebula.widgets.browser.BrowserComposite;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class BrowserDemo extends AbstractDemo {

	private BrowserComposite browserComposite;
	private String alertString = "Hello World!";

	@Override
	public void createControls(Composite composite) {
		Button alert = new Button(composite, SWT.PUSH);
		alert.setText("alert");
		alert.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("alerting");
						try {
							BrowserDemo.this.browserComposite.run(
									"alert(\"" + BrowserDemo.this.alertString
											+ "\");").get();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						log("alerted");
					}
				}).start();
			}
		});

		Button fileAlert = new Button(composite, SWT.PUSH);
		fileAlert.setText("alert using external file");
		fileAlert.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("alerting using external file");
						try {
							File jsFile = File.createTempFile(
									BrowserDemo.class.getSimpleName(), ".js");
							FileUtils.write(jsFile, "alert(\""
									+ BrowserDemo.this.alertString + "\");");
							BrowserDemo.this.browserComposite.run(jsFile);
						} catch (Exception e) {
							log(e.toString());
						}
						log("alerted using external file");
					}
				}).start();
			}
		});

		Text text = new Text(composite, SWT.BORDER);
		text.setText(this.alertString);
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				BrowserDemo.this.alertString = ((Text) e.getSource()).getText();
			}
		});
	}

	@Override
	public void createDemo(Composite parent) {
		this.browserComposite = new BrowserComposite(parent, SWT.BORDER,
				"http://codepen.io/simurai/full/qufDa") {
		};
		log(this.browserComposite.getBrowser().getUrl());
	}
}
