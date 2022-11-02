package cstjean.mobile.tp2

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import cstjean.mobile.tp2.databinding.FragmentLoginBinding
import kotlin.math.log

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Binding est null. La vue est visible ??"
        }
    private var user =  FirebaseAuth.getInstance().currentUser

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            if(user == null) {
                btnConnexion.setText(R.string.connexion)
            } else {
                btnConnexion.setText(R.string.deconnexion)
            }

            btnConnexion.setOnClickListener {
                if (user == null) {

                    // Choose authentication providers
                    val providers = arrayListOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build())

                    // Create and launch sign-in intent
                    val signInIntent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build()

                    signInLauncher.launch(signInIntent)
                } else {
                    btnConnexion.setText(R.string.connexion)
                    context?.let { it1 -> AuthUI.getInstance().signOut(it1) }
                    user = null
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            binding.btnConnexion.setText(R.string.deconnexion)
            user = FirebaseAuth.getInstance().currentUser
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Log.e("TAG-ERROR", response?.error?.errorCode.toString())
        }
    }
}