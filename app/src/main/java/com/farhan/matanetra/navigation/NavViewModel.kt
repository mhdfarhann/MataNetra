package com.farhan.matanetra.navigation

import androidx.lifecycle.ViewModel
import com.farhan.matanetra.api.Config
import com.farhan.matanetra.response.Destination
import com.farhan.matanetra.response.ShortestPathRequest
import com.farhan.matanetra.response.ShortestPathResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NavViewModel : ViewModel() {

    fun getShortestPath(source: String, target: String, callback: (List<Destination>) -> Unit) {
        val request = ShortestPathRequest(source, target)
        Config.apiService.getShortestPath(request)
            .enqueue(object : Callback<ShortestPathResponse> {
                override fun onResponse(
                    call: Call<ShortestPathResponse>,
                    response: Response<ShortestPathResponse>
                ) {
                    if (response.isSuccessful) {
                        val path = response.body()?.path ?: emptyList()
                        callback.invoke(path)
                    } else {
                        // Handle API failure
                        callback.invoke(emptyList()) // or handle failure accordingly
                    }
                }

                override fun onFailure(call: Call<ShortestPathResponse>, t: Throwable) {
                    // Handle API failure
                    callback.invoke(emptyList()) // or handle failure accordingly
                }
            })
    }


}

