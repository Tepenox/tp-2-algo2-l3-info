
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

public class Main {


    private static Map<String, List<String>> trigrammsMap;

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        Set<String> dicWords = buildDicSet("C:\\Users\\Cédric\\IdeaProjects\\tp-2-algo2-l3-info\\src\\com\\company\\dico.txt");
        /*
        //Reading fautes.txt
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("C:\\Users\\Cédric\\IdeaProjects\\tp-2-algo2-l3-info\\src\\com\\company\\fautes.txt"));
            String line = reader.readLine();
            while (line != null) {

                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        correctWord("annler",dicWords);

        long endTime = System.currentTimeMillis();
        float result = (endTime - startTime)/1000f;
        System.out.println("Total time is "+result+ " ms");
        //TODO : Cédric run it in 6 sec, goal : run it in less
    }

    private static Set<String> buildDicSet(String path) {
        Set<String> result = new HashSet<>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
            String line = reader.readLine();
            while (line != null) {
                result.add(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static void correctWord(String input,Set<String> dicWords) {

        List<String> associatedWords = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        //1 est-ce que le mot est juste ?
        if(isCorrect(dicWords,input))
            System.exit(0);
        long endTime = System.currentTimeMillis();
        float result = (endTime - startTime)/1000f;
        System.out.println("1. done in " +result+ " ms");
        startTime = endTime;

        // 2 construire la liste de trigrame de mot M
        constructTrigramMap(input);
        endTime = System.currentTimeMillis();
        result = (endTime - startTime)/1000f;
        System.out.println("2. done in " + result + " ms");
        startTime = endTime;

        //3. construire la liste L des mots qui ont au moins un trigramme commun avec M,
        processMapTrigram(dicWords,associatedWords);
        endTime = System.currentTimeMillis();
        result = (endTime - startTime)/1000f;
        System.out.println("3. done in " +result + " ms");
        startTime = endTime;

        //4 . pour chaque mot de L, calculer son nombre d’occurrences dans les listes de mots associées
        //aux trigrammes de M,
        Map<String, Integer> wordsOccurrences = countNbOccurrences(associatedWords);
        endTime = System.currentTimeMillis();
        result = (endTime - startTime)/1000f;
        System.out.println("4. done in " +result + " ms");
        startTime = endTime;

        //5. sélectionner les mots du dictionnaire qui ont le plus de trigrammes communs avec M
        wordsOccurrences = wordsOccurrences
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(50).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));
        endTime = System.currentTimeMillis();
        result = (endTime - startTime)/1000f;
        System.out.println("5. done in " +result+" ms");
        startTime = endTime;

        //6. déterminer les cinq mots de la sélection les plus proches de M au sens de la distance
        //d’édition. L’utilisateur choisira parmis ces 5 mots celui qui lui convient.
        Map<String , Integer> editionDistance = getFinalResult(input,wordsOccurrences);
        endTime = System.currentTimeMillis();
        result = (endTime - startTime)/1000f;
        System.out.println("6. done in " +result+" ms");

        System.out.println("-correction for " + input + " is " + editionDistance);
    }


    //1.
    public static Boolean isCorrect(Set<String> dicWords, String input ){
        if (dicWords.contains(input)){
            System.out.println("the word is correct");
            return true;
            //System.exit(0);
        }
        return false;
    }

    //2 construire la liste de trigrame de mot M
    public static void constructTrigramMap(String input){
        trigrammsMap = new HashMap<>();
        List<String> inputTrigrammes = makeTrigrammes(input);
        for (String inputTrigramme : inputTrigrammes){
            trigrammsMap.put(inputTrigramme , new ArrayList<>());
        }
    }

    //3. construire la liste L des mots qui ont au moins un trigramme commun avec M,
    public static void processMapTrigram(Set<String> dicWords,List<String> associatedWords){
        for (String word : dicWords) {
            List<String> wordTrigrammes = makeTrigrammes(word);
            for (String wordTrigramme : wordTrigrammes) {
                if (trigrammsMap.containsKey(wordTrigramme)){
                    trigrammsMap.get(wordTrigramme).add(word);
                    associatedWords.add(word);
                }

            }
        }
    }

    //4 . pour chaque mot de L, calculer son nombre d’occurrences dans les listes de mots associées
    //aux trigrammes de M,
    public static Map<String, Integer> countNbOccurrences(List<String> associatedWords){
        Map<String, Integer> wordsOccurrences  = new HashMap<>();
        for (String associatedWord : associatedWords ){
            int wordCounter = 0;
            for (String trigrammeKey : trigrammsMap.keySet() ){
                if (trigrammsMap.get(trigrammeKey).contains(associatedWord))
                    wordCounter++;
            }
            wordsOccurrences.put(associatedWord,wordCounter);
        }
        return wordsOccurrences;
    }
    //5. /

    //6. déterminer les cinq mots de la sélection les plus proches de M au sens de la distance
    //d’édition. L’utilisateur choisira parmis ces 5 mots celui qui lui convient.

    public static Map<String , Integer> getFinalResult(String input, Map<String, Integer> wordsOccurrences){
        Map<String , Integer> editionDistance = new HashMap<>();
        for (String wordOccurenceKey : wordsOccurrences.keySet()){
            editionDistance.put(wordOccurenceKey,distanceOfLevenshtein(wordOccurenceKey,input));
        }
        //sorting map
        return editionDistance
                .entrySet()
                .stream()
                .sorted(comparingByValue())
                .limit(5).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));
    }

    public static int distanceOfLevenshtein(String w1, String w2) {
        if (w1.isEmpty())
            return w2.length();

        if (w2.isEmpty())
            return w1.length();

        int[][] dp = new int[w1.length() + 1][w2.length() + 1];

        for (int i = 0; i <= w1.length(); i++) {
            for (int j = 0; j <= w2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1]
                                    + costOfSubstitution(w1.charAt(i - 1), w2.charAt(j - 1)),
                            Math.min(dp[i - 1][j] + 1,
                                    dp[i][j - 1] + 1));
                }
            }
        }

        return dp[w1.length()][w2.length()];
    }

    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    public static List<String> makeTrigrammes(String w1) {
        List<String> results = new ArrayList<>();
        w1 = "<".concat(w1).concat(">");
        for (int i = 0; i < w1.length(); i++) {
            try {
                results.add(w1.substring(i, i + 3));
            } catch (StringIndexOutOfBoundsException e) {
                break;
            }
        }
        return results;
    }


}
