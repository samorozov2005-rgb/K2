package com.example

import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApplicationTest {
    private val json = Json

    @Test
    fun `complete authentication and task lifecycle`() = testApplication {
        application { module() }

        val publicList = client.get("/api/v1/tasks")
        assertEquals(HttpStatusCode.OK, publicList.status)
        assertEquals("[]", publicList.bodyAsText().filterNot(Char::isWhitespace))

        val registration = client.postJson(
            "/api/v1/auth/register",
            """{"username":"admin","password":"password123"}""",
        )
        assertEquals(HttpStatusCode.Created, registration.status)
        assertFalse(registration.bodyAsText().contains("password", ignoreCase = true))

        val duplicate = client.postJson(
            "/api/v1/auth/register",
            """{"username":"admin","password":"password123"}""",
        )
        assertEquals(HttpStatusCode.Conflict, duplicate.status)

        val wrongPassword = client.postJson(
            "/api/v1/auth/login",
            """{"username":"admin","password":"incorrect"}""",
        )
        assertEquals(HttpStatusCode.Unauthorized, wrongPassword.status)

        val unknownUser = client.postJson(
            "/api/v1/auth/login",
            """{"username":"nobody","password":"password123"}""",
        )
        assertEquals(HttpStatusCode.Unauthorized, unknownUser.status)

        val login = client.postJson(
            "/api/v1/auth/login",
            """{"username":"admin","password":"password123"}""",
        )
        assertEquals(HttpStatusCode.OK, login.status)
        val token = json.parseToJsonElement(login.bodyAsText()).jsonObject["token"]!!.jsonPrimitive.content
        assertTrue(token.isNotBlank())

        val noToken = client.postJson(
            "/api/v1/tasks",
            """{"title":"Learn Ktor","description":"Homework","completed":false}""",
        )
        assertEquals(HttpStatusCode.Unauthorized, noToken.status)

        val invalidToken = client.get("/api/v1/tasks/1") {
            bearerAuth("broken.jwt.token")
        }
        // GET remains public even if the caller happens to send an invalid token.
        assertEquals(HttpStatusCode.NotFound, invalidToken.status)

        val invalidProtectedToken = client.delete("/api/v1/tasks/1") {
            bearerAuth("broken.jwt.token")
        }
        assertEquals(HttpStatusCode.Unauthorized, invalidProtectedToken.status)

        val created = client.postJson(
            "/api/v1/tasks",
            """{"title":"Learn Ktor","description":"Homework","completed":false}""",
            token,
        )
        assertEquals(HttpStatusCode.Created, created.status)
        val createdJson = json.parseToJsonElement(created.bodyAsText()).jsonObject
        assertEquals("1", createdJson["id"]!!.jsonPrimitive.content)

        val fetched = client.get("/api/v1/tasks/1")
        assertEquals(HttpStatusCode.OK, fetched.status)
        assertTrue(fetched.bodyAsText().contains("Learn Ktor"))

        val updated = client.put("/api/v1/tasks/1") {
            bearerAuth(token)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""{"title":"Master Ktor","description":"Done","completed":true}""")
        }
        assertEquals(HttpStatusCode.OK, updated.status)
        assertTrue(updated.bodyAsText().contains("Master Ktor"))

        val deleted = client.delete("/api/v1/tasks/1") { bearerAuth(token) }
        assertEquals(HttpStatusCode.NoContent, deleted.status)
        assertTrue(deleted.bodyAsText().isEmpty())

        assertEquals(HttpStatusCode.NotFound, client.get("/api/v1/tasks/1").status)
    }

    @Test
    fun `validation and status codes are correct`() = testApplication {
        application { module() }

        assertEquals(HttpStatusCode.BadRequest, client.get("/api/v1/tasks/not-a-number").status)

        val malformedRegistration = client.postJson("/api/v1/auth/register", "{bad json")
        assertEquals(HttpStatusCode.BadRequest, malformedRegistration.status)

        val shortPassword = client.postJson(
            "/api/v1/auth/register",
            """{"username":"admin","password":"short"}""",
        )
        assertEquals(HttpStatusCode.BadRequest, shortPassword.status)

        client.postJson(
            "/api/v1/auth/register",
            """{"username":"admin","password":"password123"}""",
        )
        val login = client.postJson(
            "/api/v1/auth/login",
            """{"username":"admin","password":"password123"}""",
        )
        val token = json.parseToJsonElement(login.bodyAsText()).jsonObject["token"]!!.jsonPrimitive.content

        val blankTitle = client.postJson(
            "/api/v1/tasks",
            """{"title":"   ","description":"No title","completed":false}""",
            token,
        )
        assertEquals(HttpStatusCode.BadRequest, blankTitle.status)

        val missingTask = client.put("/api/v1/tasks/999") {
            bearerAuth(token)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""{"title":"Valid","description":"Missing","completed":false}""")
        }
        assertEquals(HttpStatusCode.NotFound, missingTask.status)

        assertEquals(HttpStatusCode.Unauthorized, client.put("/api/v1/tasks/1").status)
        assertEquals(HttpStatusCode.Unauthorized, client.delete("/api/v1/tasks/1").status)
    }
}

private suspend fun io.ktor.client.HttpClient.postJson(
    path: String,
    body: String,
    token: String? = null,
) = post(path) {
    header(HttpHeaders.ContentType, ContentType.Application.Json)
    if (token != null) bearerAuth(token)
    setBody(body)
}
