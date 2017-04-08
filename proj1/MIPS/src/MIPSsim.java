/**
 * Created by Johan007 on 2017/4/7.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
public class MIPSsim {

	public static void main(String[] args) throws FileNotFoundException, IOException, Throwable {
		File file = new File("E:/sample.txt");
		Reader reader = new Reader(file);
		Parser parser = new Parser();

		Simulator simulator = new Simulator(128, file.getAbsoluteFile().getParent());
		Instruction instruction = new Instruction();

		String line;
		while ((line = reader.read()) != null) {
			instruction = parser.parseInstruction(line);//解析二进制指令内容
			simulator.addInstruction(instruction);//把指令添加到Simulator中
			if (instruction.name.equals("BREAK")) {
				break;
			}
		}
		PrintStream dis_out = new PrintStream(file.getAbsoluteFile().getParent() + "/disassembly.txt");
		//if(instruction.isEmpty()){
		if(line == null){
			dis_out.print("The input file is empty.");
		}

		//解析"BREAK"后的数据
		ArrayList<Integer> data = new ArrayList<Integer>();
		while ((line = reader.read()) != null) {
            //System.out.println("line:"+line);
			data.add(parser.parserData(line));
		}
		//System.out.println(data.toString());
		simulator.setData(data.toArray());

		//simulator.printInstructions();

		simulator.printInstructions(dis_out);
		dis_out.close();
		simulator.run(); //开始运行指令
		reader.close();
	}
	}









