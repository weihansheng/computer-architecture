/*On my honour, I have neither given nor received unauthorized aid on this assignment.*/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MIPSsim {

	public static void main(String[] args) throws FileNotFoundException, IOException, Throwable {
		File file = new File(args[0]);
		Reader reader = new Reader(file);
		Parser parser = new Parser();

		Simulator simulator = new Simulator(256, file.getAbsoluteFile().getParent());
		Instruction instruction = new Instruction();

		String line;
		while ((line = reader.read()) != null) {
			instruction = parser.parseInstruction(line);
			simulator.addInstruction(instruction);
			if (instruction.name.equals("BREAK")) {
				break;
			}
		}
		PrintStream dis_out = new PrintStream(file.getAbsoluteFile().getParent() + "/disassembly.txt");
		//if(instruction.isEmpty()){
		if(line == null){
			dis_out.print("The input file is empty.");
		}

		ArrayList<Integer> data = new ArrayList<Integer>();
		while ((line = reader.read()) != null) {
			data.add(parser.parserData(line));
		}
		simulator.setData(data.toArray());

		//simulator.printInstructions();

		simulator.printInstructions(dis_out);
		dis_out.close();
		simulator.run();
		reader.close();
	}
	}

	class Parser {

		public Parser() {
		}

		public Instruction parseInstruction(String line) {
			Instruction instruction = new Instruction();
			instruction.binary = line;
			String prefix = line.substring(0, 2);
			int opCode = binaryToDec(line.substring(2, 6), false);
			line = line.substring(6); 
			InstructionFormat definition = Config.nullDefinition;

			if (prefix.equals("01")) {
				definition = Config.formatCategory1[opCode];
			} else if (prefix.equals("11")) {
				definition = Config.formatCategory2[opCode];
			} else {
				// unrecogonized instruction
			}

			instruction.name = definition.name;
			instruction.template = definition.template;

			//System.out.print(instruction.name + " ");
			// fill args
			int[] args = new int[definition.argsLength.length];
			for (int i = 0; i != args.length; i++) {
				boolean isSignedArgs = false;
				int argsLength = definition.argsLength[i];
				if (argsLength < 0) {
					isSignedArgs = true;
					argsLength = -argsLength;
				}
				String rawArgs = line.substring(0, argsLength);
				args[i] = Parser.binaryToDec(rawArgs, isSignedArgs);
				//System.out.print(" " + args[i]);
				line = line.substring(argsLength);
			}
			//System.out.println();
			int[] orderedArgs = new int[definition.argsOrder.length];
			for (int i = 0; i != orderedArgs.length; i++) {
				orderedArgs[i] = args[definition.argsOrder[i]];
			}
			// handle special args
			instruction.args = orderedArgs;
			if (instruction.name.equals("J")) {
				instruction.args[0] = instruction.args[0] << 2;
			} else if (instruction.name.equals("BEQ")) {
				instruction.args[2] = instruction.args[2] << 2;
			} else if (instruction.name.equals("BGTZ") || instruction.name.equals("BLTZ")) {
				instruction.args[1] = instruction.args[1] << 2;
			}

			return instruction;
		}

		public int parserData(String line) {
			return binaryToDec(line, true);
		}

		public static int binaryToDec(String binary, boolean hasSymbolBit) {
			int symbolFlag = 0;
			if (hasSymbolBit) {
				symbolFlag = binary.charAt(0) - '0';
				binary = binary.substring(1);
			}

			char[] array = binary.toCharArray();
			int num = 0;
			for (int i = 0; i != array.length; i++) {
				int bit = (array[i] - '0') << (array.length - 1 - i);
				num = (num | bit);
			}
			return (symbolFlag << 31) | num;
		}

		public static String DecToBinary(int num) {
			char[] array = new char[32];
			for (int i = 0; i != 32; i++) {
				int bit = ((num & (1 << (31 - i))) == 0) ? 0 : 1;
				array[i] = (char) ('0' + bit);
			}
			return new String(array);
		}
	}

	class Reader {

		private final FileReader reader;
		private final BufferedReader bufferedReader;

		public Reader(File file) throws FileNotFoundException {
			reader = new FileReader(file);
			bufferedReader = new BufferedReader(reader);
		}

		public String read() throws IOException {
			String line = bufferedReader.readLine();
			return line;
		}

		protected void close() throws IOException, Throwable {
			bufferedReader.close();
			reader.close();
		}
	}

	class Simulator {

		private Class clazz;
		private int[] R;
		private ArrayList<Instruction> instructionList;
		private int[] memory;
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
					int instructionIndex = (pc - startAddress) >> 2;
					int oldPc = pc;
					Instruction instruction = instructionList.get(instructionIndex);
					try {
						Method method = clazz.getDeclaredMethod(instruction.name, int[].class);
						method.invoke(this, instruction.args);
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
				out.print(position + "\t");
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

			for (int i = 0; i != R.length; i++) {
				if (i % 8 == 0) {
					out.printf("\nR%02d:", i);
				}
				out.print("\t" + R[i]);
			}
			out.println();
			out.println();
			out.print("Data");
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

		private void JR(int[] args) {
			int rs = args[0];
			pc = R[rs] - 4;
		}

		private void BEQ(int[] args) {
			int rs = args[0];
			int rt = args[1];
			int offset = args[2];
			if (R[rs] == R[rt]) {
				pc = pc + offset;
			}
		}

		private void BLTZ(int[] args) {
			int rs = args[0];
			int offset = args[1];
			if (R[rs] < 0) {
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

		private void SLL(int[] args) {
			int rd = args[0];
			int rt = args[1];
			int sa = args[2];
			R[rd] = R[rt] << sa;
		}

		private void SRL(int[] args) {
			int rd = args[0];
			int rt = args[1];
			int sa = args[2];
			R[rd] = R[rt] >>> sa;
		}

		private void SRA(int[] args) {
			int rd = args[0];
			int rt = args[1];
			int sa = args[2];
			R[rd] = R[rt] >> sa;
		}

		private void NOP(int[] args) {
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

		private void SLT(int[] args) {
			int rd = args[0];
			int rs = args[1];
			int rt = args[2];
			R[rd] = (R[rs] < R[rt]) ? 1 : 0;
		}

		private void ADDI(int[] args) {
			int rt = args[0];
			int rs = args[1];
			int immediate = args[2];
			R[rt] = R[rs] + immediate;
		}

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

	class Instruction {

		public String binary;
		public String name;
		public int[] args;
		public String template;

		@Override
			public String toString() {
				switch (args.length) {
					case 0:
						return name;
					case 1:
						return String.format(name + " " + template, args[0]);
					case 2:
						return String.format(name + " " + template, args[0], args[1]);
					case 3:
						return String.format(name + " " + template, args[0], args[1], args[2]);
					case 4:
						return String.format(name + " " + template, args[0], args[1], args[2], args[3]);
					case 5:
						return String.format(name + " " + template, args[0], args[1], args[2], args[3], args[4]);
					default:
						return "";
				}
			}
	}

	class Config {

		public static InstructionFormat[] formatCategory1 = {
			new InstructionFormat("J", "#%d", new int[]{0}, new int[]{26}),
			new InstructionFormat("JR", "%d, %d, %d, %d", new int[]{0, 1, 2, 3}, new int[]{5, 10, 5, 6}),
			new InstructionFormat("BEQ", "R%d, R%d, #%d", new int[]{0, 1, 2}, new int[]{5, 5, 16}),
			new InstructionFormat("BLTZ", "R%d, #%d", new int[]{0, 2}, new int[]{5, 5, 16}),
			new InstructionFormat("BGTZ", "R%d, #%d", new int[]{0, 2}, new int[]{5, 5, 16}),
			new InstructionFormat("BREAK", "", new int[]{}, new int[]{20, 6}),
			new InstructionFormat("SW", "R%d, %d(R%d)", new int[]{1, 2, 0}, new int[]{5, 5, -16}),
			new InstructionFormat("LW", "R%d, %d(R%d)", new int[]{1, 2, 0}, new int[]{5, 5, -16}),
			new InstructionFormat("SLL", "R%d, R%d, #%d", new int[]{2, 1, 3}, new int[]{5, 5, 5, 5, 6}),
			new InstructionFormat("SRL", "R%d, R%d, #%d", new int[]{2, 1, 3}, new int[]{5, 5, 5, 5, 6}),
			new InstructionFormat("SRA", "R%d, R%d, #%d", new int[]{2, 1, 3}, new int[]{5, 5, 5, 5, 6}),
			new InstructionFormat("NOP", "", new int[]{}, new int[]{5, 5, 5, 5, 6})
		};
		public static InstructionFormat[] formatCategory2 = {
			new InstructionFormat("ADD", "R%d, R%d, R%d", new int[]{2, 0, 1}, new int[]{5, 5, 5, 11}),
			new InstructionFormat("SUB", "R%d, R%d, R%d", new int[]{2, 0, 1}, new int[]{5, 5, 5, 11}),
			new InstructionFormat("MUL", "R%d, R%d, R%d", new int[]{2, 0, 1}, new int[]{5, 5, 5, 11}),
			new InstructionFormat("AND", "R%d, R%d, R%d", new int[]{2, 0, 1}, new int[]{5, 5, 5, 11}),
			new InstructionFormat("OR", "R%d, R%d, R%d", new int[]{2, 0, 1}, new int[]{5, 5, 5, 11}),
			new InstructionFormat("XOR", "R%d, R%d, R%d", new int[]{2, 0, 1}, new int[]{5, 5, 5, 11}),
			new InstructionFormat("NOR", "R%d, R%d, R%d", new int[]{2, 0, 1}, new int[]{5, 5, 5, 11}),
			new InstructionFormat("SLT", "R%d, R%d, R%d", new int[]{2, 0, 1}, new int[]{5, 5, 5, 11}),
			new InstructionFormat("ADDI", "R%d, R%d, #%d", new int[]{1, 0, 2}, new int[]{5, 5, -16}),
			new InstructionFormat("ANDI", "R%d, R%d, #%d", new int[]{0, 1, 2}, new int[]{5, 5, 16}),
			new InstructionFormat("ORI", "R%d, R%d, #%d", new int[]{0, 1, 2}, new int[]{5, 5, 16}),
			new InstructionFormat("XORI", "R%d, R%d, #%d", new int[]{0, 1, 2}, new int[]{5, 5, 16}),};
		public static InstructionFormat nullDefinition = new InstructionFormat("NULL", "NULL", new int[]{}, new int[]{});

		private Config() {
		}
	};

	class InstructionFormat {

		public String name;
		public String template;
		public int[] argsOrder;
		public int[] argsLength;

		public InstructionFormat(String name, String template, int[] argsOrder, int[] argsLength) {
			this.name = name;
			this.template = template;
			this.argsOrder = argsOrder;
			this.argsLength = argsLength;
		}
	}
