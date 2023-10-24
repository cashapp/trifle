package app.cash.trifle.delegate

import app.cash.trifle.signers.jca.JCAContentSigner
import java.security.KeyPair

/**
 * JCA implemented delegate for certificate enrollment.
 */
internal class JCADelegate(keyPair: KeyPair) : DelegateImpl(JCAContentSigner(keyPair))
