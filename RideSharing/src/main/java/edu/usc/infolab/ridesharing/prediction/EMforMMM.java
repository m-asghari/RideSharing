package edu.usc.infolab.ridesharing.prediction;

/**
 * Created by Mohammad on 5/24/2017.
 *
 * Implementation of the Expectation Maximization Algorithm for Multinomial Mixture Models
 */
public class EMforMMM {

    private class MMM {
        //Number of topics
        private int _topicSize;

        // topic proportions
        private double[] _pi;

        // word distributions for each topic
        private double[][] _theta;

        // number of possible words
        private int _p;

        MMM(int p, int topicSize) {
            this._topicSize = topicSize;
            this._p = p;
            this._pi = new double[this._topicSize];
            this._theta = new double[this._topicSize][];
            for (int i = 0; i < this._topicSize; i++) {
                this._theta[i] = new double[this._p];
            }
        }

        double partialF(double[] x, int topicID) {
            double res = 1;
            for (int i = 0; i < _p; i++) {
                res *= Math.pow(_theta[topicID][i], x[i]);
            }
            return res;
        }

        double logLikelihood(double[][] data) {
            double sum = 0;
            for (int i = 0; i < data.length; i++) {
                double docSum = 0;
                for (int j = 0; j < _topicSize; j++) {
                    double mult = _pi[j];
                    for (int v = 0; v < _p; v++) {
                        mult *= Math.pow(_theta[j][v], data[i][v]);
                    }
                    docSum += mult;
                }
                sum += Math.log(docSum);
            }
            return sum;
        }
    }

    private int _numOfDocs;

    private int _topicSize;

    // possible number of words
    private int _p;

    // topic proportion per document
    private double[][] _tau;

    private double[][] _data;

    private MMM _model;

    public EMforMMM(double[][] data, int topicSize) {
        _numOfDocs = data.length;
        _topicSize = topicSize;
        _p = data[0].length;

        _tau = new double[_numOfDocs][];
        _data = new double[_numOfDocs][];
        for (int i = 0; i < _numOfDocs; i++) {
            _tau[i] = new double[topicSize];
            _data[i] = new double[_p];
        }

        _model = new MMM(_p, topicSize);
    }

    public void eStep() {
        for (int i = 0; i < _numOfDocs; i++) {
            for (int j = 1; j < _topicSize; j++) {
                _tau[i][j] = ComputeTau(i, j);
            }
        }
    }

    public void mStep() {
        // update _model.pi
        for (int j = 0; j < _topicSize; j++) {
            double sum = 0;
            for (int i = 0; i < _numOfDocs; i++) {
                sum += _tau[i][j];
            }
            _model._pi[j] = sum/_numOfDocs;
        }

        // update _model.theta
        double[] wordsPerDoc = new double[_numOfDocs];
        for (int i = 0; i < _numOfDocs; i++) {
            double sum = 0;
            for (int v = 0; v < _p; v++) {
                sum += _data[i][v];
            }
        }

        for (int j = 0; j < _topicSize; j++) {
            double sum1 = 0;
            for (int i = 0; i < _numOfDocs; i++) {
                sum1 += (wordsPerDoc[i] * _tau[i][j]);
            }
            for (int v = 0; v < _p; v++) {
                double sum2 = 0;
                for (int i = 0; i < _numOfDocs; i++) {
                    sum2 += (_data[i][v] * _tau[i][j]);
                }
                _model._theta[j][v] = sum2 / sum1;
            }
        }
    }

    private double ComputeTau(int docID, int topicID) {
        double res = _model._pi[topicID] * _model.partialF(_data[docID], topicID);
        double sum = 0;
        for (int i = 0; i < _topicSize; i++) {
            sum += (_model._pi[i] * _model.partialF(_data[docID], i));
        }
        return res/sum;
    }

    public static void main(String[] args) {
    }
}
