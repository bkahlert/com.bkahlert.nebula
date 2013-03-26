package com.bkahlert.nebula.gallery.demoSuits.instruction;

import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoExplorer.DemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.gallery.demoSuits.instruction.explanation.ExplanationDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.instruction.explanatory.ExplanatoryDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.instruction.note.NoteCompositeDemoSuite;


@DemoSuite({ ExplanationDemoSuite.class, NoteCompositeDemoSuite.class,
	ExplanatoryDemoSuite.class })
@Demo
public class InstructionDemoSuite extends AbstractDemo {
    @Override
    public void createDemo(Composite parent) {

    }
}
