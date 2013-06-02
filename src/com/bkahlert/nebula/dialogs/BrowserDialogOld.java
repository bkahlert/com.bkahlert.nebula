package com.bkahlert.nebula.dialogs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.AuthenticationEvent;
import org.eclipse.swt.browser.AuthenticationListener;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class BrowserDialogOld extends Dialog {

	private static final Logger LOGGER = Logger.getLogger(Browser.class
			.getName());

	public static final int COMPLETE_CALL_DELAY = 800;
	/**
	 * jQuery.ScrollTo - Easy element scrolling using jQuery. Copyright (c)
	 * 2007-2009 Ariel Flesler - aflesler(at)gmail(dot)com |
	 * http://flesler.blogspot.com Dual licensed under MIT and GPL. Date:
	 * 5/25/2009
	 * 
	 * @author Ariel Flesler
	 * @version 1.4.2
	 * 
	 *          http://flesler.blogspot.com/2007/10/jqueryscrollto.html
	 */
	public static final String JS_SCROLLTO_FUNCTION = "javascript:;(function(d){var k=d.scrollTo=function(a,i,e){d(window).scrollTo(a,i,e)};k.defaults={axis:'xy',duration:parseFloat(d.fn.jquery)>=1.3?0:1};k.window=function(a){return d(window)._scrollable()};d.fn._scrollable=function(){return this.map(function(){var a=this,i=!a.nodeName||d.inArray(a.nodeName.toLowerCase(),['iframe','#document','html','body'])!=-1;if(!i)return a;var e=(a.contentWindow||a).document||a.ownerDocument||a;return d.browser.safari||e.compatMode=='BackCompat'?e.body:e.documentElement})};d.fn.scrollTo=function(n,j,b){if(typeof j=='object'){b=j;j=0}if(typeof b=='function')b={onAfter:b};if(n=='max')n=9e9;b=d.extend({},k.defaults,b);j=j||b.speed||b.duration;b.queue=b.queue&&b.axis.length>1;if(b.queue)j/=2;b.offset=p(b.offset);b.over=p(b.over);return this._scrollable().each(function(){var q=this,r=d(q),f=n,s,g={},u=r.is('html,body');switch(typeof f){case'number':case'string':if(/^([+-]=)?\\d+(\\.\\d+)?(px|%)?$/.test(f)){f=p(f);break}f=d(f,this);case'object':if(f.is||f.style)s=(f=d(f)).offset()}d.each(b.axis.split(''),function(a,i){var e=i=='x'?'Left':'Top',h=e.toLowerCase(),c='scroll'+e,l=q[c],m=k.max(q,i);if(s){g[c]=s[h]+(u?0:l-r.offset()[h]);if(b.margin){g[c]-=parseInt(f.css('margin'+e))||0;g[c]-=parseInt(f.css('border'+e+'Width'))||0}g[c]+=b.offset[h]||0;if(b.over[h])g[c]+=f[i=='x'?'width':'height']()*b.over[h]}else{var o=f[h];g[c]=o.slice&&o.slice(-1)=='%'?parseFloat(o)/100*m:o}if(/^\\d+$/.test(g[c]))g[c]=g[c]<=0?0:Math.min(g[c],m);if(!a&&b.queue){if(l!=g[c])t(b.onAfterFirst);delete g[c]}});t(b.onAfter);function t(a){r.animate(g,j,b.easing,a&&function(){a.call(this,n,b)})}}).end()};k.max=function(a,i){var e=i=='x'?'Width':'Height',h='scroll'+e;if(!d(a).is('html,body'))return a[h]-d(a)[e.toLowerCase()]();var c='client'+e,l=a.ownerDocument.documentElement,m=a.ownerDocument.body;return Math.max(l[h],m[h])-Math.min(l[c],m[c])};function p(a){return typeof a=='object'?a:{top:a,left:a}}})(jQuery);;";
	public static final String JS_SCROLLTO = "javascript:$(document).scrollTo({ left: %d, top: %d }, { duration: 0 });";

	private Browser browser;
	private int timeout;
	private volatile boolean completed = false;

	private String url;
	private Point windowDimensions;
	private Point scrollPosition;

	private ArrayList<FutureTask<?>> finishedTasks;

	private ProgressListener progressListener = new ProgressAdapter() {
		@Override
		public void completed(ProgressEvent event) {
			complete();
		}
	};
	private Thread timeoutThread = new Thread(new Runnable() {
		@Override
		public void run() {
			try {
				synchronized (this) {
					wait(timeout);
				}
			} catch (InterruptedException e) {
				return;
			}

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					if (!browser.isDisposed())
						browser.removeProgressListener(progressListener);
					complete();
				}
			});
		}
	});

	public BrowserDialogOld(Shell parentShell, String url, Point windowDimensions,
			Point scrollPosition, int timeout) throws BrowserException {
		super(parentShell);
		this.url = url;
		this.windowDimensions = windowDimensions;
		this.scrollPosition = scrollPosition;

		this.timeout = timeout;

		this.finishedTasks = new ArrayList<FutureTask<?>>();

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					new URL(this.url).openStream()));
            in.readLine();
            in.close();
		} catch (MalformedURLException e) {

		} catch (IOException e) {
			throw new BrowserException(e);
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e1) {
					LOGGER.log(Level.WARNING, "Could not close stream", e1);
				}
		}
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		// FIXME If we do not add a control before the browser, we get a
		// "Invalid memory access of location 0x0 rip=0x0"
		Label label = new Label(composite, SWT.BORDER);
		label.setText("Hello Browser!");

		this.browser = new Browser(composite, SWT.BORDER);
		this.browser.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				cancel();
			}
		});
		this.browser.addAuthenticationListener(new AuthenticationListener() {
			@Override
			public void authenticate(AuthenticationEvent event) {
				event.user = "###";
				event.password = "###";
			}
		});
		this.browser.addProgressListener(progressListener);

		StackLayout stackLayout = new StackLayout();
		composite.setLayout(stackLayout);
		stackLayout.topControl = this.browser;

		this.timeoutThread.start();
		this.browser.setUrl(this.url);
		return composite;
	}

	public void complete() {
		if (completed)
			return;
		completed = true;
		if (browser != null && !browser.isDisposed()) {
			if (scrollPosition != null) {
				browser.execute(JS_SCROLLTO_FUNCTION);
				String js = String.format(JS_SCROLLTO, scrollPosition.x,
						scrollPosition.y);
				browser.execute(js);
			}

			/*
			 * Scrolling needs some time. We therefore have to wait in a non-UI
			 * thread and then return.
			 */
			new Thread(new Runnable() {
				@Override
				public void run() {
					synchronized (this) {
						try {
							wait(COMPLETE_CALL_DELAY);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								for (FutureTask<?> futureTask : finishedTasks) {
									futureTask.run();
								}
							}
						});
					}
				}
			}).start();
		} else {
			cancel();
		}
	}

	protected void cancel() {
		for (FutureTask<?> futureTask : finishedTasks) {
			if (!futureTask.isCancelled())
				futureTask.cancel(true);
		}
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return parent;
	}

	protected Point getInitialSize() {
		Rectangle trim = this.getShell().computeTrim(0, 0,
				this.windowDimensions.x, this.windowDimensions.y);
		return new Point(trim.width, trim.height);
	}

	public Browser getBrowser() {
		return this.browser;
	}

	public <V extends Object> Future<V> addFinishedCallable(Callable<V> callable) {
		FutureTask<V> futureTask = new FutureTask<V>(callable);
		this.finishedTasks.add(futureTask);
		return futureTask;
	}
}
