package com.bkahlert.nebula.gallery.demoSuits;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.explanation.ListExplanationComposite;
import com.bkahlert.devel.nebula.widgets.explanation.ListExplanationComposite.ListExplanation;
import com.bkahlert.nebula.gallery.ImageManager;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoExplorer.DemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.basic.BasicDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.browser.BrowserDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.chat.ChatDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.decoration.DecorationDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.dialog.DialogDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.information.InformationDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.instruction.InstructionDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.project.ProjectDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.sound.SoundDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.utils.UtilsDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.wizard.WizardDemoSuite;

@DemoSuite({ DecorationDemoSuite.class, BasicDemoSuite.class,
		InstructionDemoSuite.class, ChatDemoSuite.class,
		ProjectDemoSuite.class, WizardDemoSuite.class, BrowserDemoSuite.class,
		DialogDemoSuite.class, InformationDemoSuite.class,
		SoundDemoSuite.class, UtilsDemoSuite.class })
@Demo
public class MainDemo extends AbstractDemo {

	@Override
	public void createDemo(Composite parent) {
		ListExplanationComposite listExplanationComposite = new ListExplanationComposite(
				parent, SWT.NONE);
		ListExplanation listItemExplanation = new ListExplanation(
				ImageManager.WIDGET_GALLERY_32,
				"Welcome to the Nebula Gallery. In order to work with this Plugin...",
				"... check out existing widgets in the demo explorer on the left.",
				"... implement your own widgets by implementing your own demos.",
				"... make use of the Refresh feature (F5).");
		listExplanationComposite.setExplanation(listItemExplanation);
	}
}
