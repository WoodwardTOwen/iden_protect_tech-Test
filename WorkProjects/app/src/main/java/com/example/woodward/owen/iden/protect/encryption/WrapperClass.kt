package com.example.woodward.owen.iden.protect.encryption

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import java.security.*
import javax.crypto.Cipher
import javax.inject.Inject

class RsaKeyStore @Inject constructor(){

    companion object{
        const val AES_NOPAD_TRANS = "RSA/ECB/PKCS1Padding"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "Keyalaisras"
    }

    private fun createKeyStore(): KeyStore {
        /** Gets Instance and creates Key Store to store cryptographic keys */
        return KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M) /** From Research and IDE altering about different build versions*/
    fun createAsymmetricKeyPair(): KeyPair {
        val generator: KeyPairGenerator

        if (Build.VERSION.SDK_INT == 23) {
            generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
            getKeyGenParameterSpec(generator)
        } else {
            generator = KeyPairGenerator.getInstance("RSA")
            generator.initialize(2048)
        }

        return generator.generateKeyPair()
    }

    @RequiresApi(Build.VERSION_CODES.M)/** From Research and IDE altering about different build versions*/
    private fun getKeyGenParameterSpec(generator: KeyPairGenerator) {

        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)

        generator.initialize(builder.build())
    }

    fun getAsymmetricKeyPair(): KeyPair? {
        val keyStore = createKeyStore() /** Check instance of KeyStore*/

        /** Retrieval of Keys*/
        val privateKey = keyStore.getKey(KEY_ALIAS, null) as PrivateKey?
        val publicKey = keyStore.getCertificate(KEY_ALIAS)?.publicKey

        return if (privateKey != null && publicKey != null) {
            KeyPair(publicKey, privateKey)
        } else {
            null
        }
    }

    /** Removal of the [KeyStore]
     * Used onClear from the main UI from the clear data button OR once the application lifeCycle has ended
     * */
    fun removeKeyStoreKey() = createKeyStore().deleteEntry(KEY_ALIAS)

    fun encrypt(data: String, key: Key?): String {
        val cipher = Cipher.getInstance(AES_NOPAD_TRANS).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
        val bytes = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    fun decrypt(data: String, key: Key?): String {
        val cipher = Cipher.getInstance(AES_NOPAD_TRANS).apply {
            init(Cipher.DECRYPT_MODE, key)
        }
        val encryptedData = Base64.decode(data, Base64.DEFAULT)
        val decodedData = cipher.doFinal(encryptedData)
        return String(decodedData)
    }

}