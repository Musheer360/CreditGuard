package com.creditguard.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.creditguard.data.model.Transaction

// NOTE: build.gradle.kts needs: ksp { arg("room.schemaLocation", "$projectDir/schemas") }

@Database(entities = [Transaction::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions RENAME TO transactions_old")
                db.execSQL("""
                    CREATE TABLE transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amount INTEGER NOT NULL,
                        merchant TEXT NOT NULL,
                        cardLast4 TEXT NOT NULL,
                        bank TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        isPaid INTEGER NOT NULL,
                        rawSms TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX index_transactions_isPaid ON transactions(isPaid)")
                db.execSQL("CREATE INDEX index_transactions_timestamp ON transactions(timestamp)")
                db.execSQL("""
                    INSERT INTO transactions (id, amount, merchant, cardLast4, bank, timestamp, isPaid, rawSms)
                    SELECT id, CAST(ROUND(amount * 100) AS INTEGER), merchant, cardLast4, bank, timestamp, isPaid, rawSms
                    FROM transactions_old
                """.trimIndent())
                db.execSQL("DROP TABLE transactions_old")
            }
        }

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "creditguard.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
