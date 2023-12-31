package com.farhan.matanetra.tools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import com.farhan.matanetra.main.MainViewModel
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class WavConvert(private val context: Context, private val path: String) {
    private var filePath: String? = null
    private var tempRawFile = "temp_record.raw"
    private var tempWavFile = "audio_record.wav"
    private val bpp = 16
    private var sampleRate = 44100
    private var channel = AudioFormat.CHANNEL_IN_STEREO
    private var audioEncoding = AudioFormat.ENCODING_PCM_16BIT
    private var recorder: AudioRecord? = null
    private var bufferSize = 0
    private var recordingThread: Thread? = null
    private var isRecording = false

    private val mainViewModel = MainViewModel()

    init {
        try {
            filePath = path
            bufferSize = AudioRecord.getMinBufferSize(
                8000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getPath(name: String): String? {
        return try {
            "$filePath/$name"
        } catch (e: Exception) {
            null
        }
    }

    private fun writeRawData() {
        try {
            if (filePath != null) {
                val data = ByteArray(bufferSize)
                val path = getPath(tempRawFile)
                val fileOutputStream = FileOutputStream(path)
                var read: Int
                while (isRecording) {
                    read = recorder!!.read(data, 0, bufferSize)
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            fileOutputStream.write(data)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
                fileOutputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun wavHeader(
        fileOutputStream: FileOutputStream,
        totalAudioLen: Long,
        totalDataLen: Long,
        channels: Int,
        byteRate: Long
    ) {
        try {
            val header = ByteArray(44)
            header[0] = 'R'.code.toByte() // RIFF/WAVE header
            header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte()
            header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xffL).toByte()
            header[5] = (totalDataLen shr 8 and 0xffL).toByte()
            header[6] = (totalDataLen shr 16 and 0xffL).toByte()
            header[7] = (totalDataLen shr 24 and 0xffL).toByte()
            header[8] = 'W'.code.toByte()
            header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte()
            header[11] = 'E'.code.toByte()
            header[12] = 'f'.code.toByte() // 'fmt ' chunk
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()
            header[16] = 16 // 4 bytes: size of 'fmt ' chunk
            header[17] = 0
            header[18] = 0
            header[19] = 0
            header[20] = 1 // format = 1
            header[21] = 0
            header[22] = channels.toByte()
            header[23] = 0
            header[24] = (sampleRate.toLong() and 0xffL).toByte()
            header[25] = (sampleRate.toLong() shr 8 and 0xffL).toByte()
            header[26] = (sampleRate.toLong() shr 16 and 0xffL).toByte()
            header[27] = (sampleRate.toLong() shr 24 and 0xffL).toByte()
            header[28] = (byteRate and 0xffL).toByte()
            header[29] = (byteRate shr 8 and 0xffL).toByte()
            header[30] = (byteRate shr 16 and 0xffL).toByte()
            header[31] = (byteRate shr 24 and 0xffL).toByte()
            header[32] = (2 * 16 / 8).toByte() // block align
            header[33] = 0
            header[34] = bpp.toByte() // bits per sample
            header[35] = 0
            header[36] = 'd'.code.toByte()
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()
            header[40] = (totalAudioLen and 0xffL).toByte()
            header[41] = (totalAudioLen shr 8 and 0xffL).toByte()
            header[42] = (totalAudioLen shr 16 and 0xffL).toByte()
            header[43] = (totalAudioLen shr 24 and 0xffL).toByte()
            fileOutputStream.write(header, 0, 44)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createWavFile(tempPath: String?, wavPath: String?) {
        try {
            val fileInputStream = FileInputStream(tempPath)
            val fileOutputStream = FileOutputStream(wavPath)
            val data = ByteArray(bufferSize)
            val channels = 2
            val byteRate = (bpp * sampleRate * channels / 8).toLong()
            val totalAudioLen = fileInputStream.channel.size()
            val totalDataLen = totalAudioLen + 36
            wavHeader(fileOutputStream, totalAudioLen, totalDataLen, channels, byteRate)
            while (fileInputStream.read(data) != -1) {
                fileOutputStream.write(data)
            }
            fileInputStream.close()
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startRecording() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channel,
                audioEncoding,
                bufferSize
            )
            val status = recorder!!.state
            if (status == 1) {
                recorder!!.startRecording()
                isRecording = true
            }
            recordingThread = Thread { writeRawData() }
            recordingThread!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRecording(uploadCallback: (Boolean, String) -> Unit) {
        try {
            if (recorder != null) {
                isRecording = false
                val status = recorder!!.state
                if (status == 1) {
                    recorder!!.stop()
                }
                recorder!!.release()
                recordingThread = null

                val tempRawFilePath = getPath(tempRawFile)
                val tempWavFilePath = getPath(tempWavFile)

                createWavFile(tempRawFilePath, tempWavFilePath)

                // Upload the WAV file to the API service
                mainViewModel.uploadAudioToApi(tempWavFilePath, uploadCallback)

            }
        } catch (e: Exception) {
            e.printStackTrace()
            uploadCallback.invoke(false,"" )
        }
    }
}