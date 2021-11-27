package math;

import java.util.Map;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

public class MathParser {
	public static boolean syntaxCheck(String expression) throws MathException {
		if (expression == null || expression.trim().equals(""))
			throw new MathException("Expression string cannot be null or empty");
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
	
	private static boolean containsString(String[] array, String elem) {
		for (String s: array) if (s.equals(elem)) return true;
		return false;
	}
	
	public static double expressionEvaluation(String expression, Map<String,Double> args) throws MathException {
		if (!syntaxCheck(expression)) throw new MathException("Expression syntax is wrong!");
		Expression expr = new Expression(expression);
		String[] missingArgs = expr.getMissingUserDefinedArguments();
		if (missingArgs != null && missingArgs.length > 0) {
			int matches = 0;
			if (args != null && !args.isEmpty()) {
				for (String key: args.keySet()) {
					Double val = args.get(key);
					expr.addArguments(new Argument(key,val));
					if (containsString(missingArgs,key)) matches++;
				}
			}
			else {
				throw new MathException("Did not supply any mapping for the missing variables in the given expression");
			}
			if (matches == 0) throw new MathException("The mapping supplied did not cover any of the missing variables in the given expression");
			else if (matches < missingArgs.length) throw new MathException("The mapping supplied did not cover some of the missing variables in the given expression");
		}
		else {
			if (args != null && !args.isEmpty())
				System.out.println("Expression does not have any missing variable. The variable to value map is ignored");
		}
		return expr.calculate();
	}
}
