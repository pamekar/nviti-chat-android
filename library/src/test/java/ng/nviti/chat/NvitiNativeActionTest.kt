package ng.nviti.chat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NvitiNativeActionTest {
    @Test fun parsesOnlySupportedActions() {
        assertEquals(NvitiNativeAction.LOCATION, NvitiNativeAction.fromWireName("location"))
        assertNull(NvitiNativeAction.fromWireName("run_javascript"))
    }
}
