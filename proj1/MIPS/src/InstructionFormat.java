/**
 * Created by Johan007 on 2017/4/7.
 */
public class InstructionFormat {
    public String name; //指令名
    public String template;// 示例
    public int[] argsOrder; // 每个操作数的顺序
    public int[] argsLength; //每个操作数长度

    public InstructionFormat(String name, String template, int[] argsOrder, int[] argsLength) {
        this.name = name;
        this.template = template;
        this.argsOrder = argsOrder;
        this.argsLength = argsLength;
    }
}
