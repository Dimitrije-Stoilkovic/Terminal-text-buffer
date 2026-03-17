import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class WriteTest {

    private lateinit var buffer: TerminalBuffer

    @BeforeEach
    fun setup() {
        buffer = TerminalBuffer(width = 80, height = 24, scrollbackSize = 100)
    }

    @Test
    fun `write char at cursor position`() {
        buffer.write('A')
        assertEquals('A', buffer.getCharScreen(0, 0))
    }

    @Test
    fun `write advances cursor right by 1`() {
        buffer.write('A')
        assertEquals(Pair(1, 0), buffer.getCursorPosition())
    }

    @Test
    fun `write applies foreground color`() {
        buffer.setAttributes(Attributes(fgColor = Color.RED))
        buffer.write('A')
        assertEquals(Color.RED, buffer.getAttributesScreen(0, 0)?.fgColor)
    }

    @Test
    fun `write applies background color`() {
        buffer.setAttributes(Attributes(bgColor = Color.BLUE))
        buffer.write('A')
        assertEquals(Color.BLUE, buffer.getAttributesScreen(0, 0)?.bgColor)
    }

    @Test
    fun `write applies bold`() {
        buffer.setAttributes(Attributes(bold = true))
        buffer.write('A')
        assertEquals(true, buffer.getAttributesScreen(0, 0)?.bold)
    }

    @Test
    fun `write applies italic`() {
        buffer.setAttributes(Attributes(italic = true))
        buffer.write('A')
        assertEquals(true, buffer.getAttributesScreen(0, 0)?.italic)
    }

    @Test
    fun `write applies underline`() {
        buffer.setAttributes(Attributes(underline = true))
        buffer.write('A')
        assertEquals(true, buffer.getAttributesScreen(0, 0)?.underline)
    }

    @Test
    fun `write at right edge wraps cursor to next row`() {
        buffer.setCursorPosition(79, 0)
        buffer.write('A')
        assertEquals(Pair(0, 1), buffer.getCursorPosition())
    }

    @Test
    fun `write at right edge of last row inserts new line`() {
        buffer.setCursorPosition(79, 23)
        buffer.write('A')
        assertEquals(Pair(0, 23), buffer.getCursorPosition())
    }

    @Test
    fun `write at right edge of last row pushes content to scrollback`() {
        buffer.setCursorPosition(79, 23)
        buffer.write('A')
        assertEquals(1, buffer.scrollback.size)
    }

    @Test
    fun `writeText writes each character in sequence`() {
        buffer.writeText("Hello")
        assertEquals('H', buffer.getCharScreen(0, 0))
        assertEquals('e', buffer.getCharScreen(0, 1))
        assertEquals('l', buffer.getCharScreen(0, 2))
        assertEquals('l', buffer.getCharScreen(0, 3))
        assertEquals('o', buffer.getCharScreen(0, 4))
    }

    @Test
    fun `writeText advances cursor to end of text`() {
        buffer.writeText("Hello")
        assertEquals(Pair(5, 0), buffer.getCursorPosition())
    }

    @Test
    fun `writeText wraps to next line at right edge`() {
        buffer.setCursorPosition(79, 0)
        buffer.writeText("AB")
        assertEquals('A', buffer.getCharScreen(0, 79))
        assertEquals('B', buffer.getCharScreen(1, 0))
    }

    @Test
    fun `write overwrites existing content`() {
        buffer.write('A')
        buffer.setCursorPosition(0, 0)
        buffer.write('B')
        assertEquals('B', buffer.getCharScreen(0, 0))
    }

    @Test
    fun `write null clears cell`() {
        buffer.write('A')
        buffer.setCursorPosition(0, 0)
        buffer.write(null)
        assertNull(buffer.getCharScreen(0, 0))
    }
}