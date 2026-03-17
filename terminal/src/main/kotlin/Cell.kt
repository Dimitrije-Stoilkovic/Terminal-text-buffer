fun isWide(char : Char?) : Boolean {
    if (char == null) return false
    val code = char.code
    return code in 0x1100..0x115F ||
           code in 0x2E80..0x303E ||
           code in 0x3040..0x33FF ||
           code in 0x3400..0x4DBF ||
           code in 0x4E00..0x9FFF ||
           code in 0xA000..0xA4CF ||
           code in 0xAC00..0xD7AF ||
           code in 0xF900..0xFAFF ||
           code in 0xFE10..0xFE1F ||
           code in 0xFE30..0xFE6F ||
           code in 0xFF00..0xFF60 ||
           code in 0xFFE0..0xFFE6
}


class Cell(
    var char: Char? = null,
    var type: Type? = null,
    var fgColor: Color = Color.DEFAULT,
    var bgColor: Color = Color.DEFAULT,
    var bold: Boolean = false,
    var italic: Boolean = false,
    var underline: Boolean = false,
    var strikethrough: Boolean = false
) {
    fun applyAttributes(attributes: Attributes) {
        fgColor = attributes.fgColor
        bgColor = attributes.bgColor
        bold = attributes.bold
        italic = attributes.italic
        underline = attributes.underline
        strikethrough = attributes.strikethrough
    }
    fun toAttributes(): Attributes = Attributes(fgColor, bgColor, bold, italic, underline, strikethrough)
}