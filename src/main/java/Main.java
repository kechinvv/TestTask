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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {

    public static void main(String[] args) throws Exception {
        Main m = new Main();
        m.reader();
   }

    public void reader() throws Exception {
        List<File> list = new ArrayList<>();
        Files.walk(Paths.get("C:\\Users\\valer\\IdeaProjects\\KotlinAsFirst2019\\src"), FileVisitOption.FOLLOW_LINKS)
                .map(Path::toFile)
                .forEach(f -> {
                    if (f.isFile()) list.add(f);
                });
        list.removeIf(f -> !f.getName().endsWith(".kt"));
        System.out.println(list);
        startParse(list.get(0));
    }

    public static void startParse(File file) throws Exception{
        KotlinLexer lexer = new KotlinLexer(new ANTLRFileStream(file.getAbsolutePath()));
        TokenStream tokens = new CommonTokenStream(lexer);
        KotlinParser parser = new KotlinParser(tokens);
        printTree(parser.kotlinFile(), 0, lexer);
    }

    private static void printTree(ParseTree tree, int indentation, KotlinLexer lexer) throws Exception{
        printIndentation(indentation);
        String treeClassName = tree.getClass().getSimpleName();
        if (Objects.equals(treeClassName, "TerminalNodeImpl")) {
            TerminalNodeImpl node = (TerminalNodeImpl)tree;
            printNode(node, lexer);
        }
        else {
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

    private static void printIndentation(int indentation) throws Exception{
        String output = "";
        for (int i = 0; i < indentation; i++) {
            output += "  ";
        }
        System.out.print(output);
    }

    private static void printNode(TerminalNodeImpl node, KotlinLexer lexer) throws Exception{
        String nodeType = lexer.getVocabulary().getSymbolicName(getNodeTypeNumber(node));
        String nodeText = node.getText();
        if (Objects.equals(nodeType, "NL")) nodeText = "\\n";
        System.out.println("PsiElement" + "(" + nodeType + ")" + "('" + nodeText + "')");
    }

    private static int getNodeTypeNumber(TerminalNodeImpl node) throws Exception{
        return node.getSymbol().getType();
    }

    private static void printRule(ParseTree tree) throws Exception{
        char[] ruleName = tree.getClass().getSimpleName().replaceFirst("Context", "").toCharArray();
        ruleName[0] = Character.toLowerCase(ruleName[0]);
        System.out.println(ruleName);
    }
}