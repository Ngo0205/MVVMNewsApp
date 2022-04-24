package com.androiddevs.mvvmnewsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.adapter.NewsAdapter
import com.androiddevs.mvvmnewsapp.db.ArticleDatabase
import com.androiddevs.mvvmnewsapp.model.Article
import com.androiddevs.mvvmnewsapp.repository.NewsRepository
import com.androiddevs.mvvmnewsapp.ui.NewsActivity
import com.androiddevs.mvvmnewsapp.ui.NewsViewModel
import com.androiddevs.mvvmnewsapp.ui.NewsViewModelProviderFactory
import com.androiddevs.mvvmnewsapp.ui.util.Resource
import kotlinx.android.synthetic.main.fragment_breaking_news.*

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {

    val viewModel: NewsViewModel by activityViewModels {
        NewsViewModelProviderFactory(NewsRepository(ArticleDatabase(requireContext())))
    }
    lateinit var newsAdapter: NewsAdapter
    val TAG = "BreakingNewsTag"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupRecycleView()

        newsAdapter.setOnItemClickListener {
            findNavController().navigate(
                BreakingNewsFragmentDirections.actionBreakingNewsFragmentToArticleFragment(it)
            )
        }

        viewModel.breakingNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgessBar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles)
                    }
                }

                is Resource.Error -> {
                    hideProgessBar()
                    response.message?.let { message ->
                        Log.e(TAG, "an error: $message")
                    }
                }
                is Resource.Loading -> {
                    showProgessBar()
                }
            }
        })
    }


    private fun hideProgessBar() {
        paginationProgressBar.visibility = View.INVISIBLE
    }

    private fun showProgessBar() {
        paginationProgressBar.visibility = View.VISIBLE
    }

    private fun setupRecycleView() {
        newsAdapter = NewsAdapter()
        rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }


}