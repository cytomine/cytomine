package be.cytomine.service.command;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import be.cytomine.domain.command.Transaction;

@RequiredArgsConstructor
@Service
@Transactional
public class TransactionService {

    private final EntityManager entityManager;

    public Transaction start() {
        synchronized (this.getClass()) {
            //A transaction is a simple domain with a id (= transaction id)
            Transaction transaction = new Transaction();
            entityManager.persist(transaction);
            return transaction;
        }
    }
}
