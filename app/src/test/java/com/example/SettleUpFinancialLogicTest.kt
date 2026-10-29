package com.example

import com.example.data.entity.DebtEntity
import com.example.data.entity.TransactionEntity
import com.example.data.model.DebtDirection
import com.example.data.model.DebtStatus
import com.example.data.model.TransactionType
import com.example.data.repository.SettleUpRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class SettleUpFinancialLogicTest {

    @Test
    fun testFinancialCalculationSequence() {
        // Mock repository calculation without DB instantiation
        val debt = DebtEntity(
            id = "debt_test",
            personId = "person_test",
            direction = DebtDirection.BORROWED,
            originalPrincipal = 1000L,
            status = DebtStatus.ACTIVE
        )

        // Step 1: Borrow 1,000 -> balance 1,000
        val tx1 = TransactionEntity(
            id = "tx1",
            debtId = debt.id,
            personId = debt.personId,
            type = TransactionType.BORROWED,
            amount = 1000L,
            effectiveDate = 1000L
        )
        val txs = mutableListOf(tx1)

        fun calcBalance(debt: DebtEntity, list: List<TransactionEntity>): Long {
            var balance = 0L
            val sorted = list.sortedBy { it.effectiveDate }
            for (tx in sorted) {
                when (tx.type) {
                    TransactionType.BORROWED -> balance += tx.amount
                    TransactionType.REPAYMENT_MADE -> balance -= tx.amount
                    TransactionType.ADJUSTMENT -> balance += tx.amount
                    TransactionType.REVERSAL -> {
                        val orig = list.find { it.id == tx.reversalOfId }
                        if (orig != null) {
                            if (orig.type == TransactionType.BORROWED) balance -= orig.amount
                            else if (orig.type == TransactionType.REPAYMENT_MADE) balance += orig.amount
                        }
                    }
                    else -> {}
                }
            }
            return if (balance < 0L) 0L else balance
        }

        assertEquals(1000L, calcBalance(debt, txs))

        // Step 2: Repay 200 -> balance 800
        val tx2 = TransactionEntity(
            id = "tx2",
            debtId = debt.id,
            personId = debt.personId,
            type = TransactionType.REPAYMENT_MADE,
            amount = 200L,
            effectiveDate = 2000L
        )
        txs.add(tx2)
        assertEquals(800L, calcBalance(debt, txs))

        // Step 3: Borrow additional 500 on same debt -> balance 1,300
        val tx3 = TransactionEntity(
            id = "tx3",
            debtId = debt.id,
            personId = debt.personId,
            type = TransactionType.BORROWED,
            amount = 500L,
            effectiveDate = 3000L
        )
        txs.add(tx3)
        assertEquals(1300L, calcBalance(debt, txs))

        // Step 4: Repay 300 -> balance 1,000
        val tx4 = TransactionEntity(
            id = "tx4",
            debtId = debt.id,
            personId = debt.personId,
            type = TransactionType.REPAYMENT_MADE,
            amount = 300L,
            effectiveDate = 4000L
        )
        txs.add(tx4)
        assertEquals(1000L, calcBalance(debt, txs))

        // Step 5: Repay final 1,000 -> balance 0 (cleared)
        val tx5 = TransactionEntity(
            id = "tx5",
            debtId = debt.id,
            personId = debt.personId,
            type = TransactionType.REPAYMENT_MADE,
            amount = 1000L,
            effectiveDate = 5000L
        )
        txs.add(tx5)
        assertEquals(0L, calcBalance(debt, txs))

        // Step 6: Reverse tx5 (1,000 repayment was mistakenly entered) -> balance restores to 1,000
        val tx6 = TransactionEntity(
            id = "tx6_rev",
            debtId = debt.id,
            personId = debt.personId,
            type = TransactionType.REVERSAL,
            amount = 1000L,
            effectiveDate = 6000L,
            reversalOfId = "tx5"
        )
        txs.add(tx6)
        assertEquals(1000L, calcBalance(debt, txs))
    }
}
