package app.cash.trifle

interface Version {
  fun complete(): String

  fun major(): Int

  fun minor(): Int
}
