package com.scanner.app.tracking.data

import com.scanner.app.tracking.data.model.LoggedInUser
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(username: String, password: String): Result<LoggedInUser> {
        try {
            // TODO: handle loggedInUser authentication

                if(username.equals("admin@gmail.com") || username.equals("guest@gmail.com")){
                    if(password == "test@123") {
                        val loggedInUser =
                            LoggedInUser(java.util.UUID.randomUUID().toString(), username)
                        return Result.Success(loggedInUser)
                    }else{
                        return Result.Error(Exception("Please Enter valid username and password"))
                    }
                } else{
                    return Result.Error(Exception("Please Enter valid username and password"))
                }

            /*val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
            return Result.Success(fakeUser)*/
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}