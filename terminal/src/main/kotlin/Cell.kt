class Cell(
    var char: Char = ' ',
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
}