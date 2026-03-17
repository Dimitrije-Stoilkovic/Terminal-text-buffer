import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class FillTest {

    private lateinit var buffer: TerminalBuffer

    @BeforeEach
    fun setup() {
        buffer = TerminalBuffer(width = 80, height = 24, scrollbackSize = 100)
    }

    @Test
    fun `fill fills entire line with character`() {
        buffer.fill('A')
        for (col in 0 until 80) {
            assertEquals('A', buffer.getCharScreen(0, col))
        }
    }

    @Test
    fun `fill with null clears entire line`() {
        buffer.writeText("Hello")
        buffer.setCursorPosition(0, 0)
        buffer.fill(null)
        for (col in 0 until 80) {
            assertNull(buffer.getCharScreen(0, col))
        }
    }

    @Test
    fun `fill fills correct row based on cursor`() {
        buffer.setCursorPosition(0, 5)
        buffer.fill('X')
        for (col in 0 until 80) {
            assertEquals('X', buffer.getCharScreen(5, col))
        }
    }

    @Test
    fun `fill does not affect other rows`() {
        buffer.fill('A')
        for (col in 0 until 80) {
            assertNull(buffer.getCharScreen(1, col))
        }
    }

    @Test
    fun `fill moves cursor to end of line`() {
        buffer.fill('A')
        assertEquals(Pair(0, 1), buffer.getCursorPosition())
    }

    @Test
    fun `fill applies current attributes to all cells`() {
        buffer.setAttributes(Attributes(fgColor = Color.GREEN, bold = true))
        buffer.fill('A')
        for (col in 0 until 80) {
            assertEquals(Color.GREEN, buffer.getAttributesScreen(0, col)?.fgColor)
            assertEquals(true, buffer.getAttributesScreen(0, col)?.bold)
        }
    }

    @Test
    fun `fill overwrites existing content`() {
        buffer.writeText("Hello")
        buffer.setCursorPosition(0, 0)
        buffer.fill('X')
        for (col in 0 until 80) {
            assertEquals('X', buffer.getCharScreen(0, col))
        }
    }

    @Test
    fun `fill on last row pushes one line to scrollback`() {
        buffer.setCursorPosition(0, 23)
        buffer.fill('A')
        assertEquals(1, buffer.scrollback.size)
    }
}