package com.oussama.ppuruntime;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

/** Safe PPUJ APP15 inspector. It never executes arbitrary embedded code. */
public final class PPUCarrier {
    public static final class Segment {
        public int index;
        public int length;
        public boolean crcValid;
        public String text;
    }
    public static final class Report {
        public final boolean jpeg;
        public final int bytes;
        public final List<Segment> segments = new ArrayList<>();
        Report(boolean jpeg, int bytes) { this.jpeg = jpeg; this.bytes = bytes; }
        public String toJson() {
            StringBuilder b = new StringBuilder();
            b.append("{\"jpeg\":").append(jpeg)
             .append(",\"bytes\":").append(bytes)
             .append(",\"segments\":[");
            for (int i=0;i<segments.size();i++) {
                if (i>0) b.append(',');
                Segment s=segments.get(i);
                b.append("{\"index\":").append(s.index)
                 .append(",\"length\":").append(s.length)
                 .append(",\"crcValid\":").append(s.crcValid)
                 .append(",\"text\":\"").append(escape(s.text)).append("\"}");
            }
            return b.append("]}").toString();
        }
    }
    private static String escape(String s) {
        return (s==null?"":s).replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r");
    }
    public static Report parse(byte[] d) {
        boolean jpeg=d!=null && d.length>=2 && (d[0]&255)==255 && (d[1]&255)==216;
        Report r=new Report(jpeg,d==null?0:d.length);
        if(!jpeg) return r;
        int p=2,idx=0;
        while(p+4<=d.length) {
            if((d[p]&255)!=255){p++;continue;}
            int marker=d[p+1]&255;
            if(marker==218 || marker==217) break;
            if(marker==216 || marker==1 || (marker>=208&&marker<=215)){p+=2;continue;}
            int len=((d[p+2]&255)<<8)|(d[p+3]&255);
            if(len<2 || p+2+len>d.length) break;
            int start=p+4,end=p+2+len;
            if(marker==239 && end-start>=8 && hasMagic(d,start,"PPUJ")) {
                Segment s=new Segment(); s.index=idx++; s.length=end-start;
                byte[] payload=new byte[end-start];
                System.arraycopy(d,start,payload,0,payload.length);
                s.crcValid=crc(payload);
                s.text=printable(payload);
                r.segments.add(s);
            }
            p+=2+len;
        }
        return r;
    }
    private static boolean hasMagic(byte[] d,int o,String s){
        for(int i=0;i<s.length();i++) if(d[o+i]!=(byte)s.charAt(i)) return false;
        return true;
    }
    private static boolean crc(byte[] p){
        if(p.length<14) return false;
        long expected=((p[p.length-4]&255L)<<24)|((p[p.length-3]&255L)<<16)|((p[p.length-2]&255L)<<8)|(p[p.length-1]&255L);
        CRC32 c=new CRC32(); c.update(p,0,p.length-4);
        return c.getValue()==expected;
    }
    private static String printable(byte[] p){
        int from=Math.min(10,p.length-4),to=p.length-4;
        if(from>=to) return "";
        return new String(p,from,to-from,StandardCharsets.UTF_8).replaceAll("[^\\x20-\\x7E\\n\\r\\t]","?");
    }
}
