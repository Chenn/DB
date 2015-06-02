package ra;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import dao.Datum;
import dao.Tuple;

/**
 * Aggregate max function
 */
public class AggregatorMax extends Aggregator {

    private Map<String, Datum> maxMap;
    private Expression paraExpr;

    public AggregatorMax(Function funcIn, String[] groupByNamesIn) {
        super(funcIn, groupByNamesIn);

        maxMap = new HashMap<String, Datum>();

        @SuppressWarnings("unchecked")
        List<Expression> paraList = funcIn.getParameters().getExpressions();
        if (paraList.size() > 1)
            throw new UnsupportedOperationException("Not supported yet.");
        else
            paraExpr = paraList.get(0);
    }

    @Override
    public void aggregate(Tuple tuple, String key) {
        EvaluatorArithmeticExpres eval = new EvaluatorArithmeticExpres(tuple);
        paraExpr.accept(eval);
        Datum newVal = eval.getData();

        if (!maxMap.containsKey(key)) {
            //insert new
            maxMap.put(key, newVal);
        } else {
            //update old, max
            Datum oldVal = maxMap.get(key);
            //can not max Bool, String, Date
            double max = Math.max(oldVal.getNumericValue(), newVal.getNumericValue());
            oldVal.setNumericValue(max);
        }
    }

    @Override
    public Datum getValue(String key) {
        return maxMap.get(key);
    }
}
