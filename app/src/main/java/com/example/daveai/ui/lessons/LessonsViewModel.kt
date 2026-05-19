package com.example.daveai.ui.lessons

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daveai.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

data class LessonsUiState(
    val activeSyllabus: Syllabus? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val inputText: String = "",
    val currentLessonContent: String? = null
)

data class Syllabus(
    val topic: String,
    val description: String,
    val modules: List<Module>
)

data class Module(
    val id: Int,
    val title: String,
    val completed: Boolean
)

class LessonsViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonsUiState())
    val uiState: StateFlow<LessonsUiState> = _uiState.asStateFlow()

    private var lessonSessionId: String? = null

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun generateSyllabus(topic: String) {
        if (topic.isBlank()) return
        
        _uiState.update { it.copy(isLoading = true, error = null, inputText = "") }

        viewModelScope.launch {
            try {
                lessonSessionId = chatRepository.createNewSession(title = "Course: $topic", projectType = "LESSONS")
                
                val prompt = """
                    Generate a 3-module elite syllabus for learning: '$topic'.
                    Respond ONLY with valid JSON in this exact format:
                    {
                      "topic": "The Topic",
                      "description": "A 1-sentence hype description.",
                      "modules": [
                        {"id": 1, "title": "Module 1 Name"},
                        {"id": 2, "title": "Module 2 Name"},
                        {"id": 3, "title": "Module 3 Name"}
                      ]
                    }
                """.trimIndent()

                val jsonResponse = chatRepository.sendMessage(
                    sessionId = lessonSessionId!!,
                    userContent = prompt,
                    isGhostMode = true, // Hide the JSON generation from the actual chat log if possible
                    isFastMode = true // Use faster model for structure generation
                )

                val cleanedJson = jsonResponse.substringAfter("{").substringBeforeLast("}") + "}"
                val json = JSONObject(cleanedJson)
                
                val modulesArray = json.getJSONArray("modules")
                val modulesList = mutableListOf<Module>()
                for (i in 0 until modulesArray.length()) {
                    val modObj = modulesArray.getJSONObject(i)
                    modulesList.add(Module(id = modObj.getInt("id"), title = modObj.getString("title"), completed = false))
                }

                val syllabus = Syllabus(
                    topic = json.getString("topic"),
                    description = json.getString("description"),
                    modules = modulesList
                )

                _uiState.update { it.copy(activeSyllabus = syllabus, isLoading = false) }

            } catch (e: Exception) {
                Log.e("LessonsViewModel", "Failed to generate syllabus", e)
                _uiState.update { it.copy(isLoading = false, error = "Failed to construct syllabus. Try again, boss.") }
            }
        }
    }

    fun startLesson(moduleId: Int) {
        val syllabus = _uiState.value.activeSyllabus ?: return
        val module = syllabus.modules.find { it.id == moduleId } ?: return
        val sid = lessonSessionId ?: return

        _uiState.update { it.copy(isLoading = true, error = null, currentLessonContent = null) }

        viewModelScope.launch {
            try {
                val prompt = "Teach me Module $moduleId: '${module.title}' for the topic '${syllabus.topic}'. Keep it highly engaging, elite, and use markdown."
                
                val lessonContent = chatRepository.sendMessage(
                    sessionId = sid,
                    userContent = prompt,
                    isGhostMode = false 
                )

                _uiState.update { it.copy(isLoading = false, currentLessonContent = lessonContent) }
                
                // Mark module as complete locally
                val updatedModules = syllabus.modules.map { 
                    if (it.id == moduleId) it.copy(completed = true) else it 
                }
                _uiState.update { it.copy(activeSyllabus = syllabus.copy(modules = updatedModules)) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error loading lesson.") }
            }
        }
    }
    
    fun closeLesson() {
        _uiState.update { it.copy(currentLessonContent = null) }
    }
}
