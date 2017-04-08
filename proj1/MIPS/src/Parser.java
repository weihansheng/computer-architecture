/**
 * Created by Johan007 on 2017/4/7.
 */
//解析指令
public class Parser {
    public Parser() {
    }

    public Instruction parseInstruction(String line) {
        Instruction instruction = new Instruction();
        instruction.binary = line;
        String prefix = line.substring(0, 3); //提取前3位子串
        System.out.print("prefix--"+prefix+"  ");
        int opCode;
        InstructionFormat definition = Config.nullDefinition;

        if (prefix.equals("000")) { //获取指令字符串模板
            opCode = binaryToDec(line.substring(3, 6), false); //操作码为第3-第5位 共3位  false代表没有符号
            System.out.println("opCode--"+line.substring(3, 6)+"二进制："+opCode);
            line = line.substring(6);//截取后26位
            definition = Config.formatCategory1[opCode];
        } else if (prefix.equals("110")) {
            opCode = binaryToDec(line.substring(13, 16), false); //操作码为第13-第16位 共3位  false代表没有符号
            System.out.println("opCode--"+line.substring(13, 16)+"二进制："+opCode);
            line = line.substring(3,13)+line.substring(16,32);//截取后26位
            //System.out.println("截取的字符串为："+line);
            definition = Config.formatCategory2[opCode];
        } else if (prefix.equals("111")){
            opCode = binaryToDec(line.substring(13, 16), false); //操作码为第3-第5位 共3位  false代表没有符号
            System.out.println("opCode--"+line.substring(13, 16)+"二进制："+opCode);
            line = line.substring(3,13)+line.substring(16,32);//截取后26位
            //System.out.println("截取的字符串为："+line);
            definition = Config.formatCategory3[opCode];
        }else{
            // unrecogonized instruction
        }

        instruction.name = definition.name;
        instruction.template = definition.template;

        System.out.print(instruction.name + " ");//后台打印
        // 填充参数
        int[] args = new int[definition.argsLength.length]; //指令操作数的个数
        for (int i = 0; i != args.length; i++) { //获取每个操作数
            boolean isSignedArgs = false;
            int argsLength = definition.argsLength[i];//每个操作数的长度
            if (argsLength < 0) {
                isSignedArgs = true;
                argsLength = -argsLength;
            }
            String rawArgs = line.substring(0, argsLength);//获取操作数的二进制代码
            args[i] = Parser.binaryToDec(rawArgs, isSignedArgs);//将操作数转为十进制数
            System.out.print(" " + args[i]);
            line = line.substring(argsLength);
        }
        System.out.println();
        int[] orderedArgs = new int[definition.argsOrder.length];
        for (int i = 0; i != orderedArgs.length; i++) {
            orderedArgs[i] = args[definition.argsOrder[i]]; //得到每个操作数的顺序
        }
        // handle special args跳转指令
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

    //二进制转十进制  补码
    public static int binaryToDec(String binary, boolean hasSymbolBit) {
        int symbolFlag = 0;
        //System.out.println("转换前binary:"+binary);
        if (hasSymbolBit) {//有符号去掉第一位（符号位）
            symbolFlag = binary.charAt(0) - '0';
            binary = binary.substring(1);

        }

        char[] array = binary.toCharArray();
        int num = 0;
        for (int i = 0; i != array.length; i++) {
            int bit = (array[i] - '0') << (array.length - 1 - i);
            num = (num | bit);
        }
        //System.out.println("转换后binary:"+((symbolFlag << 31) | num));
        return (symbolFlag << 31) | num;
    }

    //十进制转二进制
    public static String DecToBinary(int num) {
        char[] array = new char[32];
        for (int i = 0; i != 32; i++) {
            int bit = ((num & (1 << (31 - i))) == 0) ? 0 : 1;
            array[i] = (char) ('0' + bit);
        }
        return new String(array);
    }
}
