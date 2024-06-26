package com.michaldrabik.ui_premium

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.extensions.bump
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.PremiumFeature
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ITEM
import com.michaldrabik.ui_premium.databinding.FragmentPremiumBinding
import com.michaldrabik.ui_premium.views.PurchaseItemView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PremiumFragment : BaseFragment<PremiumViewModel>(R.layout.fragment_premium) {

  override val viewModel by viewModels<PremiumViewModel>()
  private val binding by viewBinding(FragmentPremiumBinding::bind)

  private val highlightItem by lazy { arguments?.getSerializable(ARG_ITEM) as? PremiumFeature }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.messageFlow.collect { showSnack(it) } },
      { viewModel.eventFlow.collect { handleEvent(it) } },
      doAfterLaunch = {
        highlightItem?.let { viewModel.highlightItem(it) }
      }
    )
  }

  private fun setupView() {
    with(binding) {
      premiumToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
      premiumRoot.doOnApplyWindowInsets { view, insets, padding, _ ->
        val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
        view.updatePadding(top = padding.top + inset)
      }
    }
  }

  private fun render(uiState: PremiumUiState) {
    uiState.run {
      with(binding) {
        premiumProgress.visibleIf(isLoading)
        premiumStatus.visibleIf(isPurchasePending)
        renderPurchaseItems(isLoading)
        onFinish?.let {
          it.consume()?.let {
            requireActivity().onBackPressed()
          }
        }
      }
    }
  }

  private fun renderPurchaseItems(isLoading: Boolean) {
    with(binding) {
      premiumPurchaseItems.removeAllViews()
      premiumPurchaseItems.visibleIf(!isLoading)

      val view = PurchaseItemView(requireContext()).apply {
        bindInApp("Single Payment", "$0.00")
        onClick {
          //viewModel.unlockAndFinish()
          viewModel.sendErrorMessage()
        }
      }
      premiumPurchaseItems.addView(view)
    }
  }

  private fun handleEvent(event: Event<*>) {
    if (event is PremiumUiEvent.HighlightItem) {
      highlightItem(event.item)
    }
  }

  private fun highlightItem(item: PremiumFeature) {
    with(binding) {
      val scrollBounds = Rect()
      premiumRoot.getHitRect(scrollBounds)

      val targetTag = getString(item.tag)
      val targetViews = premiumContent.children.filter { it.tag == targetTag }

      if (targetViews.any { !it.getLocalVisibleRect(scrollBounds) }) {
        premiumRoot.smoothScrollTo(0, Int.MAX_VALUE)
      }

      targetViews.forEach {
        it.bump(duration = 450, startDelay = 300)
      }
    }
  }
}
