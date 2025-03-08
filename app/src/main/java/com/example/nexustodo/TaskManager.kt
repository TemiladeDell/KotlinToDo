package com.example.nexustodo

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Save tasks to SharedPreferences
    fun saveTasks(tasks: List<String>) {
        val json = gson.toJson(tasks)
        sharedPreferences.edit().putString("tasks", json).apply()
    }

    // Load tasks from SharedPreferences
    fun loadTasks(): List<String> {
        val json = sharedPreferences.getString("tasks", null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
        } else {
            emptyList()
        }
    }

    // Save completed tasks to SharedPreferences
    fun saveCompletedTasks(completedTasks: List<Pair<String, Long>>) {
        val json = gson.toJson(completedTasks)
        sharedPreferences.edit().putString("completedTasks", json).apply()
    }

    // Load completed tasks from SharedPreferences
    fun loadCompletedTasks(): List<Pair<String, Long>> {
        val json = sharedPreferences.getString("completedTasks", null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<List<Pair<String, Long>>>() {}.type)
        } else {
            emptyList()
        }
    }
}