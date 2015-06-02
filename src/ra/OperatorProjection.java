package ra;

import dao.Schema;
import dao.Tuple;

/**
 * Operator projection + Limit,
 * limit is an SQL sentence that constrains
 * the final result size of the output
 */
public class OperatorProjection implements Operator{
	private Operator input;
	private Schema newSchema;
	
	public OperatorProjection(Operator inputIn, Schema schemaIn){
		input = inputIn;
		newSchema = schemaIn;
	}
	
	@Override
	//project only variable in readOneTuple() method
	public Tuple readOneTuple() {
		Tuple tuple = input.readOneTuple();
		if(tuple==null)
			return null;
		tuple.changeTuple(newSchema);
		return tuple;
	}

	
	@Override
	public void reset() {
		input.reset();
	}

	@Override
	public long getLength() {
		return input.getLength();
	}
	
	@Override
	public Schema getSchema() {
		return input.getSchema();
	}

	@Override
	public void close() {
		input.close();
	}

}
