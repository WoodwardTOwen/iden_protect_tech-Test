package com.example.woodward.owen.iden.protect.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.woodward.owen.iden.protect.R
import com.example.woodward.owen.iden.protect.data.MainViewModel
import com.example.woodward.owen.iden.protect.databinding.FragmentMainBinding
import com.example.woodward.owen.iden.protect.util.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false).apply {
            viewModel = mainViewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this.viewLifecycleOwner
        setUpUtilObservers()
        setUpObserversSnackBars()
    }

    private fun setUpObserversSnackBars() {
        mainViewModel.currentSnackBarStatus.observeForever {
            snackBarStatus ->
            when(snackBarStatus) {
                MainViewModel.SnackBarStatus.SuccessfulEncryption ->
                    showSnackBar(getString(R.string.encryptionComplete))
                MainViewModel.SnackBarStatus.SuccessfulDecryption ->
                    showSnackBar(getString(R.string.decryptionComplete))
                MainViewModel.SnackBarStatus.SnackBarError ->
                    showSnackBar(getString(R.string.InvalidInput))
            }
        }
    }

    /**[setUpUtilObservers] -> generic helper methods, separate from SnackBar Observers */
    private fun setUpUtilObservers() {
        /** Just used to hide the keyboard after a transaction is completed (UX Principle) */
        mainViewModel.submittedTransaction.observe(this.viewLifecycleOwner, { success ->
            if(success){
                hideKeyboard() /** Extension function from util package -> ComponentExt.kt */
                mainViewModel.completedSubmitTransaction()
            }
        })

        /** Helps Manage TextInputField dependent on how far down the line the process is i.e. encrypted or not, decrypted or not etc */
        mainViewModel.everythingComplete.observe(this.viewLifecycleOwner, { complete ->
            if(complete){
                binding.encryptionTextInput.text.clear()
                binding.encryptionTextInput.hint = getString(R.string.SuccessfulHint)
            } else {
                binding.encryptionTextInput.hint = getString(R.string.Alert_Hint)
            }
        })
    }

    /** [showSnackBar] -> helper method to use for SnackBar calls -> used quite often for User feedback */
    private fun showSnackBar(message: String) = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        /** Deleting Keys Here Once Application LifeCycle Ends */
        mainViewModel.removeKeyStore()
        super.onDestroy()
    }
}