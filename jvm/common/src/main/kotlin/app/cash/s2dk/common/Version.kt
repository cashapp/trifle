package app.cash.s2dk.common

interface Version {
  fun complete(): String

  fun major(): Int

  fun minor(): Int
}
