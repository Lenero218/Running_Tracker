package com.example.locationtracker.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.locationtracker.db.runningDatabase
import com.example.locationtracker.other.Constants.KEY_FIRST_TIME_TOGEL
import com.example.locationtracker.other.Constants.KEY_NAME
import com.example.locationtracker.other.Constants.KEY_WEIGHT
import com.example.locationtracker.other.Constants.RUNNING_DATABASE_NAME
import com.example.locationtracker.other.Constants.sharedPreferencesname
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class) // When they should be created and destroyed
object AppModule {

    @Singleton //Only single instance of the dependency will be created at a certain time
    @Provides //Determines how our objects are getting created
    fun provideRunningDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app, //Context
        runningDatabase::class.java, //database file
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(
        db : runningDatabase
    ) = db.getRunDao()

    @Singleton
    @Provides

    fun provideShardPreferences(@ApplicationContext app: Context) = app.getSharedPreferences(
        sharedPreferencesname,MODE_PRIVATE)

    @Singleton
    @Provides
    fun providesName(sharedPref: SharedPreferences) = sharedPref.getString(KEY_NAME,"")?:""

    @Singleton
    @Provides
    fun providesWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(KEY_WEIGHT,80f)

    @Singleton
    @Provides
    fun providesFirstTimeTogel(sharedPref: SharedPreferences) = sharedPref.getBoolean(
        KEY_FIRST_TIME_TOGEL,true)




}