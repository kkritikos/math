package math;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

public class MapValueAggregator implements ArgumentsAggregator{

	@Override
	public Map<String,Map<String,Double>> aggregateArguments(ArgumentsAccessor acc, ParameterContext pc)
			throws ArgumentsAggregationException {
		Map<String,Map<String,Double>> res = new HashMap<String,Map<String,Double>>();
		String expr = acc.getString(0);
		Map<String,Double> map = new HashMap<String,Double>();
		for (int i = 1; i < acc.size(); i += 2) {
			map.put(acc.getString(i),acc.getDouble(i+1));
		}
		res.put(expr,map);
		
		return res;
	}

}
