package com.dzq;

import com.dzq.entity.Transaction;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class MybatisMapperTest {

    public static void main(String[] args) throws IOException {

        insert();

    }

    public static void select() throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory =
                new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        TransactionMapper transactionMapper = sqlSession.getMapper(TransactionMapper.class);
        sqlSession.getMapper(TransactionMapper.class);
        String flowKey = transactionMapper.getFlowKey(10);
        System.out.println(flowKey);
    }

    public static void insert() throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory =
                new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        TransactionMapper transactionMapper = sqlSession.getMapper(TransactionMapper.class);
        Transaction transaction = new Transaction(1L, "123", "32", 1);
        int count = transactionMapper.insert(transaction);
        sqlSession.commit();
        System.out.println(count);
    }


}
