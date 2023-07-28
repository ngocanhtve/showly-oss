package com.michaldrabik.ui_people.details.recycler.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.michaldrabik.ui_base.utilities.extensions.copyToClipboard
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_people.R
import com.michaldrabik.ui_people.databinding.ViewPersonDetailsBioBinding
import com.michaldrabik.ui_people.details.recycler.PersonDetailsItem

class PersonDetailsBioView : FrameLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewPersonDetailsBioBinding.inflate(LayoutInflater.from(context), this)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    with(binding) {
      personBioText.setInitialLines(5)
      personBioText.onLongClick {
        context.copyToClipboard(personBioText.text.toString())
        snackbarLayout.showInfoSnackbar(context.getString(R.string.textCopiedToClipboard), length = 1250)
      }
    }
  }

  fun bind(item: PersonDetailsItem.MainBio) {
    with(binding) {
      when {
        item.biography.isNullOrBlank() -> personBioText.text = context.getString(R.string.textNoDescription)
        !item.biographyTranslation.isNullOrBlank() -> personBioText.text = item.biographyTranslation
        else -> personBioText.text = item.biography
      }
    }
  }
}
