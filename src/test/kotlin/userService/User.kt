package userService

import jodd.util.RandomString

data class User(
        val age: Int = (18..100).random(),
        val firstName: String = RandomString.get().randomAlpha(16),
        val lastName: String = RandomString.get().randomAlpha(16)
)