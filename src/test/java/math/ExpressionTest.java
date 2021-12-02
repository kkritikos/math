package math;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class ExpressionTest {
	@ParameterizedTest
	@DisplayName("Checking null/empty/white-space expressions")
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   ", "\t", "\n" })
	public void emptyNullExpr(String expr) {
		Exception e = assertThrows(MathException.class, ()->MathParser.syntaxCheck(expr));
		assertEquals("Expression string cannot be null or empty",e.getMessage());
	}
	
	@ParameterizedTest
	@DisplayName("Checking lexically wrong expressions")
	@ValueSource(strings = { "x + ", "x * ", " + ", " / ", " / (a, b) " })
	public void wrongExpr(String expr) throws MathException {
		assertFalse(MathParser.syntaxCheck(expr));
	}
	
	@ParameterizedTest
	@DisplayName("Checking expressions with uncovered functions")
	@ValueSource(strings = { "x + myFunc(y)", "newFunc(x * y)", " f(x,y,z) ", " g(f(x)) ", " h(g(x),f(y)) " })
	public void missingFunction(String expr) throws MathException {
		assertFalse(MathParser.syntaxCheck(expr));
	}
	
	@ParameterizedTest
	@DisplayName("Checking expressions with function mispellings")
	@ValueSource(strings = { "sint(x)", "cosr(x * y)", " lns(x) + y ", " radu(x * y) ", " now(d) " })
	public void functionMispell(String expr) throws MathException {
		assertFalse(MathParser.syntaxCheck(expr));
	}
	
	@ParameterizedTest
	@DisplayName("Checking legal expressions")
	@ValueSource(strings = { "x", "x * y", " x^y/z ", " x + sin(y) + tan(z) + ln(t) ", " sin(90) + cos(180) + tan(45) " })
	public void correctExpr(String expr) throws MathException {
		assertTrue(MathParser.syntaxCheck(expr));
	}
}
