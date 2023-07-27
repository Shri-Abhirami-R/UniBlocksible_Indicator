package com.scanner.app.tracking.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.scanner.app.tracking.MenuActivity
import com.scanner.app.tracking.databinding.ActivityLoginBinding

import com.scanner.app.tracking.R
import com.scanner.app.tracking.data.model.Product
import com.scanner.app.tracking.data.model.UserDetails

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    private lateinit var database: FirebaseFirestore

    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        loading = binding.loading

        database = Firebase.firestore

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
                val intent = Intent(this, MenuActivity::class.java)
                intent.putExtra("username", username.text.toString())
                startActivity(intent)
                //Complete and destroy login activity once successful
                finish()
            }
            setResult(Activity.RESULT_OK)

        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                //loginViewModel.login(username.text.toString(), password.text.toString())
                val userDetails = UserDetails(username.text.toString(), username.text.toString(), password.text.toString())
                loginUser(userDetails)
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            this,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(this, "Please Enter valid username and password", Toast.LENGTH_SHORT).show()
    }

    fun loginUser(userDetails: UserDetails){
        database.collection("users")
            .whereEqualTo("email", userDetails.email)
            .whereEqualTo("password", userDetails.password)
            .get()
            .addOnSuccessListener { result ->
                loading.visibility = View.GONE
                var data = ""
                for (document in result) {
                    data = document.data.toString()
                    val intent = Intent(this, MenuActivity::class.java)
                    intent.putExtra("username", userDetails.email)

                    val type = object : TypeToken<UserDetails>() {}.type
                    val userDetail = Gson().fromJson<UserDetails>(data, type)
                    intent.putExtra("userdetail", Gson().toJson(userDetail))
                    startActivity(intent)
                    //Complete and destroy login activity once successful
                    finish()
                }
                if(data.isNullOrEmpty())
                    loginFailedDialog()
            }
            .addOnFailureListener {
                loading.visibility = View.GONE
                loginFailedDialog()
            }

    }

    fun loginFailedDialog(){
        AlertDialog.Builder(this)
            .setTitle("Alert!")
            .setCancelable(true)
            .setMessage("Invalid user name or password")
            .setPositiveButton("Ok") { dialog, which ->
                dialog.dismiss()
            }
            //.setNegativeButton(R.string.no, null)
            .create()
            .show()
    }

    fun registerNewUser(view: android.view.View) {
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })

}