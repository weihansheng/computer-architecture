/**
 * Created by Johan007 on 2017/4/7.
 */
public class Config {

    public static InstructionFormat nullDefinition = new InstructionFormat("NULL", "NULL", new int[]{}, new int[]{});
    public static InstructionFormat[] formatCategory1 = {//000
            new InstructionFormat("J", "#%d", new int[]{0}, new int[]{26}), //000
            nullDefinition,
            new InstructionFormat("BEQ", "R%d, R%d, #%d", new int[]{0, 1, 2}, new int[]{5, 5, 16}),//010
            nullDefinition,
            new InstructionFormat("BGTZ", "R%d, #%d", new int[]{0, 2}, new int[]{5, 5, 16}),//010
            new InstructionFormat("BREAK", "", new int[]{}, new int[]{20, 6}),//101
            new InstructionFormat("SW", "R%d, %d(R%d)", new int[]{1, 2, 0}, new int[]{5, 5, 16}),//110
            new InstructionFormat("LW", "R%d, %d(R%d)", new int[]{1, 2, 0}, new int[]{5, 5, 16}),//111
    };
    public static InstructionFormat[] formatCategory2 = {//110
            new InstructionFormat("ADD", "R%d, R%d, R%d", new int[]{2, 0, 1}, new int[]{5, 5, 5, 11}),//000
            new InstructionFormat("SUB", "R%d, R%d, R%d", new int[]{2, 0, 1}, new int[]{5, 5, 5, 11}),//001
            new InstructionFormat("MUL", "R%d, R%d, R%d", new int[]{2, 0, 1}, new int[]{5, 5, 5, 11}),//010
            new InstructionFormat("AND", "R%d, R%d, R%d", new int[]{2, 0, 1}, new int[]{5, 5, 5, 11}),//011
            new InstructionFormat("OR", "R%d, R%d, R%d", new int[]{2, 0, 1}, new int[]{5, 5, 5, 11}),//100
            new InstructionFormat("XOR", "R%d, R%d, R%d", new int[]{2, 0, 1}, new int[]{5, 5, 5, 11}),//101
            new InstructionFormat("NOR", "R%d, R%d, R%d", new int[]{2, 0, 1}, new int[]{5, 5, 5, 11}),//110
            };
    public static InstructionFormat[] formatCategory3 = {//111
            new InstructionFormat("ADDI", "R%d, R%d, #%d", new int[]{1, 0, 2}, new int[]{5, 5, 16}),//000
            new InstructionFormat("ANDI", "R%d, R%d, #%d", new int[]{0, 1, 2}, new int[]{5, 5, 16}),//001
            new InstructionFormat("ORI", "R%d, R%d, #%d", new int[]{0, 1, 2}, new int[]{5, 5, 16}),//010
            new InstructionFormat("XORI", "R%d, R%d, #%d", new int[]{0, 1, 2}, new int[]{5, 5, 16}),};//011


    private Config() {
    }
}
