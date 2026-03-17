class Line(width : Int) {
    var cells: Array<Cell> = Array(width){ Cell()}

    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        for(cell in cells){
            if (cell.type == Type.PADDING) continue
            if (cell.char == null) sb.append(" ")
            else sb.append(cell.char)
        }
        return sb.toString()
    }

    //later add resize function
}