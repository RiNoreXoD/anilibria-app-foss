package ru.radiationx.data.sslcompat

import android.annotation.SuppressLint
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@SuppressLint("CustomX509TrustManager")
class CompatTrustManager(
    private val defaultManager: X509TrustManager,
    private val additionalKeyStores: List<KeyStore>,
) : X509TrustManager {

    private val managers = mutableListOf(defaultManager)

    init {
        for (keyStore in additionalKeyStores) {
            val algorithm = TrustManagerFactory.getDefaultAlgorithm()
            val factory = TrustManagerFactory.getInstance(algorithm)
            factory.init(keyStore)
            factory.trustManagers.forEach { manager ->
                if (manager is X509TrustManager) {
                    managers.add(manager)
                }
            }
        }
    }

    /*
     * Delegate to managers until we find one that accepts the client certificate.
     */
    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate>?, authType: String?) {
        if (chain.isNullOrEmpty()) {
            throw CertificateException("Client certificate chain is null or empty")
        }
        if (authType.isNullOrBlank()) {
            throw CertificateException("Auth type is null or empty")
        }

        var latestException: CertificateException? = null
        for (manager in managers) {
            try {
                manager.checkClientTrusted(chain, authType)
                return
            } catch (e: CertificateException) {
                latestException = e
            }
        }
        throw CertificateException("Client certificate chain is not trusted by any manager", latestException)
    }

    /*
     * Loop over the trustmanagers until we find one that accepts our server
     */
    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>?, authType: String?) {
        if (chain.isNullOrEmpty()) {
            throw CertificateException("Server certificate chain is null or empty")
        }
        if (authType.isNullOrBlank()) {
            throw CertificateException("Auth type is null or empty")
        }

        var latestException: CertificateException? = null
        for (manager in managers) {
            try {
                manager.checkServerTrusted(chain, authType)
                return
            } catch (e: CertificateException) {
                latestException = e
            }
        }
        throw CertificateException("Server certificate chain is not trusted by any manager", latestException)
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        val certificates = managers.flatMap {
            it.acceptedIssuers?.toList().orEmpty()
        }
        return certificates.toTypedArray()
    }
}
