import ktlex.KotlinLexer;
import ktpars.KotlinParser;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {


    public static void main(String[] args) throws Exception {
        Main m = new Main();
        m.reader();
    }

    private static void printTree(ParseTree tree, int indentation, KotlinLexer lexer) throws Exception {
        printIndentation(indentation);
        String treeClassName = tree.getClass().getSimpleName();
        if (Objects.equals(treeClassName, "TerminalNodeImpl")) {
            TerminalNodeImpl node = (TerminalNodeImpl) tree;
            printNode(node, lexer);
        } else {
            printRule(tree);
            if (tree.getChildCount() == 0) {
                printIndentation(indentation);
                System.out.println("  <empty list>");
            }
            for (int i = 0; i < tree.getChildCount(); i++) {
                printTree(tree.getChild(i), indentation + 1, lexer);
            }
        }
    }

    private static void printIndentation(int indentation) throws Exception {
        String output = "";
        for (int i = 0; i < indentation; i++) {
            output += "  ";
        }
        System.out.print(output);
    }

    private static void printNode(TerminalNodeImpl node, KotlinLexer lexer) throws Exception {
        String nodeType = lexer.getVocabulary().getSymbolicName(getNodeTypeNumber(node));
        String nodeText = node.getText();
        if (Objects.equals(nodeType, "NL")) nodeText = "\\n";
        System.out.println("PsiElement" + "(" + nodeType + ")" + "('" + nodeText + "')");
    }

    private static int getNodeTypeNumber(TerminalNodeImpl node) throws Exception {
        return node.getSymbol().getType();
    }

    private static void printRule(ParseTree tree) throws Exception {
        char[] ruleName = tree.getClass().getSimpleName().replaceFirst("Context", "").toCharArray();
        ruleName[0] = Character.toLowerCase(ruleName[0]);
        System.out.println(ruleName);
    }


    public void startParse(File file) throws Exception {
        String key="";
        String value="";
        boolean fun = false;
        boolean val = false;
        boolean wait = false;
        boolean waitClass = false;
        int override = 0;
        int fields = 0;
        int bracket = 0;
        int a = 0;
        int c = 0;
        HashMap<String,String> extend = new HashMap<>();
        HashSet<String> operatorsA = new HashSet<String>(List.of("++", "--", "=", "*=", "/=", "%=", "+=", "-=", "<<=", ">>=", "&=", "!=", "^=", ">>>="));
        HashSet<String> operatorsC = new HashSet<String>(List.of("==", "!=", ">=", "<=", ">", "<", "!", "else", "if", "?", "try", "catch", "when"));
        KotlinLexer lexer = new KotlinLexer(new ANTLRFileStream(file.getAbsolutePath()));
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
            if (waitClass && prevToken.equals(":") && currTokenType.equals("Identifier") && tokens.get(i - 2).getText().equals(")")) {
                value=currToken;
                waitClass=false;
                extend.put(key,value);
            }
            if (currToken.equals("{") || currToken.equals("=")){
                waitClass=false;
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

            if (operatorsC.contains(currToken)) {
                c++;
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
            System.out.println("token " + currToken + " tokentype " + currTokenType);
        }
        //printTree(tree, 0, lexer);
        System.out.println("fields=" + fields + " override=" + override + " a metric=" + a + " c metric=" + c+ " key="+key+" value="+value);
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
}