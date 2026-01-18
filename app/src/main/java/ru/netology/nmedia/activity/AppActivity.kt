package ru.netology.nmedia.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.AuthViewModel


class AppActivity : AppCompatActivity(R.layout.activity_app_with_toolbar) {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(findViewById(R.id.toolbar))

        AppAuth.initApp(applicationContext)

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment)
                .navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = text
                    }
                )
        }

        viewModel.authenticated.observe(this) { authenticated ->
            invalidateOptionsMenu()
        }

        viewModel.data.observe(this){
            invalidateOptionsMenu()
            if (it.id == 0L) {
                Toast.makeText(this@AppActivity,getString(R.string.guest_entrance), Toast.LENGTH_LONG)
                    .show()
            } else {
                val welcome = getString(R.string.welcome)
                Toast.makeText(this@AppActivity,"$welcome ${it.id}", Toast.LENGTH_LONG)
                    .show()
            }
        }

        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("some stuff happened: ${task.exception}")
                return@addOnCompleteListener
            }

            val token = task.result
            println(token)
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("some stuff happened: ${task.exception}")
                return@addOnCompleteListener
            }

            val token = task.result
            println(token)
        }

        checkGoogleApiAvailability()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val authState = AppAuth.getInstance().authStateFlow.value
        val authenticated = authState.id != 0L

        menu.let {
            it.setGroupVisible(R.id.unauthenticated, !authenticated)
            it.setGroupVisible(R.id.authenticated, authenticated)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.signin -> {
                findNavController(R.id.nav_host_fragment)
                    .navigate(R.id.action_feedFragment_to_loginFragment)
                true
            }
            R.id.signup -> {
                true
            }
            R.id.signout -> {
                AppAuth.getInstance().removeAuth()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object{
        var Bundle.textArg: String? by StringArg
    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        requestPermissions(arrayOf(permission), 1)
    }

    private fun checkGoogleApiAvailability() {
        with(GoogleApiAvailability.getInstance()) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG).show()
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            println(it)
        }
    }

}