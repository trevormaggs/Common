package common.operator;

/**
 * Client can choose a runtime implementation to perform a comparison operation on 2 operand values
 * dynamically. This interface enforces the implementing classes to execute a specific operation
 * based on a specific algorithm.
 * 
 * @author Trevor Maggs
 */
public interface OperationStrategy
{
    public <T extends Comparable<T>> boolean doOperation(T operand1, T operand2);
}