package com.bkahlert.nebula.screenshots.impl.webpage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

import com.bkahlert.nebula.screenshots.webpage.IWebpage;
import com.bkahlert.nebula.screenshots.webpage.IWebpageScreenshotRenderer;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.HttpUtils;
import com.bkahlert.nebula.widgets.browser.extended.IJQueryBrowser;
import com.bkahlert.nebula.widgets.browser.extended.JQueryBrowser;

// TODO render in hidden composite
// TODO render area should not get focus so everything can be done in the background
public class WebpageScreenshotRenderer<WEBPAGE extends IWebpage> implements
		IWebpageScreenshotRenderer<WEBPAGE, IJQueryBrowser> {

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
			.getLogger(WebpageScreenshotRenderer.class);

	private static class Renderer extends Dialog {

		private JQueryBrowser browser;

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

			this.browser = new JQueryBrowser(composite,
					SWT.NONE);

			return composite;
		}

		@Override
		protected Control createButtonBar(Composite parent) {
			return parent;
		}

	}

	private final Shell parentShell;
	private final Map<Renderer, WEBPAGE> renderers;
	private final List<Renderer> renderersInUse;
	private final List<Renderer> availableRenderers;

	public WebpageScreenshotRenderer(Shell parentShell) {
		this.parentShell = parentShell;
		this.renderers = new HashMap<Renderer, WEBPAGE>();
		this.renderersInUse = new ArrayList<Renderer>();
		this.availableRenderers = new ArrayList<Renderer>();
	}

	synchronized private Renderer getRenderer(WEBPAGE request) {
		if (this.availableRenderers.size() == 0) {
			Shell shell = this.parentShell != null ? new Shell(this.parentShell)
					: new Shell(Display.getCurrent());
			Renderer renderer = new Renderer(shell);
			this.availableRenderers.add(renderer);
		}
		Renderer rendererInUse = this.availableRenderers.remove(0);
		this.renderers.put(rendererInUse, request);
		this.renderersInUse.add(rendererInUse);
		return rendererInUse;
	}

	synchronized private void releaseRenderer(Renderer renderer) {
		Assert.isLegal(renderer != null);
		this.renderers.put(renderer, null);
		this.renderersInUse.remove(renderer);
		this.availableRenderers.add(renderer);
	}

	@Override
	public Callable<IScreenshotRendererSession> render(final WEBPAGE request) {
		return new Callable<IScreenshotRendererSession>() {

			private Renderer renderer = null;

			@Override
			public IScreenshotRendererSession call() throws Exception {
				try {
					LOGGER.info("Rendering " + request.getUri().toString());

					this.verifiyResponseCode();

					this.configuredRenderer();

					WebpageScreenshotRenderer.this
							.preparedWebpageControlFinished(request,
									this.renderer.browser);

					if (!this.loadUri()) {
						throw new WebpageRendererException("Opening "
								+ request.getUri() + " timed out");
					}

					WebpageScreenshotRenderer.this.loadingWebpageFinished(
							request, this.renderer.browser);

					if (this.scroll()) {
						Thread.sleep(800);
					}

					WebpageScreenshotRenderer.this.scrollingWebpageFinished(
							request, this.renderer.browser);

					WebpageScreenshotRenderer.this.renderingFinished(request,
							this.renderer.browser);

					return new IScreenshotRendererSession() {
						@Override
						public Control display() {
							return renderer.browser;
						}

						@Override
						public void dispose() {
							WebpageScreenshotRenderer.this
									.releaseRenderer(renderer);
						}
					};
				} catch (Exception e) {
					if (this.renderer != null) {
						WebpageScreenshotRenderer.this
								.releaseRenderer(this.renderer);
					}
					throw e;
				}
			}

			private void verifiyResponseCode() throws WebpageRendererException {
				try {
					int responseCode = HttpUtils.getResponseCode(request
							.getUri());
					if (responseCode == 401) {
						return;
					}
					if (responseCode < 200 || responseCode >= 300) {
						throw new WebpageRendererException(
								"Unsupported response code " + responseCode
										+ " returned for " + request.getUri());
					}
				} catch (MalformedURLException e) {
					throw new WebpageRendererException(e);
				} catch (IOException e) {
					throw new WebpageRendererException(e);
				}
			}

			private void configuredRenderer() {
				try {
					ExecUtils.syncExec(new Runnable() {
						@Override
						public void run() {
							renderer = WebpageScreenshotRenderer.this
									.getRenderer(request);
							renderer.setBlockOnOpen(false);
							renderer.open();
							renderer.resize(request.getDimensions());
						}
					});
				} catch (Exception e) {
					LOGGER.error("Error configuring renderer", e);
				}
			}

			private boolean loadUri() throws InterruptedException,
					ExecutionException {
				return this.renderer.browser.open(request.getUri(),
						request.getTimeout()).get();
			}

			private boolean scroll() throws InterruptedException,
					ExecutionException {
				return this.renderer.browser.scrollTo(
						request.getScrollPosition()).get();
			}

		};
	}

	protected IJQueryBrowser getBrowser(WEBPAGE request) {
		for (Entry<Renderer, WEBPAGE> entry : WebpageScreenshotRenderer.this.renderers
				.entrySet()) {
			if (entry.getValue() == request) {
				return entry.getKey().browser;
			}
		}
		return null;
	}

	@Override
	public void dispose() {
		try {
			ExecUtils.syncExec(new Runnable() {
				@Override
				public void run() {
					for (Iterator<Renderer> iterator = WebpageScreenshotRenderer.this.renderers
							.keySet().iterator(); iterator.hasNext();) {
						Renderer renderer = iterator.next();
						renderer.close();
						iterator.remove();
						WebpageScreenshotRenderer.this.availableRenderers
								.remove(renderer);
						WebpageScreenshotRenderer.this.renderersInUse
								.remove(renderer);
					}
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void preparedWebpageControlFinished(WEBPAGE webpage,
			IJQueryBrowser browser) {
	}

	@Override
	public void loadingWebpageFinished(WEBPAGE webpage,
			IJQueryBrowser browser) {
	}

	@Override
	public void scrollingWebpageFinished(WEBPAGE webpage,
			IJQueryBrowser browser) {
	}

	@Override
	public void renderingFinished(WEBPAGE subject,
			IJQueryBrowser control) {
	}

}
