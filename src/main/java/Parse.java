import ktlex.KotlinLexer;
import ktpars.KotlinParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Parse {
    private Metrics metrics = new Metrics();

    public Metrics getMetric() {
        return metrics;
    }

    public void reader() throws Exception {
        List<File> list = new ArrayList<>();
        Files.walk(Paths.get("C:\\Users\\valer\\IdeaProjects\\forparse\\src"), FileVisitOption.FOLLOW_LINKS)
                .map(Path::toFile)
                .forEach(f -> {
                    if (f.isFile()) list.add(f);
                });
        list.removeIf(f -> !f.getName().endsWith(".kt"));
        System.out.println(list);
        startParse(list.get(0));
    }

    public void startParse(File file) throws Exception {
        String key = "";
        String value = "";
        boolean fun = false;
        boolean val = false;
        boolean wait = false;
        boolean waitClass = false;
        int override = 0;
        int fields = 0;
        int bracket = 0;
        int a = 0;
        int b = 0;
        int c = 0;
        HashMap<String, String> extend = new HashMap<>();
        HashSet<String> operatorsA = new HashSet<String>(List.of("++", "--", "=", "*=", "/=", "%=", "+=", "-=", "<<=", ">>=", "&=", "!=", "^=", ">>>="));
        HashSet<String> operatorsC = new HashSet<String>(List.of("==", "!=", ">=", "<=", ">", "<", "!", "else", "if", "?", "try", "catch", "when"));
        CharStream charFile = CharStreams.fromFileName(file.getAbsolutePath());
        KotlinLexer lexer = new KotlinLexer(charFile);
        TokenStream tokens = new CommonTokenStream(lexer);
        KotlinParser parser = new KotlinParser(tokens);
        KotlinParser.KotlinFileContext tree = parser.kotlinFile();
        for (int i = 1; i < tokens.size(); i++) {
            String prevToken = tokens.get(i - 1).getText();
            String currToken = tokens.get(i).getText();
            String currTokenType = lexer.getVocabulary().getDisplayName(tokens.get(i).getType());
            if (prevToken.equals("class") && currTokenType.equals("Identifier")) {
                key = currToken;
                waitClass = true;
            }
            if ((currToken.equals("val") || currToken.equals("var") || currToken.equals("const")) && operatorsA.contains(tokens.get(i + 2).getText())) {
                val = true;
            }
            if (operatorsA.contains(currToken) && !val) {
                a++;
            }
            if (val && operatorsA.contains(currToken)) {
                val = false;
            }
            if (currTokenType.equals("Identifier") && tokens.get(i + 1).getText().equals("(") && !prevToken.equals("fun") && !waitClass) {
                b++;
            }
            if (operatorsC.contains(currToken)) {
                c++;
            }

            if (waitClass && prevToken.equals(":") && currTokenType.equals("Identifier") && tokens.get(i - 2).getText().equals(")")) {
                value = currToken;
                waitClass = false;
                extend.put(key, value);
            }
            if (waitClass && (currToken.equals("{") || currToken.equals("="))) {
                waitClass = false;
                extend.put(key, "");
                System.out.println("---------------check   " + extend + "   value  " + value);
            }
            if (currToken.equals("override")) {
                override++;
            }

            if (currToken.equals("fun")) {
                fun = true;
                wait = true;
            }
            if (fun) {
                if (currTokenType.equals("{")) {
                    wait = false;
                    bracket++;
                }
                if (currTokenType.equals("}")) {
                    bracket--;
                    if (!wait && bracket == 0) fun = false;
                }
            }
            if ((currToken.equals("var") || currToken.equals("val") || currToken.equals("const")) && !fun) {
                fields++;
            }
            //  System.out.println("token " + currToken + " tokentype " + currTokenType);
        }

        metrics.metricHandler(a, b, c, fields, override, extend);
        PrintTree.printTree(tree, 0, lexer);
        //  System.out.println("fields=" + fields + " override=" + override + " a metric=" + a + " b metric=" + b + " c metric=" + c);
    }


}
