package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "notification_records")
data class NotificationRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val packageName: String,
    val appName: String,
    val senderName: String,
    val messageContent: String,
    val timestamp: Long = System.currentTimeMillis(),
    val canQuickReply: Boolean = false,
    val isGrouped: Boolean = false
)

@Entity(tableName = "app_filter_rules")
data class AppFilterRuleEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val categoryLabel: String,
    val isAllowed: Boolean = true,
    val showMessagePreview: Boolean = true,
    val allowQuickReply: Boolean = true,
    val hideSpecificKeywords: String = ""
)

@Entity(tableName = "island_presets")
data class IslandPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val compactWidthDp: Int,
    val compactHeightDp: Int,
    val expandedWidthDp: Int,
    val expandedHeightDp: Int,
    val offsetXDp: Int,
    val offsetYDp: Int,
    val cornerRadiusDp: Int,
    val animationStyleName: String,
    val animationDurationMs: Int,
    val autoHideSeconds: Int,
    val opacityPercent: Int,
    val isBuiltIn: Boolean = false
)

@Dao
interface LeovanDao {
    // Riwayat Notifikasi
    @Query("SELECT * FROM notification_records ORDER BY timestamp DESC LIMIT 100")
    fun observeNotificationRecords(): Flow<List<NotificationRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationRecord(record: NotificationRecordEntity)

    @Query("DELETE FROM notification_records WHERE id = :id")
    suspend fun deleteNotificationById(id: Long)

    @Query("DELETE FROM notification_records")
    suspend fun clearAllNotificationRecords()

    // Filter Aplikasi & Pengaturan Per Aplikasi
    @Query("SELECT * FROM app_filter_rules ORDER BY appName ASC")
    fun observeAppFilterRules(): Flow<List<AppFilterRuleEntity>>

    @Query("SELECT * FROM app_filter_rules WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppFilterRule(packageName: String): AppFilterRuleEntity?

    @Query("SELECT COUNT(*) FROM app_filter_rules")
    suspend fun getAppFilterCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppFilterRules(rules: List<AppFilterRuleEntity>)

    @Update
    suspend fun updateAppFilterRule(rule: AppFilterRuleEntity)

    // Preset Tampilan Island
    @Query("SELECT * FROM island_presets ORDER BY isBuiltIn DESC, id ASC")
    fun observePresets(): Flow<List<IslandPresetEntity>>

    @Query("SELECT COUNT(*) FROM island_presets")
    suspend fun getPresetCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: IslandPresetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresets(presets: List<IslandPresetEntity>)

    @Query("DELETE FROM island_presets WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteCustomPreset(id: Int)
}

@Database(
    entities = [
        NotificationRecordEntity::class,
        AppFilterRuleEntity::class,
        IslandPresetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LeovanDatabase : RoomDatabase() {
    abstract fun leovanDao(): LeovanDao

    companion object {
        @Volatile
        private var INSTANCE: LeovanDatabase? = null

        fun getInstance(context: Context): LeovanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LeovanDatabase::class.java,
                    "leovanisland_local.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                CoroutineScope(Dispatchers.IO).launch {
                    seedInitialDataIfNeeded(instance.leovanDao())
                }
                instance
            }
        }

        suspend fun seedInitialDataIfNeeded(dao: LeovanDao) {
            if (dao.getAppFilterCount() == 0) {
                val defaultApps = listOf(
                    AppFilterRuleEntity(
                        packageName = "com.whatsapp",
                        appName = "WhatsApp",
                        categoryLabel = "Pesan Instan",
                        isAllowed = true,
                        showMessagePreview = true,
                        allowQuickReply = true
                    ),
                    AppFilterRuleEntity(
                        packageName = "org.telegram.messenger",
                        appName = "Telegram",
                        categoryLabel = "Pesan Instan",
                        isAllowed = true,
                        showMessagePreview = true,
                        allowQuickReply = true
                    ),
                    AppFilterRuleEntity(
                        packageName = "com.instagram.android",
                        appName = "Instagram",
                        categoryLabel = "Media Sosial",
                        isAllowed = true,
                        showMessagePreview = true,
                        allowQuickReply = true
                    ),
                    AppFilterRuleEntity(
                        packageName = "com.google.android.gm",
                        appName = "Gmail",
                        categoryLabel = "Email & Kerja",
                        isAllowed = true,
                        showMessagePreview = true,
                        allowQuickReply = false
                    ),
                    AppFilterRuleEntity(
                        packageName = "com.spotify.music",
                        appName = "Spotify",
                        categoryLabel = "Musik & Audio",
                        isAllowed = true,
                        showMessagePreview = true,
                        allowQuickReply = false
                    ),
                    AppFilterRuleEntity(
                        packageName = "com.google.android.apps.youtube.music",
                        appName = "YouTube Music",
                        categoryLabel = "Musik & Audio",
                        isAllowed = true,
                        showMessagePreview = true,
                        allowQuickReply = false
                    ),
                    AppFilterRuleEntity(
                        packageName = "com.google.android.apps.messaging",
                        appName = "Pesan Android",
                        categoryLabel = "SMS & RCS",
                        isAllowed = true,
                        showMessagePreview = true,
                        allowQuickReply = true
                    ),
                    AppFilterRuleEntity(
                        packageName = "com.google.android.dialer",
                        appName = "Telepon Sistem",
                        categoryLabel = "Panggilan",
                        isAllowed = true,
                        showMessagePreview = true,
                        allowQuickReply = false
                    )
                )
                dao.upsertAppFilterRules(defaultApps)
            }

            if (dao.getPresetCount() == 0) {
                val builtInPresets = listOf(
                    IslandPresetEntity(
                        name = "Standar iPhone 16 Pro",
                        description = "Proporsi kapsul klasik dengan animasi pegas responsif",
                        compactWidthDp = 128,
                        compactHeightDp = 36,
                        expandedWidthDp = 352,
                        expandedHeightDp = 178,
                        offsetXDp = 0,
                        offsetYDp = 6,
                        cornerRadiusDp = 28,
                        animationStyleName = AnimationStyle.SPRING.name,
                        animationDurationMs = 340,
                        autoHideSeconds = 6,
                        opacityPercent = 100,
                        isBuiltIn = true
                    ),
                    IslandPresetEntity(
                        name = "Kapsul Punch-Hole Mikro",
                        description = "Ukuran ekstra ringkas untuk layar punch-hole kecil",
                        compactWidthDp = 108,
                        compactHeightDp = 32,
                        expandedWidthDp = 336,
                        expandedHeightDp = 166,
                        offsetXDp = 0,
                        offsetYDp = 4,
                        cornerRadiusDp = 24,
                        animationStyleName = AnimationStyle.MORPH.name,
                        animationDurationMs = 280,
                        autoHideSeconds = 5,
                        opacityPercent = 100,
                        isBuiltIn = true
                    ),
                    IslandPresetEntity(
                        name = "Sinematik Lebar",
                        description = "Ruang informasi lebih lega untuk musik dan pesan panjang",
                        compactWidthDp = 152,
                        compactHeightDp = 40,
                        expandedWidthDp = 368,
                        expandedHeightDp = 192,
                        offsetXDp = 0,
                        offsetYDp = 8,
                        cornerRadiusDp = 30,
                        animationStyleName = AnimationStyle.SPRING.name,
                        animationDurationMs = 380,
                        autoHideSeconds = 8,
                        opacityPercent = 100,
                        isBuiltIn = true
                    ),
                    IslandPresetEntity(
                        name = "Hemat Daya Minimalis",
                        description = "Transisi pudar cepat untuk performa ringan di semua LCD/OLED",
                        compactWidthDp = 120,
                        compactHeightDp = 34,
                        expandedWidthDp = 340,
                        expandedHeightDp = 168,
                        offsetXDp = 0,
                        offsetYDp = 6,
                        cornerRadiusDp = 26,
                        animationStyleName = AnimationStyle.FADE.name,
                        animationDurationMs = 220,
                        autoHideSeconds = 4,
                        opacityPercent = 96,
                        isBuiltIn = true
                    )
                )
                dao.insertPresets(builtInPresets)

                // Tambahkan contoh riwayat notifikasi awal agar pengguna dapat langsung melihat fitur bekerja
                dao.insertNotificationRecord(
                    NotificationRecordEntity(
                        packageName = "com.whatsapp",
                        appName = "WhatsApp",
                        senderName = "Raka Pratama",
                        messageContent = "Halo! Jangan lupa rapat desain jam 19.30 malam ini ya.",
                        timestamp = System.currentTimeMillis() - 180_000L,
                        canQuickReply = true,
                        isGrouped = false
                    )
                )
                dao.insertNotificationRecord(
                    NotificationRecordEntity(
                        packageName = "org.telegram.messenger",
                        appName = "Telegram",
                        senderName = "Grup Developer Kotlin ID",
                        messageContent = "Rilis pembaruan modul UI Dynamic Island berjalan sangat mulus.",
                        timestamp = System.currentTimeMillis() - 600_000L,
                        canQuickReply = true,
                        isGrouped = true
                    )
                )
            }
        }
    }
}
