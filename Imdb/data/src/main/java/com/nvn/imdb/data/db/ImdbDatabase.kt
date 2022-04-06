package com.nvn.imdb.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nvn.imdb.data.db.dao.MovieDao
import com.nvn.imdb.data.db.table.Movie

@Database(
    exportSchema = false,
    version = 1, entities = [Movie::class]
)
abstract class ImdbDatabase : RoomDatabase() {
    abstract val sellerDao: MovieDao
}