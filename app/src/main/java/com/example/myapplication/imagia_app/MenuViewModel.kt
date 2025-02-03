package com.example.myapplication.imagia_app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MenuViewModel : ViewModel() {
    private val _isMenuUnlocked = MutableLiveData(false) // Inicialmente bloqueado
    val isMenuUnlocked: LiveData<Boolean> get() = _isMenuUnlocked

    fun unlockMenu() {
        _isMenuUnlocked.value = true
    }
}
