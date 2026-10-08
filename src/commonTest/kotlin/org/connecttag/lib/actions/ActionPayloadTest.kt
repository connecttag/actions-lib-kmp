package org.connecttag.lib.actions

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ActionPayloadTest {
    private val json = Json

    @Test
    fun factoryCreatesPayloadOwnedByActionSpecification() {
        val url = ActionRequestFactory.from(ActionType.OPEN_URL, " https://example.com ")
        val route = ActionRequestFactory.from(ActionType.NAVIGATION, "home")
        val none = ActionRequestFactory.from(ActionType.TOGGLE_THEME, "ignored")

        assertIs<ActionContent.Url>(url.payload)
        assertEquals("https://example.com", url.payload.rawValue)
        assertIs<ActionContent.Route>(route.payload)
        assertIs<ActionContent.None>(none.payload)
    }

    @Test
    fun validatorRejectsPayloadThatDoesNotMatchActionType() {
        val request = ActionRequest(
            type = ActionType.OPEN_URL,
            payload = ActionContent.Phone("+967700000000"),
        )

        assertEquals(
            ActionFailureReason.ContentTypeMismatch(
                expected = ActionContentKind.URL,
                actual = ActionContentKind.PHONE,
            ),
            ActionValidator.validate(request, hasNavController = true),
        )
    }

    @Test
    fun typedRequestRoundTripsThroughCacheSerialization() {
        val request = ActionRequestFactory.from(
            type = ActionType.OPEN_URL,
            content = "https://example.com/update",
            source = ActionSource.SERVER_CONTENT,
        )

        val encoded = json.encodeToString(ActionRequest.serializer(), request)
        val decoded = json.decodeFromString(ActionRequest.serializer(), encoded)

        assertEquals(request, decoded)
        assertIs<ActionContent.Url>(decoded.payload)
    }

    @Test
    fun dialogButtonsAcceptOnlyCanonicalActionTypeOrNoAction() {
        val dismissButton = ActionRequestFactory.from(
            ActionType.SHOW_DIALOG,
            """{"message":"Info","negativeButton":{"text":"Close"}}""",
        )
        val canonicalButton = ActionRequestFactory.from(
            ActionType.SHOW_DIALOG,
            """{"message":"Info","positiveButton":{"text":"Open","actionType":"OPEN_URL","content":"https://example.com"}}""",
        )
        val aliasedButton = ActionRequestFactory.from(
            ActionType.SHOW_DIALOG,
            """{"message":"Info","positiveButton":{"text":"Open","actionType":"open_url","content":"https://example.com"}}""",
        )

        assertEquals(null, ActionValidator.validate(dismissButton, hasNavController = true))
        assertEquals(null, ActionValidator.validate(canonicalButton, hasNavController = true))
        assertEquals(
            ActionFailureReason.InvalidContent,
            ActionValidator.validate(aliasedButton, hasNavController = true),
        )
    }
}
