package com.example.wifilocalizer;

import java.util.Arrays;

/**
 * This is a wrapper class that can hold two double arrays, one of which is 
 * interpreted as containing the real values, the other the imaginary values
 * of the complex numbers.
 * @author Marc Schr&ouml;der
 *
 */
public class ComplexArray
{
    public double[] real;
    public double[] imag;

    public ComplexArray(int len)
    {
        init(len);
    }
    
    public ComplexArray(double[] realVals, double[] imagVals)
    {
        if (realVals!=null && imagVals!=null && realVals.length==imagVals.length)
        {
            init(realVals.length);
            System.arraycopy(realVals, 0, real, 0, realVals.length);
            System.arraycopy(imagVals, 0, imag, 0, imagVals.length);
        }
    }

    public ComplexArray(ComplexArray c)
    {
        this(c.real, c.imag);
    }

    public void init(int len)
    {
        if (len>0)
        {
            real = new double[len];
            imag = new double[len];

            Arrays.fill(real, 0.0);
            Arrays.fill(imag, 0.0);
        }
        else
        {
            real = null;
            imag = null;
        }
    }
    
    public ComplexNumber get(int index)
    {
        return new ComplexNumber((float)real[index], (float)imag[index]);
    }
}

