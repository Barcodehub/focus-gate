package com.example.focusgate.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusgate.domain.model.Task
import com.example.focusgate.domain.usecase.CompleteTaskUseCase
import com.example.focusgate.domain.usecase.CreateTaskUseCase
import com.example.focusgate.domain.usecase.DeleteTaskUseCase
import com.example.focusgate.domain.usecase.ObserveTasksUseCase
import com.example.focusgate.domain.usecase.ResetTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    observeTasksUseCase: ObserveTasksUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val resetTaskUseCase: ResetTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = observeTasksUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createTask(title: String, points: Int, description: String?) {
        viewModelScope.launch { createTaskUseCase(title, points, description) }
    }

    fun complete(task: Task) {
        viewModelScope.launch { completeTaskUseCase(task.id) }
    }

    fun reset(task: Task) {
        viewModelScope.launch { resetTaskUseCase(task.id) }
    }

    fun delete(task: Task) {
        viewModelScope.launch { deleteTaskUseCase(task.id) }
    }
}