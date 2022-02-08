package computerdatabase

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import java.time.Duration

class BasicSimulation : Simulation() {

    val httpProtocol = http
            .baseUrl("http://localhost:8081/api") // Here is the root for all relative URLs
            .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
            .acceptEncodingHeader("gzip, deflate")
            .acceptLanguageHeader("en-US,en;q=0.5")
            .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")
            .contentTypeHeader("application/json")
            .enableHttp2()
            .shareConnections()

    val scn = scenario("#1")
            .exec(http("Get users")
                    .get("/users")
                    .check(status().shouldBe(200))
            )

    val scn1 = scenario("#2")
            .exec(http("Post user")
                    .post("/user")
                    .body(StringBody("""
                        {
                          "age": 18,
                          "firstName": "firstNameTest",
                          "lastName": "lastNameTest"
                        }
                    """.trimIndent()))
                    .check(
                            status().shouldBe(201),
                            jsonPath("$.id").exists().saveAs("userId")
                    )
            )
            .exec(http("Get user by id")
                    .get { session -> "/user/${session.getInt("userId")}" }
                    .check(status().shouldBe(200)))

    val scn2 = scenario("#3")
            .exec(http("Post user")
                    .post("/user")
                    .body(StringBody("""
                        {
                          "age": 18,
                          "firstName": "firstNameTest",
                          "lastName": "lastNameTest"
                        }
                    """.trimIndent()))
                    .check(
                            status().shouldBe(201),
                            jsonPath("$.id").exists().saveAs("userId")
                    )
            )
            .exec(http("Patch user")
                    .put { session -> "/user/${session.getInt("userId")}" }
                    .body(StringBody("""
                        {
                          "age": 25,
                          "firstName": "firstNameTest1",
                          "lastName": "lastNameTest1"
                        }
                    """.trimIndent())))

    val scn3 = scenario("#4")
            .exec(http("Post user")
                    .post("/user")
                    .body(StringBody("""
                        {
                          "age": 18,
                          "firstName": "firstNameTest",
                          "lastName": "lastNameTest"
                        }
                    """.trimIndent()))
                    .check(
                            status().shouldBe(201),
                            jsonPath("$.id").exists().saveAs("userId")
                    )
            )
            .exec(http("Delete user")
                    .delete { session -> "/user/${session.getInt("userId")}" })

    init {
        setUp(scn.injectOpen(atOnceUsers(30))
                .throttle(
                        jumpToRps(10),
                        holdFor(Duration.ofMinutes(1))
                ),
                scn1.injectOpen(atOnceUsers(30))
                        .throttle(
                                jumpToRps(10),
                                holdFor(Duration.ofMinutes(1))
                        ),
                scn2.injectOpen(atOnceUsers(30))
                        .throttle(
                                jumpToRps(10),
                                holdFor(Duration.ofMinutes(1))
                        ),
                scn3.injectOpen(atOnceUsers(30))
                        .throttle(
                                jumpToRps(10),
                                holdFor(Duration.ofMinutes(1))
                        )
        ).protocols(httpProtocol)
                .maxDuration(Duration.ofHours(1))
                .assertions(
                        global().responseTime().max().lte(1000),
                        forAll().requestsPerSec().gte(10.0),
                        details("Delete user").failedRequests().percent().lte(0.0)
                )
    }
}
