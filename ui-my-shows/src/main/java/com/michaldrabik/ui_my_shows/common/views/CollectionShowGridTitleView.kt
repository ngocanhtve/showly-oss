package com.michaldrabik.ui_my_shows.common.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.views.ShowView
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.isTablet
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.onLongClick
import com.michaldrabik.ui_base.utilities.extensions.screenWidth
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_shows.common.recycler.CollectionListItem
import com.michaldrabik.ui_my_shows.databinding.ViewCollectionShowGridTitleBinding
import java.util.Locale

@SuppressLint("SetTextI18n")
class CollectionShowGridTitleView : ShowView<CollectionListItem.ShowItem> {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val binding = ViewCollectionShowGridTitleBinding.inflate(LayoutInflater.from(context), this)

  private val width by lazy {
    val span = if (context.isTablet()) Config.LISTS_GRID_SPAN_TABLET else Config.LISTS_GRID_SPAN
    val itemSpacing = context.dimenToPx(R.dimen.spaceSmall)
    val screenMargin = context.dimenToPx(R.dimen.screenMarginHorizontal)
    val screenWidth = screenWidth().toFloat()
    ((screenWidth - (screenMargin * 2.0)) - ((span - 1) * itemSpacing)) / span
  }
  private val height by lazy { width * 1.7305 }

  init {
    layoutParams = LayoutParams(width.toInt(), height.toInt())

    clipChildren = false
    clipToPadding = false

    with(binding) {
      collectionShowRoot.onClick { itemClickListener?.invoke(item) }
      collectionShowRoot.onLongClick { itemLongClickListener?.invoke(item) }
    }

    imageLoadCompleteListener = { loadTranslation() }
  }

  override val imageView: ImageView = binding.collectionShowImage
  override val placeholderView: ImageView = binding.collectionShowPlaceholder

  private lateinit var item: CollectionListItem.ShowItem

  override fun bind(item: CollectionListItem.ShowItem) {
    clear()
    this.item = item

    with(binding) {
      collectionShowProgress.visibleIf(item.isLoading)

      collectionShowTitle.text =
        if (item.translation?.title.isNullOrBlank()) item.show.title
        else item.translation?.title

      if (item.sortOrder == SortOrder.RATING) {
        bindRating(item)
      } else if (item.sortOrder == SortOrder.USER_RATING && item.userRating != null) {
        collectionShowRating.visible()
        collectionShowRating.text = String.format(Locale.ENGLISH, "%d", item.userRating)
      } else {
        collectionShowRating.gone()
      }
    }

    loadImage(item)
  }

  private fun bindRating(item: CollectionListItem.ShowItem) {
    with(binding) {
      var rating = String.format(Locale.ENGLISH, "%.1f", item.show.rating)

      if (item.spoilers.isSpoilerRatingsHidden) {
        collectionShowRating.tag = rating
        rating = Config.SPOILERS_RATINGS_HIDE_SYMBOL

        if (item.spoilers.isSpoilerTapToReveal) {
          with(collectionShowRating) {
            onClick {
              tag?.let { text = it.toString() }
              isClickable = false
            }
          }
        }
      }

      collectionShowRating.visible()
      collectionShowRating.text = rating
    }
  }

  private fun loadTranslation() {
    if (item.translation == null) {
      missingTranslationListener?.invoke(item)
    }
  }

  private fun clear() {
    with(binding) {
      collectionShowTitle.text = ""
      collectionShowPlaceholder.gone()
      Glide.with(this@CollectionShowGridTitleView).clear(collectionShowImage)
    }
  }
}
