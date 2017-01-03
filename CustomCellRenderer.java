import javax.swing.*;
import java.awt.*;
import java.io.File;

public class CustomCellRenderer extends JLabel implements ListCellRenderer {
    private static final Color HIGHLIGHT_COLOR = new Color(0, 0, 128);

    public CustomCellRenderer() {
        setOpaque(true);
        setIconTextGap(12);
    }

    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        File entry = (File) value;
        setText(entry.getPath().substring(0, entry.getPath().indexOf('\\') + 1)
                + "...\\..." + entry.getPath().substring(entry.getPath().lastIndexOf('\\')));
        if (isSelected) {
            setBackground(HIGHLIGHT_COLOR);
            setForeground(Color.white);
        } else {
            setBackground(Color.white);
            setForeground(Color.black);
        }
        return this;
    }
}