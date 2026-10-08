package org.connecttag.lib.actions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ActionRegistryTest {
    @Test
    fun everyActionTypeOwnsItsSpecification() {
        ActionType.entries.forEach { type ->
            assertEquals(type, ActionRegistry.specFor(type).type)
        }
    }

    @Test
    fun closeAppIsAContentFreeSupportedAction() {
        val spec = ActionRegistry.specFor(ActionType.CLOSE_APP)

        assertTrue(spec.supported)
        assertFalse(spec.requiresContent)
        assertEquals(ActionContentKind.NONE, spec.contentKind)
    }

    @Test
    fun actionParserAcceptsOnlyCanonicalWireValues() {
        assertEquals(ActionType.OPEN_URL, ActionRegistry.parseCanonicalActionType("OPEN_URL"))
        assertNull(ActionRegistry.parseCanonicalActionType("URL"))
        assertNull(ActionRegistry.parseCanonicalActionType("open_url"))
        assertNull(ActionRegistry.parseCanonicalActionType("OPEN-URL"))
        assertNull(ActionRegistry.parseCanonicalActionType(" OPEN_URL "))
    }

    @Test
    fun publicContentPoliciesRejectInternalActions() {
        assertTrue(ActionRegistry.isRuntimeInfoActionType(ActionType.OPEN_URL))
        assertTrue(ActionRegistry.isExperienceCampaignActionType(ActionType.NAVIGATION))
        assertFalse(ActionRegistry.isRuntimeInfoActionType(ActionType.CLEAR_CACHE))
        assertFalse(ActionRegistry.isExperienceCampaignActionType(ActionType.CLOSE_APP))
    }
}
