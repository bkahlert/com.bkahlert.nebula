package com.bkahlert.nebula.gallery.demoSuits.basic.loader;

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
import com.bkahlert.nebula.widgets.loader.Loader;
import com.bkahlert.nebula.widgets.loader.LoaderComposite;
import com.bkahlert.nebula.widgets.timeline.impl.TimePassed;

@Demo
public class LoaderDemo extends AbstractDemo {

	@Override
	public void createDemo(Composite composite) {
		composite.setBackground(ColorUtils.createRandomColor());
		composite.setLayout(GridLayoutFactory.fillDefaults().margins(10, 10)
				.equalWidth(true).spacing(10, 10).numColumns(4).create());

		Button button = new Button(composite, SWT.PUSH);
		button.setText("Click me...");
		button.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(4, 2).create());
		final Loader buttonLoader = new Loader(button);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				buttonLoader.run(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						System.err.println(Thread.currentThread());
						TimePassed timePassed = new TimePassed();
						while (timePassed.getTimePassed() < 3000) {
							System.err.println(timePassed.getTimePassed()
									+ "ms passed");
							log(timePassed.getTimePassed() + "ms passed");
						}
						return null;
					}
				});
			}
		});

		LoaderComposite lc2 = new LoaderComposite(composite);
		lc2.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(2, 2).create());

		LoaderComposite lc3 = new LoaderComposite(composite);
		lc3.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(2, 4).create());

		LoaderComposite lc4 = new LoaderComposite(composite);
		lc4.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(1, 2).create());

		LoaderComposite lc5 = new LoaderComposite(composite);
		lc5.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(1, 1).create());

		LoaderComposite lc6 = new LoaderComposite(composite);
		lc6.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(1, 1).create());

		log("");

	}

}
