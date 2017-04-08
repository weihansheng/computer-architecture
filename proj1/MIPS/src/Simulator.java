import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Johan007 on 2017/4/7.
 */
public class Simulator {


    private Class clazz;
    private int[] R; //32个寄存器
    private ArrayList<Instruction> instructionList;
    private int[] memory; //内存大小为Break后的指令数
    private int startAddress;
    private int memoryStartAddress;
    private int pc;
    private int cycle;
    private boolean isRunning;
    private String workingDir;

    public Simulator(int startAddress, String workingDir) {
        this.startAddress = startAddress;
        this.cycle = 0;
        this.workingDir = workingDir;

        clazz = this.getClass();
        instructionList = new ArrayList<Instruction>();
        isRunning = false;
        R = new int[32];
        for (int i = 0; i != R.length; i++) {
            R[i] = 0;
        }
    }

    public void addInstruction(Instruction instruction) {
        instructionList.add(instruction);
    }

    public void setData(Object[] data) {
        memory = new int[data.length];
        for (int i = 0; i != data.length; i++) {
            memory[i] = (int) data[i];
        }
    }

    private void setMemory(int address, int value) {
        int index = (address - memoryStartAddress) >> 2;
        memory[index] = value;
    }

    public int getMemory(int address) {
        int index = (address - memoryStartAddress) >> 2;
        return memory[index];
    }

    //开始运行指令
    public void run() {
        try{
            PrintStream sim_out = new PrintStream(workingDir + "/simulation.txt");

            if(instructionList.isEmpty()){
                sim_out.print("The input file is empty.");
            }
            cycle = 1;
            pc = startAddress;
            isRunning = true;
            memoryStartAddress = startAddress + (instructionList.size() << 2);

            for (; isRunning; cycle++) {
                int instructionIndex = (pc - startAddress) >> 2; //右移二位相当于除以4
                //System.out.println("instructionIndex:"+(pc - startAddress)+"右移后："+instructionIndex);
                int oldPc = pc;
                Instruction instruction = instructionList.get(instructionIndex);
                try {
                    Method method = clazz.getDeclaredMethod(instruction.name, int[].class);
                    method.invoke(this, instruction.args); //调用指令
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, "Unsupported instruction " + instruction.name, ex);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
                }
                dump(oldPc, sim_out);
                pc = pc + 4;

            }
            sim_out.close();
        }catch(Exception err)
        {

        }
    }

    public void printInstructions(PrintStream out) {
        Iterator<Instruction> iter = instructionList.iterator();
        int position = startAddress;
        while (iter.hasNext()) {
            Instruction instruction = iter.next();
            out.print(instruction.binary + "\t");
            out.print(position + "\t");  //打印开始地址
            out.print(instruction);
            out.println();
            position = position + 4;
        }
        for (int i = 0; i != memory.length; i++) {
            out.print(Parser.DecToBinary(memory[i]) + "\t");
            out.print(position + "\t");
            out.print(memory[i]);
            out.println();
            position = position + 4;
        }
    }

    public void dump(int pc, PrintStream out) {
        int currentInstructionIndex = (pc - startAddress) >> 2;
        Instruction currentInstruction = instructionList.get(currentInstructionIndex);
        out.println("--------------------");
        out.printf("Cycle:%d\t%d\t", cycle, pc);
        out.print(currentInstruction);
        out.println();
        out.println();
        out.print("Registers");

        //打印寄存器中的值
        for (int i = 0; i != R.length; i++) {
            if (i % 8 == 0) {
                out.printf("\nR%02d:", i);
            }
            out.print("\t" + R[i]);
        }
        out.println();
        out.println();
        out.print("Data");
        //打印内存中的数据的值
        for (int i = 0; i != memory.length; i++) {
            if (i % 8 == 0) {
                out.printf("\n%3d:", memoryStartAddress + (i << 2));
            }
            out.print("\t" + memory[i]);
        }
        out.println();
        out.println();
    }

    private void J(int[] args) {
        int instrIndex = args[0];
        pc = instrIndex - 4;
    }



    //条件转移指令，当两个寄存器内容相等时转移发生  rs:5位 rt：5位 offset：16位
    private void BEQ(int[] args) {
        int rs = args[0];
        int rt = args[1];
        int offset = args[2];
        if (R[rs] == R[rt]) {
            pc = pc + offset;
        }
    }



    private void BGTZ(int[] args) {
        int rs = args[0];
        int offset = args[1];
        if (R[rs] > 0) {
            pc = pc + offset;
        }
    }

    private void BREAK(int[] args) {
        isRunning = false;
    }

    private void SW(int[] args) {
        int rt = args[0];
        int offset = args[1];
        int base = args[2];
        setMemory(R[base] + offset, R[rt]);
    }

    private void LW(int[] args) {
        int rt = args[0];
        int offset = args[1];
        int base = args[2];
        R[rt] = getMemory(R[base] + offset);
    }


    private void ADD(int[] args) {
        int rd = args[0];
        int rs = args[1];
        int rt = args[2];
        R[rd] = R[rs] + R[rt];
    }

    private void SUB(int[] args) {
        int rd = args[0];
        int rs = args[1];
        int rt = args[2];
        R[rd] = R[rs] - R[rt];
    }

    private void MUL(int[] args) {
        int rd = args[0];
        int rs = args[1];
        int rt = args[2];
        R[rd] = R[rs] * R[rt];
    }

    private void AND(int[] args) {
        int rd = args[0];
        int rs = args[1];
        int rt = args[2];
        R[rd] = R[rs] & R[rt];
    }

    private void OR(int[] args) {
        int rd = args[0];
        int rs = args[1];
        int rt = args[2];
        R[rd] = R[rs] | R[rt];
    }

    private void XOR(int[] args) {
        int rd = args[0];
        int rs = args[1];
        int rt = args[2];
        R[rd] = R[rs] ^ R[rt];
    }

    private void NOR(int[] args) {
        int rd = args[0];
        int rs = args[1];
        int rt = args[2];
        R[rd] = ~(R[rs] | R[rt]);
    }



    private void ADDI(int[] args) {
        int rt = args[0];
        int rs = args[1];
        int immediate = args[2];
        R[rt] = R[rs] + immediate;
    }

    //
    private void ANDI(int[] args) {
        int rt = args[0];
        int rs = args[1];
        int immediate = args[2];
        R[rt] = R[rs] & immediate;
    }

    private void ORI(int[] args) {
        int rt = args[0];
        int rs = args[1];
        int immediate = args[2];
        R[rt] = R[rs] | immediate;
    }

    private void XORI(int[] args) {
        int rt = args[0];
        int rs = args[1];
        int immediate = args[2];
        R[rt] = R[rs] ^ immediate;
    }
}
