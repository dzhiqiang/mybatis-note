package com.dzq.excption;

import java.util.List;

public class Student {
    private String name;
    // 学生有多地址
    private List<Address> addressList;
    public List<Address> getAddressList() {
        if (addressList == null || addressList.size() == 0) {
            throw new RuntimeException(name + " 没有填写家庭地址");
        }
        return addressList;
    }
    public static void main(String[] args) {
        Student student = new Student();
        List<Address> addressList = student.getAddressList();
        //TODO
    }
}
