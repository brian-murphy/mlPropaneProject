package datasets;

public interface Instance {
    double[] getInput();
    void setInput(double[] input);

    double getOutput();
    void setOutput(double output);

    double[] getPossibleOutputs();

    /**
     * @param computedOutput error will be determined relative to this value
     * @return 0 if correct, one if incorrect
     */
    double getDifference(double computedOutput);

    Instance deepCopy();
}
