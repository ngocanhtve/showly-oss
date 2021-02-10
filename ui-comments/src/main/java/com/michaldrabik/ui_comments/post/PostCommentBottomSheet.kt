package com.michaldrabik.ui_comments.post

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_comments.R
import com.michaldrabik.ui_comments.post.di.UiPostCommentComponentProvider
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_COMMENT
import kotlinx.android.synthetic.main.view_post_comment.*
import kotlinx.android.synthetic.main.view_post_comment.view.*

class PostCommentBottomSheet : BaseBottomSheetFragment<PostCommentViewModel>() {

  private val showTraktId by lazy { IdTrakt(requireArguments().getLong(ARG_SHOW_ID)) }
  private val movieTraktId by lazy { IdTrakt(requireArguments().getLong(ARG_MOVIE_ID)) }

  override val layoutResId = R.layout.view_post_comment

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiPostCommentComponentProvider).providePostCommentComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(PostCommentViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it) })
    }
    setupView(view)
  }

  private fun setupView(view: View) {
    view.run {
      viewPostCommentInputValue.doOnTextChanged { text, _, _, _ ->
        val isValid =
          !text?.trim().isNullOrEmpty() &&
            (text?.trim()?.split(" ")?.size ?: 0) >= 5
        viewPostCommentButton.isEnabled = isValid
      }
      viewPostCommentButton.onClick {
        val commentText = viewPostCommentInputValue.text.toString()
        val isSpoiler = viewPostCommentSpoilersCheck.isChecked
        when {
          showTraktId.id > 0 -> viewModel.postShowComment(showTraktId, commentText, isSpoiler)
          movieTraktId.id > 0 -> viewModel.postMovieComment(movieTraktId, commentText, isSpoiler)
          else -> error("Invalid comment target.")
        }
      }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiModel: PostCommentUiModel) {
    uiModel.run {
      isLoading?.let {
        viewPostCommentButton.isEnabled = !it
        viewPostCommentInput.isEnabled = !it
        viewPostCommentInputValue.isEnabled = !it
        viewPostCommentSpoilersCheck.isEnabled = !it
      }
      successEvent?.let {
        it.consume()?.let {
          setFragmentResult(REQUEST_COMMENT, bundleOf())
          dismiss()
        }
      }
    }
  }
}
