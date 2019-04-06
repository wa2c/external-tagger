package com.wa2c.java.externaltagger.view.component;

import com.wa2c.java.externaltagger.controller.source.AbstractExternalSource;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class SourceTableModel extends DefaultTableModel {

    private static String[] columnNames = new String[] { "Check", "Name" };

    public SourceTableModel(List<AbstractExternalSource> externalSource){
        super(columnNames, 0);
        for (AbstractExternalSource anExternalSource : externalSource) {
            Object[] val = new Object[2];
            val[0] = anExternalSource.getEnabled();
            val[1] = anExternalSource.getName();
           addRow(val);
        }
    }

    public Class getColumnClass(int col){
        //return getValueAt(0, col).getClass();
        if (col == 0)
            return Boolean.class;
        else
            return String.class;
    }

}
