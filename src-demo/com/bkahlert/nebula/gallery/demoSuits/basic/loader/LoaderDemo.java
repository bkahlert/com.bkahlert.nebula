package com.bkahlert.nebula.gallery.demoSuits.basic.loader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.widgets.browser.Browser;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.MouseAdapter;
import com.bkahlert.nebula.widgets.loader.Loader;
import com.bkahlert.nebula.widgets.timeline.impl.TimePassed;

@Demo
public class LoaderDemo extends AbstractDemo {

	@Override
	public void createDemo(Composite composite) {
		composite.setBackground(ColorUtils.createRandomColor());
		composite.setLayout(GridLayoutFactory.fillDefaults().margins(10, 10)
				.equalWidth(true).spacing(10, 10).numColumns(4).create());

		Button btn1 = new Button(composite, SWT.PUSH);
		btn1.setText("Click me...");
		btn1.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(4, 2).create());
		final Loader btn1Loader = new Loader(btn1);
		btn1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btn1Loader.run(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						TimePassed timePassed = new TimePassed();
						while (timePassed.getTimePassed() < 3000) {
							log(timePassed.getTimePassed() + "ms passed");
						}
						return null;
					}
				});
			}
		});

		Browser lc2 = new Browser(composite, SWT.NONE);
		lc2.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(2, 2).create());
		try {
			lc2.open(new URI("http://color.hailpixel.com"), 60000);
		} catch (URISyntaxException e1) {
			log(e1.getMessage());
		}

		Browser lc3 = new Browser(composite, SWT.NONE);
		lc3.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(2, 4).create());
		final Loader loader3 = new Loader(lc3);
		lc3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(double x, double y, IElement element) {
				loader3.run(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						TimePassed timePassed = new TimePassed();
						while (timePassed.getTimePassed() < 3000) {
							log(timePassed.getTimePassed() + "ms passed");
						}
						return null;
					}
				});
			}
		});
		try {
			lc3.open(new URI("http://wikipedia.com"), 60000);
		} catch (URISyntaxException e1) {
			log(e1.getMessage());
		}

		Button btn4 = new Button(composite, SWT.PUSH);
		btn4.setText("Click me...");
		btn4.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(2, 1).create());
		final Loader btn4Loader = new Loader(btn4);
		btn4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btn4Loader.run(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						TimePassed timePassed = new TimePassed();
						while (timePassed.getTimePassed() < 3000) {
							log(timePassed.getTimePassed() + "ms passed");
						}
						return null;
					}
				});
			}
		});

		Button btn5 = new Button(composite, SWT.PUSH);
		btn5.setText("Click me...");
		btn5.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(1, 1).create());
		final Loader btn5Loader = new Loader(btn5);
		btn5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btn5Loader.run(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						TimePassed timePassed = new TimePassed();
						while (timePassed.getTimePassed() < 3000) {
							log(timePassed.getTimePassed() + "ms passed");
						}
						return null;
					}
				});
			}
		});

		Button btn6 = new Button(composite, SWT.PUSH);
		btn6.setText("Click me...");
		btn6.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(1, 1).create());
		final Loader btn6Loader = new Loader(btn6);
		btn6.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btn6Loader.run(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						TimePassed timePassed = new TimePassed();
						while (timePassed.getTimePassed() < 3000) {
							log(timePassed.getTimePassed() + "ms passed");
						}
						return null;
					}
				});
			}
		});

		log("");

	}

}
