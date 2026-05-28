package com.ceylonapz.aikeyboard

import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class AIKeyboardService : InputMethodService(),
    LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateCtrl = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateCtrl.savedStateRegistry

    private fun installLifecycleOwner() {
        val decorView = window?.window?.decorView
        decorView?.let {
            it.setViewTreeLifecycleOwner(this)
            it.setViewTreeViewModelStoreOwner(this)
            it.setViewTreeSavedStateRegistryOwner(this)
        }
    }

    private val viewModel by lazy { KeyboardViewModel() }

    override fun onCreate() {
        super.onCreate()
        savedStateCtrl.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        viewModel.vibrator = resolveVibrator()
    }

    private fun readClipboard(): String? {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
        val clip = cm.primaryClip ?: return null
        if (clip.itemCount == 0) return null
        return clip.getItemAt(0)?.coerceToText(this)?.toString()
    }

    private fun resolveVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                    as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onCreateInputView(): View {
        installLifecycleOwner()  // ← called HERE, before ComposeView
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        val composeView = ComposeView(this).apply {

            setContent {
                MaterialTheme(
                    colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
                ) {
                    ComposeKeyboard(
                        viewModel = viewModel,
                        onCommitText = { text ->
                            currentInputConnection?.commitText(text, 1)
                        },
                        onDeleteOne = {
                            currentInputConnection
                                ?.deleteSurroundingText(1, 0)
                        },
                        onSendEnter = {
                            currentInputConnection?.sendKeyEvent(
                                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                            )
                            currentInputConnection?.sendKeyEvent(
                                KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER)
                            )
                        },
                        onDeleteSurrounding = { len ->
                            currentInputConnection
                                ?.deleteSurroundingText(len, 0)
                        },
                        onLanguageSwitch = {
                            (getSystemService(Context.INPUT_METHOD_SERVICE)
                                    as? InputMethodManager)
                                ?.showInputMethodPicker()
                        },
                        readClipboard = { readClipboard() }
                    )
                }
            }
        }
        return composeView
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        super.onDestroy()
    }
}
