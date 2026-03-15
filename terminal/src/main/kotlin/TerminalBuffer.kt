class TerminalBuffer(
    var width: Int,
    var height: Int,
    var scrollbackSize: Int
) {
    val screen =  ArrayDeque<Line>(height)
    val scrollback = ArrayDeque<Line>(scrollbackSize)

    var cursorCol: Int = 0
    var cursorRow: Int = 0

    var currentAttributes: Attributes = Attributes()

    init {
        repeat(height) {
            screen.addFirst(Line(width))
        }
    }

    fun setAttributes(attributes: Attributes) {
        currentAttributes = attributes
    }

    fun getCursorPosition():Pair<Int, Int>{
        return Pair(cursorCol, cursorRow)
    }

    fun setCursorPosition(col:Int, row:Int){
        cursorCol = col.coerceIn(0,width-1)
        cursorRow = row.coerceIn(0,height-1)
    }

    fun moveCursorUp(n: Int){
        cursorRow = (cursorRow - n).coerceIn(0, height - 1)
    }

    fun moveCursorDown(n: Int){
        cursorRow = (cursorRow + n).coerceIn(0, height - 1)
    }

    fun moveCursorLeft(n: Int){
        cursorCol = (cursorCol - n).coerceIn(0, width - 1)
    }

    fun moveCursorRight(n: Int){
        cursorCol = (cursorCol + n).coerceIn(0, width - 1)
    }

    fun clearScreen(){
        screen.clear()
        repeat(height) {
            screen.addLast(Line(width))
        }
    }

    fun clearScreenScrollback(){
        scrollback.clear()
        clearScreen()
    }

    fun insertEmpty(){
        screen.addLast(Line(width))
        scrollback.addFirst(screen.removeFirst())
        if (scrollback.size > scrollbackSize) {
            scrollback.removeLast()
        }
    }

    fun write (char: Char?){
        val cell = screen.get(cursorRow).cells[cursorCol]
        cell.char = char
        cell.applyAttributes(currentAttributes)
        if (cursorCol + 1 == width) {
            if (cursorRow + 1 == height) {
                insertEmpty()
                setCursorPosition(0, height - 1)
            } else {
                setCursorPosition(0, cursorRow + 1)
            }
        } else {
            moveCursorRight(1)
        }
    }

    fun writeText(text: String){
        for (char in text){
            write(char)
        }
    }

    fun fill(char: Char? = null){
        setCursorPosition(0, cursorRow)
        repeat(width) {
            write(char)
        }
    }

    fun getCharScreen(row: Int, col: Int): Char? {
        if (col < 0 || col >= width || row < 0 || row >= height) return null
        return screen.get(row).cells[col].char
    }

    fun getCharScrollback(row: Int, col: Int): Char? {
        if (col < 0 || col >= width || row < 0 || row >= scrollback.size) return null
        return scrollback.get(row).cells[col].char
    }

    fun getAttributesScreen(row: Int, col: Int): Attributes? {
        if (col < 0 || col >= width || row < 0 || row >= height) return null
        return screen.get(row).cells[col].toAttributes()
    }

    fun getAttributesScrollback(row: Int, col: Int): Attributes? {
        if (col < 0 || col >= width || row < 0 || row >= scrollback.size) return null
        return scrollback.get(row).cells[col].toAttributes()
    }
}