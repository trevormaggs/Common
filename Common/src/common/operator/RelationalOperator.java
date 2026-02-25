package common.operator;

/**
 * <p>
 * Performs a comparison operation on 2 values supplied by the client at runtime. The type of the
 * operator that is dynamically performed on these 2 values represents a specific algorithm for
 * performing a specific operation or task. Below is the list of supported operators.
 * </p>
 * 
 * <ul>
 * <li><em>eq</em> or <em>==</em> Equal test</li>
 * <li><em>ne</em> or <em>!=</em> Not equal test</li>
 * <li><em>lt</em> Less than test</li>
 * <li><em>le</em> Less than or equal to test</li>
 * <li><em>gt</em> Greater than test</li>
 * <li><em>ge</em> Greater than or equal to test</li>
 * <li><em>=~</em> Regular expression test</li>
 * <li><em>!~</em> Negated regular expression test</li>
 * <li><em>ci</em> Can include. It is basically a case-insensitive equal operator. For Windows only</li>
 * <li><em>me</em> Must exclude. It is basically a case-insensitive equal operator. For Windows only</li>
 * </ul>
 * 
 * <p>
 * The logic is simple. It applies a strategy design pattern to encapsulate a group of disparate
 * comparison operations (operator algorithms). This class serves as a context class, whereas the
 * various algorithms are the strategy classes, which provide the context class with the necessary
 * operation that can be dynamically executed.
 * </p>
 * 
 * 
 * <p>
 * <b>Change logs:</b>
 * </p>
 * 
 * <ul>
 * <li>Trevor Maggs created on 14 January 2020</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @since 14 January 2020
 */
public final class RelationalOperator
{
    private OperationStrategy algorithm;

    /**
     * Private constructor to prevent this object from being instantiated.
     * 
     * @param strategy
     *        a type of strategy that represents a specific comparison operator
     */
    private RelationalOperator(OperationStrategy strategy)
    {
        algorithm = strategy;
    }

    /**
     * Executes the comparison operation based on the given strategy or algorithm.
     * 
     * @param <T>
     *        operand values to be used in the comparison
     * @param operand1
     *        the left value to be compared
     * @param operand2
     *        the right value to compare with the left value
     * 
     * @return boolean true if the operation is successful
     */
    private <T extends Comparable<T>> boolean executeStrategy(T operand1, T operand2)
    {
        return algorithm.doOperation(operand1, operand2);
    }

    /**
     * Public static factory method to perform a comparison operation on the 2 values.
     * 
     * @param <T>
     *        operand values to be used in the comparison
     * @param operand1
     *        the left value to be compared
     * @param operator
     *        the type of comparison operation to be performed
     * @param operand2
     *        the right value to compare with the left value
     * 
     * @return boolean true if the operation is successful
     * @throws IllegalArgumentException
     *         if the operator is undefined
     */
    public static <T extends Comparable<T>> boolean execute(T operand1, String operator, T operand2)
    {
        RelationalOperator context;
        OperationStrategy strategy;

        switch (operator)
        {
            case "eq":
            case "==":
                strategy = new OperationEqual();
            break;

            case "ne":
            case "!=":
                strategy = new OperationNotEqual();
            break;

            case "lt":
                strategy = new OperationLessThan();
            break;

            case "le":
                strategy = new OperationLessEqual();
            break;

            case "gt":
                strategy = new OperationGreaterThan();
            break;

            case "ge":
                strategy = new OperationGreaterEqual();
            break;

            case "=~":
                strategy = new OperationRegex();
            break;

            case "!~":
                strategy = new OperationRegexNegation();
            break;

            // For Windows only
            case "ci":
            case "me":
                strategy = new OperationEqualIgnoreCase();
            break;

            default:
                throw new IllegalArgumentException("Unknown operator type [" + operator + "]");
        }

        context = new RelationalOperator(strategy);

        return (context.executeStrategy(operand1, operand2));
    }
}