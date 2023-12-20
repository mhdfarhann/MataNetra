package com.farhan.matanetra.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.farhan.matanetra.api.Config
import com.farhan.matanetra.response.SpeechToDestinationResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MainViewModel : ViewModel() {

    var destinationTitleCallback: ((String) -> Unit)? = null


    fun uploadAudioToApi(audioFilePath: String?, uploadCallback: (Boolean, String) -> Unit) {
        try {
            if (audioFilePath != null) {
                val audioFile = File(audioFilePath)
                val requestBody: RequestBody = audioFile.asRequestBody("audio/wav".toMediaTypeOrNull())
                val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, requestBody)

                val apiService = Config.apiService
                apiService.uploadAudio(audioPart)
                    .enqueue(object : Callback<SpeechToDestinationResponse> {
                        override fun onResponse(
                            call: Call<SpeechToDestinationResponse>,
                            response: Response<SpeechToDestinationResponse>
                        ) {
                            if (response.isSuccessful) {
                                val title = response.body()?.destination?.title ?: ""

                                destinationTitleCallback?.invoke(title)

                                // Pass the destination ID and title to the callback
                                uploadCallback.invoke(true, title)
                            } else {
                                handleApiFailure(Exception("API call failed"), uploadCallback)
                            }
                        }

                        override fun onFailure(call: Call<SpeechToDestinationResponse>, t: Throwable) {
                            handleApiFailure(t, uploadCallback)
                        }
                    })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            uploadCallback.invoke(false, "")
        }
    }

    private fun handleApiFailure(throwable: Throwable, uploadCallback: (Boolean, String) -> Unit) {
        // Handle API failure here
        // You can inform the callback about the failure
        uploadCallback.invoke(false, "")
    }
}