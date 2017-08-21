package com.dong.rpc.trace;

import com.dong.rpc.trace.RPCTrace;

import java.util.Calendar;
import java.util.UUID;

/**
 * 当前线程RPC调用信息Holder
 * @author caolidong
 * @date 17/8/21.
 */
public class RPCHolder {

    private static ThreadLocal<RPCTrace> traceThreadLocal = new ThreadLocal<>();

    public static void setTrace(RPCTrace trace) {
        traceThreadLocal.set(trace);
    }

    public static RPCTrace getTrace() {
        return traceThreadLocal.get();
    }

    public static boolean hasTrace() {
        return traceThreadLocal.get() != null;
    }

    public static void init() {
        RPCTrace trace = new RPCTrace();
        trace.setTraceId(UUID.randomUUID().getMostSignificantBits());
        trace.setParentId(0l);
        trace.setSeq(1l);
        trace.setRequestTime(Calendar.getInstance().getTime());
        setTrace(trace);
    }

    public static void remove() {
        traceThreadLocal.remove();
    }

}
