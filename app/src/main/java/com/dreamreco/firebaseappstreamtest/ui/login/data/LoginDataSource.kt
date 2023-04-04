package com.dreamreco.firebaseappstreamtest.ui.login.data

import android.util.Log
import com.dreamreco.firebaseappstreamtest.ui.login.data.model.LoggedInUser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.IOException
import javax.inject.Singleton

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */

object LoginDataSource {

    fun login(username: String, password: String): Result<LoggedInUser> {
        try {
            // TODO: handle loggedInUser authentication
            val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
            return Result.Success(fakeUser)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
        Log.e("LoginDataSource", "logout 작동됨")
    }
}