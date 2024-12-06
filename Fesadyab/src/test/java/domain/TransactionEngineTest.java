package domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class TransactionEngineTest {
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
    private static TransactionEngine CreateTransactionEngine(ArrayList<Transaction> transactionHistory)
    {
        TransactionEngine transactionEngine = new TransactionEngine();
        transactionEngine.transactionHistory = transactionHistory;
        return transactionEngine;
    }

    private static Transaction CreateTransactions(int transactionId, int accountId , int amount, boolean isDebit)
    {
        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setTransactionId(transactionId);
        transaction.setDebit(isDebit);
        return transaction;
    }

    static Object[][] transactionAverageAmountParameters() {
        ArrayList<Transaction> transactionHistory = new ArrayList<>(List.of(
                CreateTransactions(1,1, 10, true),
                CreateTransactions(2,2, 26, true),
                CreateTransactions(3,1, 30, true)
        ));

        TransactionEngine transactionEngine = CreateTransactionEngine(transactionHistory);

        return new Object[][] {
                { transactionEngine, 1, 20 },
                { transactionEngine, 2, 26 },
                { transactionEngine, 3, 0 }
        };
    }
    @ParameterizedTest
    @MethodSource("transactionAverageAmountParameters")
    void shouldReturnCorrectAverageTransactionAmountForAccount(TransactionEngine transactionEngine,
                                                               int accountId, int expectedAverageTransactionAmount) {
        assertEquals(expectedAverageTransactionAmount, transactionEngine.getAverageTransactionAmountByAccount(accountId));
    }

    static Object[][] transactionPatternAboveThresholdParameters() {
        ArrayList<Transaction> transactionHistory = new ArrayList<>(List.of(
                CreateTransactions(1,1, 5, true),
                CreateTransactions(2,2, 13, true),
                CreateTransactions(3,3, 19, true),
                CreateTransactions(4,4,33, true)
        ));

        TransactionEngine transactionEngine = CreateTransactionEngine(transactionHistory);
        TransactionEngine emptyTransactionEngine = CreateTransactionEngine(new ArrayList<>());


        return new Object[][] {
                { emptyTransactionEngine , 6, 0 },
                { transactionEngine, 50, 0 },
                { transactionEngine, 13, 14 }, //changed for mutation coverage (threshold = amount)
                { transactionEngine, 10, 0 },
        };
    }
    @ParameterizedTest
    @MethodSource("transactionPatternAboveThresholdParameters")
    void shouldReturnCorrectTransactionPatternAboveThreshold(
            TransactionEngine transactionEngine, int threshold, int expectedTransactionPatternAboveThreshold) {

        assertEquals(expectedTransactionPatternAboveThreshold,
                transactionEngine.getTransactionPatternAboveThreshold(threshold));
    }

    static Object[][] transactionFraudScoreParameters() {
        ArrayList<Transaction> transactionHistory = new ArrayList<>(List.of(
                CreateTransactions(1,1, 10, true),
                CreateTransactions(2,1, 5, true),
                CreateTransactions(3,1, 15, true),
                CreateTransactions(4,2, 20, true)
        ));

        TransactionEngine transactionEngine = CreateTransactionEngine(transactionHistory);

        return new Object[][] {
                { transactionEngine, CreateTransactions(4,1, 12, false), 0 },
                { transactionEngine, CreateTransactions(4,1, 8, true), 0 },
                { transactionEngine, CreateTransactions(4,1, 30, true), 10 },
                { transactionEngine, CreateTransactions(4,1, 20, true), 0 } //changed for mutation coverage (amount = 2 * averageAmount)
        };
    }
    @ParameterizedTest
    @MethodSource("transactionFraudScoreParameters")
    void shouldReturnCorrectFraudNumber(TransactionEngine transactionEngine, Transaction transaction,
                                                     int expectedFraud) {
        assertEquals(expectedFraud, transactionEngine.detectFraudulentTransaction(transaction));
    }

    static Object[][] addTransactionParameters() {
        ArrayList<Transaction> transactionHistory = new ArrayList<>(List.of(
                CreateTransactions(1, 1, 10, true),
                CreateTransactions(2, 1, 5, true),
                CreateTransactions(3, 1, 12, true)
        ));

        TransactionEngine transactionEngine = CreateTransactionEngine(transactionHistory);

        return new Object[][] {
                { transactionEngine, CreateTransactions(1,1,10,true), 0 },
                { transactionEngine, CreateTransactions(4,1, 22, true),
                        transactionEngine.detectFraudulentTransaction(CreateTransactions(4,1, 22, true)) },
                { transactionEngine, CreateTransactions(5,1, 8, true),
                        transactionEngine.getTransactionPatternAboveThreshold(1000) }
        };
    }
    @ParameterizedTest
    @MethodSource("addTransactionParameters")
    void shouldAddTransactionCorrectly(TransactionEngine transactionEngine,
                                                              Transaction transaction, int expectedFraudScore) {
        assertEquals(expectedFraudScore, transactionEngine.addTransactionAndDetectFraud(transaction));
    }
}
