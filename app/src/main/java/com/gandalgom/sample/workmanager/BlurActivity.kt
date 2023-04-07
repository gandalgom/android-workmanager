package com.gandalgom.sample.workmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.WorkInfo

import com.gandalgom.sample.workmanager.databinding.ActivityBlurBinding

class BlurActivity : AppCompatActivity() {

    private val blurViewModel by viewModels<BlurViewModel> {
        BlurViewModel.BlurViewModelFactory(application)
    }
    private lateinit var binding: ActivityBlurBinding

    private val blurLevel: Int
        get() =
            when (binding.radioBlurGroup.checkedRadioButtonId) {
                R.id.radio_blur_lv_1 -> 1
                R.id.radio_blur_lv_2 -> 2
                R.id.radio_blur_lv_3 -> 3
                else -> 1
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlurBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.goButton.setOnClickListener { blurViewModel.applyBlur(blurLevel) }

        binding.seeFileButton.setOnClickListener {
            blurViewModel.outputUri?.let { currentUri ->
                val actionView = Intent(Intent.ACTION_VIEW, currentUri)
                actionView.resolveActivity(packageManager)?.run {
                    startActivity(actionView)
                }
            }
        }

        binding.cancelButton.setOnClickListener { blurViewModel.cancelWork() }

        // Observe work status, added in onCreate()
        blurViewModel.outputWorkInfoList.observe(this, workInfoListObserver())
    }

    private fun workInfoListObserver(): Observer<List<WorkInfo>> {
        return Observer { workInfoList ->
            if (workInfoList.isEmpty()) {
                return@Observer
            }
            val workInfo = workInfoList[0]
            if (workInfo.state.isFinished) {
                showWorkFinished()
                // Normally this processing, which is not directly related to drawing views on
                // screen would be in the ViewModel. For simplicity we are keeping it here.
                val outputImageUri = workInfo.outputData.getString(KEY_IMAGE_URI)

                // If there is an output file show "See File" button
                if (!outputImageUri.isNullOrEmpty()) {
                    blurViewModel.setOutputUri(outputImageUri)
                    binding.seeFileButton.visibility = View.VISIBLE
                }
            } else {
                showWorkInProgress()
            }
        }
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private fun showWorkInProgress() {
        with(binding) {
            progressBar.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
            goButton.visibility = View.GONE
            seeFileButton.visibility = View.GONE
        }
    }

    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private fun showWorkFinished() {
        with(binding) {
            progressBar.visibility = View.GONE
            cancelButton.visibility = View.GONE
            goButton.visibility = View.VISIBLE
        }
    }
}