package com.androiddevs.mvvmnewsapp.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.mvvmnewsapp.model.Article
import com.androiddevs.mvvmnewsapp.model.NewsResponse
import com.androiddevs.mvvmnewsapp.repository.NewsRepository
import com.androiddevs.mvvmnewsapp.ui.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(
    private val newsRepository: NewsRepository
): ViewModel() {
    val breakingNews : MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponse : NewsResponse? = null

    val searchNews : MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse : NewsResponse? = null

    init {
        getBreakingNews("us")
    }

    fun getBreakingNews(countryCode : String) = viewModelScope.launch {
        breakingNews.postValue(Resource.Loading())
        val response = newsRepository.getBreakingNews(countryCode, breakingNewsPage)
        breakingNews.postValue(handleBreakingNewsResponse(response))
    }

    fun searchNews(searchQuery : String) = viewModelScope.launch {
        searchNews.postValue(Resource.Loading())
        val response = newsRepository.searchNews(searchQuery,searchNewsPage)
        searchNews.postValue(handleSearchNewsResponse(response))
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>) : Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let { resultResponse ->
                breakingNewsPage++
                if(breakingNewsResponse == null){
                    breakingNewsResponse = resultResponse
                }else{
                    val oldArticle = breakingNewsResponse?.articles
                    val newsArticle = resultResponse.articles
                    oldArticle?.addAll(newsArticle)
                }
                return Resource.Success(breakingNewsResponse?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>) : Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if(searchNewsResponse == null){
                    searchNewsResponse = resultResponse
                }else{
                    val oldArticle = searchNewsResponse?.articles
                    val newsArticle = resultResponse.articles
                    oldArticle?.addAll(newsArticle)
                }
                return Resource.Success(searchNewsResponse?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun savedArticle(article : Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }
     fun getSavedNews() = newsRepository.getSavedNews()

    fun deleterArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

}