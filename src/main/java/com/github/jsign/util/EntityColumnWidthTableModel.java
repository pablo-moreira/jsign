/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jsign.util;

import javax.swing.JTable;

/**
 *
 * @author pablo-moreira
 */
public abstract class EntityColumnWidthTableModel<E> extends EntityTableModel<E> {

    public EntityColumnWidthTableModel(JTable table) {
        super(table);
        init();
    }
    
    abstract public Integer[] getColumnsWidth();
    
    public final void init() {
        for (int i=0; i < getTable().getColumnModel().getColumnCount(); i++) {
            getTable().getColumnModel().getColumn(i).setPreferredWidth(getColumnsWidth()[i]);
        }
    }
}
