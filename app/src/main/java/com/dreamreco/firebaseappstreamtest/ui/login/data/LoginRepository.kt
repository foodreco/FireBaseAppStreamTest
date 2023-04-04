package com.dreamreco.firebaseappstreamtest.ui.login.data

import com.dreamreco.firebaseappstreamtest.room.Database
import com.dreamreco.firebaseappstreamtest.ui.login.data.model.LoggedInUser
import javax.inject.Inject

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

//class LoginRepository @Inject constructor(private val dataSource: LoginDataSource) {

class LoginRepository @Inject constructor(private val database: Database) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
//        dataSource.logout()
        LoginDataSource.logout()
    }

    fun login(username: String, password: String): Result<LoggedInUser> {
        // handle login
//        val result = dataSource.login(username, password)
        val result = LoginDataSource.login(username, password)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }

        return result
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}