package app.cash.trifle.common

interface Version {
  fun complete(): String

  fun major(): Int

  fun minor(): Int
}
