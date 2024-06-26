package com.michaldrabik.ui_base.common.views.tips

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.databinding.ViewTipOverlayBinding
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.screenHeight
import com.michaldrabik.ui_model.Tip

class TipOverlayView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewTipOverlayBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    setBackgroundResource(R.color.colorBlackTranslucent)
    setupView()
  }

  private val springStartValue by lazy { (screenHeight().toFloat()) / 3F }
  private val springAnimation by lazy {
    SpringAnimation(binding.tutorialTipView, DynamicAnimation.TRANSLATION_Y, 0F).apply {
      spring.stiffness = 300F
      spring.dampingRatio = 0.65F
      setStartValue(springStartValue)
    }
  }

  private fun setupView() {
    onClick { /* Block background clicks */ }
    binding.tutorialViewButton.onClick { fadeOut() }
  }

  fun showTip(tip: Tip) {
    binding.tutorialViewText.setText(tip.textResId)
    springAnimation.setStartValue(springStartValue)
    springAnimation.start()
    fadeIn()
  }
}
