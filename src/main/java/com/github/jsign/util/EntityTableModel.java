/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jsign.util;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author pablo-moreira
 */
public abstract class EntityTableModel<E> extends AbstractTableModel {
    
    private JTable table;
    private TableRowSorter<EntityTableModel<E>> rowSorter;
    abstract public String[] getColumns();
    abstract public List<E> getEntities();

    protected void initRenders() {}
    
    public EntityTableModel(JTable table) {
        this.table = table;
        this.table.setModel(this);
        
        rowSorter = new TableRowSorter<EntityTableModel<E>>(this);
        this.table.setRowSorter(rowSorter);
        
        initRenders();
    }

    public TableRowSorter<EntityTableModel<E>> getRowSorter() {
        return rowSorter;
    }
    
    @Override
    public String getColumnName(int column) {
        return getColumns()[column];
    }
    
    @Override
    public int getRowCount() {
        return (getEntities() != null) ?  getEntities().size() : 0;
    }

    @Override
    public int getColumnCount() {
        return getColumns().length;
    }    
    
    public E getEntitySelected() {
        if (table.getSelectedRow() != -1) {
            int selectedRow = table.getSelectedRow();
            int selectedRowModel = table.convertRowIndexToModel(selectedRow);
            return getEntities().get(selectedRowModel);
        }
        else {
            return null;
        }
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return getValueAt(getEntities().get(rowIndex), columnIndex);
    }
    
    abstract public Object getValueAt(E entity, int columnIndex);

    public void removeRow(int selectedRow) {
        getEntities().remove(selectedRow);
        fireTableDataChanged();
    }
    
    public JTable getTable() {
        return table;
    }

    public List<E> getEntitiesSelecteds() {
         
        if (table.getSelectedRowCount() > 0) {
            
            List<E> selecteds = new ArrayList<E>();
            
            int[] selectedRows = table.getSelectedRows();
            
            for (int selectedRow : selectedRows) {
                int selectedRowModel = table.convertRowIndexToModel(selectedRow);                
                selecteds.add(getEntities().get(selectedRowModel));
            }
            
            return selecteds;
        }
        else {
            return new ArrayList<E>();
        }
    }
}