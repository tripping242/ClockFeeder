package com.codingblocks.clock.core.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.codingblocks.clock.core.local.data.FeedToClockItem

@Dao
interface FeedToClockItemDao {
    @Query("SELECT * FROM feedTheClock ORDER BY orderIndex ASC")
    fun getAll(): List<FeedToClockItem>


    @Query("SELECT * FROM feedTheClock WHERE unit = :unit")
    fun getByUnit(unit: String): FeedToClockItem?

    @Insert
    fun insert(item: FeedToClockItem)

    @Delete
    fun delete(item: FeedToClockItem)

    @Update
    fun update(item: FeedToClockItem)

    @Query("UPDATE feedTheClock SET orderIndex = :newOrderIndex WHERE unit = :unit")
    fun updateOrderIndexForUnit(unit: String, newOrderIndex: Int)

    @Query("UPDATE feedTheClock SET price = :newPrice WHERE unit = :unit")
    fun updatePriceForUnit(unit: String, newPrice: Double)

    @Query("DELETE FROM feedTheClock WHERE unit = :unit")
    fun deleteByUnit(unit: String)

    @Query("DELETE FROM feedTheClock WHERE deleteWhenDone = 1")
    fun deleteDoneItems()

    @Query("DELETE FROM feedTheClock")
    fun deleteAll()

    @Transaction
    fun insertAtFront(item: FeedToClockItem) {
        val allItems = getAll()
        allItems.forEach { existingItem ->
            updateOrderIndexForUnit(existingItem.unit, existingItem.orderIndex + 1)
        }

        // Insert the new item at the front (orderIndex = 0)
        val newItem = item.copy(orderIndex = 0)
        insert(newItem)
    }

    @Query("SELECT MAX(orderIndex) FROM feedTheClock")
    fun getMaxOrderIndex(): Int?

    @Transaction
    fun insertAtEnd(item: FeedToClockItem) {
        // Calculate the next orderIndex
        val maxOrderIndex = getMaxOrderIndex() ?: -1
        val newOrderIndex = maxOrderIndex + 1

        // Insert the new item with the calculated orderIndex
        val newItem = item.copy(orderIndex = newOrderIndex)
        insert(newItem)
    }
}