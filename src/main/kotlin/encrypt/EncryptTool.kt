package encrypt

import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptTool private constructor() {

    data class AES(val clazz: Class<*>? = null) {
        private val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        private val secretKey = "d4taC0MsoLU51nDO"
        private val utf8 = charset("UTF-8")

        fun encrypt(text: String): String {
            val secretKeySpec = SecretKeySpec(secretKey.toByteArray(utf8), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(ByteArray(16)))
            val cipherText: ByteArray = cipher.doFinal(text.toByteArray(utf8))
            val te = Base64.getEncoder().encodeToString(cipherText)
            URLEncoder.encode(te, "UTF-8")
            return URLEncoder.encode(te, "UTF-8")
        }

        fun decrypt(encryptText: String): String {
            val secretKeySpec = SecretKeySpec(secretKey.toByteArray(utf8), "AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, IvParameterSpec(ByteArray(16)))
            val textDe = Base64.getDecoder().decode(URLDecoder.decode(encryptText, "UTF-8"))
            val decrypted = cipher.doFinal(textDe)
            return String(decrypted)
        }

    }

}