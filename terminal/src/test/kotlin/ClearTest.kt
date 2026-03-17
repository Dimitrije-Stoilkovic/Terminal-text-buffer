import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class ClearTest {

    private lateinit var buffer: TerminalBuffer

    @BeforeEach
    fun setup() {
        buffer = TerminalBuffer(width = 80, height = 24, scrollbackSize = 100)
    }

    @Test
    fun `clearScreen removing all content`() {
        buffer.writeText("Hello")
        buffer.clearScreen()
        for (row in 0 until 24) {
            for (col in 0 until 80) {
                assertNull(buffer.getCharScreen(row, col))
            }
        }
    }

    @Test
    fun `clearScreen keeps screen at correct dimensions`() {
        buffer.clearScreen()
        assertEquals(24, buffer.screen.size)
    }

    @Test
    fun `clearScreen preserves scrollback content`() {
        buffer.writeText("preserved")
        buffer.insertEmpty()
        buffer.clearScreen()
        assertEquals('p', buffer.getCharScrollback(0, 0))
    }

    @Test
    fun `clearScreenScrollback removes all screen content`() {
        buffer.writeText("Hello")
        buffer.clearScreenScrollback()
        for (row in 0 until 24) {
            for (col in 0 until 80) {
                assertNull(buffer.getCharScreen(row, col))
            }
        }
    }

    @Test
    fun `clearScreenScrollback empties scrollback`() {
        repeat(10) { buffer.insertEmpty() }
        buffer.clearScreenScrollback()
        assertEquals(0, buffer.scrollback.size)
    }

    @Test
    fun `clearScreenScrollback keeps screen at correct dimensions`() {
        buffer.clearScreenScrollback()
        assertEquals(24, buffer.screen.size)
    }

}