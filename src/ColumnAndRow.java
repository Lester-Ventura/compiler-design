public class ColumnAndRow {
  public int column;
  public int row;

  public ColumnAndRow(int row, int column) {
    this.column = column;
    this.row = row;
  }

  public static ColumnAndRow calculate(int index, String source) {
    String[] lines = source.split("\n", -1);
    int currentLine = 0;

    for (; index > lines[currentLine].length(); index -= lines[currentLine++].length() + 1)
      ;
    return new ColumnAndRow(currentLine, index);
  }
}