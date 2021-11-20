package com.boardgamegeek.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.boardgamegeek.R
import com.boardgamegeek.extensions.toast
import com.boardgamegeek.ui.viewmodel.NewPlayViewModel
import kotlinx.android.synthetic.main.fragment_new_play_comments.*

class NewPlayCommentsFragment : Fragment(R.layout.fragment_new_play_comments) {
    private val viewModel by activityViewModels<NewPlayViewModel>()

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.setSubtitle(R.string.title_comments)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doneButton.setOnClickListener {
            viewModel.setComments(commentsView.text.toString())
            viewModel.save()
            toast(R.string.msg_logging_play)
        }
    }
}
