package app.cash.trifle

import app.cash.trifle.delegate.CertificateAuthorityDelegate
import app.cash.trifle.delegate.EndEntityDelegate
import app.cash.trifle.delegate.JCADelegate
import app.cash.trifle.delegate.TinkDelegate
import com.google.crypto.tink.KeysetHandle
import java.security.KeyPair

object Trifle {
  class CertificateAuthority internal constructor(delegate: CertificateAuthorityDelegate) :
    CertificateAuthorityDelegate by delegate {
    constructor(keysetHandle: KeysetHandle) : this(TinkDelegate(keysetHandle))
  }

  class EndEntity internal constructor(delegate: EndEntityDelegate) :
    EndEntityDelegate by delegate {
    constructor(keyPair: KeyPair) : this(JCADelegate(keyPair))
  }
}
