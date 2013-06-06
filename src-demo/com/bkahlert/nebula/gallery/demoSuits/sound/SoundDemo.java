package com.bkahlert.nebula.gallery.demoSuits.sound;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.sound.Sound;
import com.bkahlert.nebula.sound.Sound.SoundException;

// TODO implements sound demo correctly (sync and async)
@Demo(title = "TODO")
public class SoundDemo extends AbstractDemo {

	@Override
	public void createControls(Composite composite) {
		Button sound1 = new Button(composite, SWT.PUSH);
		sound1.setText("Play Sound #1");
		sound1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Sound sound = new Sound(SoundDemo.class
						.getResourceAsStream("Startup1.wav"));
				try {
					sound.play();
				} catch (SoundException e1) {
					log(e1.getMessage());
				}
			}
		});

		Button sound2 = new Button(composite, SWT.PUSH);
		sound2.setText("Play Sound #2");
		sound2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Sound sound = new Sound(SoundDemo.class
						.getResourceAsStream("Startup2.wav"));
				try {
					sound.play();
				} catch (SoundException e1) {
					log(e1.getMessage());
				}
			}
		});
	}

}
