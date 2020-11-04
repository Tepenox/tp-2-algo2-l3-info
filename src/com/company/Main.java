package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

public class Main {


    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        Map<String, List<String>> trigrammsMap = new HashMap<>();
        String input = "annuler";
        List<String> associatedWords = new ArrayList<>();

        Set<String> dicWords = new HashSet<>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    "C:\\Users\\Tepenox\\IdeaProjects\\tp 2 algo2 l3 info\\src\\com\\company\\dico.txt"));
            String line = reader.readLine();
            while (line != null) {
                dicWords.add(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //1
            if (dicWords.contains(input)){
                System.out.println("the word is correct");
                System.exit(0);
            }



        // 2 construire la liste de trigrame de mot M
        List<String> inputTrigrammes = makeTrigrammes(input);
        for (String inputTrigramme : inputTrigrammes){
            trigrammsMap.put(inputTrigramme , new ArrayList<>());
        }

        //3. construire la liste L des mots qui ont au moins un trigramme commun avec M,
        for (String word : dicWords) {
            List<String> wordTrigrammes = makeTrigrammes(word);
            for (String wordTrigramme : wordTrigrammes) {
                if (trigrammsMap.containsKey(wordTrigramme)){
                    trigrammsMap.get(wordTrigramme).add(word);
                    associatedWords.add(word);
                }

            }
        }

        //4 . pour chaque mot de L, calculer son nombre d’occurrences dans les listes de mots associées
        //aux trigrammes de M,
        Map<String, Integer> wordsOccurrences  = new HashMap<>();
        for (String associatedWord : associatedWords ){
            int wordCounter = 0;
            for (String trigrammeKey : trigrammsMap.keySet() ){
               if (trigrammsMap.get(trigrammeKey).contains(associatedWord))
                   wordCounter++;
            }
            wordsOccurrences.put(associatedWord,wordCounter);
        }

        //5. sélectionner les mots du dictionnaire qui ont le plus de trigrammes communs avec M
        List<String> result = new ArrayList<>();
        int maxOcurence = wordsOccurrences.values().stream().max(Integer::compareTo).get();

        wordsOccurrences = wordsOccurrences
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(50).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));

        //6. déterminer les cinq mots de la sélection les plus proches de M au sens de la distance
        //d’édition. L’utilisateur choisira parmis ces 5 mots celui qui lui convient.

        Map<String , Integer> editionDistance = new HashMap<>();

        for (String wordOccurenceKey : wordsOccurrences.keySet()){
            editionDistance.put(wordOccurenceKey,distanceOfLevenshtein(wordOccurenceKey,input));
        }
        //sorting map
        editionDistance = editionDistance
                .entrySet()
                .stream()
                .sorted(comparingByValue())
                .limit(5).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));

        System.out.println(editionDistance);

        long endTime = System.currentTimeMillis();



        System.out.println(TimeUnit.MILLISECONDS.toSeconds(endTime - startTime));

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
