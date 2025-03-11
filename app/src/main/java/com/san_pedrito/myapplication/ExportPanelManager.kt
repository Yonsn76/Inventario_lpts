package com.san_pedrito.myapplication

import android.view.View
import android.util.Log
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils

class ExportPanelManager(private val panel: View) {
    private var isShowing = false
    private lateinit var slideUpAnimation: Animation
    private lateinit var slideDownAnimation: Animation

    init {
        // Guardar el padre original del panel para poder restaurarlo
        panel.tag = panel.parent
        setupAnimations()
        setupListeners()
    }

    private fun setupAnimations() {
        val context = panel.context
        val packageName = context.packageName
        val slideUpId = context.resources.getIdentifier("anim_subir", "anim", packageName)
        val slideDownId = context.resources.getIdentifier("anim_bajar", "anim", packageName)
        
        slideUpAnimation = AnimationUtils.loadAnimation(context, slideUpId)
        slideDownAnimation = AnimationUtils.loadAnimation(context, slideDownId)

        slideDownAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // Deshabilitar interacción inmediatamente cuando comienza a cerrarse
                panel.isClickable = false
            }
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                try {
                    panel.visibility = View.GONE
                    isShowing = false
                    panel.z = -1f

                } catch (e: Exception) {
                    Log.e("ExportPanelManager", "Error panel: ${e.message}")
                }
            }
        })
    }

    private fun setupListeners() {
        // Configurar el botón de cierre
        panel.findViewById<View>(R.id.btnClose)?.setOnClickListener {
            hidePanel()
        }
    }

    fun togglePanel() {
        if (isShowing) {
            hidePanel()
        } else {
            showPanel()
        }
    }

    private fun showPanel() {
        // Asegurarse de que el panel esté en su contenedor si fue removido
        if (panel.parent == null) {
            (panel.tag as? ViewGroup)?.addView(panel)
        }
        panel.z = 1f  // Asegurar que esté por encima
        panel.isClickable = true
        panel.isEnabled = true
        panel.visibility = View.VISIBLE
        panel.startAnimation(slideUpAnimation)
        isShowing = true
    }

    fun hidePanel() {
        panel.startAnimation(slideDownAnimation)
    }

    fun isPanelShowing(): Boolean {
        return isShowing
    }
}