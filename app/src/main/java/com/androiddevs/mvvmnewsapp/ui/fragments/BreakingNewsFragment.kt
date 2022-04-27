package com.androiddevs.mvvmnewsapp.ui.fragments

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.adapter.NewsAdapter
import com.androiddevs.mvvmnewsapp.db.ArticleDatabase
import com.androiddevs.mvvmnewsapp.model.Article
import com.androiddevs.mvvmnewsapp.repository.NewsRepository
import com.androiddevs.mvvmnewsapp.ui.NewsActivity
import com.androiddevs.mvvmnewsapp.ui.NewsViewModel
import com.androiddevs.mvvmnewsapp.ui.NewsViewModelProviderFactory
import com.androiddevs.mvvmnewsapp.ui.util.Constans.Companion.QUERY_PAGE_SIZE
import com.androiddevs.mvvmnewsapp.ui.util.Resource
import kotlinx.android.synthetic.main.fragment_breaking_news.*
import kotlinx.android.synthetic.main.fragment_breaking_news.paginationProgressBar
import kotlinx.android.synthetic.main.fragment_search_news.*

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {
    val viewModel: NewsViewModel by activityViewModels {
        NewsViewModelProviderFactory(context?.applicationContext as Application,NewsRepository(ArticleDatabase(requireContext())))
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
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.breakingNewsPage == totalPages
                        if(isLastPage){
                            rvBreakingNews.setPadding(0,0,0,0)
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgessBar()
                    response.message?.let { message ->
                        Toast.makeText(activity,"An error occured : $message",Toast.LENGTH_LONG).show()
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
        isLoading = false
    }

    private fun showProgessBar() {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    var scrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotBegining = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotBegining &&
                    isTotalMoreThanVisible && isScrolling

            if(shouldPaginate){
                viewModel.getBreakingNews("us")
                isLoading = false
            }
        }
    }

    private fun setupRecycleView() {
        newsAdapter = NewsAdapter()
        rvBreakingNews.apply {

            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }
    }


}