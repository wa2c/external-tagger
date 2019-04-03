package com.wa2c.java.externaltagger.view;

import javax.swing.*;

public class SourceTable extends JTable {

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column == 0)
            return true;
        else
            return false;
    }

}
