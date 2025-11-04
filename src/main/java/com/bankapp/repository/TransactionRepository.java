package com.bankapp.repository;

import com.bankapp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderAccountOrReceiverAccountOrderByTimestampDesc(String sender, String receiver);

    @Query("select t from Transaction t where (t.senderAccount = :acc or t.receiverAccount = :acc) and t.timestamp between :from and :to order by t.timestamp asc")
    List<Transaction> findForAccountBetween(@Param("acc") String account,
                                           @Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);
}
