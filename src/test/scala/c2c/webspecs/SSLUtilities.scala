package c2c.webspecs

import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.scheme.Scheme
import javax.net.ssl.{X509TrustManager, TrustManager, KeyManager, SSLContext}
import java.security.cert.X509Certificate
import java.lang.String
import java.security.{SecureRandom, KeyStore}

object SSLUtilities {

  val sslcontext = SSLContext.getInstance(SSLSocketFactory.TLS);
  sslcontext.init(Array[KeyManager](), Array[TrustManager](TrustingTrustManager), new SecureRandom());
  def socketFactory = new SSLSocketFactory(sslcontext){
    override def toString = "Trusting Socket Factory"
  };
  def fakeSSLScheme(port:Int) = new Scheme("https", port, socketFactory);

  object TrustingTrustManager extends X509TrustManager {
    def getAcceptedIssuers = null
    def checkServerTrusted(p1: Array[X509Certificate], p2: String) = {}
    def checkClientTrusted(p1: Array[X509Certificate], p2: String) = {}
    override def toString = "Trusting TrustManager"
  }
}
