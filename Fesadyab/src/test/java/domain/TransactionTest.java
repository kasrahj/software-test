package domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionTest {
    private Transaction transaction;
    private TransactionEngine transactionEngine;


    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transactionEngine = new TransactionEngine();
    }

    @AfterEach
    void tearDown() {
        transaction = null;
        transactionEngine = null;
    }

    @Test
    void EqualsShouldReturnTrueWhenTransactionIsEqual()
    {
        assertTrue(transaction.equals(transaction));
        Transaction transaction1 = new Transaction();
        transaction.setTransactionId(1);
        transaction1.setTransactionId(1);
        assertTrue(transaction.equals(transaction1));
    }

    @Test
    void EqualsShouldReturnFalseWhenTransactionIsNotEqual()
    {
        Transaction transaction1 = new Transaction();
        transaction.setTransactionId(1);
        transaction1.setTransactionId(2);
        assertFalse(transaction.equals(transaction1));
    }
    @Test
    void EqualsShouldReturnFalseWhenObjectIsNotInstanceOfTransactionClass()
    {
        assertFalse(transaction.equals(transactionEngine));
    }

    @Test
    void EqualsShouldReturnFalseWhenObjectIsNull()
    {
        assertFalse(transaction.equals(null));
    }

    @Test
    void GetterAndSetterShouldReturnCorrectValue()
    {
        transaction.setTransactionId(1);
        transaction.setAccountId(1);
        transaction.setAmount(10);
        transaction.setDebit(true);
        assertTrue(transaction.getTransactionId() == 1);
        assertTrue(transaction.getAccountId() == 1);
        assertTrue(transaction.getAmount() == 10);
        assertTrue(transaction.isDebit());
    }

}