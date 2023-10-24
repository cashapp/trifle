package app.cash.trifle.delegate

import app.cash.trifle.signers.tink.TinkContentSigner
import com.google.crypto.tink.KeysetHandle

/**
 * Tink implemented delegate for certificate enrollment.
 */
internal class TinkDelegate(keysetHandle: KeysetHandle) : DelegateImpl(TinkContentSigner(keysetHandle))
