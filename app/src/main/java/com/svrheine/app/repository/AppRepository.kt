package com.svrheine.app.repository

import com.svrheine.app.data.Entry
import com.svrheine.app.data.EntryDao
import com.svrheine.app.data.UserDao
import com.svrheine.app.data.UserProfile
import kotlinx.coroutines.flow.Flow

class AppRepository(private val userDao: UserDao, private val entryDao: EntryDao) {
    val userProfile: Flow<UserProfile?> = userDao.getUserProfile()

    suspend fun insertProfile(profile: UserProfile) {
        userDao.insertProfile(profile)
    }

    fun getEntriesByQuarter(quarter: Int, year: Int): Flow<List<Entry>> {
        return entryDao.getEntriesByQuarter(quarter, year)
    }

    suspend fun getAllEntries(): List<Entry> {
        return entryDao.getAllEntries()
    }

    suspend fun insertEntry(entry: Entry) {
        entryDao.insertEntry(entry)
    }

    suspend fun updateEntry(entry: Entry) {
        entryDao.updateEntry(entry)
    }

    suspend fun deleteEntry(id: Int) {
        entryDao.deleteEntry(id)
    }

    suspend fun getEntryById(id: Int): Entry? {
        return entryDao.getEntryById(id)
    }

    suspend fun hasKmEntryOnDate(date: Long, excludeId: Int = -1): Boolean {
        return entryDao.countEntriesWithKmOnDate(date, excludeId) > 0
    }
}
