package ra;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import dao.Datum;
import dao.Tuple;

/**
 * Aggregate min function
 */
public class AggregatorMin extends Aggregator {

    private Map<String, Datum> minMap;
    private Expression paraExpr;

    public AggregatorMin(Function funcIn, String[] groupByNamesIn) {
        super(funcIn, groupByNamesIn);

        minMap = new HashMap<String, Datum>();

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

        if (!minMap.containsKey(key)) {
            //insert new
            minMap.put(key, newVal);
        } else {
            //update old, min
            Datum oldVal = minMap.get(key);
            //can not min Bool, String, Date
            double min = Math.min(oldVal.getNumericValue(), newVal.getNumericValue());
            oldVal.setNumericValue(min);
        }
    }

    @Override
    public Datum getValue(String key) {
        return minMap.get(key);
    }
}
