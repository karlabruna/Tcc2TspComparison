package data;

import model.graph.Graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Contains all inputs used to test  solution.
 */
public class Inputs {

  public static List<List<Integer>> returnAllInputs() {
    return returnAllInputs("data/inputs.csv");
  }

  public static List<List<Integer>> returnAllTinyInputs() {
    return returnAllInputs("data/tiny_inputs.csv");
  }

  public static List<List<Integer>> returnAllInputs(String source) {
    List<List<Integer>> allInputs = new ArrayList<>();
    InputStream in = Inputs.class.getClassLoader().getResourceAsStream(source);
    String line;
    String cvsSplitBy = ",";
    try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
      while ((line = br.readLine()) != null) {
        List<Integer> nodeIds = new ArrayList<>();
        // use comma as separator
        for (String nodeIdString : line.split(cvsSplitBy)) {
          nodeIds.add(Integer.parseInt(nodeIdString));
        }
        allInputs.add(nodeIds);
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Error reading input data", e);
    }
    return allInputs;
  }

  public static List<Integer> getInputForGraphCountry(Graph.Country graphCountry) {
    Integer[] arrayInput;
    Integer[] adInput = {272, 329, 1640, 653};
    Integer[] atInput = {135453, 388326, 157591, 439506};
    Integer[] deInput = {1193700, 229003, 3258667, 2512448};
    Integer[] euInput = {17238825, 7935204, 13620123, 8016804};
    switch (graphCountry) {
      case TINY:
        arrayInput = adInput;
        break;
      case SMALL:
        arrayInput = atInput;
        break;
      case MEDIUM:
        arrayInput = deInput;
        break;
      case LARGE:
      case EU:
        arrayInput = euInput;
        break;
      default:
        throw new IllegalArgumentException("Non supported graph country.");
    }
    return Arrays.asList(arrayInput);
  }

  public static List<Integer> getRandomInput(int inputSize, Graph.Country graphCountry) {
    List<Integer> input = new ArrayList<>();
    Random random = new Random(System.currentTimeMillis());
    for (int i = 0; i < inputSize; i++) {
      input.add(random.nextInt(graphCountry.getNodes()));
    }
    return input;
  }
}
