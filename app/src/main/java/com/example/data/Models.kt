package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "departments")
data class Department(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val code: String
) : Serializable

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val employeeId: String,
    val department: String,
    val subjectsHandled: String, // Comma-separated list of subject names
    val maxHoursPerWeek: Int,
    val preferredFreeSlots: String, // Comma-separated day-period list: "Mon-0,Wed-3"
    val preferredTeachingSlots: String, // Comma-separated day-period: "Mon-1,Tue-2"
    val availabilityConstraints: String, // Comma-separated unavailable day-periods: "Wed-4"
    val labEligibility: Boolean,
    val designation: String
) : Serializable

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val code: String,
    val department: String,
    val teacherId: Int? = null, // ID of assigned teacher
    val periodsPerWeek: Int,
    val isLab: Boolean,
    val consecutivePeriodsRequired: Int = 1,
    val preferredClassrooms: String = "" // Comma-separated classroom names
) : Serializable

@Entity(tableName = "classrooms")
data class Classroom(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val capacity: Int,
    val isLab: Boolean,
    val equipment: String = "", // Comma-separated equipment
    val campus: String = "Main"
) : Serializable

@Entity(tableName = "timetable_cells")
data class TimetableCell(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val departmentId: Int,
    val section: String, // "Section A", "Section B" etc.
    val subjectId: Int,
    val teacherId: Int,
    val classroomId: Int,
    val day: String, // "Monday", "Tuesday", etc.
    val periodIndex: Int // 0-indexed period of the day
) : Serializable

@Entity(tableName = "leave_requests")
data class LeaveRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teacherId: Int,
    val startDate: String, // YYYY-MM-DD
    val endDate: String, // YYYY-MM-DD
    val notes: String,
    val status: String = "Pending" // "Pending", "Approved", "Rejected"
) : Serializable

@Entity(tableName = "substitute_assignments")
data class SubstituteAssignment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cellId: Int, // Refers to timetable_cells id
    val substituteTeacherId: Int,
    val date: String // Specific date and time
) : Serializable

@Entity(tableName = "saved_scenarios")
data class SavedScenario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val resultText: String,
    val activeCellsCount: Int,
    val score: Int,
    val balance: Int,
    val utilization: Int,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "schedule_presets")
data class SchedulePreset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val cellsJson: String,
    val activeCellsCount: Int,
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable


