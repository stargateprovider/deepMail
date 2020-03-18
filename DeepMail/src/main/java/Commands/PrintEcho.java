package Commands;

public class PrintEcho extends Command {

    String[] args;

    public PrintEcho(String[] args){
        this.args = args;
    }

    @Override
    public int task() {
        if(!hasCorrectInput()) return -1;

        StringBuilder builder = new StringBuilder();

        for (String arg : args) {
            builder.append(arg);
        }
        System.out.println(builder);

        return 1;
    }

    @Override
    public boolean hasCorrectInput() {
        return this.args.length > 0;
    }

    @Override
    public String errorMsg() {
        return "null\n" + usage();
    }

    @Override
    public String usage() {
        return "Usage: printecho arg1 arg2 arg3 ...";
    }

    @Override
    public String wrongInputErrorMsg() {
        return "PrintEcho requires at least one argument.\n" + usage();
    }

    @Override
    public String toString() {
        return "Outputs all inputs given by the user.\n" + usage();
    }
}
