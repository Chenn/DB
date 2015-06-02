package sqlparse;

import java.rmi.UnexpectedException;
import java.util.Map;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * Parse Table from FromItem
 * Visitor pattern
 * if a FromItem is a Table,
 * then put it into fromItemTable
 */
public class TableParser implements FromItemVisitor {

	private boolean isSubSelect;
	private boolean isSubJoin;
	private boolean isTable;
	
	private Map<String, Table> fromItemTable;
	
	public TableParser(Map<String, Table> localTableIn){
		if(localTableIn==null)
			try {
				throw new UnexpectedException("Input Argument is null!");
			} catch (UnexpectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		setFromItemTable(localTableIn);
	}
	

	public boolean isSubSelect() {
		return isSubSelect;
	}

	public void setSubSelect(boolean isSubSelect) {
		this.isSubSelect = isSubSelect;
	}

	boolean isSubJoin() {
		return isSubJoin;
	}

	void setSubJoin(boolean isSubJoin) {
		this.isSubJoin = isSubJoin;
	}

	public boolean isTable() {
		return isTable;
	}

	public void setTable(boolean isTable) {
		this.isTable = isTable;
	}

	public Map<String, Table> getFromItemTable() {
		return fromItemTable;
	}

	public void setFromItemTable(Map<String, Table> fromItemTable) {
		this.fromItemTable = fromItemTable;
	}
	
	@Override
	public void visit(Table arg0) {
		String tname = arg0.getAlias();
		if(tname==null){
			tname = arg0.getName();
		}

		getFromItemTable().put(tname, arg0);
		
		setTable(true);
		setSubJoin(false);
		setSubSelect(false);
	}

	@Override
	public void visit(SubSelect arg0) {
		setTable(false);
		setSubJoin(false);
		setSubSelect(true);
	}

	@Override
	public void visit(SubJoin arg0) {
		setTable(false);
		setSubJoin(true);
		setSubSelect(false);
	}


}
