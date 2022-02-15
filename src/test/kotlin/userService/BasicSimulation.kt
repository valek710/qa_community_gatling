package userService

import com.google.gson.Gson
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.ScenarioBuilder
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.time.Duration

class BasicSimulation : Simulation() {

    private val userApi = UserApi()

    private val httpProtocol = http
            .baseUrl("http://localhost:8081/api")
            .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .acceptEncodingHeader("gzip, deflate")
            .acceptLanguageHeader("en-US,en;q=0.5")
            .contentTypeHeader("application/json")
            .enableHttp2()
            .shareConnections()
            .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

    private val getAllUsersScenario = scenario("Get all users")
            .exec(userApi.getAllUserRequest())

    private val getUserByIdScenario: ScenarioBuilder = scenario("Get user by id")
            .exec { session -> session.set("user", Gson().toJson(User())) }
            .exec(userApi.postUserRequest())
            .exec(userApi.getUserByIdRequest())

    private val patchUser = scenario("Patch user")
            .exec { session -> session.set("user", Gson().toJson(User())) }
            .exec { session -> session.set("userForUpdate", Gson().toJson(User())) }
            .exec(userApi.postUserRequest())
            .exec(userApi.patchUserRequest())

    private val deleteUser = scenario("Delete user by id scenario")
            .exec { session -> session.set("user", Gson().toJson(User())) }
            .exec(userApi.postUserRequest())
            .exec(userApi.deleteUserRequest())


    init {
        setUp(
                getAllUsersScenario.injectOpen(atOnceUsers(1000)),
                getUserByIdScenario.injectOpen(atOnceUsers(1000)),
                patchUser.injectOpen(atOnceUsers(1000)),
                deleteUser.injectOpen(atOnceUsers(1000))
        )
                .throttle(
                        jumpToRps(100),
                        holdFor(Duration.ofMinutes(5))
                )
                .protocols(httpProtocol).maxDuration(Duration.ofMinutes(30))
                .assertions(
                        global().successfulRequests().percent().gt(95.0),
                        forAll().successfulRequests().percent().gt(95.0),
                        details("Delete user").failedRequests().percent().shouldBe(0.0),
                        forAll().failedRequests().count().shouldBe(0),
                        global().allRequests().percent().gt(99.0)
                )
    }
}
