package lexer;

public class ColumnAndRow {
    public int column;
    public int row;

    public ColumnAndRow(int row, int column) {
        this.column = column;
        this.row = row;
    }

    // Start at index 1 functions.
    public int getActualRow() {
        return row + 1;
    }

    public int getActualColumn() {
        return column;
    }

    public static ColumnAndRow calculate(int index, String source) {
        String[] lines = source.split("\n", -1);
        int currentLine = 0;
        int column = index;

        for (; column > lines[currentLine].length(); column -= lines[currentLine++].length() + 1)
            ;

        return new ColumnAndRow(currentLine, column);
    }
}
