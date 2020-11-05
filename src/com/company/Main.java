
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

public class Main {

    private static Map<String, List<String>> trigrammsMapOfDico = new HashMap<>();

    public static void main(String[] args) {

        String URLOfDico = "C:\\Users\\Cédric\\IdeaProjects\\tp-2-algo2-l3-info\\src\\com\\company\\dico.txt";
        String URLOfFautes = "C:\\Users\\Cédric\\IdeaProjects\\tp-2-algo2-l3-info\\src\\com\\company\\fautes.txt";
        //====================First : build trigrammsMapOfDico=================================
        prepareMap(URLOfDico);

        //====================Then : start correction of all word one by one===================
        long startTime = System.currentTimeMillis(); //start of chronos
        //Reading fautes.txt
        BufferedReader readerDico;
        try {
            readerDico = new BufferedReader(new FileReader(URLOfFautes));
            String line = readerDico.readLine();
            while (line != null) {
                makeCorrectionOfTheWord(line);
                // read next line
                line = readerDico.readLine();
            }
            readerDico.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Total time is "+ ((endTime - startTime)/1000f) + " sec");
    }

    private static void makeCorrectionOfTheWord(String input) {
        //==================Build Trigramm of input=================
        List<String> searchedTrigramms = makeTrigrammes(input);

        //==================check if input is correct================
        for (String searchedTrigramm : searchedTrigramms) {
            if(trigrammsMapOfDico.containsKey(searchedTrigramm) && trigrammsMapOfDico.get(searchedTrigramm).contains(input)){
                System.out.println(input+" is a correct word");
                return;
            }
        }
        //==================Search word sharing at least 1 of the trigramm searched=====
        Map<String,Integer> mapOfWord = new HashMap<>();
        for (String searchedTrigramm : searchedTrigramms) {
            //calc occurence of each word of this trigram
            if(!trigrammsMapOfDico.containsKey(searchedTrigramm))
                continue;
            for (String word : trigrammsMapOfDico.get(searchedTrigramm)) {
                if(mapOfWord.containsKey(word))
                    mapOfWord.replace(word,mapOfWord.get(word)+1);
                else
                    mapOfWord.put(word,1);
            }
        }
        //take 50
        mapOfWord = mapOfWord
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(50).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));

        //=================calc levenshtein===============================================
        for (String wordOccurenceKey : mapOfWord.keySet()){
            mapOfWord.replace(wordOccurenceKey,distanceOfLevenshtein(wordOccurenceKey,input));
        }
        //take 5
        mapOfWord = mapOfWord
                .entrySet()
                .stream()
                .sorted(comparingByValue())
                .limit(5).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));
        System.out.println("correction of " + input + " can be " + mapOfWord);
    }

    public static void prepareMap(String URL){
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(URL));
            String line = reader.readLine();
            while (line != null) {
                List<String> trigramms = makeTrigrammes(line);
                for (String trigramm:trigramms) {
                    if(trigrammsMapOfDico.containsKey(trigramm))
                        trigrammsMapOfDico.get(trigramm).add(line);
                    else
                        trigrammsMapOfDico.put(trigramm,new ArrayList<String>(Arrays.asList(line)));
                }
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        for (int i = 0; i < w1.length() - 2; i++) {
            results.add(w1.substring(i, i + 3));
        }
        return results;
    }


}
