package is.first.lab;

import org.apache.commons.io.IOUtils;
import java.awt.Point;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Arrays;

public class PlayfairCipher {

    private static final String RUSSIAN_ALPHABET = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ-./";
    private static final int SIZE_FOR_RUSSIAN_ALPHABET = 6;
    private static final int randomNumber = getRandomNumber(RUSSIAN_ALPHABET.length() - SIZE_FOR_RUSSIAN_ALPHABET);
    private static final char[][] firstMatrix = new char[SIZE_FOR_RUSSIAN_ALPHABET][SIZE_FOR_RUSSIAN_ALPHABET];
    private static final char[][] secondMatrix = new char[SIZE_FOR_RUSSIAN_ALPHABET][SIZE_FOR_RUSSIAN_ALPHABET];
    private static final String firstKey = RUSSIAN_ALPHABET.substring(3, randomNumber);
    private static final String secondKey = RUSSIAN_ALPHABET.substring(randomNumber);
    private static final String INPUT_FILE_PATH = "src/main/resources/Init.txt";
    private static final String OUTPUT_FILE_PATH = "src/main/resources/Result.txt";

    public static void main(String[] args) throws IOException {
        FileInputStream fis = null;
        FileOutputStream ops = null;

        try {
            fis = new FileInputStream(INPUT_FILE_PATH);
            ops = new FileOutputStream(OUTPUT_FILE_PATH);

            String data = IOUtils.toString(fis, StandardCharsets.UTF_8).trim();
            String result;
            data = prepareText(data);
            String[] bigrams = createBigrams(data);

            createTable(firstKey, firstMatrix);
            ops.write(("First secret key: " + firstKey + "\n").getBytes(StandardCharsets.UTF_8));
            ops.write(getMatrixString("First Matrix: ", firstMatrix).toString().getBytes(StandardCharsets.UTF_8));

            createTable(secondKey, secondMatrix);
            ops.write(("Second secret key: " + secondKey + "\n").getBytes(StandardCharsets.UTF_8));
            ops.write(getMatrixString("Second Matrix: ", secondMatrix).toString().getBytes(StandardCharsets.UTF_8));

            ops.write(("Text before encode: " + data + "\n").getBytes(StandardCharsets.UTF_8));
            ops.write(("Bigrams: " + Arrays.toString(bigrams) + "\n").getBytes(StandardCharsets.UTF_8));

            result = encode(bigrams);
            ops.write(("Text after encode:  " + result + "\n").getBytes(StandardCharsets.UTF_8));

            String[] resultBigrams = createBigrams(result);
            result = encode(createBigrams(result));
            ops.write(("Bigrams of encoded text: " + Arrays.toString(resultBigrams) + "\n").getBytes(StandardCharsets.UTF_8));
            ops.write(("Text after decode:  " + result + "\n").getBytes(StandardCharsets.UTF_8));

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
        str = str.toUpperCase().trim().replaceAll("[^А-Я-./]", "");
        if(str.length() % 2 != 0){
            str = str + ".";
        }
        return str;
    }

    /*
    * Метод для разбития строки на массив биграмм
    * */
    private static String[] createBigrams(String str){
        String[] array = new String[str.length()/2];
        for (int i = 0, k = 0; i < str.length(); i = i + 2){
            array[k] = str.substring(i, i+2);
            k++;
        }
        return array;
    }

    /**
     * Заполнение матрицы буквами 6 на 6
     * В цикле идет побуквенное заполнение матрицы слева на право
     * В случае если есть совпадение(дупликация) мы пропускаем букву и идет дальше
     * Так же запоминаются все позиции символов
     * */
    private static void createTable(String key, char[][] charTable) {
        String newAlphabet = cutAndInsert(key);
        int charIndex = 0;
        for (int i = 0; i < charTable.length; i++){
            for (int j = 0; j < charTable[i].length; j++){
                charTable[i][j] = newAlphabet.charAt(charIndex);
                charIndex++;
            }
        }
    }

    /**
     * Метод шифрования текста
     * */
    private static String encode(String[] bigrams){
        StringBuilder result = new StringBuilder();
        for (String bigram : bigrams) {

            char first = bigram.charAt(0);
            char second = bigram.charAt(1);

            Point firstPosition = findPositions(firstMatrix, first);
            System.out.println("first position: " + firstPosition);
            Point secondPosition = findPositions(secondMatrix, second);
            System.out.println("second position: " + secondPosition);

            if(firstPosition.getX() != secondPosition.getX()){
                result.append(firstMatrix[(int) firstPosition.getY()][(int) secondPosition.getX()]);
                result.append(secondMatrix[(int) secondPosition.getY()][(int) firstPosition.getX()]);
            }
            else if(firstPosition.getX() == secondPosition.getX()){
                result.append(firstMatrix[(int) secondPosition.getY()][(int) secondPosition.getX()]);
                result.append(secondMatrix[(int) firstPosition.getY()][(int) firstPosition.getX()]);
            }
        }


        return result.toString();
    }

    /**
     * Генерируем случайное число в пределах от min до max
     * */
    private static int getRandomNumber(int max) {
        Random random = new Random();
        return random.ints(PlayfairCipher.SIZE_FOR_RUSSIAN_ALPHABET, max)
                .findFirst().orElseThrow(RuntimeException::new);
    }

    /**
     * Простой вывод двумерных символьных массивов
     * */
    private static StringBuilder getMatrixString(String prefix, char[][] array){
        StringBuilder sb = new StringBuilder(prefix + "\n");
        for (char[] chars : array) {
            for (int j = 0; j < array.length; j++) {
                sb.append(chars[j]).append(" ");
            }
            sb.append("\n");
        }
        return sb.append("\n");
    }

    /**
     * Метод, чтобы вырезать подстроку и вставить в начало
     * */
    private static String cutAndInsert(String targetString){
        String[] array =  PlayfairCipher.RUSSIAN_ALPHABET.split(targetString);
        StringBuilder result = new StringBuilder();
        result.append(targetString);
        for (String s : array) {
            result.append(s);
        }
        return result.toString();
    }

    /*
    * Метод для поиска позиции символа в матрице
    * */
    private static Point findPositions(char[][] matrix, char symbol){
        for(int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix.length; j++){
                if(matrix[i][j] == symbol){
                    return new Point(j, i);
                }
            }
        }
        throw new RuntimeException("Undefined symbol: " + symbol);
    }
}
