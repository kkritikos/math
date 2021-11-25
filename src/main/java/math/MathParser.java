package math;

import java.util.HashMap;
import java.util.Map;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

public class MathParser {
	public static boolean syntaxCheck(String expression) {
		Expression expr = new Expression(expression);
		if (!expr.checkLexSyntax()) {
			System.out.println("Lexical syntax of expression: " + expression + " is wrong!");
			return false;
		}
		else if (!expr.checkSyntax()) {
			String[] missingFuncs = expr.getMissingUserDefinedFunctions(); 
			if (missingFuncs != null && missingFuncs.length > 0) {
				System.out.println("Syntax of expression: " + expression + " is wrong as some user-defined functions are missing");
				return false;
			}
		}
		return true;
	}
	
	public static double expressionEvaluation(String expression, Map<String,Double> args) throws Exception {
		if (!syntaxCheck(expression)) throw new Exception("Expression syntax is wrong!");
		Expression expr = new Expression(expression);
		if (args != null && !args.isEmpty()) {
			for (String key: args.keySet()) {
				Double val = args.get(key);
				expr.addArguments(new Argument(key,val));
			}
		}
		return expr.calculate();
	}
}
