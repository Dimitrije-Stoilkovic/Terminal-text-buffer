import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

class WideCharTest {

    private lateinit var buffer: TerminalBuffer

    @BeforeEach
    fun setup() {
        buffer = TerminalBuffer(width = 10, height = 5, scrollbackSize = 100)
    }

    @Test
    fun `write wide char sets WIDE type at cursor`() {
        buffer.write('中')
        assertEquals(Type.WIDE, buffer.screen[0].cells[0].type)
    }

    @Test
    fun `write wide char sets PADDING type at next cell`() {
        buffer.write('中')
        assertEquals(Type.PADDING, buffer.screen[0].cells[1].type)
    }

    @Test
    fun `write wide char advances cursor by 2`() {
        buffer.write('中')
        assertEquals(Pair(2, 0), buffer.getCursorPosition())
    }

    @Test
    fun `write wide char at last column wraps to next line`() {
        buffer.setCursorPosition(9, 0)
        buffer.write('中')
        assertEquals(Type.WIDE, buffer.screen[1].cells[0].type)
        assertEquals(Type.PADDING, buffer.screen[1].cells[1].type)
    }

    @Test

    fun `write wide char at second to last column wraps to next line`() {
        buffer.setCursorPosition(9, 0)
        buffer.write('中')
        assertEquals(Pair(2, 1), buffer.getCursorPosition())
    }

    @Test
    fun `overwrite wide char clears padding cell`() {
        buffer.write('中')
        buffer.setCursorPosition(0, 0)
        buffer.write('A')
        assertEquals(Type.NORMAL, buffer.screen[0].cells[1].type)
        assertNull(buffer.screen[0].cells[1].char)
    }

    @Test
    fun `overwrite padding cell clears wide partner`() {
        buffer.write('中')
        buffer.setCursorPosition(1, 0)
        buffer.write('A')
        assertEquals(Type.NORMAL, buffer.screen[0].cells[0].type)
        assertNull(buffer.screen[0].cells[0].char)
    }

    @Test
    fun `getLineAsString skips padding cells`() {
        buffer.write('中')
        val line = buffer.getLineAsStringScreen(0)
        assertEquals('中', line[0])
        assertEquals(' ', line[1])
    }

    @Test
    fun `write multiple wide chars advances cursor correctly`() {
        buffer.write('中')
        buffer.write('文')
        assertEquals(Pair(4, 0), buffer.getCursorPosition())
        assertEquals(Type.WIDE, buffer.screen[0].cells[0].type)
        assertEquals(Type.WIDE, buffer.screen[0].cells[2].type)
    }

    @Test
    fun `insertText into line with wide char shifts correctly`() {
        buffer.writeText("AB")
        buffer.setCursorPosition(0, 0)
        buffer.insertText("中")
        assertEquals(Type.WIDE, buffer.screen[0].cells[0].type)
        assertEquals(Type.PADDING, buffer.screen[0].cells[1].type)
        assertEquals('A', buffer.getCharScreen(0, 2))
        assertEquals('B', buffer.getCharScreen(0, 3))
    }

    @Test
    fun `insertText does not split wide char during overflow`() {
        buffer.write('中')
        buffer.write('文')
        buffer.write('字')
        buffer.write('A')
        buffer.write('B')
        buffer.setCursorPosition(0, 0)
        buffer.insertText("X")
        // no PADDING cell should appear without a preceding WIDE cell
        for (col in 1 until buffer.width) {
            if (buffer.screen[0].cells[col].type == Type.PADDING) {
                assertEquals(Type.WIDE, buffer.screen[0].cells[col - 1].type)
            }
        }
    }

    @Test
    fun `normal char after wide chars has correct type`() {
        buffer.write('中')
        buffer.write('A')
        assertEquals(Type.NORMAL, buffer.screen[0].cells[2].type)
        assertEquals('A', buffer.getCharScreen(0, 2))
    }

    @Test
    fun `wide char attributes applied correctly`() {
        buffer.setAttributes(Attributes(fgColor = Color.RED, bold = true))
        buffer.write('中')
        assertEquals(Color.RED, buffer.getAttributesScreen(0, 0)?.fgColor)
        assertEquals(true, buffer.getAttributesScreen(0, 0)?.bold)
    }
}