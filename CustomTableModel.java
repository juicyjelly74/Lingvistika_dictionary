import javax.swing.table.AbstractTableModel;
import java.util.Map;

public class CustomTableModel extends AbstractTableModel {
    Map<String, Integer> dictionary;

    CustomTableModel(Map dictionary) {
        super();
        this.dictionary = dictionary;
    }

    @Override
    public int getRowCount() {
        return dictionary.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int c) {
        String result = "";
        switch (c) {
            case 0:
                result = "Word";
                break;
            case 1:
                result = "Amount";
                break;
        }
        return result;
    }

    @Override
    public Object getValueAt(int r, int c) {
        Object[] entries = dictionary.entrySet().toArray();
        Map.Entry entry = (Map.Entry) entries[r];
        if (c == 0) {
            return entry.getKey();
        } else {
            return entry.getValue();
        }
    }
}