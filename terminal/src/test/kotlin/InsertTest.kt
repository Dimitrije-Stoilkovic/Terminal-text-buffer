import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class InsertTest {

    private lateinit var buffer: TerminalBuffer

    @BeforeEach
    fun setup() {
        buffer = TerminalBuffer(width = 80, height = 24, scrollbackSize = 100)
    }

    @Test
    fun `insertText writes char at cursor position`() {
        buffer.insertText("A")
        assertEquals('A', buffer.getCharScreen(0, 0))
    }

    @Test
    fun `insertText advances cursor`() {
        buffer.insertText("A")
        assertEquals(Pair(1, 0), buffer.getCursorPosition())
    }

    @Test
    fun `insertText shifts existing content right`() {
        buffer.writeText("BC")
        buffer.setCursorPosition(0, 0)
        buffer.insertText("A")
        assertEquals('A', buffer.getCharScreen(0, 0))
        assertEquals('B', buffer.getCharScreen(0, 1))
        assertEquals('C', buffer.getCharScreen(0, 2))
    }

    @Test
    fun `insertText into empty cell just writes`() {
        buffer.insertText("A")
        assertEquals('A', buffer.getCharScreen(0, 0))
        assertNull(buffer.getCharScreen(0, 1))
    }

    @Test
    fun `insertText overflow wraps to next line`() {
        buffer.writeText("A".repeat(80))
        buffer.setCursorPosition(0, 0)
        buffer.insertText("X")
        assertEquals('X', buffer.getCharScreen(0, 0))
        assertEquals('A', buffer.getCharScreen(1, 0))
    }

    @Test
    fun `insertText overflow cascades across multiple lines`() {
        buffer.writeText("A".repeat(80))
        buffer.writeText("B".repeat(80))
        buffer.setCursorPosition(0, 0)
        buffer.insertText("X")
        assertEquals('X', buffer.getCharScreen(0, 0))
        assertEquals('A', buffer.getCharScreen(1, 0))
        assertEquals('B', buffer.getCharScreen(2, 0))
    }

    @Test
    fun `insertText overflow on last row inserts new line`() {
        repeat(24) { buffer.writeText("A".repeat(80)) }
        buffer.setCursorPosition(0, 0)
        buffer.insertText("X")
        assertEquals(1, buffer.scrollback.size)
    }

    @Test
    fun `insertText multiple chars shifts content correctly`() {
        buffer.writeText("CD")
        buffer.setCursorPosition(0, 0)
        buffer.insertText("AB")
        assertEquals('A', buffer.getCharScreen(0, 0))
        assertEquals('B', buffer.getCharScreen(0, 1))
        assertEquals('C', buffer.getCharScreen(0, 2))
        assertEquals('D', buffer.getCharScreen(0, 3))
    }

    @Test
    fun `insertText applies current attributes`() {
        buffer.setAttributes(Attributes(fgColor = Color.RED))
        buffer.insertText("A")
        assertEquals(Color.RED, buffer.getAttributesScreen(0, 0)?.fgColor)
    }

    @Test
    fun `insertText preserves attributes of shifted cells`() {
        buffer.setAttributes(Attributes(fgColor = Color.BLUE))
        buffer.writeText("B")
        buffer.setCursorPosition(0, 0)
        buffer.setAttributes(Attributes(fgColor = Color.RED))
        buffer.insertText("A")
        assertEquals(Color.BLUE, buffer.getAttributesScreen(0, 1)?.fgColor)
    }

    @Test
    fun `insertText at end of line wraps to next line`() {
        buffer.setCursorPosition(79, 0)
        buffer.insertText("AB")
        assertEquals('A', buffer.getCharScreen(0, 79))
        assertEquals('B', buffer.getCharScreen(1, 0))
    }
}