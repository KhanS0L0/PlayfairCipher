package is.first.lab;

import org.apache.commons.io.IOUtils;
import java.awt.Point;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class PlayfairCipher {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final int size = 5;
    private static final int randomNumber = getRandomNumber(size, ALPHABET.length() - size);

    private static final char[][] firstMatrix = new char[size][size];

    private static final String firstKey = ALPHABET.substring(3, randomNumber);

    private static final char[][] secondMatrix = new char[size][size];

    private static final String secondKey = ALPHABET.substring(randomNumber);

    private static final String INPUT_FILE_PATH = "src/main/resources/Init.txt";

    private static final String OUTPUT_FILE_PATH = "src/main/resources/Result.txt";

    private static Point[] firstMatrixPositions;

    private static Point[] secondMatrixPositions;


    public static void main(String[] args) throws IOException {
        FileInputStream fis = null;
        FileOutputStream ops = null;

        try {
            fis = new FileInputStream(INPUT_FILE_PATH);
            ops = new FileOutputStream(OUTPUT_FILE_PATH);

            String data = IOUtils.toString(fis, StandardCharsets.UTF_8).trim();

            firstMatrixPositions = createTable(firstKey, firstMatrix);
            ops.write(("First secret key: " + firstKey + "\n").getBytes(StandardCharsets.UTF_8));
            ops.write(getMatrixString("First Matrix: ", firstMatrix).toString().getBytes(StandardCharsets.UTF_8));

            secondMatrixPositions = createTable(secondKey, secondMatrix);
            ops.write(("Second secret key: " + secondKey + "\n").getBytes(StandardCharsets.UTF_8));
            ops.write(getMatrixString("Second Matrix: ", secondMatrix).toString().getBytes(StandardCharsets.UTF_8));

            ops.write(("Text before encode: " + prepareText(data) + "\n").getBytes(StandardCharsets.UTF_8));
            data = encode(new StringBuilder(prepareText(data)));
            ops.write(("Text after encode:  " + data + "\n").getBytes(StandardCharsets.UTF_8));

            data = encode(new StringBuilder(prepareText(data)));
            ops.write(("Text after decode:  " + data + "\n").getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            System.err.println("SOMETHING WENT WRONG");
        } finally {
            assert fis != null;
            fis.close();
            assert ops != null;
            ops.close();
        }
    }

    /**
     * Подготавливаем текст
     * Все буквы приводятся к верхнему регистру
     * Все пробелы удаляются
     * Так как в латинице 26 букв мы объединяем I и J
     * */
    private static String prepareText(String str) {
        return str.toUpperCase().replaceAll("[^A-Z]", "").replace("J", "I");
    }

    /**
     * Заполнение матрицы буквами 5 на 5
     * В цикле идет побуквенное заполнение матрицы слева на право
     * В случае если есть совпадение(дупликация) мы пропускаем букву и идет дальше
     * Так же запоминаются все позиции символов
     * */
    private static Point[] createTable(String key, char[][] charTable) {
        Point[] positions = new Point[26];

        String str = prepareText(key + ALPHABET);

        for (int i = 0, k = 0; i < str.length(); i++) {
            char element = str.charAt(i);
            if (positions[element - 'A'] == null) {
                charTable[k / size][k % size] = element;
                positions[element - 'A'] = new Point(k % size, k / size);
                k++;
            }
        }
        return positions;
    }

    /**
     * Метод шифрования текста
     * */
    private static String encode(StringBuilder text){
        for (int i = 0; i < text.length(); i += 2)
            if (i == text.length() - 1) text.append(text.length() % 2 == 1 ? 'X' : "");

        for(int i = 0; i < text.length(); i +=2){
            char a = text.charAt(i);
            char b = text.charAt(i+1);

            int row1 = firstMatrixPositions[a - 'A'].y;
            int column1 = firstMatrixPositions[a - 'A'].x;

            int row2 = secondMatrixPositions[b - 'A'].y;
            int column2 = secondMatrixPositions[b - 'A'].x;

            if(column1 == column2){
                int temp = row1;
                row1 = row2;
                row2 = temp;
            }else{
                int temp = column1;
                column1 = column2;
                column2 = temp;
            }

            text.setCharAt(i, firstMatrix[row1][column1]);
            text.setCharAt(i + 1, secondMatrix[row2][column2]);
        }
        return text.toString();
    }

    /**
     * Генерируем случайное число в пределах от min до max
     * */
    public static int getRandomNumber(int min, int max) {
        Random random = new Random();
        return random.ints(min, max)
                .findFirst()
                .getAsInt();
    }

    /**
     * Простой вывод двумерных символьных массивов
     * */
    public static StringBuilder getMatrixString(String prefix, char[][] array){
        StringBuilder sb = new StringBuilder(prefix + "\n");
        for (char[] chars : array) {
            for (int j = 0; j < array.length; j++) {
                sb.append(chars[j]).append(" ");
            }
            sb.append("\n");
        }
        return sb.append("\n");
    }
}
