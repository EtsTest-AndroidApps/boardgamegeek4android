package com.boardgamegeek.pref

import android.content.Context
import android.content.Intent
import androidx.preference.Preference
import android.util.AttributeSet

import com.boardgamegeek.auth.AccountUtils
import com.boardgamegeek.auth.Authenticator
import com.boardgamegeek.extensions.get
import com.boardgamegeek.extensions.preferences
import com.boardgamegeek.ui.LoginActivity

class LoginPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
    private var username = context.preferences()[AccountUtils.KEY_USERNAME, ""]

    init {
        intent = Intent(getContext(), LoginActivity::class.java)
    }

    override fun isEnabled(): Boolean {
        return !Authenticator.isSignedIn(context)
    }

    override fun getSummary(): CharSequence? {
        return username
    }

    fun update(username: String) {
        this.username = username
        notifyChanged()
    }
}
