import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;

/**
 * java计算器2.0版本
 * @author  brownii
 */
public class Cal {
    static final byte LEVEL1 = 1;
    static final byte LEVEL2 = 2;
    static final byte LEVEL3 = 3;

    /**
     * 建立总栈
     */
    private static final Stack<MyStack> stacks = new Stack<>();

    /**
     * @param string 计算语句
     * @return 返回结果
     */
    private static double calculator(String string) {
        stacks.push(new MyStack());
        for (int i = 0, j = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == '(') {
                stacks.push(new MyStack());
                j = i + 1;
            } else if (c == ')') {
                if (string.charAt(i - 1) != ')') {
                    stacks.peek().push(Double.parseDouble(string.substring(j, i)));
                }
                handleCal();
                j = i + 1;
            } else if (!Character.isDigit(c)) {
                handleCal();
                for (Operation operation : Operation.values()) {
                    if (c == operation.getSymbol()) {
                        if (operation.getLevel() == LEVEL1) {
                            stacks.push(new MyStack());
                        } else {
                            stacks.peek().push(Double.parseDouble(string.substring(j, i)));
                        }
                        stacks.peek().push(operation);
                        j = i + 1;
                        break;
                    }
                }
            } else if (i == string.length() - 1) {
                stacks.peek().push(Double.valueOf(string.substring(j)));
                handleCal();
            }
        }
        return stacks.pop().cal();
    }

    private static void handleCal() {
        if (stacks.size() > 1) {
            if (stacks.peek().size() > 1 && stacks.peek().peek().getClass() != Operation.class) {
                MyStack stack = stacks.pop();
                stacks.peek().push(stack.cal());
                handleCal();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("welcome to brownii calculator");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            stacks.clear();
            String string = scanner.nextLine();
            if (string.equals("quit")) {
                System.out.println("bye");
                scanner.close();
                System.exit(0);
            } else {
                try {
                    System.out.println(new BigDecimal(Double.toString(Cal.calculator(string))).stripTrailingZeros().toPlainString());
                } catch (Exception e) {
                    System.out.println(string);
                }
            }
        }
    }
}

enum Operation {
    /**
     * 加法
     */
    Plus(Cal.LEVEL3, '+', Double::sum),
    /**
     * 减法
     */
    Sub(Cal.LEVEL3, '-', (x, y) -> x - y),
    /**
     * 乘法
     */
    Multi(Cal.LEVEL2, '*', (x, y) -> x * y),
    /**
     * 除法
     */
    Divi(Cal.LEVEL2, '/', (x, y) -> x / y),
    /**
     * 余法
     */
    Surplus(Cal.LEVEL2, '%', (x, y) -> x % y),
    /**
     * 叠加
     */
    Cumu(Cal.LEVEL1, '!', (x, y) -> {
        double num = Math.ceil(x);
        return (num + 1) * num / 2;
    }),
    /**
     * 开方
     */
    Sqrt(Cal.LEVEL1, '$', (x, y) -> Math.sqrt(x));

    Operation(byte level, char symbol, BiFunction<Double, Double, Double> function) {
        this.level = level;
        this.symbol = symbol;
        this.function = function;
    }

    private final byte level;
    private final char symbol;
    private final BiFunction<Double, Double, Double> function;

    public byte getLevel() {
        return level;
    }

    public char getSymbol() {
        return symbol;
    }

    public BiFunction<Double, Double, Double> getFunction() {
        return function;
    }
}

class MyStack extends Stack<Object> {
    private static final long serialVersionUID = 1L;

    public double cal() {
        double result;

        if (this.size() == 0) {
            return 0;
        } else if (this.size() == 1) {
            return (double) this.pop();
        }
        MyStack innerStack = new MyStack();
        TreeSet<Byte> symbolSet = new TreeSet<>();

        while (!this.isEmpty()) {
            Object o = this.pop();
            if (o.getClass() == Operation.class) {
                Operation operation = (Operation) o;
                if (operation == Operation.Cumu || operation == Operation.Sqrt) {
                    innerStack.push(((Operation) o).getFunction().apply((double) innerStack.pop(), 0d));
                    continue;
                }
                symbolSet.add(operation.getLevel());
            }
            innerStack.push(o);
        }
        Byte level = symbolSet.pollFirst();
        if (level != null) {
            while (!innerStack.isEmpty()) {
                Object o = innerStack.pop();
                if (o.getClass().equals(Operation.class)) {
                    Operation operation = (Operation) o;
                    if (((Operation) o).getLevel() == level) {
                        this.push(operation.getFunction().apply((double) this.pop(), (double) innerStack.pop()));
                        continue;
                    }
                }
                this.push(o);
            }
            result = this.cal();
        } else {
            return (double) innerStack.pop();
        }
        return result;
    }
}
