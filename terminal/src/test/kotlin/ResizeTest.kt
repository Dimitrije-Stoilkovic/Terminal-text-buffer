import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class ResizeTest {

    private lateinit var buffer: TerminalBuffer

    @BeforeEach
    fun setup() {
        buffer = TerminalBuffer(width = 80, height = 24, scrollbackSize = 100)
    }

    @Test
    fun `resize updates width and height`() {
        buffer.resize(40, 12, 100)
        assertEquals(40, buffer.width)
        assertEquals(12, buffer.height)
    }

    @Test
    fun `resize updates scrollback size`() {
        buffer.resize(80, 24, 50)
        assertEquals(50, buffer.scrollbackSize)
    }

    @Test
    fun `resize screen has correct number of lines`() {
        buffer.resize(40, 12, 100)
        assertEquals(12, buffer.screen.size)
    }

    @Test
    fun `resize screen lines have correct width`() {
        buffer.resize(40, 12, 100)
        for (line in buffer.screen) {
            assertEquals(40, line.cells.size)
        }
    }

    @Test
    fun `resize preserves content when growing width`() {
        buffer.writeText("Hello")
        buffer.resize(120, 24, 100)
        assertEquals('H', buffer.getCharScreen(23, 0))
        assertEquals('e', buffer.getCharScreen(23, 1))
        assertEquals('l', buffer.getCharScreen(23, 2))
        assertEquals('l', buffer.getCharScreen(23, 3))
        assertEquals('o', buffer.getCharScreen(23, 4))
    }

    @Test
    fun `resize reflows content when shrinking width`() {
        buffer.writeText("Hello World")
        buffer.resize(5, 24, 100)
        assertEquals('H', buffer.getCharScreen(21, 0))
        assertEquals('o', buffer.getCharScreen(21, 4))
        assertEquals(' ', buffer.getCharScreen(22, 0))
        assertEquals('l', buffer.getCharScreen(22, 4))
        assertEquals('d', buffer.getCharScreen(23, 0))
    }

    @Test
    fun `resize preserves content when growing height`() {
        buffer.writeText("Hello")
        buffer.resize(80, 48, 100)
        assertEquals('H', buffer.getCharScreen(47, 0))
    }

    @Test
    fun `resize clamps cursor col when shrinking width`() {
        buffer.setCursorPosition(79, 0)
        buffer.resize(40, 24, 100)
        assertEquals(39, buffer.getCursorPosition().first)
    }

    @Test
    fun `resize clamps cursor row when shrinking height`() {
        buffer.setCursorPosition(0, 23)
        buffer.resize(80, 12, 100)
        assertEquals(11, buffer.getCursorPosition().second)
    }

    @Test
    fun `resize moves excess lines to scrollback`() {
        repeat(30) {
            buffer.writeText("line")
            buffer.insertEmpty()
        }
        buffer.resize(80, 10, 100)
        assertTrue(buffer.scrollback.isNotEmpty())
    }

    @Test
    fun `resize trims scrollback`() {
        repeat(50) {
            buffer.setCursorPosition(0, 0)
            buffer.writeText("line")
            buffer.insertEmpty()
        }
        buffer.resize(80, 24, 20)
        assertEquals(20, buffer.scrollback.size)
    }

    @Test
    fun `resize to same dimensions preserves content`() {
        buffer.writeText("Hello")
        buffer.resize(80, 24, 100)
        assertEquals('H', buffer.getCharScreen(23, 0))
    }

    @Test
    fun `resize preserves scrollback content`() {
        buffer.writeText("scrolled")
        buffer.insertEmpty()
        buffer.resize(80, 24, 100)
        assertEquals('s', buffer.getCharScreen(23, 0))
    }

    @Test
    fun `resize with wide chars preserves wide char`() {
        buffer.write('中')
        buffer.resize(80, 24, 100)
        assertEquals('中', buffer.getCharScreen(23, 0))
        assertEquals(Type.WIDE, buffer.screen[23].cells[0].type)
        assertEquals(Type.PADDING, buffer.screen[23].cells[1].type)
    }

    @Test
    fun `resize reflows wide char if it does not fit`() {
        val smallBuffer = TerminalBuffer(width = 5, height = 24, scrollbackSize = 100)
        smallBuffer.writeText("ABCD")
        smallBuffer.write('中')
        smallBuffer.resize(4, 24, 100)
        assertEquals('A', smallBuffer.getCharScreen(22, 0))
        assertEquals('中', smallBuffer.getCharScreen(23, 0))
    }
}