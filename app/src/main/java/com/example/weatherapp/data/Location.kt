package com.example.weatherapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_table")
data class Location (
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "city") val city: String?
)