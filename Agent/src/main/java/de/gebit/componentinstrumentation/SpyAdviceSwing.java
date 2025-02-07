package de.gebit.componentinstrumentation;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.This;

public class SpyAdviceSwing {
    @Advice.OnMethodExit
    public static void enter(@This Object self) {
        try {
            String operatorQualifiedName = "de.gebit.componentinstrumentation.ControlAgent";
            ClassLoader.getSystemClassLoader()
                    .loadClass(operatorQualifiedName)
                    .getDeclaredMethod("onSwingComponentAdded", Object.class)
                    .invoke(null, self);
        } catch (Exception e) {
            System.out.println("Exception On " + self);
        }
    }
}