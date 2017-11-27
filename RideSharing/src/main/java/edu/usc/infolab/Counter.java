package edu.usc.infolab;

/**
 * Created by mohammad on 11/20/17.
 */
public class Counter {
    double[] m_steps;
    double[] m_mins;
    double[] m_maxs;
    double[] m_current;
    int m_size;

    public Counter(int size) {
        m_size = size;
        m_steps = new double[m_size];
        m_mins = new double[m_size];
        m_maxs = new double[m_size];
        m_current = new double[m_size];
        for (int i = 0; i < m_size; i++) {
            m_steps[i] = 1;
            m_mins[i] = 0;
            m_maxs[i] = 10;
            m_current[i] = 0;
        }
    }

    public Counter(double[] mins, double[] maxs) {
        m_size = maxs.length;
        m_steps = new double[m_size];
        m_mins = mins;
        m_maxs = maxs;
        m_current = mins;
        for (int i = 0; i < m_size; i++) {
            m_steps[i] = 1;
        }
    }

    public boolean hasNext() {
        for (int i = m_size - 1; i >= 0; i--) {
            if (m_current[i] + m_steps[i] < m_maxs[i])
                return true;
        }
        return false;
    }

    public double[] next() {
        int i = 0;
        while (true) {
            m_current[i] = m_current[i] + m_steps[i];
            if (m_current[i] < m_maxs[i]) {
                break;
            }
            m_current[i] = m_mins[i];
            i++;
        }
        return m_current;
    }
}
