package com.android.sitbak.home.news

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sitbak.R
import com.android.sitbak.databinding.ItemNewsBinding
import com.bumptech.glide.Glide

class NewsAdapter(val newsItemInterface: NewsItemInterface, val newsList: MutableList<NewsData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var binding: ItemNewsBinding
    private lateinit var mContext: Context

    interface NewsItemInterface {
        fun onNewsItemClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context

        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_news, parent, false)
        return NewsItem(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NewsItem)
            holder.bind(position)
    }

    override fun getItemCount(): Int = newsList.size

    inner class NewsItem(val binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {

            Glide.with(mContext)
                .load(newsList[position].image_path)
                .into(binding.ivNewsImage)

            binding.tvNewsHeadline.text = newsList[position].title
            binding.tvNewsSubHeadline.text = newsList[position].detail
            binding.tvNewsDate.text = newsList[position].created_at

            binding.newsItem.setOnClickListener {
                newsItemInterface.onNewsItemClicked(position)
            }
        }
    }

}