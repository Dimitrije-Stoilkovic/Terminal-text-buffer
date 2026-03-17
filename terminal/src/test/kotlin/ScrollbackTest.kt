import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class ScrollbackTest {

    private lateinit var buffer: TerminalBuffer

    @BeforeEach
    fun setup() {
        buffer = TerminalBuffer(width = 80, height = 24, scrollbackSize = 100)
    }

    @Test
    fun `insertEmpty adds blank line at bottom of screen`() {
        buffer.insertEmpty()
        assertNull(buffer.getCharScreen(23, 0))
    }

    @Test
    fun `insertEmpty pushes top screen line to scrollback`() {
        buffer.writeText("Hello")
        buffer.insertEmpty()
        assertEquals('H', buffer.getCharScrollback(0, 0))
    }

    @Test
    fun `insertEmpty scrollback size grows by 1`() {
        buffer.insertEmpty()
        assertEquals(1, buffer.scrollback.size)
    }

    @Test
    fun `insertEmpty most recent line is at scrollback index 0`() {
        buffer.setCursorPosition(0, 0)
        buffer.writeText("first")
        buffer.insertEmpty()
        buffer.setCursorPosition(0, 0)
        buffer.writeText("second")
        buffer.insertEmpty()
        assertEquals('s', buffer.getCharScrollback(0, 0))
        assertEquals('f', buffer.getCharScrollback(1, 0))
    }

    @Test
    fun `scrollback does not exceed max size`() {
        repeat(200) {
            buffer.insertEmpty()
        }
        assertEquals(100, buffer.scrollback.size)
    }

    @Test
    fun `oldest scrollback line is dropped when cap exceeded`() {
        buffer.setCursorPosition(0, 0)
        buffer.writeText("oldest")
        repeat(123) {
            buffer.insertEmpty()
        }
        for (i in 0 until buffer.scrollback.size) {
            assertNotEquals('o', buffer.getCharScrollback(i, 0))
        }
    }


}