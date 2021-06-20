package proyectofinalacr.utils;

import java.awt.*;

public class Out {
    public static Color ColorToPrint=Color.black;

    public static void error(String text){
        ColorToPrint=Color.pink;
        System.out.println(text);
        ColorToPrint=Color.black;
    }

    public static void info(String text){
        ColorToPrint=Color.ORANGE;
        System.out.println(text);
        ColorToPrint=Color.black;
    }

    public static void strong(String text){
        ColorToPrint=Color.BLACK;
        System.out.println(text);
        ColorToPrint=Color.black;
    }

    public static void log(String text){
        ColorToPrint=Color.DARK_GRAY;
        System.out.println(text);
        ColorToPrint=Color.black;
    }
}
