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
            screen.addLast(Line(width))
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
        scrollback.addFirst(screen.removeFirst())
        screen.addLast(Line(width))
        if (scrollback.size > scrollbackSize) {
            scrollback.removeLast()
        }
    }

    private fun advance(){
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
    fun write (char: Char?){
        var cell = screen[cursorRow].cells[cursorCol]
        if (cell.type == Type.PADDING) {
            screen[cursorRow].cells[cursorCol - 1].char = null
            screen[cursorRow].cells[cursorCol - 1].type = Type.NORMAL
        }
        if (isWide(char)){
            advance()
            moveCursorLeft(1)
            cell = screen.get(cursorRow).cells[cursorCol]
            cell.char = char
            cell.type = Type.WIDE
            cell.applyAttributes(currentAttributes)
            moveCursorRight(1)
            cell = screen[cursorRow].cells[cursorCol]
            cell.char = null
            cell.type = Type.PADDING
            cell.applyAttributes(currentAttributes)
        }
        else{
            if (cell.type == Type.WIDE ) {
                screen[cursorRow].cells[cursorCol + 1].char = null
                screen[cursorRow].cells[cursorCol + 1].type = Type.NORMAL
            }
            cell.char = char
            cell.applyAttributes(currentAttributes)
            cell.type = Type.NORMAL
        }
        advance()
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
        if (col !in 0..<width || row < 0 || row >= height) return null
        return screen[row].cells[col].char
    }

    fun getCharScrollback(row: Int, col: Int): Char? {
        if (col !in 0..<width || row < 0 || row >= scrollback.size) return null
        return scrollback[row].cells[col].char
    }

    fun getAttributesScreen(row: Int, col: Int): Attributes? {
        if (col !in 0..<width || row < 0 || row >= height) return null
        return screen[row].cells[col].toAttributes()
    }

    fun getAttributesScrollback(row: Int, col: Int): Attributes? {
        if (col !in 0..<width || row < 0 || row >= scrollback.size) return null
        return scrollback.get(row).cells[col].toAttributes()
    }

    fun insertText(text: String) {
        for (char in text) {
            val currentCell = screen[cursorRow].cells[cursorCol]

            if (currentCell.type == Type.PADDING) {
                moveCursorLeft(1)
            }

            if (screen[cursorRow].cells[cursorCol].char == null &&
                screen[cursorRow].cells[cursorCol].type != Type.PADDING) {
                write(char)
                continue
            }

            val charWidth = if (isWide(char)) 2 else 1

            var overflowStart = width - charWidth
            if (overflowStart > 0 && screen[cursorRow].cells[overflowStart].type == Type.PADDING) {
                overflowStart--
            }

            val overflow = mutableListOf<Cell>()
            for (i in overflowStart until width) {
                val src = screen[cursorRow].cells[i]
                overflow.add(Cell().also { copy ->
                    copy.char = src.char
                    copy.type = src.type
                    copy.applyAttributes(src.toAttributes())
                })
            }

            for (col in width - 1 downTo cursorCol + charWidth) {
                val src = screen[cursorRow].cells[col - charWidth]
                val dst = screen[cursorRow].cells[col]
                dst.char = src.char
                dst.type = src.type
                dst.applyAttributes(src.toAttributes())
            }

            for (col in cursorCol until cursorCol + charWidth) {
                screen[cursorRow].cells[col].char = null
                screen[cursorRow].cells[col].type = Type.NORMAL
            }

            write(char)

            var currentOverflow = overflow.filter { it.char != null || it.type == Type.PADDING }
            var row = cursorRow

            while (currentOverflow.isNotEmpty()) {
                if (row == height - 1) {
                    insertEmpty()
                } else {
                    row++
                }

                var nextOverflowStart = width - currentOverflow.size
                if (nextOverflowStart > 0 && screen[row].cells[nextOverflowStart].type == Type.PADDING) {
                    nextOverflowStart--
                }

                val nextOverflow = mutableListOf<Cell>()
                for (i in nextOverflowStart until width) {
                    val src = screen[row].cells[i]
                    nextOverflow.add(Cell().also { copy ->
                        copy.char = src.char
                        copy.type = src.type
                        copy.applyAttributes(src.toAttributes())
                    })
                }

                for (col in width - 1 downTo currentOverflow.size) {
                    val src = screen[row].cells[col - currentOverflow.size]
                    val dst = screen[row].cells[col]
                    dst.char = src.char
                    dst.type = src.type
                    dst.applyAttributes(src.toAttributes())
                }

                for (i in currentOverflow.indices) {
                    screen[row].cells[i].char = currentOverflow[i].char
                    screen[row].cells[i].type = currentOverflow[i].type
                    screen[row].cells[i].applyAttributes(currentOverflow[i].toAttributes())
                }

                currentOverflow = nextOverflow.filter { it.char != null || it.type == Type.PADDING }
            }
        }
    }

    fun getLineAsStringScreen(row: Int): String {
        if (row !in 0..<height) return ""
        return screen[row].toString()
    }

    fun getLineAsStringScrollback(row: Int): String {
        if (row !in 0..< scrollback.size) return ""
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

    fun resize(newWidth: Int, newHeight: Int, newScrollbackSize: Int) {
        val allLines = mutableListOf<Line>()
        for (i in scrollback.size - 1 downTo 0) allLines.add(scrollback[i])
        for (i in 0 until screen.size) allLines.add(screen[i])

        val reflowed = mutableListOf<Line>()
        var currentLine = Line(newWidth)
        var currentCol = 0

        for (line in allLines) {
            var lastContentIndex = -1
            for (i in line.cells.indices.reversed()) {
                if (line.cells[i].char != null || line.cells[i].type == Type.PADDING) {
                    lastContentIndex = i
                    break
                }
            }

            if (lastContentIndex == -1) continue

            for (i in 0..lastContentIndex) {
                val cell = line.cells[i]
                if (cell.type == Type.PADDING) continue

                val cellWidth = if (cell.type == Type.WIDE) 2 else 1

                if (currentCol + cellWidth > newWidth) {
                    reflowed.add(currentLine)
                    currentLine = Line(newWidth)
                    currentCol = 0
                }

                currentLine.cells[currentCol].char = cell.char
                currentLine.cells[currentCol].type = if (cell.type == Type.WIDE) Type.WIDE else Type.NORMAL
                currentLine.cells[currentCol].applyAttributes(cell.toAttributes())

                if (cell.type == Type.WIDE && currentCol + 1 < newWidth) {
                    currentLine.cells[currentCol + 1].char = null
                    currentLine.cells[currentCol + 1].type = Type.PADDING
                    currentLine.cells[currentCol + 1].applyAttributes(cell.toAttributes())
                }

                currentCol += cellWidth

                if (currentCol >= newWidth) {
                    reflowed.add(currentLine)
                    currentLine = Line(newWidth)
                    currentCol = 0
                }
            }

            if (lastContentIndex < line.cells.size - 1 && currentCol > 0) {
                reflowed.add(currentLine)
                currentLine = Line(newWidth)
                currentCol = 0
            }
        }

        if (currentCol > 0) reflowed.add(currentLine)

        width = newWidth
        height = newHeight
        scrollbackSize = newScrollbackSize

        screen.clear()
        scrollback.clear()

        if (reflowed.size <= newHeight) {
            repeat(newHeight - reflowed.size) { screen.addLast(Line(newWidth)) }
            for (line in reflowed) screen.addLast(line)
        } else {
            val scrollbackLines = reflowed.subList(0, reflowed.size - newHeight)
            val screenLines = reflowed.subList(reflowed.size - newHeight, reflowed.size)
            for (line in scrollbackLines.reversed()) {
                if (scrollback.size < newScrollbackSize) scrollback.addLast(line)
            }
            for (line in screenLines) screen.addLast(line)
        }

        cursorCol = cursorCol.coerceIn(0, width - 1)
        cursorRow = cursorRow.coerceIn(0, height - 1)
    }
}