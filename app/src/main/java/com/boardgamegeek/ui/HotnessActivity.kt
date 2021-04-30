package com.boardgamegeek.ui

import android.view.MenuItem
import com.boardgamegeek.R
import org.jetbrains.anko.intentFor


class HotnessActivity : TopLevelSinglePaneActivity() {
    override val answersContentType = "Hotness"

    override fun onCreatePane() = HotnessFragment()

    override val navigationItemId = R.id.hotness

    override val optionsMenuId = R.menu.search

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_search -> startActivity(intentFor<SearchResultsActivity>())
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
