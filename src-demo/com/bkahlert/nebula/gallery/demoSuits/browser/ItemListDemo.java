package com.bkahlert.nebula.gallery.demoSuits.browser;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonOption;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonSize;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonStyle;
import com.bkahlert.nebula.widgets.decoration.EmptyText;
import com.bkahlert.nebula.widgets.itemlist.ItemList;
import com.bkahlert.nebula.widgets.itemlist.ItemList.IItemListListener;

@Demo
public class ItemListDemo extends AbstractDemo {

	private ItemList itemList;
	private String alertString = "Hello World!";
	private static String timeoutString = "15000";

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
							ItemListDemo.this.itemList.run(
									"alert(\"" + ItemListDemo.this.alertString
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
									ItemListDemo.class.getSimpleName(), ".js");
							FileUtils.write(jsFile, "alert(\""
									+ ItemListDemo.this.alertString + "\");");
							ItemListDemo.this.itemList.run(jsFile);
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
				ItemListDemo.this.alertString = ((Text) e.getSource())
						.getText();
			}
		});

		Text timeout = new Text(composite, SWT.BORDER);
		timeout.setText(ItemListDemo.timeoutString);
		timeout.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				ItemListDemo.timeoutString = ((Text) e.getSource()).getText();
			}
		});

		new EmptyText(timeout, "Timeout for page load");

		Button changeBackground = new Button(composite, SWT.PUSH);
		changeBackground.setText("change background color using CSS injection");
		changeBackground.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("changing background");
						try {
							ItemListDemo.this.itemList
									.injectCss("html, body { background-color: "
											+ ColorUtils.getRandomRGB()
													.toHexString() + "; }");
						} catch (Exception e) {
							log(e.toString());
						}
						log("changed background");
					}
				}).start();
			}
		});
	}

	@Override
	public void createDemo(final Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().create());

		new Label(parent, SWT.NONE).setLayoutData(GridDataFactory
				.fillDefaults().grab(true, true).create());

		this.itemList = new ItemList(parent, SWT.BORDER);
		this.itemList.addListener(new IItemListListener() {
			@Override
			public void itemClicked(String key) {
				log("clicked " + key);
			}

			@Override
			public void itemClicked(String key, int i) {
				log("clicked " + key + " - " + i);
			}

			@Override
			public void itemHovered(String key, boolean entered) {
				log("hovered " + (entered ? "over" : "out") + " " + key);
			}

			@Override
			public void itemHovered(String key, int i, boolean entered) {
				log("hovered " + (entered ? "over" : "out") + " " + key + " - "
						+ i);
			}
		});

		this.itemList.addItem("item1", "Item #1");
		this.itemList.addItem("item2", "Item #2", ButtonOption.PRIMARY,
				ButtonSize.EXTRA_SMALL, ButtonStyle.DROPDOWN,
				Arrays.asList("Option #1", "Option #2"));
		this.itemList.addItem("item3", "Item #3", ButtonOption.PRIMARY,
				ButtonSize.LARGE, ButtonStyle.HORIZONTAL,
				Arrays.asList("Option #1", "Option #2"));

		final Future<Void> rendering = this.itemList.setSpacing(30);

		this.itemList.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false,
				false));

		new Label(parent, SWT.NONE).setLayoutData(GridDataFactory
				.fillDefaults().grab(true, true).create());

		ExecUtils.nonUIAsyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					rendering.get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ExecUtils.asyncExec(new Runnable() {
					@Override
					public void run() {
						parent.layout();
					}
				});
			}
		});
	}
}
