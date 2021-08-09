package com.dzq.proxy;

import java.lang.reflect.Proxy;

public class PaperProxyFactory {
    public static Object createProxy(Object instance) {
        return Proxy.newProxyInstance(instance.getClass().getClassLoader(),
                instance.getClass().getInterfaces(), new InvocationHandlerImpl(instance));
    }
}
