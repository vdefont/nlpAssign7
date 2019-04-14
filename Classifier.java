import java.util.*;
import java.io.*;

public class Classifier {

  Map<String,Integer> posMap, negMap;
  Set<String> vocab;
  int posTotalWords, negTotalWords;
  int posNum, negNum;

  public Classifier (String trainData, String testData, double lambda) {

    // Initialize instance variables
    posMap = new HashMap<>();
    negMap = new HashMap<>();
    vocab = new HashSet<>();
    posTotalWords = 0;
    negTotalWords = 0;
    posNum = 0;
    negNum = 0;

    // Train
    train(trainData);

    // Test
    test(testData, lambda);
  }

  private void test (String testData, double lambda) {
    try {
      Scanner sc = new Scanner(new File(testData));

      while(sc.hasNextLine()) {
        String line = sc.nextLine();
        System.out.println(classify(line, lambda));
      }
    } catch (IOException e) {
      System.out.println("Error reading test data");
    }
  }

  private String classify(String sent, double lambda) {
    String[] words = sent.split("\\s");

    double totalSents = posNum + negNum;
    double posSum = Math.log10(((double)posNum) / totalSents);
    double negSum = Math.log10(((double)negNum) / totalSents);

    for(String word : words) {
        if(!vocab.contains(word)) continue;

        int posMapVal = posMap.containsKey(word) ? posMap.get(word) : 0;
        double posProb = (lambda + posMapVal) /
          ((lambda * vocab.size()) + posTotalWords);
        posSum += Math.log10(posProb);

        int negMapVal = negMap.containsKey(word) ? negMap.get(word) : 0;
        double negProb = (lambda + negMapVal) /
          ((lambda * vocab.size()) + negTotalWords);
        negSum += Math.log10(negProb);
    }

    // return ("positive\t" + posSum) + "\t\t" + ("negative\t" + negSum);
    return posSum >= negSum ? ("positive\t" + posSum) : ("negative\t" + negSum);
  }

  // Populate instance variables
  private void train (String trainData) {
    try {
      Scanner sc = new Scanner(new File(trainData));
      while (sc.hasNextLine()) {
        String line = sc.nextLine();

        String[] words = line.split("\\s");
        boolean isPos = words[0].equals("positive");

        // Increment label count
        if (isPos) posNum += 1;
        else negNum += 1;

        // Handle each word
        for (int i = 1; i < words.length; i += 1) {
          String word = words[i];
          if (word.isEmpty()) continue;

          vocab.add(word);

          if (isPos) {
            posMap.merge(word, 1, Integer::sum);
            posTotalWords += 1;
          } else {
            negMap.merge(word, 1, Integer::sum);
            negTotalWords += 1;
          }
        }
      }
    } catch (IOException e) {
      System.out.println("Error reading training data");
    }
  }

  public static void main (String[] args) {
    new Classifier("movies.data", "testData", 0.01);
  }

}
