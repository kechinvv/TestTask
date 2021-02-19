import ktlex.KotlinLexer;
import ktpars.KotlinParser;
import org.antlr.v4.runtime.*;
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
        Parse p = new Parse();
        p.reader();
        p.getMetric().summary();
    }


}