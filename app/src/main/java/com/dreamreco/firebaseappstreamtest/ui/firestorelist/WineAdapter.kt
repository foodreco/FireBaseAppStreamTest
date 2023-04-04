package com.dreamreco.firebaseappstreamtest.ui.firestorelist

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dreamreco.firebaseappstreamtest.ui.firestorefts.FireStoreAdapter
import com.dreamreco.firebaseappstreamtest.databinding.ListFragmentChildBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

/**
 * RecyclerView adapter for a list of Restaurants.
 */
open class WineAdapter(query: Query?, private val listener: OnRestaurantSelectedListener) :
        FireStoreAdapter<WineAdapter.ViewHolder>(query) {

    interface OnRestaurantSelectedListener {

//        fun onRestaurantSelected(wine: Wine)
        fun onRestaurantSelected(wine: CustomDrink)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ListFragmentChildBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }

    inner class ViewHolder(val binding: ListFragmentChildBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnRestaurantSelectedListener?
        ) {

//            val wine = snapshot.toObject<Wine>()
            val wine = snapshot.toObject<CustomDrink>()

            Log.e("WineAdapter","snapshot : $snapshot")
            Log.e("WineAdapter","wine : $wine")

            if (wine == null) {
                return
            }

            val resources = binding.root.resources

//            // Load image
//            Glide.with(binding.restaurantItemImage.context)
//                    .load(restaurant.photo)
//                    .into(binding.restaurantItemImage)

            binding.diaryTitle.text = wine.productName
            binding.diaryContent.text = wine.type
            binding.diaryDrinkType.text = wine.degree.toString()

            // Click listener
            binding.root.setOnClickListener {
                listener?.onRestaurantSelected(wine)
            }
        }
    }
}
