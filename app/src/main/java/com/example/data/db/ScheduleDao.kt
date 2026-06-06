package com.example.data.db

import androidx.room.*
import com.example.data.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    // Departments
    @Query("SELECT * FROM departments")
    fun getAllDepartments(): Flow<List<Department>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(department: Department): Long

    @Delete
    suspend fun deleteDepartment(department: Department)

    @Query("DELETE FROM departments")
    suspend fun deleteAllDepartments()

    // Teachers
    @Query("SELECT * FROM teachers")
    fun getAllTeachers(): Flow<List<Teacher>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher): Long

    @Delete
    suspend fun deleteTeacher(teacher: Teacher)

    @Query("DELETE FROM teachers")
    suspend fun deleteAllTeachers()

    // Subjects
    @Query("SELECT * FROM subjects")
    fun getAllSubjects(): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Delete
    suspend fun deleteSubject(subject: Subject)

    @Query("DELETE FROM subjects")
    suspend fun deleteAllSubjects()

    // Classrooms
    @Query("SELECT * FROM classrooms")
    fun getAllClassrooms(): Flow<List<Classroom>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassroom(classroom: Classroom): Long

    @Delete
    suspend fun deleteClassroom(classroom: Classroom)

    @Query("DELETE FROM classrooms")
    suspend fun deleteAllClassrooms()

    // Timetable Cells (Schedules)
    @Query("SELECT * FROM timetable_cells")
    fun getAllTimetableCells(): Flow<List<TimetableCell>>

    @Query("SELECT * FROM timetable_cells")
    suspend fun getAllTimetableCellsList(): List<TimetableCell>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableCell(cell: TimetableCell): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableCells(cells: List<TimetableCell>)

    @Query("DELETE FROM timetable_cells WHERE id = :id")
    suspend fun deleteTimetableCellById(id: Int)

    @Query("DELETE FROM timetable_cells")
    suspend fun clearTimetable()

    // Leave Requests
    @Query("SELECT * FROM leave_requests")
    fun getAllLeaveRequests(): Flow<List<LeaveRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(leaveRequest: LeaveRequest): Long

    @Query("UPDATE leave_requests SET status = :status WHERE id = :id")
    suspend fun updateLeaveStatus(id: Int, status: String)

    @Delete
    suspend fun deleteLeaveRequest(leaveRequest: LeaveRequest)

    // Substitute Assignments
    @Query("SELECT * FROM substitute_assignments")
    fun getAllSubstitutes(): Flow<List<SubstituteAssignment>>

    @Query("SELECT * FROM substitute_assignments")
    suspend fun getAllSubstitutesList(): List<SubstituteAssignment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubstitute(substitute: SubstituteAssignment): Long

    @Query("DELETE FROM substitute_assignments WHERE cellId = :cellId AND date = :date")
    suspend fun removeSubstitute(cellId: Int, date: String)

    // Saved Scenarios
    @Query("SELECT * FROM saved_scenarios ORDER BY timestamp DESC")
    fun getAllSavedScenarios(): Flow<List<SavedScenario>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedScenario(scenario: SavedScenario): Long

    @Delete
    suspend fun deleteSavedScenario(scenario: SavedScenario)

    @Query("DELETE FROM saved_scenarios")
    suspend fun deleteAllSavedScenarios()
}
