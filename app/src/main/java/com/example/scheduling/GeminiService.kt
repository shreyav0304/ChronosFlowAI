package com.example.scheduling

import com.example.BuildConfig
import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini API Moshi Data Classes ---
data class GenerateContentRequest(
    val contents: List<ContentDto>,
    val systemInstruction: ContentDto? = null
)

data class ContentDto(
    val parts: List<PartDto>
)

data class PartDto(
    val text: String
)

data class GenerateContentResponse(
    val candidates: List<CandidateDto>?
)

data class CandidateDto(
    val content: ContentDto?
)

// --- Retrofit API Service Interface ---
interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Gemini Retrofit Client ---
object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    // Safely reads key or returns empty/placeholder
    fun getApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        return if (key.isEmpty() || key == "MY_GEMINI_API_KEY" || key.contains("PLACEHOLDER")) {
            ""
        } else {
            key
        }
    }
}

// --- Gemini Core Helper Class ---
object GeminiService {

    suspend fun requestAiSuggestions(
        timetableDetails: String,
        promptType: String // "optimize", "substitute", "conflict"
    ): String {
        val apiKey = GeminiApiClient.getApiKey()
        if (apiKey.isEmpty()) {
            return "AI Suggestion Unavailable: Please set a valid GEMINI_API_KEY in the Secrets panel on AI Studio to run real-time generative suggestions."
        }

        val instruction = when (promptType) {
            "optimize" -> "You are an expert academic scheduling consultant. Analyze the provided timetable state, check for any resource gaps or sub-optimal patterns, and give 3 short, concrete bullet-pointed suggestions for better lesson packing and workload fairness."
            "substitute" -> "You are an administrative director. A teacher has requested leave. Examine the affected sessions and suggest the best alternative substitute teachers from the available list, considering department compatibility and class load balance."
            else -> "You are a logical scheduling solver. Suggest a simple, quick visual resolution to the listed resource clashes, detailing which slots can be swapped cleanly."
        }

        val request = GenerateContentRequest(
            contents = listOf(
                ContentDto(parts = listOf(PartDto(text = "Timetable Data State:\n$timetableDetails")))
            ),
            systemInstruction = ContentDto(parts = listOf(PartDto(text = instruction)))
        )

        return try {
            val response = GeminiApiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No suggestions retrieved from Gemini model. Check prompt structure."
        } catch (e: Exception) {
            "AI Server Error: Unable to query Gemini model. ${e.localizedMessage ?: "Please confirm network connections."}"
        }
    }

    // Runs a visual "What-If Analysis" which simulates teacher leave or room capacity shortage.
    suspend fun runWhatIfSimulation(
        scenario: String,
        teachers: List<Teacher>,
        subjects: List<Subject>,
        classrooms: List<Classroom>,
        timetableCells: List<TimetableCell>
    ): String {
        val apiKey = GeminiApiClient.getApiKey()
        if (apiKey.isEmpty()) {
            return "What-If Simulator: Configure your GEMINI_API_KEY to see a full, conversational AI assessment of this event's impact on your academic semester."
        }

        val payload = """
            SCENARIO: $scenario
            TEACHERS: ${teachers.joinToString { "${it.name} (ID: ${it.id}, Handles: ${it.subjectsHandled})" }}
            SUBJECTS: ${subjects.joinToString { "${it.name} (periods/week: ${it.periodsPerWeek})" }}
            ROOMS: ${classrooms.joinToString { "${it.name} (Cap: ${it.capacity})" }}
            SESSIONS SCHEDULED: ${timetableCells.size} active cell records.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                ContentDto(parts = listOf(PartDto(text = payload)))
            ),
            systemInstruction = ContentDto(parts = listOf(PartDto(text = "You are a predictive predictive simulator. Estimate the quantitative impact of this scenario (e.g., number of disrupted classes, substitute burden, lab capacity shortages) and summarize the visual trade-off of alternative actions in 3 highly professional, structured bullet points.")))
        )

        return try {
            val response = GeminiApiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Unable to calculate predictive simulation results."
        } catch (e: Exception) {
            "Simulator Error: Failed to execute AI calculation. Reason: ${e.message}"
        }
    }
}
