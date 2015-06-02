package dao;

import java.util.List;

import net.sf.jsqlparser.schema.Table;

public class DAOTools {
	
	/**
	 * If the two Tables are the same
	 * @param left
	 * @param right
	 * @return
	 */
	public static boolean isSameTable(Table left, Table right){
		if(left!=null&&right!=null){
			if(left.getAlias()!=null&&right.getAlias()!=null){
				if(left.getAlias().equalsIgnoreCase(right.getAlias()))
					return true;
				else
					return false;
			}else if(left.getName().equalsIgnoreCase(right.getName()))
				return true;
			else
				return false;
		}else{
			throw new UnsupportedOperationException("Unexpected......  DAOTools.isSameTable(), left or right Table is null!"); 
		}
	}
	
	
	/**
	 * If the list Tables are the same
	 * @param tabList
	 * @return
	 */
	public static boolean isSameTable(List<Table> tabList){
		Table firstTab = tabList.get(0);
		//compare the first Table to the each of the rest Tables
		for(int i=1; i<tabList.size(); i++){
			Table restTab = tabList.get(i);
			if(!isSameTable(firstTab, restTab))
				return false;
		}
		return true;
	}



}
