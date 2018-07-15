package io.spring2go.tools.stat;


import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.spring2go.tools.common.OperatingSystemStats;
import io.spring2go.tools.common.StatsGetter;


public class OperatingSystemStatsGetter implements StatsGetter {

    static OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
    static Class clazz = bean.getClass();
    static ConcurrentHashMap<String, Method> methodMap = new ConcurrentHashMap<String, Method>();

    static Class statsClazz = OperatingSystemStats.class;
    static ConcurrentHashMap<String, Field> fieldMap = new ConcurrentHashMap<String, Field>();

    static {
        String[] arr1 = new String[]{
                "committedVirtualMemory",
                "totalSwapSpaceSize",
                "freeSwapSpaceSize",
                "processCpuTime",
                "freePhysicalMemorySize",
                "totalPhysicalMemorySize",
                "systemCpuLoad",
                "processCpuLoad"
//                ,
//                "maxFileDescriptorCount",
//                "openFileDescriptorCount"
        };
        String[] arr2 = new String[]{
                "getCommittedVirtualMemorySize",
                "getTotalSwapSpaceSize",
                "getFreeSwapSpaceSize",
                "getProcessCpuTime",
                "getFreePhysicalMemorySize",
                "getTotalPhysicalMemorySize",
                "getSystemCpuLoad",
                "getProcessCpuLoad"
//                ,
//                "getMaxFileDescriptorCount",
//                "getOpenFileDescriptorCount"
        };

        Method m;
        for (int i = 0; i < arr1.length; i++) {
            String fname = arr1[i];

            try {
                m = clazz.getMethod(arr2[i]);
                m.setAccessible(true);
                methodMap.putIfAbsent(fname, m);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            Field f;
            try {
                f = statsClazz.getDeclaredField(fname);
                f.setAccessible(true);
                fieldMap.putIfAbsent(fname, f);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

    }

    OperatingSystemStats previous = new OperatingSystemStats();

    @Override
    public OperatingSystemStats get() {

        OperatingSystemStats s = new OperatingSystemStats();
        for (Map.Entry<String, Method> entry : methodMap.entrySet()) {
            String fname = entry.getKey();
            Method m = entry.getValue();
            Field f = fieldMap.get(fname);
            if (m != null && f != null) {
                try {
                    f.set(s, m.invoke(bean));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        long cpuTime = s.getProcessCpuTime();
        s.setProcessCpuTime(cpuTime - previous.getProcessCpuTime());

        previous.setProcessCpuTime(cpuTime);

        return s;
    }


    public static void main(String[] args) {
        final OperatingSystemStatsGetter g = new OperatingSystemStatsGetter();

        new Thread() {
            @Override
            public void run() {
                int i = 0;
                while (i++ < 5) {
                    System.out.println(g.get());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
