package ca.ualberta.zhihao9.ece493_zhihao9;

import android.text.InputFilter;
import android.text.Spanned;


//reference
// https://stackoverflow.com/questions/14212518/is-there-a-way-to-define-a-min-and-max-value-for-edittext-in-android
public class inputfilter_minmax implements InputFilter {


    private int max,min;

    public inputfilter_minmax( int min, int max){
        this.min = min;
        this.max = max;
    }
    public inputfilter_minmax( String min , String max){
        this.max = Integer.parseInt(max);
        this.min = Integer.parseInt(min);
    }


    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try{
            int input = Integer. parseInt (dest.toString() + source.toString()) ;
            if (isInRange( min , max, input))
                return null;

            }catch (NumberFormatException e){
                e.printStackTrace() ;
        }
        return "";
    }
    private boolean isInRange ( int a , int b , int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a ;
    }
}
