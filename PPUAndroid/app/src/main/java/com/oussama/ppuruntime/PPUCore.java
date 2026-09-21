package com.oussama.ppuruntime;

import android.content.Context;
import org.json.JSONObject;
import java.io.InputStream;

/** Bounded native execution kernels used by the Android host. */
public final class PPUCore {
    private PPUCore(){}
    public static String inspectBytes(byte[] bytes){
        try{
            PPUCarrier.Report r=PPUCarrier.parse(bytes);
            int valid=0; for(PPUCarrier.Segment s:r.segments) if(s.crcValid) valid++;
            return new JSONObject().put("carrier","PPUJ").put("jpeg",r.jpeg).put("bytes",r.bytes)
                .put("ppuSegments",r.segments.size()).put("validCrc",valid)
                .put("crcAllValid",valid==r.segments.size()).toString();
        }catch(Exception e){return err("inspect",e);}
    }
    public static String monteCarlo(long seed,int samples){
        samples=Math.max(1,Math.min(samples,20_000_000));
        long t=System.nanoTime(),s=seed,inside=0;
        for(int i=0;i<samples;i++){
            s=xorshift(s); double x=((s>>>11)*0x1.0p-53)*2.0-1.0;
            s=xorshift(s); double y=((s>>>11)*0x1.0p-53)*2.0-1.0;
            if(x*x+y*y<=1.0) inside++;
        }
        double sec=(System.nanoTime()-t)/1e9;
        try{return new JSONObject().put("samples",samples).put("inside",inside).put("pi",4.0*inside/samples)
            .put("elapsedMs",sec*1000).put("samplesPerSecond",samples/sec).toString();}
        catch(Exception e){return err("monteCarlo",e);}
    }
    public static String primeKernel(int count,int rounds){
        count=Math.max(16,Math.min(count,5_000_000)); rounds=Math.max(1,Math.min(rounds,16));
        long t=System.nanoTime();
        int limit=upper(count); boolean[] c=new boolean[limit+1]; int[] p=new int[count]; int n=0;
        for(int i=2;i<=limit&&n<count;i++) if(!c[i]){p[n++]=i;if((long)i*i<=limit)for(int j=i*i;j<=limit;j+=i)c[j]=true;}
        long acc=0x9E3779B97F4A7C15L;
        for(int r=0;r<rounds;r++)for(int i=0;i<n;i++){long q=p[i];acc^=mix(q+0x9E3779B97F4A7C15L*(r+1));acc=Long.rotateLeft(acc,17)+q*q;}
        double ms=(System.nanoTime()-t)/1e6;
        try{return new JSONObject().put("primeCount",n).put("limit",limit).put("rounds",rounds)
            .put("accumulator",Long.toUnsignedString(acc)).put("elapsedMs",ms).toString();}
        catch(Exception e){return err("primeKernel",e);}
    }
    private static int upper(int n){if(n<6)return 15;double x=n;return Math.max(16,(int)Math.ceil(x*(Math.log(x)+Math.log(Math.log(x))))+10);}
    private static long xorshift(long x){if(x==0)x=0x2545F4914F6CDD1DL;x^=x<<13;x^=x>>>7;x^=x<<17;return x;}
    private static long mix(long z){z=(z^(z>>>30))*0xBF58476D1CE4E5B9L;z=(z^(z>>>27))*0x94D049BB133111EBL;return z^(z>>>31);}
    public static String bundledReport(Context c){return new JSONObject().put("carrier","optional").put("note","Open a JPG/PNG carrier or use the native PPU runtime").put("primeBasis",4096).toString();}
    private static String err(String op,Exception e){try{return new JSONObject().put("error",op).put("message",String.valueOf(e.getMessage())).toString();}catch(Exception x){return "{\"error\":\""+op+"\"}";}}
}
