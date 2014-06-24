package com.bkahlert.nebula.gallery.demoSuits;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoExplorer.DemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.basic.BasicDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.basic.loader.LoaderDemo;
import com.bkahlert.nebula.gallery.demoSuits.browser.BrowserDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.chat.ChatDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.decoration.DecorationDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.dialog.DialogDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.information.InformationDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.instruction.InstructionDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.sound.SoundDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.utils.UtilsDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.viewer.ViewerDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.wizard.WizardDemoSuite;

@DemoSuite({ DecorationDemoSuite.class, BasicDemoSuite.class,
		InstructionDemoSuite.class, ChatDemoSuite.class, ViewerDemoSuite.class,
		WizardDemoSuite.class, BrowserDemoSuite.class, DialogDemoSuite.class,
		InformationDemoSuite.class, SoundDemoSuite.class, UtilsDemoSuite.class })
@Demo
public class MainDemo extends LoaderDemo {

}
