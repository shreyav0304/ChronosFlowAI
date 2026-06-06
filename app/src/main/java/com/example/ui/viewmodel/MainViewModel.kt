package com.example.ui.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.db.AppDatabase
import com.example.data.repository.ScheduleRepository
import com.example.scheduling.GeminiService
import com.example.scheduling.ScheduleEngine
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screen {
    Login,
    SetupWizard,
    TeacherManagement,
    SubjectAllocation,
    Generator,
    Dashboard,
    ConflictCenter,
    TeacherPortal,
    Analytics,
    Settings
}

enum class DashboardViewType {
    Section,
    Teacher,
    Classroom
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScheduleRepository

    // Native Database Flows
    val departments: StateFlow<List<Department>>
    val teachers: StateFlow<List<Teacher>>
    val subjects: StateFlow<List<Subject>>
    val classrooms: StateFlow<List<Classroom>>
    val timetableCells: StateFlow<List<TimetableCell>>
    val leaveRequests: StateFlow<List<LeaveRequest>>
    val substitutes: StateFlow<List<SubstituteAssignment>>
    val savedScenarios: StateFlow<List<SavedScenario>>
    val schedulePresets: StateFlow<List<SchedulePreset>>

    // Navigation and UX state variables
    private val _currentScreen = MutableStateFlow(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isAdmin = MutableStateFlow(true)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _selectedPortalTeacher = MutableStateFlow<Teacher?>(null)
    val selectedPortalTeacher: StateFlow<Teacher?> = _selectedPortalTeacher.asStateFlow()

    // Dashboard Filters
    private val _dashboardViewType = MutableStateFlow(DashboardViewType.Section)
    val dashboardViewType: StateFlow<DashboardViewType> = _dashboardViewType.asStateFlow()

    private val _selectedSectionFilter = MutableStateFlow("Section A")
    val selectedSectionFilter: StateFlow<String> = _selectedSectionFilter.asStateFlow()

    private val _selectedTeacherFilterId = MutableStateFlow<Int?>(null)
    val selectedTeacherFilterId: StateFlow<Int?> = _selectedTeacherFilterId.asStateFlow()

    private val _selectedRoomFilterId = MutableStateFlow<Int?>(null)
    val selectedRoomFilterId: StateFlow<Int?> = _selectedRoomFilterId.asStateFlow()

    // Generator Results
    private val _optimizationScore = MutableStateFlow(0)
    val optimizationScore: StateFlow<Int> = _optimizationScore.asStateFlow()

    private val _teacherBalanceScore = MutableStateFlow(0)
    val teacherBalanceScore: StateFlow<Int> = _teacherBalanceScore.asStateFlow()

    private val _classroomUtilizationScore = MutableStateFlow(0)
    val classroomUtilizationScore: StateFlow<Int> = _classroomUtilizationScore.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _softConstraintsNotes = MutableStateFlow<List<String>>(emptyList())
    val softConstraintsNotes: StateFlow<List<String>> = _softConstraintsNotes.asStateFlow()

    private val _unassignedSubjects = MutableStateFlow<List<Pair<Subject, Int>>>(emptyList())
    val unassignedSubjects: StateFlow<List<Pair<Subject, Int>>> = _unassignedSubjects.asStateFlow()

    // AI States
    private val _aiSuggestions = MutableStateFlow("")
    val aiSuggestions: StateFlow<String> = _aiSuggestions.asStateFlow()

    private val _isQueryingAi = MutableStateFlow(false)
    val isQueryingAi: StateFlow<Boolean> = _isQueryingAi.asStateFlow()

    private val _whatIfScenarioText = MutableStateFlow("")
    val whatIfScenarioText: StateFlow<String> = _whatIfScenarioText.asStateFlow()

    private val _whatIfAiResponse = MutableStateFlow("")
    val whatIfAiResponse: StateFlow<String> = _whatIfAiResponse.asStateFlow()

    private val _isSimulatingWhatIf = MutableStateFlow(false)
    val isSimulatingWhatIf: StateFlow<Boolean> = _isSimulatingWhatIf.asStateFlow()

    // AI Slot Recommendations States
    private val _slotRecommendations = MutableStateFlow<List<ScheduleEngine.RecommendedSlot>>(emptyList())
    val slotRecommendations: StateFlow<List<ScheduleEngine.RecommendedSlot>> = _slotRecommendations.asStateFlow()

    private val _aiRecommendationEnrichment = MutableStateFlow("")
    val aiRecommendationEnrichment: StateFlow<String> = _aiRecommendationEnrichment.asStateFlow()

    private val _isCalculatingRecommendations = MutableStateFlow(false)
    val isCalculatingRecommendations: StateFlow<Boolean> = _isCalculatingRecommendations.asStateFlow()

    // Drag, Drop, Swap State
    private val _swappingCell = MutableStateFlow<TimetableCell?>(null)
    val swappingCell: StateFlow<TimetableCell?> = _swappingCell.asStateFlow()

    // Main UI Thread Undo/Redo historical State
    private val undoStack = mutableListOf<List<TimetableCell>>()
    private val redoStack = mutableListOf<List<TimetableCell>>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private fun captureHistory() {
        val current = ArrayList(timetableCells.value)
        undoStack.add(current)
        if (undoStack.size > 30) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
        _canUndo.value = true
        _canRedo.value = false
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val currentState = timetableCells.value
            val previousState = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(currentState)
            if (redoStack.size > 30) {
                redoStack.removeAt(0)
            }
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = true
            
            viewModelScope.launch(Dispatchers.IO) {
                repository.saveTimetable(previousState)
            }
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val currentState = timetableCells.value
            val nextState = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(currentState)
            if (undoStack.size > 30) {
                undoStack.removeAt(0)
            }
            _canUndo.value = true
            _canRedo.value = redoStack.isNotEmpty()
            
            viewModelScope.launch(Dispatchers.IO) {
                repository.saveTimetable(nextState)
            }
        }
    }

    // Real-Time Validation Conflicts
    val liveConflicts: StateFlow<List<String>>

    // Onboarding Walkthrough State
    private val _isOnboardingActive = MutableStateFlow(false)
    val isOnboardingActive: StateFlow<Boolean> = _isOnboardingActive.asStateFlow()


    init {
        val database = AppDatabase.getDatabase(application)
        repository = ScheduleRepository(database.scheduleDao())

        departments = repository.allDepartments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        teachers = repository.allTeachers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        subjects = repository.allSubjects.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        classrooms = repository.allClassrooms.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        timetableCells = repository.allTimetableCells.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        leaveRequests = repository.allLeaveRequests.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        substitutes = repository.allSubstitutes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        savedScenarios = repository.allSavedScenarios.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        schedulePresets = repository.allSchedulePresets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Calculate live conflicts reactively when cells, teachers, subjects or classrooms alter
        liveConflicts = combine(timetableCells, teachers, subjects, classrooms) { cells, tList, sList, rList ->
            ScheduleEngine.analyzeConflicts(cells, tList, sList, rList)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Calculate live metrics reactively O(N) when cells, teachers, subjects, classrooms or departments alter
        combine(timetableCells, teachers, subjects, classrooms, departments) { cells, tList, sList, rList, dList ->
            ScheduleEngine.evaluate(cells, dList, tList, sList, rList)
        }.onEach { evaluation ->
            _optimizationScore.value = evaluation.healthScore
            _teacherBalanceScore.value = evaluation.teacherBalanceScore
            _classroomUtilizationScore.value = evaluation.classroomUtilizationScore
            _softConstraintsNotes.value = evaluation.softConstraintsNotes
            _unassignedSubjects.value = evaluation.unassignedSubjects
        }.flowOn(Dispatchers.Default)
         .launchIn(viewModelScope)

        // Auto-Seed demo on first run if database is empty
        viewModelScope.launch(Dispatchers.IO) {
            repository.seedDemoData()
            // Set defaults for filters once data is ready
            teachers.first { it.isNotEmpty() }.let {
                _selectedTeacherFilterId.value = it.firstOrNull()?.id
                _selectedPortalTeacher.value = it.firstOrNull()
            }
            classrooms.first { it.isNotEmpty() }.let {
                _selectedRoomFilterId.value = it.firstOrNull()?.id
            }
        }
    }

    private fun recomputeMetrics(cells: List<TimetableCell>) {
        // Handled completely and reactively via background Flow combination!
    }

    // Navigation and Role Control
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setOnboardingActive(active: Boolean) {
        _isOnboardingActive.value = active
    }


    fun setAdminMode(isAdmin: Boolean) {
        _isAdmin.value = isAdmin
    }

    fun selectPortalTeacher(teacher: Teacher) {
        _selectedPortalTeacher.value = teacher
    }

    // Dashboard Filters
    fun setDashboardViewType(type: DashboardViewType) {
        _dashboardViewType.value = type
    }

    fun setSectionFilter(section: String) {
        _selectedSectionFilter.value = section
    }

    fun setTeacherFilter(teacherId: Int) {
        _selectedTeacherFilterId.value = teacherId
    }

    fun setRoomFilter(classroomId: Int) {
        _selectedRoomFilterId.value = classroomId
    }

    // Department CRUD
    fun addDepartment(name: String, code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertDepartment(Department(name = name, code = code))
        }
    }

    fun deleteDepartment(department: Department) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDepartment(department)
        }
    }

    // Teacher CRUD
    fun addTeacher(
        name: String,
        empId: String,
        dept: String,
        subjects: String,
        maxHours: Int,
        freeSlots: String,
        teachingSlots: String,
        unavailableSlots: String,
        labEligible: Boolean,
        desig: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTeacher(
                Teacher(
                    name = name,
                    employeeId = empId,
                    department = dept,
                    subjectsHandled = subjects,
                    maxHoursPerWeek = maxHours,
                    preferredFreeSlots = freeSlots,
                    preferredTeachingSlots = teachingSlots,
                    availabilityConstraints = unavailableSlots,
                    labEligibility = labEligible,
                    designation = desig
                )
            )
        }
    }

    fun deleteTeacher(teacher: Teacher) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTeacher(teacher)
        }
    }

    // Subject CRUD
    fun addSubject(
        name: String,
        code: String,
        dept: String,
        teacherId: Int?,
        periods: Int,
        isLab: Boolean,
        consecutive: Int,
        rooms: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSubject(
                Subject(
                    name = name,
                    code = code,
                    department = dept,
                    teacherId = teacherId,
                    periodsPerWeek = periods,
                    isLab = isLab,
                    consecutivePeriodsRequired = consecutive,
                    preferredClassrooms = rooms
                )
            )
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSubject(subject)
        }
    }

    // Classroom CRUD
    fun addClassroom(name: String, cap: Int, isLab: Boolean, eq: String, camp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertClassroom(
                Classroom(name = name, capacity = cap, isLab = isLab, equipment = eq, campus = camp)
            )
        }
    }

    fun deleteClassroom(classroom: Classroom) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteClassroom(classroom)
        }
    }

    // Emergency Re-Seeding / Clear Database
    fun wipeAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearTimetable()
            repository.clearSubjects()
            repository.clearTeachers()
            repository.clearClassrooms()
            repository.clearDepartments()
            repository.clearSavedScenarios()
        }
    }

    fun forceSeed() {
        viewModelScope.launch(Dispatchers.IO) {
            _isGenerating.value = true
            // Load custom preset seed data with overwrite
            repository.seedDemoData(force = true)
            
            // Wait slightly for reactive database streams to sync the new list
            val depts = repository.allDepartments.first()
            val tList = repository.allTeachers.first()
            val sList = repository.allSubjects.first()
            val rList = repository.allClassrooms.first()
            
            // Run constraint solver immediately
            val result = ScheduleEngine.generate(depts, tList, sList, rList)
            repository.saveTimetable(result.cells)
            
            _optimizationScore.value = result.healthScore
            _teacherBalanceScore.value = result.teacherBalanceScore
            _classroomUtilizationScore.value = result.classroomUtilizationScore
            _softConstraintsNotes.value = result.softConstraintsNotes
            _unassignedSubjects.value = result.unassignedSubjects
            
            // Reset active filter selections to defaults
            _selectedSectionFilter.value = "Section A"
            _selectedTeacherFilterId.value = tList.firstOrNull()?.id
            _selectedPortalTeacher.value = tList.firstOrNull()
            _selectedRoomFilterId.value = rList.firstOrNull()?.id
            
            _isOnboardingActive.value = true
            _isGenerating.value = false
        }
    }


    // Solver Generation Command
    fun generateTimetable() {
        captureHistory()
        viewModelScope.launch(Dispatchers.IO) {
            _isGenerating.value = true
            
            val depts = departments.value
            val tList = teachers.value
            val sList = subjects.value
            val rList = classrooms.value

            val result = ScheduleEngine.generate(depts, tList, sList, rList)
            repository.saveTimetable(result.cells)

            _optimizationScore.value = result.healthScore
            _teacherBalanceScore.value = result.teacherBalanceScore
            _classroomUtilizationScore.value = result.classroomUtilizationScore
            _softConstraintsNotes.value = result.softConstraintsNotes
            _unassignedSubjects.value = result.unassignedSubjects

            _isGenerating.value = false
        }
    }

    // Period Interactive Swapping State
    fun selectCellForSwap(cell: TimetableCell) {
        val currentSwapper = _swappingCell.value
        if (currentSwapper == null) {
            // First select
            _swappingCell.value = cell
        } else {
            if (currentSwapper.id == cell.id) {
                // De-select
                _swappingCell.value = null
            } else {
                // Secondary select: execute SWAP in database!
                executeSwapCells(currentSwapper, cell)
                _swappingCell.value = null
            }
        }
    }

    private fun executeSwapCells(cellA: TimetableCell, cellB: TimetableCell) {
        captureHistory()
        viewModelScope.launch(Dispatchers.IO) {
            // Swap day and period index between cell A and cell B
            val swappedA = cellA.copy(day = cellB.day, periodIndex = cellB.periodIndex)
            val swappedB = cellB.copy(day = cellA.day, periodIndex = cellA.periodIndex)

            repository.insertTimetableCell(swappedA)
            repository.insertTimetableCell(swappedB)

            // Recompute evaluation metrics immediately
            recomputeMetrics(timetableCells.value)
        }
    }

    fun moveCellToVacantSlot(cell: TimetableCell, targetDay: String, targetPeriodIndex: Int) {
        captureHistory()
        viewModelScope.launch(Dispatchers.IO) {
            val updated = cell.copy(day = targetDay, periodIndex = targetPeriodIndex)
            repository.insertTimetableCell(updated)
            
            // Clear current selection state
            _swappingCell.value = null
            
            // Recompute evaluation metrics immediately
            recomputeMetrics(timetableCells.value)
        }
    }

    // Emergency Re-Timetabling for Absent Instructor
    fun applyEmergencyReTimetabling(absentTeacherId: Int, dayOfWeek: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGenerating.value = true
            val activeCells = timetableCells.value
            val currentRooms = classrooms.value
            val currentTeachers = teachers.value
            val currentSubjects = subjects.value

            // Identify affected periods where this teacher is serving
            val affectedCells = activeCells.filter { 
                it.teacherId == absentTeacherId && (dayOfWeek == null || it.day.equals(dayOfWeek, ignoreCase = true)) 
            }
            if (affectedCells.isEmpty()) {
                _isGenerating.value = false
                return@launch
            }

            // Find compatible substitutes in the same department who are FREE in those slots
            val activeDept = currentTeachers.find { it.id == absentTeacherId }?.department ?: ""
            val candidates = currentTeachers.filter { it.department == activeDept && it.id != absentTeacherId }

            affectedCells.forEach { cell ->
                // Look for first teacher who doesn't have an active slot in this day-period
                val substitute = candidates.find { candidate ->
                    val isBusy = activeCells.any { it.teacherId == candidate.id && it.day == cell.day && it.periodIndex == cell.periodIndex }
                    !isBusy
                }
                if (substitute != null) {
                    // Reassign cell to substitute teacher in DB
                    repository.insertTimetableCell(cell.copy(teacherId = substitute.id))
                    // Also capture in substitution logs
                    repository.insertSubstitute(
                        SubstituteAssignment(
                            cellId = cell.id,
                            substituteTeacherId = substitute.id,
                            date = dayOfWeek ?: "All Week" // Simulating or capturing specific scope
                        )
                    )
                }
            }

            // Trigger recomputation of validation
            recomputeMetrics(timetableCells.value)
            _isGenerating.value = false
        }
    }

    // Leave Application / Leave approvals
    fun applyForLeave(teacherId: Int, start: String, end: String, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val leaveId = repository.insertLeaveRequest(
                LeaveRequest(teacherId = teacherId, startDate = start, endDate = end, notes = notes, status = "Pending")
            )
        }
    }

    fun approveLeaveRequest(leave: LeaveRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateLeaveStatus(leave.id, "Approved")
            
            val absentTeacher = teachers.value.find { it.id == leave.teacherId }
            val tName = absentTeacher?.name ?: "Unknown Teacher"
            
            // Automatically execute Emergency Re-Timetabling parameters for this leave window!
            // If startDate/endDate lists a specific working day, re-schedule only that day
            val dayOfWeek = if (ScheduleEngine.WORKING_DAYS.contains(leave.startDate)) leave.startDate else null
            applyEmergencyReTimetabling(leave.teacherId, dayOfWeek)

            val dayText = dayOfWeek ?: "Specified Leave Wave"
            triggerPushNotification(
                title = "Emergency Cover Assigned!",
                message = "Leave approved for Prof. $tName ($dayText). Systems auto-triggered cover duty cover schedules!"
            )
        }
    }

    fun rejectLeaveRequest(leave: LeaveRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateLeaveStatus(leave.id, "Rejected")
        }
    }

    // AI Suggestions calling Gemini
    fun queryAiSuggestions() {
        viewModelScope.launch(Dispatchers.IO) {
            _isQueryingAi.value = true
            val cells = timetableCells.value
            val sList = subjects.value
            val tList = teachers.value
            val rList = classrooms.value

            val detailSummary = """
                Total scheduled periods: ${cells.size}
                Optimization score: ${_optimizationScore.value}%
                Teacher balance score: ${_teacherBalanceScore.value}%
                Classroom utilization score: ${_classroomUtilizationScore.value}%
                Unassigned subjects count: ${_unassignedSubjects.value.size}
                Active schedules:
                ${cells.take(10).joinToString("\n") { cell -> 
                    val subjName = sList.find { it.id == cell.subjectId }?.name ?: "Unknown"
                    val teachName = tList.find { it.id == cell.teacherId }?.name ?: "Unknown"
                    val roomName = rList.find { it.id == cell.classroomId }?.name ?: "Unknown"
                    "- ${cell.day} period ${cell.periodIndex + 1}: $subjName by $teachName in $roomName for ${cell.section}"
                }}
            """.trimIndent()

            val aiResponse = GeminiService.requestAiSuggestions(detailSummary, "optimize")
            _aiSuggestions.value = aiResponse
            _isQueryingAi.value = false
        }
    }

    // What-If Simulation Command
    fun runSimulationScenario(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSimulatingWhatIf.value = true
            _whatIfScenarioText.value = text

            val scenarioSummary = GeminiService.runWhatIfSimulation(
                text,
                teachers.value,
                subjects.value,
                classrooms.value,
                timetableCells.value
            )

            _whatIfAiResponse.value = scenarioSummary
            _isSimulatingWhatIf.value = false
        }
    }

    // Requests slot recommendations using local heuristics + optional Gemini AI enrichment
    fun getSlotRecommendations(
        subject: Subject?,
        teacher: Teacher?,
        classroom: Classroom?,
        section: String
    ) {
        if (subject == null || teacher == null || classroom == null) return
        
        viewModelScope.launch(Dispatchers.IO) {
            _isCalculatingRecommendations.value = true
            _aiRecommendationEnrichment.value = ""
            
            // Calculate heuristic recommendations
            val locals = ScheduleEngine.recommendSlots(
                cells = timetableCells.value,
                teachers = teachers.value,
                subjects = subjects.value,
                classrooms = classrooms.value,
                targetSubject = subject,
                targetTeacher = teacher,
                targetClassroom = classroom,
                targetSection = section
            )
            _slotRecommendations.value = locals

            // Optional: query Gemini to enrich with conversational explanations
            val enrichedText = GeminiService.enrichRecommendationsWithAi(
                targetSubject = subject,
                targetTeacher = teacher,
                targetClassroom = classroom,
                targetSection = section,
                localRecommendations = locals
            )
            _aiRecommendationEnrichment.value = enrichedText
            _isCalculatingRecommendations.value = false
        }
    }

    fun clearSlotRecommendations() {
        _slotRecommendations.value = emptyList()
        _aiRecommendationEnrichment.value = ""
    }

    fun deleteTimetableCellById(cellId: Int) {
        captureHistory()
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTimetableCellById(cellId)
            recomputeMetrics(timetableCells.value)
        }
    }

    fun addManualCell(
        section: String,
        subjectId: Int,
        teacherId: Int,
        classroomId: Int,
        day: String,
        periodIndex: Int
    ) {
        captureHistory()
        viewModelScope.launch(Dispatchers.IO) {
            val cell = TimetableCell(
                departmentId = subjects.value.find { it.id == subjectId }?.id ?: 1,
                section = section,
                subjectId = subjectId,
                teacherId = teacherId,
                classroomId = classroomId,
                day = day,
                periodIndex = periodIndex
            )
            repository.insertTimetableCell(cell)
            recomputeMetrics(timetableCells.value)
        }
    }

    fun removeSubstitute(cellId: Int, date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeSubstitute(cellId, date)
            recomputeMetrics(timetableCells.value)
        }
    }

    // Interactive What-If Scenario Saving
    fun saveCurrentScenarioState(name: String, resultText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSavedScenario(
                SavedScenario(
                    name = name,
                    resultText = resultText,
                    activeCellsCount = timetableCells.value.size,
                    score = _optimizationScore.value,
                    balance = _teacherBalanceScore.value,
                    utilization = _classroomUtilizationScore.value
                )
            )
        }
    }

    fun deleteScenarioState(scenario: SavedScenario) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSavedScenario(scenario)
        }
    }

    fun clearAllScenarioStates() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearSavedScenarios()
        }
    }

    // --- Moshi serialization config for schedule presets ---
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val cellsAdapter = moshi.adapter<List<TimetableCell>>(
        Types.newParameterizedType(List::class.java, TimetableCell::class.java)
    )

    fun saveCurrentAsPreset(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cells = timetableCells.value
            val serialized = cellsAdapter.toJson(cells)
            repository.insertSchedulePreset(
                SchedulePreset(
                    name = name,
                    cellsJson = serialized,
                    activeCellsCount = cells.size,
                    score = _optimizationScore.value
                )
            )
        }
    }

    fun loadPreset(preset: SchedulePreset) {
        captureHistory()
        viewModelScope.launch(Dispatchers.IO) {
            val cells = cellsAdapter.fromJson(preset.cellsJson) ?: emptyList()
            repository.saveTimetable(cells)
        }
    }

    fun deletePreset(preset: SchedulePreset) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSchedulePreset(preset)
        }
    }

    fun clearAllPresets() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearSchedulePresets()
        }
    }

    // Push Notifications & Reminders Setup
    fun triggerPushNotification(title: String, message: String) {
        val context = getApplication<Application>().applicationContext
        val channelId = "chronos_flow_alerts"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ChronosFlow Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent cover duty notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    // CSV Export Capability
    fun generateTimetableCsv(): String {
        val cells = timetableCells.value
        val sList = subjects.value
        val tList = teachers.value
        val rList = classrooms.value
        
        val csvBuilder = StringBuilder()
        csvBuilder.append("Day,PeriodIndex,Section,SubjectCode,SubjectName,InstructorName,RoomName,Campus\n")
        
        cells.forEach { cell ->
            val d = cell.day
            val p = cell.periodIndex + 1
            val sec = cell.section
            val subj = sList.find { it.id == cell.subjectId }
            val t = tList.find { it.id == cell.teacherId }
            val r = rList.find { it.id == cell.classroomId }
            
            val subjCode = subj?.code ?: "N/A"
            val subjName = (subj?.name ?: "N/A").replace("\"", "\"\"")
            val tName = (t?.name ?: "N/A").replace("\"", "\"\"")
            val rName = (r?.name ?: "N/A").replace("\"", "\"\"")
            val camp = r?.campus ?: "Main"
            
            csvBuilder.append("\"$d\",\"Period $p\",\"$sec\",\"$subjCode\",\"$subjName\",\"$tName\",\"$rName\",\"$camp\"\n")
        }
        return csvBuilder.toString()
    }
}
