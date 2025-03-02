package com.san_pedrito.myapplication

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils

class ExportPanelManager(private val panel: View) {
    private var isShowing = false
    private lateinit var slideUpAnimation: Animation
    private lateinit var slideDownAnimation: Animation

    init {
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
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                panel.visibility = View.GONE
                isShowing = false
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
        panel.visibility = View.VISIBLE
        panel.startAnimation(slideUpAnimation)
        isShowing = true
    }

    fun hidePanel() {
        panel.startAnimation(slideDownAnimation)
    }
} 