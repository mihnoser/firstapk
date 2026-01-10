package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ru.netology.nmedia.databinding.FragmentLoginBinding
import ru.netology.nmedia.view.afterTextChanged
import ru.netology.nmedia.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    private var fragmentBinding: FragmentLoginBinding? = null
    private val viewModelAuth: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLoginBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding

        with(binding) {
            username.requestFocus()

            username.afterTextChanged {
                viewModelAuth.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            password.afterTextChanged {
                viewModelAuth.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            viewModelAuth.loginFormState.observe(viewLifecycleOwner) {
                login.isEnabled = it.isDataValid
            }

            login.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    println("DEBUG: Login button clicked")
                    println("DEBUG: Username: ${username.text}, Password: ${password.text}")

                    val response = viewModelAuth.authenticate(
                        username.text.toString(),
                        password.text.toString()
                    )

                    println("DEBUG: Authentication response id=${response.id}")

                    if (response.id != 0L) {
                        println("DEBUG: Authentication successful, navigating back")
                        findNavController().navigateUp()
                    } else {
                        println("DEBUG: Authentication failed")
                        Toast.makeText(
                            requireContext(),
                            "Ошибка авторизации. Проверьте логин и пароль",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}