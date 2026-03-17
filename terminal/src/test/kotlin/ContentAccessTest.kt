import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class ContentAccessTest {

    private lateinit var buffer: TerminalBuffer

    @BeforeEach
    fun setup() {
        buffer = TerminalBuffer(width = 80, height = 24, scrollbackSize = 100)
    }

    @Test
    fun `getCharScreen returns correct character`() {
        buffer.writeText("Hello")
        assertEquals('H', buffer.getCharScreen(0, 0))
        assertEquals('e', buffer.getCharScreen(0, 1))
    }

    @Test
    fun `getCharScreen returns null for empty cell`() {
        assertNull(buffer.getCharScreen(0, 0))
    }

    @Test
    fun `getCharScreen returns null for out of bounds row`() {
        assertNull(buffer.getCharScreen(-1, 0))
        assertNull(buffer.getCharScreen(24, 0))
    }

    @Test
    fun `getCharScreen returns null for out of bounds col`() {
        assertNull(buffer.getCharScreen(0, -1))
        assertNull(buffer.getCharScreen(0, 80))
    }

    @Test
    fun `getCharScrollback returns correct character`() {
        buffer.writeText("Hello")
        buffer.insertEmpty()
        assertEquals('H', buffer.getCharScrollback(0, 0))
    }

    @Test
    fun `getCharScrollback returns null for empty cell`() {
        buffer.insertEmpty()
        assertNull(buffer.getCharScrollback(0, 0))
    }

    @Test
    fun `getCharScrollback returns null for out of bounds`() {
        assertNull(buffer.getCharScrollback(-1, 0))
        assertNull(buffer.getCharScrollback(9999, 0))
    }

    @Test
    fun `getAttributesScreen returns correct attributes`() {
        buffer.setAttributes(Attributes(fgColor = Color.RED, bold = true))
        buffer.write('A')
        val attrs = buffer.getAttributesScreen(0, 0)
        assertEquals(Color.RED, attrs?.fgColor)
        assertEquals(true, attrs?.bold)
    }

    @Test
    fun `getAttributesScreen returns null for out of bounds`() {
        assertNull(buffer.getAttributesScreen(-1, 0))
        assertNull(buffer.getAttributesScreen(0, -1))
        assertNull(buffer.getAttributesScreen(24, 0))
        assertNull(buffer.getAttributesScreen(0, 80))
    }

    @Test
    fun `getAttributesScrollback returns correct attributes`() {
        buffer.setAttributes(Attributes(fgColor = Color.BLUE, italic = true))
        buffer.write('A')
        buffer.insertEmpty()
        val attrs = buffer.getAttributesScrollback(0, 0)
        assertEquals(Color.BLUE, attrs?.fgColor)
        assertEquals(true, attrs?.italic)
    }

    @Test
    fun `getAttributesScrollback returns null for out of bounds`() {
        assertNull(buffer.getAttributesScrollback(-1, 0))
        assertNull(buffer.getAttributesScrollback(9999, 0))
    }

    @Test
    fun `getLineAsStringScreen returns correct line`() {
        buffer.writeText("Hello")
        val line = buffer.getLineAsStringScreen(0)
        assertTrue(line.startsWith("Hello"))
    }

    @Test
    fun `getLineAsStringScreen returns spaces for empty line`() {
        val line = buffer.getLineAsStringScreen(0)
        assertEquals(" ".repeat(80), line)
    }

    @Test
    fun `getLineAsStringScreen returns empty string for out of bounds`() {
        assertEquals("", buffer.getLineAsStringScreen(-1))
        assertEquals("", buffer.getLineAsStringScreen(24))
    }

    @Test
    fun `getLineAsStringScrollback returns correct line`() {
        buffer.writeText("Hello")
        buffer.insertEmpty()
        val line = buffer.getLineAsStringScrollback(0)
        assertTrue(line.startsWith("Hello"))
    }

    @Test
    fun `getLineAsStringScrollback returns empty string for out of bounds`() {
        assertEquals("", buffer.getLineAsStringScrollback(-1))
        assertEquals("", buffer.getLineAsStringScrollback(9999))
    }

    @Test
    fun `getScreenAsString returns all rows joined by newline`() {
        buffer.writeText("Hello")
        val screen = buffer.getScreenAsString()
        val lines = screen.split("\n")
        assertEquals(24, lines.size)
        assertTrue(lines[0].startsWith("Hello"))
    }

    @Test
    fun `getAllAsString includes scrollback before screen`() {
        buffer.writeText("scrolled")
        buffer.insertEmpty()
        buffer.writeText("visible")
        val all = buffer.getAllAsString()
        val scrolledIndex = all.indexOf("scrolled")
        val visibleIndex = all.indexOf("visible")
        assertTrue(scrolledIndex < visibleIndex)
    }

    @Test
    fun `getAllAsString with empty scrollback equals getScreenAsString`() {
        buffer.writeText("Hello")
        assertEquals(buffer.getScreenAsString(), buffer.getAllAsString())
    }
}