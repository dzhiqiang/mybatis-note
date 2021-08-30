package com.dzq;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class MybatisSessionTest {


    public static void main(String[] args) throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory =
                new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        String flowKey = sqlSession.selectOne("com.dzq.TransactionMapper.getFlowKey1", 10);
        String flowKey1 = sqlSession.selectOne("com.dzq.TransactionMapper.getFlowKey", 10);

        System.out.println(flowKey);
    }

}
