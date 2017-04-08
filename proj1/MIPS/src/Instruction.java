/**
 * Created by Johan007 on 2017/4/7.
 */
public class Instruction {
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
