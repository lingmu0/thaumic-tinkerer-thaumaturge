import net.lingmu.thaumictinkerer.ExperienceTransfer;

public class ExperienceTransferCheck {
    public static void main(String[] args) {
        int cases=0;
        for(int stored=0;stored<=1500;stored++)for(int value:new int[]{1,2,3,7,11,73,149,617,1237,2477})
            for(int count:new int[]{1,2,5,32,255,32767}) {
                int consumed=ExperienceTransfer.count(stored,1500,value,count);
                long remaining=(long)(count-consumed)*value;
                int after=stored+consumed*value;
                require(after>=stored&&after<=1500,"Capacity overflow");
                require(consumed>=0&&consumed<=count,"Invalid orb count");
                require((long)stored+(long)count*value==after+remaining,"XP lost or duplicated");
                require(consumed==count||1500-after<value,"Unused absorbable orb");
                cases++;
            }
        require(ExperienceTransfer.count(1499,1500,2,500)==0,"Partial orb must be preserved");
        require(ExperienceTransfer.count(0,1500,0,1)==0,"Invalid zero value");
        System.out.println("Passed "+cases+" XP conservation/capacity cases.");
    }
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
