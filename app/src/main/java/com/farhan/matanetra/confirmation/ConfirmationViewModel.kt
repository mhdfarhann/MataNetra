package com.farhan.matanetra.confirmation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.farhan.matanetra.api.ApiService
import com.farhan.matanetra.response.Route
import com.farhan.matanetra.response.RoutesResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConfirmationViewModel : ViewModel() {

    private val _selectedRoute = MutableLiveData<Route>()
    val selectedRoute: LiveData<Route> get() = _selectedRoute

    fun fetchData(apiService: ApiService) {
        val call = apiService.getRoutes()

        call.enqueue(object : Callback<RoutesResponse> {
            override fun onResponse(call: Call<RoutesResponse>, response: Response<RoutesResponse>) {
                if (response.isSuccessful) {
                    val routesResponse = response.body()
                    routesResponse?.let {
                        val routes = it.routes
                        if (routes.isNotEmpty()) {
                            // Select a random route
                            val randomRoute = routes.random()
                            _selectedRoute.postValue(randomRoute)
                        }
                    }
                } else {
                    // Handle error
                }
            }

            override fun onFailure(call: Call<RoutesResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }
}