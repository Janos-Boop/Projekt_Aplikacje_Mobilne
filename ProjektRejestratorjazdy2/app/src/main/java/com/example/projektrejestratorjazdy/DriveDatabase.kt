package com.example.projektrejestratorjazdy

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Encja Sesji (Podsumowanie)
@Entity(tableName = "drive_sessions")
data class DriveSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val maxSpeed: Double,
    val maxGForce: Float,
    val distance: Double,
    val note: String = ""
)

// 2. NOWA ENCJA: Punkt pomiarowy (szczegóły zapisu co np. sekundę)
@Entity(
    tableName = "drive_points",
    foreignKeys = [ForeignKey(
        entity = DriveSession::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE // Jak usuniesz sesję, usuną się też punkty
    )]
)
data class DrivePoint(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int, // Klucz obcy
    val timestamp: Long,
    val speed: Double,
    val gForce: Float
)

// 3. DAO
@Dao
interface DriveDao {
    // Operacje na sesjach
    @Query("SELECT * FROM drive_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<DriveSession>>

    @Insert
    suspend fun insertSession(session: DriveSession): Long // Zwraca ID nowej sesji

    @Delete
    suspend fun deleteSession(session: DriveSession)

    // Operacje na punktach
    @Insert
    suspend fun insertPoints(points: List<DrivePoint>)

    @Query("SELECT * FROM drive_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getPointsForSession(sessionId: Int): List<DrivePoint>
}

// 4. Baza Danych (Wersja 3)
@Database(entities = [DriveSession::class, DrivePoint::class], version = 3)
abstract class DriveDatabase : RoomDatabase() {
    abstract fun driveDao(): DriveDao

    companion object {
        @Volatile
        private var Instance: DriveDatabase? = null

        fun getDatabase(context: Context): DriveDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, DriveDatabase::class.java, "drive_db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}