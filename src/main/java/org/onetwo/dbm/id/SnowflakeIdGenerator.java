package org.onetwo.dbm.id;


/**
 * @author copy from http://www.wolfbe.com/detail/201611/381.html
 * 1位标识符
 * 41位时间戳，这个是毫秒级的时间，一般实现上不会存储当前的时间戳，而是时间戳的差值（当前时间-固定的开始时间），这样可以使产生的ID从更小值开始；41位的时间戳可以使用69年，(1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69年；
 * 5位数据中心，5位机器标识，Twitter实现中使用前5位作为数据中心标识，后5位作为机器标识，可以部署1024个节点；
 * 12位序列号，支持同一毫秒内同一个节点可以生成4096个ID
 * <br/>
 */
public class SnowflakeIdGenerator {

	 
    /**
    * 起始的时间戳
    */
    private final static long START_STMP = 1480166465631L; //2016-11-26 21:21:05
 
    /**
    * 每一部分占用的位数
    */
    private final static long SEQUENCE_BIT = 12; //序列号占用的位数,支持同一毫秒内同一个节点可以生成4096个ID
    private final static long MACHINE_BIT = 5;  //机器标识占用的位数
    private final static long DATACENTER_BIT = 5;//数据中心占用的位数
 
    /**
    * 每一部分的最大值
    * -1左移n位，再与-1异或，相当于2**n-1，即n位所能表示的最大正数
    */
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    /***
     * 支持同一毫秒内同一个节点可以生成4096个ID
     */
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);
 
    /**
    * 每一部分向左的位移
    */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;
 
    private long datacenterId;  //数据中心
    private long machineId;    //机器标识
    private long sequence = 0L; //序列号
    private long lastStmp = -1L;//上一次时间戳
 
    public SnowflakeIdGenerator(long workerId) {
    	this(1, workerId);
    }
    
    public SnowflakeIdGenerator(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }
 
    /**
    * 产生下一个ID
    *
    * @return
    */
    public synchronized long nextId() {
        long currStmp = getNewstmp();
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }
 
        if (currStmp == lastStmp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }
 
        lastStmp = currStmp;
 
        return (currStmp - START_STMP) << TIMESTMP_LEFT //时间戳部分
                | datacenterId << DATACENTER_LEFT      //数据中心部分
                | machineId << MACHINE_LEFT            //机器标识部分
                | sequence;                            //序列号部分
    }
 
    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }
 
    private long getNewstmp() {
        return System.currentTimeMillis();
    }
 

}
