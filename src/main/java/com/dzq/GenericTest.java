package com.dzq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericTest {

    private GenericInterface genericInterface;

    public GenericTest(GenericInterface genericInterface) {
        this.genericInterface = genericInterface;
    }

    public static void main(String[] args) {

        GenericTest test = new GenericTest(new GenericInterfaceImpl());
        System.out.println(test.getValueGeneric());
    }

    public <T> List<T> getValueGeneric() {
        return genericInterface.getValue();
    }





}
interface GenericInterface{
    public <T> List<T> getValue();
}
class GenericInterfaceImpl implements GenericInterface {

    public List<Object> getValue() {
        List<Object> object = new ArrayList<Object>();
        object.add("123");
        object.add("456");
        return object;
    }
}