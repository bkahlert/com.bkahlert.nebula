package com.bkahlert.nebula.screenshots.impl.webpage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.extended.JQueryEnabledBrowserComposite;
import com.bkahlert.nebula.screenshots.IScreenshotRenderer;
import com.bkahlert.nebula.screenshots.webpage.IWebpageScreenshotRequest;
import com.bkahlert.nebula.utils.HttpUtils;
import com.bkahlert.nebula.utils.ShellUtils;

public class WebpageRenderer implements
		IScreenshotRenderer<IWebpageScreenshotRequest> {

	public static class WebpageRendererException extends Exception {

		private static final long serialVersionUID = 1L;

		public WebpageRendererException() {
			super();
		}

		public WebpageRendererException(String message, Throwable cause) {
			super(message, cause);
		}

		public WebpageRendererException(String message) {
			super(message);
		}

		public WebpageRendererException(Throwable cause) {
			super(cause);
		}

	}

	private static final Logger LOGGER = Logger
			.getLogger(WebpageRenderer.class);

	private static class Renderer extends Dialog {

		private JQueryEnabledBrowserComposite browser;

		public Renderer(Shell parentShell) {
			super(parentShell);
		}

		private void resize(Point innerDimensions) {
			Rectangle trim = this.getShell().computeTrim(0, 0,
					innerDimensions.x, innerDimensions.y);
			this.getShell().setSize(trim.width, trim.height);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			composite.setLayout(new FillLayout());

			this.browser = new JQueryEnabledBrowserComposite(composite,
					SWT.NONE);

			return composite;
		}

		@Override
		protected Control createButtonBar(Composite parent) {
			return parent;
		}

	}

	private Shell parentShell;
	private List<Renderer> availableRenderers;

	public WebpageRenderer(Shell parentShell) {
		this.parentShell = parentShell;
		this.availableRenderers = new ArrayList<Renderer>();
	}

	private Renderer getRenderer() {
		if (this.availableRenderers.size() == 0) {
			Shell shell = this.parentShell != null ? new Shell(this.parentShell)
					: new Shell(Display.getCurrent());
			this.availableRenderers.add(new Renderer(shell));
		}
		return this.availableRenderers.remove(0);
	}

	private void releaseRenderer(Renderer renderer) {
		Assert.isLegal(renderer != null);
		ShellUtils.setVisible(renderer.getShell(), false);
		this.availableRenderers.add(renderer);
	}

	@Override
	public Callable<IScreenshotRendererSession> render(
			final IWebpageScreenshotRequest order) {
		return new Callable<IScreenshotRendererSession>() {

			private Renderer renderer = null;

			@Override
			public IScreenshotRendererSession call() throws Exception {
				try {
					LOGGER.info("Rendering " + order.getUri().toString());

					this.verifiyResponseCode();

					this.configuredRenderer();

					if (!this.loadUri()) {
						throw new WebpageRendererException("Opening "
								+ order.getUri() + " timeout out");
					}

					if (order.getFormFiller() != null) {
						this.fillForm();
					}

					if (this.scroll()) {
						Thread.sleep(800);
					}

					return new IScreenshotRendererSession() {
						@Override
						public void bringToFront() {
							ShellUtils.bringToFront(renderer.getShell());
						}

						@Override
						public Rectangle getBounds() {
							return ShellUtils.getInnerArea(renderer.getShell());
						}

						@Override
						public void dispose() {
							WebpageRenderer.this.releaseRenderer(renderer);
						}
					};
				} catch (Exception e) {
					if (this.renderer != null) {
						WebpageRenderer.this.releaseRenderer(this.renderer);
					}
					throw e;
				}
			}

			private void verifiyResponseCode() throws WebpageRendererException {
				try {
					int responseCode = HttpUtils
							.getResponseCode(order.getUri());
					if (responseCode < 200 || responseCode >= 300) {
						throw new WebpageRendererException(
								"Unsupported response code " + responseCode
										+ " returned for " + order.getUri());
					}
				} catch (MalformedURLException e) {
					throw new WebpageRendererException(e);
				} catch (IOException e) {
					throw new WebpageRendererException(e);
				}
			}

			private void configuredRenderer() {
				ExecutorUtil.syncExec(new Runnable() {
					@Override
					public void run() {
						renderer = WebpageRenderer.this.getRenderer();
						renderer.setBlockOnOpen(false);
						renderer.open();
						renderer.resize(order.getDimensions());
					}
				});
			}

			private boolean loadUri() throws InterruptedException,
					ExecutionException {
				return this.renderer.browser.open(order.getUri(),
						order.getTimeout()).get();
			}

			private boolean scroll() throws InterruptedException,
					ExecutionException {
				return this.renderer.browser
						.scrollTo(order.getScrollPosition()).get();
			}

			private void fillForm() {
				order.getFormFiller().fill(this.renderer.browser);
			}

		};
	}

	@Override
	public void dispose() {
		ExecutorUtil.syncExec(new Runnable() {
			@Override
			public void run() {
				for (Iterator<Renderer> iterator = WebpageRenderer.this.availableRenderers
						.iterator(); iterator.hasNext();) {
					Renderer availableRenderer = iterator.next();
					availableRenderer.close();
					iterator.remove();
				}
			}
		});
	}

}
