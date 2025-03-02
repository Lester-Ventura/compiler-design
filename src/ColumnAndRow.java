public class ColumnAndRow {
  public int column;
  public int row;

  public ColumnAndRow(int row, int column) {
      this.column = column;
      this.row = row;
  }

  // we add one to getRow and getColumn if debugging is important, but will not be consistent to orig numbering
  public int getRow() {
      return row; 
  }

  public int getColumn() {
      return column; 
  }

  public static ColumnAndRow calculate(int index, String source) {
      String[] lines = source.split("\n", -1);
      int currentLine = 0;
      int column = index;

      for (; column > lines[currentLine].length(); column -= lines[currentLine++].length() + 1);

      return new ColumnAndRow(currentLine, column);
  }
}
