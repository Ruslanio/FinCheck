package com.rumir.database.dao

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.rumir.database.AppDatabase
import com.rumir.database.entity.TransactionEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TransactionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TransactionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.transactionDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // region upsertAll

    @Test
    fun upsertAll_insertedTransactionIsRetrievable() = runTest {
        val entity = makeEntity(id = "t1", userId = "u1")
        dao.upsertAll(listOf(entity))
        assertEquals(1, dao.countByUserId("u1"))
    }

    @Test
    fun upsertAll_sameIdTwiceDoesNotCreateDuplicate() = runTest {
        val entity = makeEntity(id = "t1", userId = "u1")
        dao.upsertAll(listOf(entity))
        dao.upsertAll(listOf(entity))
        assertEquals(1, dao.countByUserId("u1"))
    }

    @Test
    fun upsertAll_sameIdWithDifferentAmountUpdatesRow() = runTest {
        dao.upsertAll(listOf(makeEntity(id = "t1", userId = "u1", amount = 10.0)))
        dao.upsertAll(listOf(makeEntity(id = "t1", userId = "u1", amount = 99.0)))

        assertEquals(1, dao.countByUserId("u1"))

        val result = dao.getTransactions("u1").load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false),
        ) as PagingSource.LoadResult.Page
        assertEquals(99.0, result.data.first().amount, 0.001)
    }

    // endregion

    // region clearAll

    @Test
    fun clearAll_deletesAllRowsForUser() = runTest {
        dao.upsertAll(listOf(makeEntity(id = "t1", userId = "u1"), makeEntity(id = "t2", userId = "u1")))
        dao.clearAll("u1")
        assertEquals(0, dao.countByUserId("u1"))
    }

    @Test
    fun clearAll_doesNotDeleteRowsForDifferentUser() = runTest {
        dao.upsertAll(listOf(makeEntity(id = "t1", userId = "u1"), makeEntity(id = "t2", userId = "u2")))
        dao.clearAll("u1")
        assertEquals(1, dao.countByUserId("u2"))
    }

    // endregion

    // region getLatestTimestamp

    @Test
    fun getLatestTimestamp_returnsNullWhenTableIsEmpty() = runTest {
        assertNull(dao.getLatestTimestamp("u1"))
    }

    @Test
    fun getLatestTimestamp_returnsMaxOccurredAt() = runTest {
        dao.upsertAll(
            listOf(
                makeEntity(id = "t1", userId = "u1", occurredAt = 1000L),
                makeEntity(id = "t2", userId = "u1", occurredAt = 3000L),
                makeEntity(id = "t3", userId = "u1", occurredAt = 2000L),
            ),
        )
        assertEquals(3000L, dao.getLatestTimestamp("u1"))
    }

    // endregion

    // region getTransactions ordering

    @Test
    fun getTransactions_returnsRowsOrderedByOccurredAtDesc() = runTest {
        val older = makeEntity(id = "t1", userId = "u1", occurredAt = 1000L)
        val newer = makeEntity(id = "t2", userId = "u1", occurredAt = 2000L)
        dao.upsertAll(listOf(older, newer))

        val result = dao.getTransactions("u1").load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false),
        ) as PagingSource.LoadResult.Page

        assertEquals(listOf(newer, older), result.data)
    }

    // endregion

    private fun makeEntity(
        id: String = "test-id",
        userId: String = "u1",
        amount: Double = 100.0,
        category: String = "food",
        description: String? = null,
        idempotencyKey: String? = null,
        occurredAt: Long = 1_000_000L,
        createdAt: Long = System.currentTimeMillis(),
    ) = TransactionEntity(
        id = id,
        userId = userId,
        amount = amount,
        category = category,
        description = description,
        idempotencyKey = idempotencyKey,
        occurredAt = occurredAt,
        createdAt = createdAt,
    )
}
