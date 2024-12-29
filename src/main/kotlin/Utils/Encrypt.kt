package Utils

import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

class Encrypt {
    private val algorithm = "AES"
    private val transformation = "AES/ECB/PKCS5Padding" // Use CBC para maior segurança, mas requer IV.
    private val secretKey = "your-16-char-key" // Substitua por uma chave de 16 caracteres.

    private fun getKey(): Key {
        return SecretKeySpec(secretKey.toByteArray(), algorithm)
    }

    fun encrypt(input: String): String {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val encryptedBytes = cipher.doFinal(input.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decrypt(input: String): String {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.DECRYPT_MODE, getKey())
        val decodedBytes = Base64.getDecoder().decode(input)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes)
    }
}
