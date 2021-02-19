public class Main {


    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Incorrect args");
            throw new IllegalArgumentException();
        }
        Parse p = new Parse();
        p.reader(args[0]);
        p.getMetric().summary(args[1]);
    }


}