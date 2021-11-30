package ru.netology.nmedia

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.ActivityAppBinding
import ru.netology.nmedia.ui.NewPostFragment.Companion.textArg
import ru.netology.nmedia.viewmodel.AuthViewModel
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ActivityApp : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var auth: AppAuth

    @Inject
    lateinit var firebase: FirebaseMessaging

    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability
    private lateinit var appBarConfiguration: AppBarConfiguration


    private lateinit var binding: ActivityAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }
            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }
            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment).navigate(
                R.id.action_postsFeedFragment_to_NewPostFragment,
                Bundle().apply {
                    textArg = text
                }
            )
        }
        viewModel.data.observe(this) {
            invalidateOptionsMenu()
        }

        binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        viewModel.data.observe(this) {
            navView.menu.findItem(R.id.fragment_jobs_feed).isVisible = viewModel.authenticated
        }


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        /** Управление показом навконтроллера на страницах*/
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragment_jobs_feed, R.id.fragment_posts_feed, R.id.fragment_events_feed -> binding.navView.visibility =
                    View.VISIBLE
                else -> binding.navView.visibility = View.GONE
            }
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.fragment_posts_feed,
                R.id.fragment_events_feed,
                R.id.fragment_jobs_feed,
            )
        )


        /**Показ информации о юзере в экшен баре*/
        viewModel.data.observe(this) {
            viewModel.loadUser(it?.id ?: 0L)
        }

        viewModel.user.observe(this) { user ->
            binding.toolbarLogin.visibility =
                if (user?.login?.isNotBlank() == true) View.VISIBLE else View.GONE
            binding.toolbarAvatar.visibility =
                if (user?.login?.isNotBlank() == true) View.VISIBLE else View.GONE
            binding.toolbarLogin.text = user?.login
            if (user?.avatar?.isNotBlank() == true) {
                Glide.with(binding.toolbarAvatar)
                    .load("${user.avatar}")
                    .override(96, 96)
                    .placeholder(R.drawable.ic_baseline_photo_camera_24)
                    .error(R.drawable.ic_baseline_error_24)
                    .timeout(10_000)
                    .transform(MultiTransformation(FitCenter(), CircleCrop()))
                    .into(binding.toolbarAvatar)
            } else {
                binding.toolbarAvatar.setImageDrawable(getDrawable(R.drawable.ic_baseline_photo_camera_24))
            }
        }

        /**Установка кастомного экшен бара*/
        setSupportActionBar(findViewById(R.id.my_toolbar))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        firebase.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("some stuff happened: ${task.exception}")
                return@addOnCompleteListener
            }

            val token = task.result
            println(token)
        }

        checkGoogleApiAvailability()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(this.appBarConfiguration)
                || super.onSupportNavigateUp()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        menu?.let {
            it.setGroupVisible(R.id.unauthenticated, !viewModel.authenticated)
            it.setGroupVisible(R.id.authenticated, viewModel.authenticated)
        }
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.signin -> {
                findNavController(R.id.nav_host_fragment)
                    .navigate(
                        R.id.fragment_sign_in,
                    )
                true
            }
            R.id.signup -> {
                findNavController(R.id.nav_host_fragment)
                    .navigate(
                        R.id.fragment_sign_up,
                    )
                true
            }
            R.id.signout -> {
                auth.removeAuth()
                findNavController(R.id.nav_host_fragment)
                    .navigate(
                        R.id.fragment_posts_feed,
                    )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkGoogleApiAvailability() {
        with(googleApiAvailability) {
            val code = isGooglePlayServicesAvailable(this@ActivityApp)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@ActivityApp, code, 9000)?.show()
                return
            }
            Snackbar.make(binding.root, R.string.google_play_unavailable, Snackbar.LENGTH_LONG)
                .show()
        }
    }

}