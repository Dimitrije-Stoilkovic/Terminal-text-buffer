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

    
}