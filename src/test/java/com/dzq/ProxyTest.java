package com.dzq;

import com.dzq.proxy.*;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyTest {
    @Test
    public void test_01() {
        Print print = new PrintImpl();
        Print printProxy = (Print) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{Print.class}, new InvocationHandlerImpl(print));
        printProxy.print();
    }

    @Test
    public void test_02() {
        Wash wash = new WashImpl();
        Wash proxy = (Wash) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{Wash.class}, new InvocationHandlerImpl(wash));
        proxy.wash();
    }

    @Test
    public void test_03() {
        Wash wash = new WashImpl();
        Wash proxy = (Wash) PaperProxyFactory.createProxy(wash);
        proxy.wash();
    }

}
