package com.example.wifilocalizer;

/**
 * A complex number: real+sqrt(-1).imag
 * 
 * @author Oytun T&uumlrk
 *
 */
public class ComplexNumber {
    
    public float real;
    public float imag;
    
    public ComplexNumber()
    {
        
    }
    public ComplexNumber(ComplexNumber c)
    {
        this.real = c.real;
        this.imag = c.imag;
    }
    
    public ComplexNumber(float realIn, float imagIn)
    {
        this.real = realIn;
        this.imag = imagIn;
    }
    
    public ComplexNumber(double realIn, double imagIn)
    {
        this.real = (float)realIn;
        this.imag = (float)imagIn;
    }
    
    @Override
    public boolean equals(Object other)
    {
    	if (!(other instanceof ComplexNumber)) {
    		return false;
    	}
    	ComplexNumber cn = (ComplexNumber) other;
        if (real!=cn.real) return false;
        if (imag!=cn.imag) return false;
        
        return true;
    }
    
    public String toString()
    {
        String str;
        //if (Math.abs(real)>1e-10 || Math.abs(imag)>1e-10)
        //{
            if (imag>=0.0)
                str = String.valueOf(real) + "+i" + String.valueOf(Math.abs(imag));
            else
                str = String.valueOf(real) + "-i" + String.valueOf(Math.abs(imag));
        //}
        //else
        //    str = "0";
        
        return str;
    }
}

