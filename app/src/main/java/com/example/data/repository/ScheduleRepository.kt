package com.example.data.repository

import com.example.data.*
import com.example.data.db.ScheduleDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ScheduleRepository(private val scheduleDao: ScheduleDao) {

    val allDepartments: Flow<List<Department>> = scheduleDao.getAllDepartments()
    val allTeachers: Flow<List<Teacher>> = scheduleDao.getAllTeachers()
    val allSubjects: Flow<List<Subject>> = scheduleDao.getAllSubjects()
    val allClassrooms: Flow<List<Classroom>> = scheduleDao.getAllClassrooms()
    val allTimetableCells: Flow<List<TimetableCell>> = scheduleDao.getAllTimetableCells()
    val allLeaveRequests: Flow<List<LeaveRequest>> = scheduleDao.getAllLeaveRequests()
    val allSubstitutes: Flow<List<SubstituteAssignment>> = scheduleDao.getAllSubstitutes()

    // Department CRUD
    suspend fun insertDepartment(department: Department) = scheduleDao.insertDepartment(department)
    suspend fun deleteDepartment(department: Department) = scheduleDao.deleteDepartment(department)
    suspend fun clearDepartments() = scheduleDao.deleteAllDepartments()

    // Teacher CRUD
    suspend fun insertTeacher(teacher: Teacher) = scheduleDao.insertTeacher(teacher)
    suspend fun deleteTeacher(teacher: Teacher) = scheduleDao.deleteTeacher(teacher)
    suspend fun clearTeachers() = scheduleDao.deleteAllTeachers()

    // Subject CRUD
    suspend fun insertSubject(subject: Subject) = scheduleDao.insertSubject(subject)
    suspend fun deleteSubject(subject: Subject) = scheduleDao.deleteSubject(subject)
    suspend fun clearSubjects() = scheduleDao.deleteAllSubjects()

    // Classroom CRUD
    suspend fun insertClassroom(classroom: Classroom) = scheduleDao.insertClassroom(classroom)
    suspend fun deleteClassroom(classroom: Classroom) = scheduleDao.deleteClassroom(classroom)
    suspend fun clearClassrooms() = scheduleDao.deleteAllClassrooms()

    // Timetable Actions
    suspend fun saveTimetable(cells: List<TimetableCell>) {
        scheduleDao.clearTimetable()
        scheduleDao.insertTimetableCells(cells)
    }
    suspend fun insertTimetableCell(cell: TimetableCell) = scheduleDao.insertTimetableCell(cell)
    suspend fun deleteTimetableCellById(id: Int) = scheduleDao.deleteTimetableCellById(id)
    suspend fun clearTimetable() = scheduleDao.clearTimetable()
    suspend fun getAllTimetableCellsList(): List<TimetableCell> = scheduleDao.getAllTimetableCellsList()

    // Leave Requests
    suspend fun insertLeaveRequest(leaveRequest: LeaveRequest) = scheduleDao.insertLeaveRequest(leaveRequest)
    suspend fun updateLeaveStatus(id: Int, status: String) = scheduleDao.updateLeaveStatus(id, status)
    suspend fun deleteLeaveRequest(leaveRequest: LeaveRequest) = scheduleDao.deleteLeaveRequest(leaveRequest)

    // Substitutes
    suspend fun insertSubstitute(substitute: SubstituteAssignment) = scheduleDao.insertSubstitute(substitute)
    suspend fun removeSubstitute(cellId: Int, date: String) = scheduleDao.removeSubstitute(cellId, date)
    suspend fun getAllSubstitutesList(): List<SubstituteAssignment> = scheduleDao.getAllSubstitutesList()

    // Saved Scenarios
    val allSavedScenarios: Flow<List<SavedScenario>> = scheduleDao.getAllSavedScenarios()
    suspend fun insertSavedScenario(scenario: SavedScenario) = scheduleDao.insertSavedScenario(scenario)
    suspend fun deleteSavedScenario(scenario: SavedScenario) = scheduleDao.deleteSavedScenario(scenario)
    suspend fun clearSavedScenarios() = scheduleDao.deleteAllSavedScenarios()

    // Seeding Demo Data
    suspend fun seedDemoData(force: Boolean = false) {
        if (force) {
            clearTimetable()
            clearSubjects()
            clearTeachers()
            clearClassrooms()
            clearDepartments()
        } else {
            // Only seed if empty
            val existingDepts = allDepartments.first()
            if (existingDepts.isNotEmpty()) return
        }

        // 1. Seed Departments
        val deptCsId = insertDepartment(Department(name = "Computer Science", code = "CS")).toInt()
        val deptItId = insertDepartment(Department(name = "Information Technology", code = "IT")).toInt()
        val deptEeId = insertDepartment(Department(name = "Electrical Engineering", code = "EE")).toInt()

        // 2. Seed Classrooms
        val room101Id = insertClassroom(Classroom(name = "Room 301", capacity = 60, isLab = false, campus = "Main Campus")).toInt()
        val room102Id = insertClassroom(Classroom(name = "Room 302", capacity = 65, isLab = false, campus = "Main Campus")).toInt()
        val room103Id = insertClassroom(Classroom(name = "Room 401", capacity = 55, isLab = false, campus = "Annex Campus")).toInt()
        val labAId = insertClassroom(Classroom(name = "CS Networks Lab", capacity = 40, isLab = true, equipment = "35 Workstations, Cisco Switch Edge Router", campus = "Main Campus")).toInt()
        val labBId = insertClassroom(Classroom(name = "EE Circuits Lab", capacity = 30, isLab = true, equipment = "20 Multimeters, Oscilloscopes", campus = "Main Campus")).toInt()

        // 3. Seed Teachers
        val tSmithId = insertTeacher(Teacher(
            name = "Dr. Robert Smith",
            employeeId = "EMP-CS-01",
            department = "Computer Science",
            subjectsHandled = "Mathematics, Statistics, Algorithms",
            maxHoursPerWeek = 18,
            preferredFreeSlots = "Wednesday-4,Friday-4", // Avoid late periods
            preferredTeachingSlots = "Monday-0,Monday-1,Tuesday-0",
            availabilityConstraints = "Wednesday-5,Friday-5", // Leave early days
            labEligibility = false,
            designation = "Senior Professor"
        )).toInt()

        val tJohnsonId = insertTeacher(Teacher(
            name = "Dr. Susan Johnson",
            employeeId = "EMP-CS-02",
            department = "Computer Science",
            subjectsHandled = "Networks, System Programming",
            maxHoursPerWeek = 16,
            preferredFreeSlots = "Monday-4",
            preferredTeachingSlots = "Wednesday-1,Wednesday-2",
            availabilityConstraints = "Monday-0", // No early classes
            labEligibility = true,
            designation = "Associate Professor"
        )).toInt()

        val tLeeId = insertTeacher(Teacher(
            name = "Prof. Ken Lee",
            employeeId = "EMP-IT-01",
            department = "Information Technology",
            subjectsHandled = "Database Systems, Web Security",
            maxHoursPerWeek = 16,
            preferredFreeSlots = "Tuesday-4",
            preferredTeachingSlots = "Thursday-0,Thursday-1",
            availabilityConstraints = "Tuesday-0",
            labEligibility = true,
            designation = "Assistant Professor"
        )).toInt()

        val tGarciaId = insertTeacher(Teacher(
            name = "Dr. Maria Garcia",
            employeeId = "EMP-EE-01",
            department = "Electrical Engineering",
            subjectsHandled = "Circuit Theory, Digital Logic",
            maxHoursPerWeek = 14,
            preferredFreeSlots = "Thursday-4",
            preferredTeachingSlots = "Monday-2,Monday-3",
            availabilityConstraints = "Friday-4",
            labEligibility = true,
            designation = "Senior Lecturer"
        )).toInt()

        // 4. Seed Subjects
        insertSubject(Subject(name = "Discrete Mathematics", code = "CS-101", department = "Computer Science", teacherId = tSmithId, periodsPerWeek = 4, isLab = false))
        insertSubject(Subject(name = "Algorithms Design", code = "CS-102", department = "Computer Science", teacherId = tSmithId, periodsPerWeek = 4, isLab = false))
        insertSubject(Subject(name = "Computer Networks", code = "CS-201", department = "Computer Science", teacherId = tJohnsonId, periodsPerWeek = 3, isLab = false))
        insertSubject(Subject(name = "Networks Laboratory", code = "CS-201L", department = "Computer Science", teacherId = tJohnsonId, periodsPerWeek = 2, isLab = true, consecutivePeriodsRequired = 2, preferredClassrooms = "CS Networks Lab"))
        insertSubject(Subject(name = "Database Systems", code = "IT-202", department = "Information Technology", teacherId = tLeeId, periodsPerWeek = 4, isLab = false))
        insertSubject(Subject(name = "Circuits Analysis", code = "EE-101", department = "Electrical Engineering", teacherId = tGarciaId, periodsPerWeek = 3, isLab = false))
        insertSubject(Subject(name = "Circuits Laboratory", code = "EE-101L", department = "Electrical Engineering", teacherId = tGarciaId, periodsPerWeek = 2, isLab = true, consecutivePeriodsRequired = 2, preferredClassrooms = "EE Circuits Lab"))
    }
}
