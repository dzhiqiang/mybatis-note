package com.dzq.spring;

import com.dzq.mapper.TransactionMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MybatisSpringTest {

    public static void main(String[] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");

        TransactionMapper transactionMapper = context.getBean(TransactionMapper.class);

        String flowKey = transactionMapper.getFlowKey(10);

        System.out.println(flowKey);

    }

}
