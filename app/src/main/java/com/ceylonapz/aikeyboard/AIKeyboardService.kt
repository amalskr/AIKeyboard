package com.ceylonapz.aikeyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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
    }

    override fun onCreateInputView(): View {
        installLifecycleOwner()  // ← called HERE, before ComposeView
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        val composeView = ComposeView(this).apply {

            setContent {
                MaterialTheme(
                    colorScheme = darkColorScheme()
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
                                KeyEvent(
                                    KeyEvent.ACTION_DOWN,
                                    KeyEvent.KEYCODE_ENTER
                                )
                            )
                        },
                        onDeleteSurrounding = { len ->
                            currentInputConnection
                                ?.deleteSurroundingText(len, 0)
                        }
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
