package com.svrheine.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries WHERE quarter = :quarter AND year = :year ORDER BY date DESC")
    fun getEntriesByQuarter(quarter: Int, year: Int): Flow<List<Entry>>

    @Query("SELECT * FROM entries")
    suspend fun getAllEntries(): List<Entry>

    @Insert
    suspend fun insertEntry(entry: Entry)

    @Update
    suspend fun updateEntry(entry: Entry)

    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteEntry(id: Int)

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getEntryById(id: Int): Entry?

    @Query("SELECT COUNT(*) FROM entries WHERE date = :date AND kilometers > 0 AND id != :excludeId")
    suspend fun countEntriesWithKmOnDate(date: Long, excludeId: Int): Int
}
