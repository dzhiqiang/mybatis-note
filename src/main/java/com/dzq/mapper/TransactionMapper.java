package com.dzq.mapper;

import com.dzq.entity.Transaction;

public interface TransactionMapper {
    String getFlowKey(long id);

    int insert(Transaction transaction);

    Transaction selectById(long id);
}
