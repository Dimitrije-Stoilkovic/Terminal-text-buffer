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

    fun insertText(text: String){
        for (char in text){
            if (getCharScreen(cursorRow, cursorCol) == null){
                write(char)
                continue
            }

            val overflow = screen[cursorRow].cells[width - 1].let {
                Cell().also { copy ->
                    copy.char = it.char
                    copy.applyAttributes(it.toAttributes())
                }
            }

            for (col in width - 1 downTo cursorCol + 1) {
                val src = screen[cursorRow].cells[col - 1]
                val dst = screen[cursorRow].cells[col]
                dst.char = src.char
                dst.applyAttributes(src.toAttributes())
            }

            write(char)

            var currentOverflow: Cell? = if (overflow.char != null) overflow else null
            var row = cursorRow

            while (currentOverflow != null){
                if (row == height - 1) {
                    insertEmpty()
                } else row++
                val nextOverflow = screen[row].cells[width - 1].let {
                    Cell().also { copy ->
                        copy.char = it.char
                        copy.applyAttributes(it.toAttributes())
                    }
                }

                for (col in width - 1 downTo 1) {
                    val src = screen[row].cells[col - 1]
                    val dst = screen[row].cells[col]
                    dst.char = src.char
                    dst.applyAttributes(src.toAttributes())
                }

                screen[row].cells[0].char = currentOverflow.char
                screen[row].cells[0].applyAttributes(currentOverflow.toAttributes())

                currentOverflow = if (nextOverflow.char != null) nextOverflow else null
            }
        }
    }

    fun getLineAsStringScreen(row: Int): String {
        if (row < 0 || row >= height) return ""
        return screen[row].toString()
    }

    fun getLineAsStringScrollback(row: Int): String {
        if (row < 0 || row >= scrollback.size) return ""
        return scrollback[row].toString()
    }

    fun getScreenAsString(): String {
        val sb = StringBuilder()
        for (row in 0 until height) {
            sb.append(screen[row].toString())
            if (row < height - 1) {
                sb.append("\n")
            }
        }
        return sb.toString()
    }

    fun getAllAsString(): String {
        val sb = StringBuilder()
        for (row in scrollback.size-1 downTo  0) {
            sb.append(scrollback[row].toString())
            sb.append("\n")
        }
        sb.append(getScreenAsString())
        return sb.toString()
    }
}