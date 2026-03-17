import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class CursorTest {

    private lateinit var buffer: TerminalBuffer

    @BeforeEach
    fun setup() {
        buffer = TerminalBuffer(width = 80, height = 24, scrollbackSize = 100)
    }

    @Test
    fun `cursor initial values`() {
        assertEquals(Pair(0, 0), buffer.getCursorPosition())
    }

    @Test
    fun `move cursor down by 1`() {
        buffer.moveCursorDown(1)
        assertEquals(Pair(0, 1), buffer.getCursorPosition())
    }

    @Test
    fun `move cursor down by N`() {
        buffer.moveCursorDown(5)
        assertEquals(Pair(0, 5), buffer.getCursorPosition())
    }

    @Test
    fun `move cursor up by 1`() {
        buffer.setCursorPosition(0, 5)
        buffer.moveCursorUp(1)
        assertEquals(Pair(0, 4), buffer.getCursorPosition())
    }

    @Test
    fun `move cursor up by N`() {
        buffer.setCursorPosition(0, 10)
        buffer.moveCursorUp(5)
        assertEquals(Pair(0, 5), buffer.getCursorPosition())
    }

    @Test
    fun `move cursor right by 1`() {
        buffer.moveCursorRight(1)
        assertEquals(Pair(1, 0), buffer.getCursorPosition())
    }

    @Test
    fun `move cursor right by N`() {
        buffer.moveCursorRight(5)
        assertEquals(Pair(5, 0), buffer.getCursorPosition())
    }

    @Test
    fun `move cursor left by 1`() {
        buffer.setCursorPosition(5, 0)
        buffer.moveCursorLeft(1)
        assertEquals(Pair(4, 0), buffer.getCursorPosition())
    }

    @Test
    fun `move cursor left by N`() {
        buffer.setCursorPosition(10, 0)
        buffer.moveCursorLeft(5)
        assertEquals(Pair(5, 0), buffer.getCursorPosition())
    }

    @Test
    fun `move cursor up past row 0`() {
        buffer.moveCursorUp(9999)
        assertEquals(Pair(0, 0), buffer.getCursorPosition())
    }

    @Test
    fun `move cursor down past last row`() {
        buffer.moveCursorDown(9999)
        assertEquals(Pair(0, 23), buffer.getCursorPosition())
    }

    @Test
    fun `move cursor left past column 0`() {
        buffer.moveCursorLeft(9999)
        assertEquals(Pair(0, 0), buffer.getCursorPosition())
    }

    @Test
    fun `move cursor right past last column`() {
        buffer.moveCursorRight(9999)
        assertEquals(Pair(79, 0), buffer.getCursorPosition())
    }

    @Test
    fun `set cursor to valid position`() {
        buffer.setCursorPosition(10, 5)
        assertEquals(Pair(10, 5), buffer.getCursorPosition())
    }


    @Test
    fun `set cursor to negative col`() {
        buffer.setCursorPosition(-5, 0)
        assertEquals(Pair(0, 0), buffer.getCursorPosition())
    }

    @Test
    fun `set cursor to negative row`() {
        buffer.setCursorPosition(0, -5)
        assertEquals(Pair(0, 0), buffer.getCursorPosition())
    }

    @Test
    fun `set cursor to col beyond width`() {
        buffer.setCursorPosition(9999, 0)
        assertEquals(Pair(79, 0), buffer.getCursorPosition())
    }

    @Test
    fun `set cursor to row beyond height`() {
        buffer.setCursorPosition(0, 9999)
        assertEquals(Pair(0, 23), buffer.getCursorPosition())
    }
}