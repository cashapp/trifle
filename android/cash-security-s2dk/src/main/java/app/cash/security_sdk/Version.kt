package app.cash.security_sdk

interface Version {
  fun complete(): String

  fun major(): Int

  fun minor(): Int
}
