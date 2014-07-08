package com.bkahlert.nebula.gallery.demoExplorer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DemoSuite {
    Class<? extends AbstractDemo>[] value();
}
