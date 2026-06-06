package com.example.scheduling

import com.example.data.*
import kotlin.random.Random

object ScheduleEngine {

    val WORKING_DAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    const val NUM_PERIODS = 6 // 6 periods per day (e.g., 9:00 AM - 4:00 PM)
    const val LUNCH_PERIOD = 3 // 4th period is Lunch (index 3)

    data class GenerationResult(
        val cells: List<TimetableCell>,
        val unassignedSubjects: List<Pair<Subject, Int>>, // Subject and remaining hours
        val healthScore: Int,
        val teacherBalanceScore: Int,
        val classroomUtilizationScore: Int,
        val softConstraintsNotes: List<String>
    )

    // Solves and generates a timetable with hard constraints satisfied as much as possible,
    // and returns full details.
    fun generate(
        departments: List<Department>,
        teachers: List<Teacher>,
        subjects: List<Subject>,
        classrooms: List<Classroom>,
        sections: Map<String, List<String>> = mapOf(
            "Computer Science" to listOf("Section A", "Section B"),
            "Information Technology" to listOf("Section A"),
            "Electrical Engineering" to listOf("Section A")
        )
    ): GenerationResult {
        var bestResult: GenerationResult? = null
        var bestScore = -1

        // Run the solver multiple times with randomized heuristics to find a highly optimized schedule
        for (attempt in 1..25) {
            val result = solveSingleAttempt(departments, teachers, subjects, classrooms, sections, attempt)
            if (result.healthScore > bestScore) {
                bestScore = result.healthScore
                bestResult = result
            }
            if (result.healthScore >= 98 && result.unassignedSubjects.isEmpty()) {
                break // Found a nearly perfect schedule early
            }
        }

        return bestResult ?: GenerationResult(emptyList(), emptyList(), 0, 0, 0, listOf("Unable to schedule class lines."))
    }

    // Fast, lightweight O(N) evaluation of any active timetable state to compute scores and conflicts
    fun evaluate(
        cells: List<TimetableCell>,
        departments: List<Department>,
        teachers: List<Teacher>,
        subjects: List<Subject>,
        classrooms: List<Classroom>,
        sections: Map<String, List<String>> = mapOf(
            "Computer Science" to listOf("Section A", "Section B"),
            "Information Technology" to listOf("Section A"),
            "Electrical Engineering" to listOf("Section A")
        )
    ): GenerationResult {
        val teacherLoad = teachers.associate { it.id to 0 }.toMutableMap()
        val classroomUsage = classrooms.associate { it.id to 0 }.toMutableMap()

        // Distribute subjects to process
        val classSubjects = mutableListOf<Triple<String, String, Subject>>() // DeptId, Section, Subject
        departments.forEach { dept ->
            val deptSections = sections[dept.name] ?: listOf("Section A")
            val deptSubjects = subjects.filter { it.department.equals(dept.name, ignoreCase = true) }
            
            deptSections.forEach { section ->
                deptSubjects.forEach { sub ->
                    classSubjects.add(Triple(dept.name, section, sub))
                }
            }
        }

        // Track how many periods are left to assign for each subject-section combination
        val hoursRemaining = classSubjects.associate { triple ->
            val key = "${triple.first}_${triple.second}_${triple.third.id}"
            key to triple.third.periodsPerWeek
        }.toMutableMap()

        // Track workload and decrement hours remaining based on loaded cells
        cells.forEach { cell ->
            teacherLoad[cell.teacherId] = (teacherLoad[cell.teacherId] ?: 0) + 1
            classroomUsage[cell.classroomId] = (classroomUsage[cell.classroomId] ?: 0) + 1
            
            val activeDept = departments.find { it.id == cell.departmentId }?.name ?: ""
            val key = "${activeDept}_${cell.section}_${cell.subjectId}"
            val currentVal = hoursRemaining[key]
            if (currentVal != null && currentVal > 0) {
                hoursRemaining[key] = currentVal - 1
            }
        }

        // Calculate Quality Metrics
        // 1. Unassigned Hours Penalty
        val unassignedList = mutableListOf<Pair<Subject, Int>>()
        var totalUnassignedHours = 0
        classSubjects.forEach { (deptName, section, subject) ->
            val key = "${deptName}_${section}_${subject.id}"
            val unassigned = hoursRemaining[key] ?: 0
            if (unassigned > 0) {
                unassignedList.add(subject to unassigned)
                totalUnassignedHours += unassigned
            }
        }

        // 2. Teacher Workload Fairness Score
        val assignedLoads = teacherLoad.values.filter { it > 0 }
        val teacherBalanceScore = if (assignedLoads.isEmpty()) 100 else {
            val maxLoad = assignedLoads.maxOrNull() ?: 1
            val minLoad = assignedLoads.minOrNull() ?: 1
            val diff = maxLoad - minLoad
            (100 - (diff * 5)).coerceIn(50, 100)
        }

        // 3. Classroom utilization rate
        val totalCapPeriods = classrooms.size * WORKING_DAYS.size * (NUM_PERIODS - 1)
        val totalOccupiedPeriods = cells.size
        val classroomUtilizationScore = if (totalCapPeriods == 0) 0 else {
            ((totalOccupiedPeriods.toFloat() / totalCapPeriods) * 100).toInt().coerceIn(0, 100)
        }

        // 4. Soft constraint notes
        val notes = mutableListOf<String>()
        if (totalUnassignedHours > 0) {
            notes.add("⚠ Could not schedule $totalUnassignedHours class periods due to heavy resource bottlenecks.")
        } else if (cells.isNotEmpty()) {
            notes.add("✓ Conflict-Free: 100% of subject hour requirements scheduled successfully.")
        }

        teachers.forEach { t ->
            val currentLoad = teacherLoad[t.id] ?: 0
            if (currentLoad > t.maxHoursPerWeek) {
                notes.add("⚠ Workload Limit Crossed: ${t.name} handles $currentLoad periods (Limit: ${t.maxHoursPerWeek}).")
            }
        }

        // Check teacher gaps/idle-periods
        var idleGapsDetected = 0
        teachers.forEach { t ->
            WORKING_DAYS.forEach { day ->
                var startPeriod = -1
                var endPeriod = -1
                val activePeriods = mutableSetOf<Int>()
                
                cells.forEach { cell ->
                    if (cell.teacherId == t.id && cell.day == day) {
                        activePeriods.add(cell.periodIndex)
                        if (startPeriod == -1 || cell.periodIndex < startPeriod) startPeriod = cell.periodIndex
                        if (cell.periodIndex > endPeriod) endPeriod = cell.periodIndex
                    }
                }

                if (startPeriod != -1 && endPeriod != -1) {
                    for (p in startPeriod..endPeriod) {
                        if (p != LUNCH_PERIOD && !activePeriods.contains(p)) {
                            idleGapsDetected++
                        }
                    }
                }
            }
        }

        if (idleGapsDetected > 0) {
            notes.add("✦ Gap Alert: Identified $idleGapsDetected teacher idle gaps. AI suggests compaction.")
        } else if (cells.isNotEmpty()) {
            notes.add("✓ Compact Workload: All instructors enjoy streamlined sequential sessions, zero idle gap hours.")
        }

        // Compute ultimate Optimization Score
        val unassignedPenalty = totalUnassignedHours * 8
        val gapPenalty = idleGapsDetected * 2
        val overworkPenalty = teachers.count { (teacherLoad[it.id] ?: 0) > it.maxHoursPerWeek } * 10
        val healthScore = if (cells.isEmpty()) 0 else (100 - unassignedPenalty - gapPenalty - overworkPenalty).coerceIn(40, 100)

        // Make the list unique by subject id to prevent lists of pairs overloading the UI
        val uniqueUnassigned = unassignedList.distinctBy { it.first.id }

        return GenerationResult(
            cells = cells,
            unassignedSubjects = uniqueUnassigned,
            healthScore = healthScore,
            teacherBalanceScore = teacherBalanceScore,
            classroomUtilizationScore = classroomUtilizationScore,
            softConstraintsNotes = notes
        )
    }

    private fun solveSingleAttempt(
        departments: List<Department>,
        teachers: List<Teacher>,
        subjects: List<Subject>,
        classrooms: List<Classroom>,
        sections: Map<String, List<String>>,
        seedOffset: Int
    ): GenerationResult {
        val random = Random(42 + seedOffset)
        val generatedCells = mutableListOf<TimetableCell>()

        // Tracker sets for Busy resources: key is "ResourceID/ClassKey-Day-Period"
        val teacherBusy = mutableSetOf<String>()
        val classroomBusy = mutableSetOf<String>()
        val sectionPeriodBusy = mutableSetOf<String>()

        // Workloads
        val teacherLoad = teachers.associate { it.id to 0 }.toMutableMap()
        val classroomUsage = classrooms.associate { it.id to 0 }.toMutableMap()

        // Distribute subjects to process
        // Class key is "DepartmentName_Section" -> list of subjects that section has
        val classSubjects = mutableListOf<Triple<String, String, Subject>>() // DeptId, Section, Subject
        
        departments.forEach { dept ->
            val deptSections = sections[dept.name] ?: listOf("Section A")
            val deptSubjects = subjects.filter { it.department.equals(dept.name, ignoreCase = true) }
            
            deptSections.forEach { section ->
                deptSubjects.forEach { sub ->
                    classSubjects.add(Triple(dept.name, section, sub))
                }
            }
        }

        // Sort subjects: Labs first since they require consecutive slots and laboratory classrooms,
        // then subjects with higher hours since they are harder to fit.
        val sortedClassSubjects = classSubjects.sortedWith(
            compareByDescending<Triple<String, String, Subject>> { it.third.isLab }
                .thenByDescending { it.third.periodsPerWeek }
        ).toMutableList()

        // Track how many periods are left to assign for each subject-section combination
        val hoursRemaining = sortedClassSubjects.associate { triple ->
            val key = "${triple.first}_${triple.second}_${triple.third.id}"
            key to triple.third.periodsPerWeek
        }.toMutableMap()

        val selectWeeks = WORKING_DAYS.toMutableList()

        // Place subjects into slots
        sortedClassSubjects.forEach { (deptName, section, subject) ->
            val hoursKey = "${deptName}_${section}_${subject.id}"
            var remainingHours = hoursRemaining[hoursKey] ?: 0
            val teacherId = subject.teacherId ?: return@forEach
            val teacher = teachers.find { it.id == teacherId } ?: return@forEach

            // Resolve suitable classrooms
            val candidateClassrooms = classrooms.filter { room ->
                if (subject.isLab) room.isLab else !room.isLab
            }

            if (candidateClassrooms.isEmpty()) return@forEach

            // Try to assign slots
            var failedAttempts = 0
            while (remainingHours > 0 && failedAttempts < 50) {
                // Shuffle days and periods to avoid scheduling biases
                val trialDays = selectWeeks.shuffled(random)
                var placed = false

                for (day in trialDays) {
                    val trialPeriods = (0 until NUM_PERIODS).shuffled(random)
                    
                    for (periodIndex in trialPeriods) {
                        // Hard Constraint: Respect Lunch period
                        if (periodIndex == LUNCH_PERIOD) continue

                        // Check if teacher has availability constraints
                        val checkUnavailableStr = "${day.take(3)}-$periodIndex"
                        if (teacher.availabilityConstraints.contains(checkUnavailableStr, ignoreCase = true)) {
                            continue
                        }

                        // Determine consecutive requirements
                        val reqConsecutive = if (subject.isLab) subject.consecutivePeriodsRequired.coerceIn(1, 2) else 1

                        val slotsToCheck = (0 until reqConsecutive).map { periodIndex + it }
                        
                        // Ensure it fits within daily bounds and doesn't cross lunch
                        if (slotsToCheck.last() >= NUM_PERIODS || slotsToCheck.contains(LUNCH_PERIOD)) {
                            continue
                        }

                        // Verify that all candidate slots are free
                        var slotsAreFree = true
                        var chosenClassroom: Classroom? = null

                        for (room in candidateClassrooms.shuffled(random)) {
                            var roomFreeAllSlots = true
                            for (p in slotsToCheck) {
                                val sectionKey = "${deptName}_${section}-$day-$p"
                                val teacherKey = "$teacherId-$day-$p"
                                val roomKey = "${room.id}-$day-$p"

                                if (sectionPeriodBusy.contains(sectionKey) ||
                                    teacherBusy.contains(teacherKey) ||
                                    classroomBusy.contains(roomKey)
                                ) {
                                    roomFreeAllSlots = false
                                    break
                                }
                            }
                            if (roomFreeAllSlots) {
                                chosenClassroom = room
                                break
                            }
                        }

                        if (chosenClassroom != null) {
                            // Perfect! Allocate slots
                            slotsToCheck.forEach { p ->
                                val sectionKey = "${deptName}_${section}-$day-$p"
                                val teacherKey = "$teacherId-$day-$p"
                                val roomKey = "${chosenClassroom.id}-$day-$p"

                                sectionPeriodBusy.add(sectionKey)
                                teacherBusy.add(teacherKey)
                                classroomBusy.add(roomKey)

                                val deptId = departments.find { it.name.equals(deptName, ignoreCase = true) }?.id ?: 1
                                generatedCells.add(
                                    TimetableCell(
                                        departmentId = deptId,
                                        section = section,
                                        subjectId = subject.id,
                                        teacherId = teacherId,
                                        classroomId = chosenClassroom.id,
                                        day = day,
                                        periodIndex = p
                                    )
                                )

                                teacherLoad[teacherId] = (teacherLoad[teacherId] ?: 0) + 1
                                classroomUsage[chosenClassroom.id] = (classroomUsage[chosenClassroom.id] ?: 0) + 1
                            }

                            remainingHours -= reqConsecutive
                            hoursRemaining[hoursKey] = remainingHours
                            placed = true
                            break
                        }
                    }
                    if (placed) break
                }
                if (!placed) {
                    failedAttempts++
                }
            }
        }

        // Calculate Quality Metrics
        // 1. Unassigned Hours Penalty
        val unassignedList = mutableListOf<Pair<Subject, Int>>()
        var totalUnassignedHours = 0
        sortedClassSubjects.forEach { (deptName, section, subject) ->
            val key = "${deptName}_${section}_${subject.id}"
            val unassigned = hoursRemaining[key] ?: 0
            if (unassigned > 0) {
                unassignedList.add(subject to unassigned)
                totalUnassignedHours += unassigned
            }
        }

        // 2. Teacher Workload Fairness Score
        val assignedLoads = teacherLoad.values.filter { it > 0 }
        val teacherBalanceScore = if (assignedLoads.isEmpty()) 100 else {
            val maxLoad = assignedLoads.maxOrNull() ?: 1
            val minLoad = assignedLoads.minOrNull() ?: 1
            val diff = maxLoad - minLoad
            (100 - (diff * 5)).coerceIn(50, 100)
        }

        // 3. Classroom utilization rate
        // Total available periods = classrooms * days * (periods - 1 for lunch)
        val totalCapPeriods = classrooms.size * WORKING_DAYS.size * (NUM_PERIODS - 1)
        val totalOccupiedPeriods = generatedCells.size
        val classroomUtilizationScore = if (totalCapPeriods == 0) 0 else {
            ((totalOccupiedPeriods.toFloat() / totalCapPeriods) * 100).toInt().coerceIn(0, 100)
        }

        // 4. Soft constraint notes
        val notes = mutableListOf<String>()
        val totalTargetHours = subjects.sumOf { it.periodsPerWeek }
        
        if (totalUnassignedHours > 0) {
            notes.add("⚠ Could not schedule $totalUnassignedHours class periods due to heavy resource bottlenecks.")
        } else {
            notes.add("✓ Conflict-Free: 100% of subject hour requirements scheduled successfully.")
        }

        // Soft Constraint optimization reviews
        teachers.forEach { t ->
            val currentLoad = teacherLoad[t.id] ?: 0
            if (currentLoad > t.maxHoursPerWeek) {
                notes.add("⚠ Workload Limit Crossed: ${t.name} handles $currentLoad periods (Limit: ${t.maxHoursPerWeek}).")
            }
        }

        // Check teacher gaps/idle-periods
        var idleGapsDetected = 0
        teachers.forEach { t ->
            WORKING_DAYS.forEach { day ->
                var startPeriod = -1
                var endPeriod = -1
                val activePeriods = mutableSetOf<Int>()
                
                generatedCells.forEach { cell ->
                    if (cell.teacherId == t.id && cell.day == day) {
                        activePeriods.add(cell.periodIndex)
                        if (startPeriod == -1 || cell.periodIndex < startPeriod) startPeriod = cell.periodIndex
                        if (cell.periodIndex > endPeriod) endPeriod = cell.periodIndex
                    }
                }

                if (startPeriod != -1 && endPeriod != -1) {
                    for (p in startPeriod..endPeriod) {
                        if (p != LUNCH_PERIOD && !activePeriods.contains(p)) {
                            idleGapsDetected++
                        }
                    }
                }
            }
        }

        if (idleGapsDetected > 0) {
            notes.add("✦ Gap Alert: Identified $idleGapsDetected teacher idle gaps. AI suggests compaction.")
        } else {
            notes.add("✓ Compact Workload: All instructors enjoy streamlined sequential sessions, zero idle gap hours.")
        }

        // Compute ultimate Optimization Score
        val unassignedPenalty = totalUnassignedHours * 8
        val gapPenalty = idleGapsDetected * 2
        val overworkPenalty = teachers.count { (teacherLoad[it.id] ?: 0) > it.maxHoursPerWeek } * 10
        val healthScore = (100 - unassignedPenalty - gapPenalty - overworkPenalty).coerceIn(40, 100)

        return GenerationResult(
            cells = generatedCells,
            unassignedSubjects = unassignedList,
            healthScore = healthScore,
            teacherBalanceScore = teacherBalanceScore,
            classroomUtilizationScore = classroomUtilizationScore,
            softConstraintsNotes = notes
        )
    }

    // Resolves simple real-time conflicts for rendering highlights
    fun analyzeConflicts(cells: List<TimetableCell>, teachers: List<Teacher>, subjects: List<Subject>, classrooms: List<Classroom>): List<String> {
        val conflicts = mutableListOf<String>()
        val teacherBusyMap = mutableMapOf<String, Int>() // "day-period" to cellId
        val roomBusyMap = mutableMapOf<String, Int>() // "day-period" to cellId
        val sectionBusyMap = mutableMapOf<String, Int>() // "section-day-period" to cellId

        cells.forEach { cell ->
            val teacherKey = "${cell.day}-${cell.periodIndex}"
            val roomKey = "${cell.classroomId}-${cell.day}-${cell.periodIndex}"
            val sectionKey = "${cell.section}-${cell.day}-${cell.periodIndex}"

            val activeTeacher = teachers.find { it.id == cell.teacherId }
            val activeRoom = classrooms.find { it.id == cell.classroomId }
            val activeSubject = subjects.find { it.id == cell.subjectId }

            // Teacher busy check
            if (teacherBusyMap.contains(teacherKey)) {
                val conflictingTeacher = activeTeacher?.name ?: "Unknown Teacher"
                conflicts.add("⚠ Teacher Conflict: $conflictingTeacher double-booked on ${cell.day} Period ${cell.periodIndex + 1}.")
            } else {
                teacherBusyMap[teacherKey] = cell.id
            }

            // Room busy check
            if (roomBusyMap.contains(roomKey)) {
                val conflictingRoom = activeRoom?.name ?: "Unknown Room"
                conflicts.add("⚠ Room Conflict: $conflictingRoom double-booked on ${cell.day} Period ${cell.periodIndex + 1}.")
            } else {
                roomBusyMap[roomKey] = cell.id
            }

            // Section busy check
            if (sectionBusyMap.contains(sectionKey)) {
                conflicts.add("⚠ Section Conflict: Student ${cell.section} scheduled for duplicate classes on ${cell.day} Period ${cell.periodIndex + 1}.")
            } else {
                sectionBusyMap[sectionKey] = cell.id
            }

            // Room Capacity Match
            if (activeRoom != null && !cell.section.contains("Lab")) {
                val studentCount = if (cell.section.contains("Section A")) 60 else 35
                if (studentCount > activeRoom.capacity) {
                    conflicts.add("⚠ Capacity Constraint: Room ${activeRoom.name} capacity (${activeRoom.capacity}) is smaller than current group $studentCount.")
                }
            }
        }

        return conflicts
    }
}
