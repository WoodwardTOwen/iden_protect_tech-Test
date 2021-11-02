package com.example.woodward.owen.iden.protect.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.woodward.owen.iden.protect.encryption.RsaKeyStore
import timber.log.Timber
import java.security.KeyPair

class MainViewModel @ViewModelInject constructor(private val rsaKeyStore: RsaKeyStore) : ViewModel() {

    private lateinit var rsaKeyPair: KeyPair

    private val _encryptedText = MutableLiveData<String?>()
    val encryptedText : LiveData<String?> get() = _encryptedText
    private val _decryptedText = MutableLiveData<String?>()
    val decryptedText : LiveData<String?> get() = _decryptedText

    private val isTextEncrypted = MutableLiveData<Boolean?>(null)
    private val isTextDecrypted = MutableLiveData(true)

    private val _everythingComplete = MutableLiveData(false)
    val everythingComplete: LiveData<Boolean>
        get() = _everythingComplete

    private var _currentSnackBarStatus = MutableLiveData<SnackBarStatus>()
    val currentSnackBarStatus: LiveData<SnackBarStatus> get() = _currentSnackBarStatus

    val buttonInputControlEncryption = Transformations.map(isTextEncrypted) {
        it == null
    }

    val buttonControlDecrypted = Transformations.map(isTextDecrypted) {
        it == null
    }

    val buttonControlClear = Transformations.map(_everythingComplete) {
        it == true
    }

    private fun resetEncryptedText() {
        _encryptedText.value = null
    }

    private fun resetDecryptedText() {
        _decryptedText.value = null
    }

    private fun setEverythingComplete() {
        _everythingComplete.value = true
    }

    private fun resetEverythingComplete() {
        _everythingComplete.value = false
    }

    private val _submittedTransaction = MutableLiveData(false)
    val submittedTransaction: LiveData<Boolean>
        get() = _submittedTransaction

    fun completedSubmitTransaction() {
        _submittedTransaction.value = false
    }

    private fun setCurrentSnackBarStatus(status: SnackBarStatus) {
        this._currentSnackBarStatus.value = status
    }


    /** [onEncrypt] -> User Requests Encryption*/

    @RequiresApi(Build.VERSION_CODES.M)
    fun onEncrypt(plainText: String?) {
        Timber.i("onEncryptCalled")

        when {
            /** Checking validity of data */
            plainText.isNullOrEmpty() -> {
                setCurrentSnackBarStatus(SnackBarStatus.SnackBarError)

            }
            else -> {
                /** Creating the key pair, dependent on SDK version */
                rsaKeyPair = if (Build.VERSION.SDK_INT == 23) {
                    rsaKeyStore.createAsymmetricKeyPair()
                    rsaKeyStore.getAsymmetricKeyPair()!!
                } else {
                    rsaKeyStore.createAsymmetricKeyPair()
                }
                /** Calls wrapper class for encryption of the data */
                _encryptedText.value = rsaKeyStore.encrypt(plainText, rsaKeyPair.public)
                /** Using Public Key to Encrypt*/

                isTextEncrypted.value = true
                isTextDecrypted.value = null
                setCurrentSnackBarStatus(SnackBarStatus.SuccessfulEncryption)
            }
        }
    }


    fun onDecrypt() {
        Timber.i("onEncryptCalled")
        _decryptedText.value = rsaKeyStore.decrypt(_encryptedText.value as String, rsaKeyPair.private)
        setCurrentSnackBarStatus(SnackBarStatus.SuccessfulDecryption)
        setEverythingComplete()
        isTextDecrypted.value = true
    }

    /** [onClear] Resets State of application */

    fun onClear() {
        isTextEncrypted.value = null
        removeKeyStore()
        resetEverythingComplete()
        resetEncryptedText()
        resetDecryptedText()
    }

    /** Removing the stored key*/
    fun removeKeyStore() {
        if (::rsaKeyPair.isInitialized) rsaKeyStore.removeKeyStoreKey()
    }

    sealed class SnackBarStatus {
        object SuccessfulEncryption : SnackBarStatus()
        object SuccessfulDecryption : SnackBarStatus()
        object SnackBarError : SnackBarStatus()
    }
}