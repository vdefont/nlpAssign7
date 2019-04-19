/**
Sarp Misoglu
Victor de Fontnouvelle
4/18/2019
*/

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

  class WordAndProb implements Comparable<WordAndProb> {
    private Double prob;
    private String word;
    public WordAndProb(String word, Double prob) {
      this.word = word;
      this.prob = prob;
    }
    public Double getProb () {
      return prob;
    }
    public String getWord () {
      return word;
    }
    @Override
    public int compareTo(WordAndProb other) {
      return prob.compareTo(other.getProb());
    }
  }

  // Print the most predictive features
  // Useful for writeup question 3
  private void mostPredictiveFeats () {

    List<WordAndProb> predicts = new ArrayList<>();

    for (String word : posMap.keySet()) {
      // Must be in both maps
      if (!negMap.containsKey(word)) continue;

      double posProb = ((double) posMap.get(word)) / ((double) posTotalWords);
      double negProb = ((double) negMap.get(word)) / ((double) negTotalWords);

      double predict = posProb / negProb;
      predicts.add(new WordAndProb(word, predict));
    }

    Collections.sort(predicts);

    // Get top posPredicts
    System.out.println("Top negative predictors:");
    for (int i = 0; i < 10; i += 1) {
      WordAndProb wap = predicts.get(i);
      double prob = 1 / wap.getProb();
      System.out.println(wap.getWord() + " -- " + prob);
    }
    System.out.println("\n\nTop positive predictors:");
    for (int i = 0; i < 10; i += 1) {
      int j = predicts.size() - 1 - i;
      WordAndProb wap = predicts.get(j);
      System.out.println(wap.getWord() + " -- " + wap.getProb());
    }

  }

  // Prints all probs from training data
  // Useful for writeup question 2
  private void printProbs () {
    double p = posNum;
    double n = negNum;
    double pn = posNum + negNum;
    double pp = p / pn;
    double nn = n / pn;
    System.out.println("Pos prob: " + pp);
    System.out.println("Neg prob: " + nn);

    System.out.println("Pos:");
    for (String s : vocab) {
      double num = posMap.get(s);
      double denom = posTotalWords;
      double prob = num / denom;
      System.out.println(s + " -- " + prob);
    }

    System.out.println("Neg:");
    for (String s : vocab) {
      double num = negMap.get(s);
      double denom = negTotalWords;
      double prob = num / denom;
      System.out.println(s + " -- " + prob);
    }
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
    new Classifier(args[0], args[1], new Double(args[2]));
  }

}
