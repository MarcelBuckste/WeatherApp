package com.example.weatherapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface LocationDao {

    @Query("SELECT * FROM location_table")
    fun getAll(): List<Location>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun  insert(location: Location)

    @Delete
    suspend fun delete(location: Location)

    @Query("DELETE FROM location_table")
    suspend fun deleteAll()

    @Query("DELETE FROM location_table WHERE city = :cityName")
    suspend fun deleteByCity(cityName: String)



}