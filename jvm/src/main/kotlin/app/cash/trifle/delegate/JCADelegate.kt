package app.cash.trifle.delegate

import app.cash.trifle.internal.signers.JCAContentSigner
import java.security.KeyPair

/**
 * JCA implemented delegate for certificate enrollment.
 */
internal class JCADelegate(keyPair: KeyPair) : DelegateImpl(JCAContentSigner(keyPair))
