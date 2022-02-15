package userService

import io.gatling.javaapi.core.CoreDsl
import io.gatling.javaapi.http.HttpDsl
import io.gatling.javaapi.http.HttpDsl.status
import io.gatling.javaapi.http.HttpRequestActionBuilder

class UserApi {

    fun getAllUserRequest(): HttpRequestActionBuilder {
        return HttpDsl.http("Get all users")
                .get("/users")
                .check(status().shouldBe(200))
    }

    fun postUserRequest(): HttpRequestActionBuilder {
        return HttpDsl.http("Post user")
                .post("/user")
                .body(CoreDsl.StringBody{session -> session.getString("user")})
                .check(
                        status().shouldBe(201),
                        CoreDsl.jsonPath("$.id").exists().saveAs("userId")
                )
    }

    fun getUserByIdRequest(): HttpRequestActionBuilder {
        return HttpDsl.http("Get user by id")
                .get { session -> "/user/${session.getString("userId")}" }
                .check(status().shouldBe(200))
    }


    fun patchUserRequest(): HttpRequestActionBuilder {
        return HttpDsl.http("Patch user")
                .put { session -> "/user/${session.getString("userId")}" }
                .body(CoreDsl.StringBody{session -> session.getString("userForUpdate")})
    }

    fun deleteUserRequest(): HttpRequestActionBuilder {
        return HttpDsl.http("Delete user")
                .delete { session -> "/user/${session.getInt("userId")}" }
                .check(status().shouldBe(202))
    }
}