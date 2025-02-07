package de.gebit.componentinstrumentation;

import net.bytebuddy.asm.Advice;
import java.lang.reflect.Method;

public class SpyAdviceFx {
    @Advice.OnMethodEnter
    public static void enter(
            @Advice.AllArguments Object[] allArguments
    ) {
        try {
            String operatorQualifiedName = "de.gebit.componentinstrumentation.ControlAgent";
            Class<?> methodListener = ClassLoader.getSystemClassLoader().loadClass(operatorQualifiedName);
            Method onEnter = methodListener.getDeclaredMethod("onFxComponentAdded", Object.class);
            onEnter.setAccessible(true);
            onEnter.invoke(null, allArguments[0]);
        } catch (Exception e) {
            System.out.println("Exception FX" + e.getMessage());
        }
    }
}