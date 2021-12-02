package math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class EvaluationTest {
	@ParameterizedTest
	@DisplayName("Checking null/empty/white-space expressions")
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   ", "\t", "\n" })
	public void emptyNullExpr(String expr) {
		Exception e = assertThrows(MathException.class, ()->MathParser.expressionEvaluation(expr,null));
		assertEquals("Expression string cannot be null or empty",e.getMessage());
	}
	
	@ParameterizedTest
	@DisplayName("Checking lexically wrong expressions")
	@ValueSource(strings = { "x + ", "x * ", " + ", " / ", " / (a, b) " })
	public void wrongExpr(String expr) throws MathException {
		Exception e = assertThrows(MathException.class, ()->MathParser.expressionEvaluation(expr,null));
		assertEquals("Expression syntax is wrong!",e.getMessage());
	}
	
	@ParameterizedTest
	@DisplayName("Checking expressions with uncovered functions")
	@ValueSource(strings = { "x + myFunc(y)", "newFunc(x * y)", " f(x,y,z) ", " g(f(x)) ", " h(g(x),f(y)) " })
	public void missingFunction(String expr) throws MathException {
		Exception e = assertThrows(MathException.class, ()->MathParser.expressionEvaluation(expr,null));
		assertEquals("Expression syntax is wrong!",e.getMessage());
	}
	
	@ParameterizedTest
	@DisplayName("Checking expressions with function mispellings")
	@ValueSource(strings = { "sint(x)", "cosr(x * y)", " lns(x) + y ", " radu(x * y) ", " now(d) " })
	public void functionMispell(String expr) throws MathException {
		Exception e = assertThrows(MathException.class, ()->MathParser.expressionEvaluation(expr,null));
		assertEquals("Expression syntax is wrong!",e.getMessage());
	}
	
	@ParameterizedTest
	@DisplayName("Checking whether variables are missing")
	@ValueSource(strings = { "x + y", "x * y", " x / y ", " sin(x) ", " cos(x) " })
	public void varsTotallyMissing(String expr) throws MathException {
		Exception e = assertThrows(MathException.class, ()->MathParser.expressionEvaluation(expr,null));
		assertEquals("Did not supply any mapping for the missing variables in the given expression",e.getMessage());
	}
	
	@ParameterizedTest
	@DisplayName("Checking whether some variables were not covered")
	@CsvSource({
		"x + y, x, 10.0",
		"x * y, y, 20.0",
		"x / y / z, z, 5.0",
		"sin(x) + cos(y) + tan(z), y, 5.0"
	})
	public void someVarsMissing(String expr, String var, Double val) {
		Map<String,Double> map = new HashMap<String,Double>();
		map.put(var,val);
		System.out.println("Map is: " + map);
		Exception e = assertThrows(MathException.class, ()->MathParser.expressionEvaluation(expr,map));
		assertEquals("The mapping supplied did not cover some of the missing variables in the given expression",e.getMessage());
	}
	
	@ParameterizedTest
	@DisplayName("Checking whether variables are missing with non-empty map")
	@CsvSource({
		"x + y, z, 10.0",
		"x * y, t, 20.0",
		"x / y / z, k, 5.0",
		"sin(x) + cos(y) + tan(z), l, 5.0"
	})
	public void allVarsMissingNonEmptyMap(ArgumentsAccessor acc) {
		String expr = acc.getString(0);
		Map<String,Double> map = new HashMap<String,Double>();
		map.put(acc.getString(1),acc.getDouble(2));
		Exception e = assertThrows(MathException.class, ()->MathParser.expressionEvaluation(expr,map));
		assertEquals("The mapping supplied did not cover any of the missing variables in the given expression",e.getMessage());
	}
	
	@ParameterizedTest
	@DisplayName("Checking correct result with precise map")
	@CsvSource({
		"x + y, x, 10.0, y, 20.0",
		"x * y, x, 4.0, y, 5.0",
		"x / y / z, x, 20.0, y, 10.0, z, 2.0",
		"sin(x) + cos(y) + tan(z), x, 90.0, y, 90.0, z, 90.0"
	})
	public void correctResPreciseMap(@AggregateWith(MapValueAggregator.class) Map<String,Map<String,Double>> cMap) throws MathException {
		String expr = cMap.keySet().iterator().next();
		Map<String,Double> map = cMap.get(expr);
		assertNotEquals(Double.NaN,MathParser.expressionEvaluation(expr,map));
	}
	
	@ParameterizedTest
	@DisplayName("Checking correct result with imprecise map")
	@CsvSource({
		"x + y, x, 10.0, y, 20.0, z, 30.0",
		"x * y, x, 4.0, y, 5.0, t, 20.0",
		"x / y / z, x, 20.0, y, 10.0, z, 2.0, l, 4.0",
		"sin(x) + cos(y) + tan(z), x, 90.0, y, 90.0, z, 90.0, m, 100.0"
	})
	public void correctResImpreciseMap(@AggregateWith(MapValueAggregator.class) Map<String,Map<String,Double>> cMap) throws MathException {
		String expr = cMap.keySet().iterator().next();
		Map<String,Double> map = cMap.get(expr);
		assertNotEquals(Double.NaN,MathParser.expressionEvaluation(expr,map));
	}
	
	@ParameterizedTest
	@DisplayName("DivisionByZero Check")
	@CsvSource({
		"1 / 0",
		"x / y, x, 5.0, y, 0.0",
		"1 / (5-5)",
		"1 / (x-y), x, 5.0, y, 5.0"
	})
	public void divisionByZero(@AggregateWith(MapValueAggregator.class) Map<String,Map<String,Double>> cMap) throws MathException {
		String expr = cMap.keySet().iterator().next();
		Map<String,Double> map = cMap.get(expr);
		assertEquals(Double.NaN,MathParser.expressionEvaluation(expr,map));
	}
	
	@ParameterizedTest
	@DisplayName("Computations with no Arguments")
	@CsvSource({
		"5 * 2, 10",
		"7 + 12, 19",
		"12 / 4, 3",
		"(2 + 3) * 6 / 3, 10"
	})
	public void correctComputationsWithNoArguments(String expr, Double result) throws MathException {
		assertEquals(result,MathParser.expressionEvaluation(expr,null));
	}
	
	@ParameterizedTest
	@DisplayName("Additions with arbitrary int Arguments")
	@MethodSource("TwoIntsProvider")
	void checkCorrectAddition(int op1, int op2) throws MathException {
	    assertEquals(op1+op2,MathParser.expressionEvaluation("" + op1 + " + " + op2,null));
	}

	static Stream<Arguments> TwoIntsProvider() {
		Random r = new Random();
	    return Stream.of(
	        arguments(r.nextInt(100),r.nextInt(100)),
	        arguments(r.nextInt(100),r.nextInt(100)),
	        arguments(r.nextInt(100),r.nextInt(100)),
	        arguments(r.nextInt(100),r.nextInt(100)),
	        arguments(r.nextInt(100),r.nextInt(100)),
	        arguments(r.nextInt(100),r.nextInt(100)),
	        arguments(r.nextInt(100),r.nextInt(100)),
	        arguments(r.nextInt(100),r.nextInt(100)),
	        arguments(r.nextInt(100),r.nextInt(100)),
	        arguments(r.nextInt(100),r.nextInt(100))
	    );
	}

	@TestFactory
	@DisplayName("Multiplications with arbitrary int Arguments")
    public Stream<DynamicTest> checkCorrectMults() {

        Iterator<Integer> inputGenerator = new Iterator<Integer>() {

	        Random random = new Random();
	        int current;
	
	        @Override
	         public boolean hasNext() {
	              current = random.nextInt(100);
	              return !(current > 90);
	         }
	        @Override
	        public Integer next() {
	            return current;
	        }
        };

        Function<Integer, String> displayNameGenerator = (input) -> "MultCase:" + input;
        ThrowingConsumer<Integer> testExecutor = (input) -> assertEquals(input * 4, MathParser.expressionEvaluation("4 * " + input,null));
        
        return DynamicTest.stream(inputGenerator, displayNameGenerator, testExecutor);
	}
	
	@TestFactory
	@DisplayName("Multiplications with arbitrary int Arguments")
    public Stream<DynamicTest> checkCorrectOps() {

        Iterator<Map<String,Double>> inputGenerator = new Iterator<Map<String,Double>>() {
	        Random random = new Random();
	        int oper1, oper2, op;
	        int times = 0;
	
	        @Override
	         public boolean hasNext() {
	              oper1 = random.nextInt(100);
	              oper2 = random.nextInt(100);
	              op = random.nextInt(4);
	              if (op == 2 && oper2 == 0) oper2 = 4;
	              if (times != 10) {
	            	  times++;
	            	  return true;
	              }
	              else {
	            	  times = 0;
	            	  return false;
	              }
	         }
	        @Override
	        public Map<String,Double> next() {
	        	Map<String,Double> map = new HashMap<String,Double>();
	            switch(op) {
	            	case 0: map.put(oper1 + " + " + oper2, (double)(oper1 + oper2)); break;
	            	case 1: map.put(oper1 + " * " + oper2, (double)(oper1 * oper2)); break;
	            	case 2: map.put(oper1 + " / " + oper2, (double)((double)oper1 / oper2)); break;
	            	case 3: map.put(oper1 + " ^ " + oper2, (double)(Math.pow(oper1, oper2))); break;
	            }
	            System.out.println("String EXPR: " + map.keySet().iterator().next());
	            
	            return map;
	        }
        };

        Function<Map<String,Double>, String> displayNameGenerator = (input) -> "MultCase:" + input;
        ThrowingConsumer<Map<String,Double>> testExecutor = (input) -> assertEquals(input.values().iterator().next(), MathParser.expressionEvaluation(input.keySet().iterator().next(),null));
        
        return DynamicTest.stream(inputGenerator, displayNameGenerator, testExecutor);
	}


}
