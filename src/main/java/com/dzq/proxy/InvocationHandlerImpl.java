package com.dzq.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class InvocationHandlerImpl implements InvocationHandler {
    private Object object;
    public InvocationHandlerImpl(Object object) {
        this.object = object;
    }
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("去拿纸...");
        Class<?>[] classes = proxy.getClass().getInterfaces();
        for (int i = 0; i < classes.length; i++) {
            System.out.println(classes[i].getName());
        }
        System.out.println(method.getName());
        return method.invoke(object, args);
    }
}
