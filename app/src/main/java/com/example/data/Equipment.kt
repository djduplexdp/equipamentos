package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "equipments",
    foreignKeys = [
        ForeignKey(
            entity = Warehouse::class,
            parentColumns = ["id"],
            childColumns = ["warehouseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["warehouseId"])]
)
data class Equipment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val warehouseId: Int,
    val name: String,
    val tag: String,
    val category: String,
    val status: String,
    val description: String = "",
    val dateAdded: Long = System.currentTimeMillis()
)
