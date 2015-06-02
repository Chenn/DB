package ra;

import java.util.Iterator;
import java.util.List;

import dao.Schema;
import dao.Tuple;

/**
 * A operator that caches all the tuples
 * in memory
 */
public class OperatorCache implements Operator {
	
	private List<Tuple> tuples;
	private Iterator<Tuple> iter;
	private Schema schema;
	
	public OperatorCache(List<Tuple> tuplesIn){
		if(tuplesIn==null)
			throw new IllegalArgumentException();
		
		if(tuplesIn.size()==0)
			schema = null;
		else
			schema = tuplesIn.get(0).getSchema();
		
		tuples = tuplesIn;
		iter = tuples.iterator();
	}
	
	@Override
	public Tuple readOneTuple() {
		if(iter.hasNext())
			return iter.next();
		else
			return null;
	}

	@Override
	public void reset() {
		iter = tuples.iterator();
	}
	
	@Override
	public void close(){}


	@Override
	public long getLength() {
		if(tuples.size()!=0){
			Tuple t = tuples.get(0);
			long unitSize = t.getBytes();
			return tuples.size()*unitSize;	
		}else
			return 0;
		
	}


	@Override
	public Schema getSchema() {
		return schema;
	}

}
