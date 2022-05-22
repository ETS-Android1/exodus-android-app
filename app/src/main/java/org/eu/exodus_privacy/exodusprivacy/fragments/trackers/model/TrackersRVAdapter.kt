package org.eu.exodus_privacy.exodusprivacy.fragments.trackers.model

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.RecyclerViewTrackerItemBinding
import org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.AppDetailFragmentDirections
import org.eu.exodus_privacy.exodusprivacy.fragments.trackers.TrackersFragmentDirections
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData

class TrackersRVAdapter(
    private val showSuggestions: Boolean,
    private val currentDestinationId: Int,
) :
    ListAdapter<TrackerData, TrackersRVAdapter.ViewHolder>(TrackersDiffUtil()) {

    inner class ViewHolder(val binding: RecyclerViewTrackerItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerViewTrackerItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val trackerApps = mutableSetOf<String>().apply {
            currentList.forEach { this.addAll(it.exodusApplications) }
        }
        var trackerPercentage = 0F
        val app = getItem(position)

        // Fix padding for TrackersFragment
        if (!showSuggestions) {
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            val horMargin = convertPXToDP(20F, context).toInt()
            val verMargin = convertPXToDP(10F, context).toInt()
            params.setMargins(horMargin, verMargin, horMargin, verMargin)
            holder.itemView.layoutParams = params
        }

        holder.binding.apply {
            trackerTitleTV.text = app.name
            if (showSuggestions) {
                chipGroup.visibility = View.VISIBLE
                trackersPB.visibility = View.GONE
                chipGroup.removeAllViews()
                app.categories.forEach {
                    val chip = Chip(context)
                    val chipStyle = ChipDrawable.createFromAttributes(
                        context,
                        null,
                        0,
                        R.style.Theme_Exodus_Chip
                    )
                    chip.text = it
                    chip.setChipDrawable(chipStyle)
                    chipGroup.addView(chip)
                }
            } else {
                trackerPercentage = (app.exodusApplications.size / trackerApps.size.toFloat()) * 100
                trackersStatusTV.text =
                    context.getString(
                        R.string.trackers_status,
                        trackerPercentage.toInt(),
                        app.exodusApplications.size
                    )
                trackersPB.afterMeasured {
                    val newWidth = (width * trackerPercentage) / 100
                    val params = layoutParams.apply {
                        width = newWidth.toInt()
                    }
                    layoutParams = params
                }
            }
            root.setOnClickListener {
                val action = if (currentDestinationId == R.id.appDetailFragment) {
                    AppDetailFragmentDirections.actionAppDetailFragmentToTrackerDetailFragment(
                        app.id,
                        trackerPercentage.toInt()
                    )
                } else {
                    TrackersFragmentDirections.actionTrackersFragmentToTrackerDetailFragment(
                        app.id,
                        trackerPercentage.toInt()
                    )
                }
                holder.itemView.findNavController().navigate(action)
            }
        }
    }

    private fun convertPXToDP(pixels: Float, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            pixels,
            context.resources.displayMetrics
        )
    }

    private inline fun ProgressBar.afterMeasured(crossinline f: ProgressBar.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (measuredWidth > 0 && measuredHeight > 0) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        f()
                    }
                }
            })
    }
}
