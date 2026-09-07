package com.financetracker.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.financetracker.database.AppDatabase
import com.financetracker.database.entity.CategoryEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CategoryDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: CategoryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.categoryDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun upsertAll_insertsCategories() = runTest {
        dao.upsertAll(listOf(makeCategory("c1"), makeCategory("c2")))
        assertEquals(2, dao.count())
    }

    @Test
    fun upsertAll_updatesExistingCategory() = runTest {
        dao.upsertAll(listOf(makeCategory("c1", name = "Old Name")))
        dao.upsertAll(listOf(makeCategory("c1", name = "New Name")))
        assertEquals(1, dao.count())
        dao.getAll().test {
            val list = awaitItem()
            assertEquals("New Name", list.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAll_emitsReactiveUpdates() = runTest {
        dao.getAll().test {
            assertTrue(awaitItem().isEmpty())
            dao.upsertAll(listOf(makeCategory("c1")))
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun reconcile_upsertsPresentAndDeletesAbsent() = runTest {
        dao.upsertAll(listOf(makeCategory("c1"), makeCategory("c2"), makeCategory("c3")))

        val updated = listOf(
            makeCategory("c1", name = "Updated"),
            makeCategory("c4"),
        )
        dao.reconcile(updated)

        assertEquals(2, dao.count())
        dao.getAll().test {
            val ids = awaitItem().map { it.id }.toSet()
            assertTrue(ids.contains("c1"))
            assertTrue(ids.contains("c4"))
            assertTrue(!ids.contains("c2"))
            assertTrue(!ids.contains("c3"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun reconcile_withEmptyList_doesNotDeleteAll() = runTest {
        dao.upsertAll(listOf(makeCategory("c1"), makeCategory("c2")))
        dao.reconcile(emptyList())
        assertEquals(2, dao.count())
    }

    @Test
    fun reconcile_updatesRenamedCategory() = runTest {
        dao.upsertAll(listOf(makeCategory("c1", name = "Food")))
        dao.reconcile(listOf(makeCategory("c1", name = "Groceries")))

        dao.getAll().test {
            val category = awaitItem().first()
            assertEquals("Groceries", category.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun makeCategory(
        id: String = "cat-1",
        name: String = "Food",
        type: String = "expense",
        isFallback: Boolean = false,
    ) = CategoryEntity(id = id, name = name, type = type, isFallback = isFallback)
}
